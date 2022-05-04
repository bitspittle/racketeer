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
import kotlin.io.path.name

interface UploadService {
    /**
     * Upload some data file to a folder in the Cloud.
     *
     * This method shouldn't block; instead, it should perform this action on a background thread and, if it fails,
     * just fail silently.
     */
    fun upload(fileName: String, path: Path)
}

class DriveUploadService(title: String) : UploadService {
    private val UPLOAD_FOLDER_ID = "14bplvWMj-3qTuydASDz1LcBUzxDjz28U"
    private val JSON_FACTORY = GsonFactory.getDefaultInstance()

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

    override fun upload(fileName: String, path: Path) {
        CoroutineScope(Dispatchers.IO).launch {
            run {
                val fileMetadata = File()
                fileMetadata.name = fileName
                fileMetadata.parents = listOf(UPLOAD_FOLDER_ID)
                val mediaContent = FileContent("text/yaml", path.toFile())
                val result = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute()

                println("Created ${result.id}")
            }
        }

    }
}