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
import java.nio.file.Path
import java.time.Duration

private val DEFAULT_THROTTLE_DURATION = Duration.ofMinutes(1)

interface UploadService {
    val throttleDurations: Map<Any, Duration>

    /**
     * Upload some data file to a folder in the Cloud.
     *
     * This method shouldn't block; instead, it should perform this action on a background thread and, if it fails,
     * just fail silently.
     *
     * This method takes in optional callbacks you can use to do followup work when it is finished. These callbacks
     * will be triggered on the same background thread used to handle the upload.
     *
     * @param throttleKey If set, will be used as a key to check against another data payload with the same key having
     *   been uploaded somewhat recently. Pass in `null` to not throttle. See also: [throttleDurations]
     */
    fun upload(fileName: String, path: Path, throttleKey: Any? = null, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {})
}

fun UploadService.upload(fileName: String, path: Path, onFinished: () -> Unit) =
    upload(fileName, path, onFinished, onFinished)

enum class UploadThrottleCategory {
    CRASH_REPORT
}

class DriveUploadService(title: String, override val throttleDurations: Map<Any, Duration> = mapOf()) : UploadService {
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

    override fun upload(fileName: String, path: Path, throttleKey: Any?, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val now = System.currentTimeMillis()
        if (throttleKey != null && nextAllowedUpload.getOrDefault(throttleKey, 0) > now) {
            onFailure()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            run {
                try {
                    val fileMetadata = File()
                    fileMetadata.name = fileName
                    fileMetadata.parents = listOf(UPLOAD_FOLDER_ID)
                    val mediaContent = FileContent("text/yaml", path.toFile())
                    driveService.files().create(fileMetadata, mediaContent).execute()
                    if (throttleKey != null) {
                        nextAllowedUpload[throttleKey] = now + (throttleDurations[throttleKey] ?: DEFAULT_THROTTLE_DURATION).toMillis()
                    }

                    onSuccess()
                } catch (ignored: Throwable) {
                    onFailure()
                }
            }
        }

    }
}