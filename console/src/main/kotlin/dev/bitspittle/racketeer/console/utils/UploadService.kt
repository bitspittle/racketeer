package dev.bitspittle.racketeer.console.utils

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.nio.file.Files
import java.time.Duration
import kotlin.io.path.deleteExisting
import kotlin.io.path.fileSize
import kotlin.io.path.writeText

private val DEFAULT_THROTTLE_DURATION = Duration.ofMinutes(1)

interface UploadService {
    object MimeTypes {
        const val YAML = "text/yaml"
    }

    val throttleDurations: Map<Any, Duration>
    val throttleSizes: Map<Any, Long>

    /**
     * Upload some data file to a folder in the Cloud.
     *
     * This method shouldn't block; instead, it should perform this action on a background thread and, if it fails,
     * just fail silently.
     *
     * This method takes in optional callbacks you can use to do followup work when it is finished. These callbacks
     * will be triggered on the same background thread used to handle the upload.
     *
     * @param mimeType The content type of this data. See something like:
     *   https://www.freeformatter.com/mime-types-list.html
     *
     * @param throttleKey If set, will be used as a key to check against another data payload with the same key having
     *   been uploaded somewhat recently. Pass in `null` to not throttle. See also: [throttleDurations]
     */
    fun upload(fileName: String, mimeType: String, throttleKey: Any? = null, produceData: () -> String)

    /**
     * Remove any throttles currently set.
     *
     * This is useful if you have really long-running throttles that are only meant to limit data collected for a single
     * game.
     */
    fun clearThrottles()
}

enum class UploadThrottleCategory {
    CRASH_REPORT
}

class DriveUploadService(
    title: String,
    override val throttleDurations: Map<Any, Duration> = mapOf(),
    override val throttleSizes: Map<Any, Long> = mapOf()
) : UploadService {
    private val UPLOAD_FOLDER_ID = "14bplvWMj-3qTuydASDz1LcBUzxDjz28U"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()
    private val nextAllowedUpload = mutableMapOf<Any, Long>()

    private fun getCredentials(): Credential {
        val credentialsJson = DriveUploadService::class.java.getResourceAsStream("/credentials.json")
            ?: throw FileNotFoundException("Credentials not found")

        // Of course leave it to Google to deprecate an API and suggest using another library which, when you try to
        // use it, isn't clear how it should replace the following lines.
        @Suppress("DEPRECATION")
        return GoogleCredential
            .fromStream(credentialsJson)
            .createScoped(listOf(DriveScopes.DRIVE))
    }

    private val driveService by lazy {
        Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            getCredentials()
        ).setApplicationName(title)
            .build()
    }

    override fun upload(fileName: String, mimeType: String, throttleKey: Any?, produceData: () -> String) {
        val now = System.currentTimeMillis()
        if (throttleKey != null) {
            if (nextAllowedUpload.getOrDefault(throttleKey, 0) > now) {
                return
            } else {
                nextAllowedUpload[throttleKey] = now + (throttleDurations[throttleKey] ?: DEFAULT_THROTTLE_DURATION).toMillis()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            run {
                val tmp = Files.createTempFile("docrimes-upload-", ".txt").apply {
                    writeText(produceData())
                }

                try {
                    if (throttleKey != null && tmp.fileSize() > (throttleSizes[throttleKey] ?: Long.MAX_VALUE)) return@launch

                    val fileMetadata = File()
                    fileMetadata.name = fileName
                    fileMetadata.parents = listOf(UPLOAD_FOLDER_ID)
                    val mediaContent = FileContent(mimeType, tmp.toFile())
                    driveService.files().create(fileMetadata, mediaContent).execute()
                } catch (ignored: Throwable) { }
                finally { tmp.deleteExisting() }
            }
        }
    }

    override fun clearThrottles() {
        nextAllowedUpload.clear()
    }
}