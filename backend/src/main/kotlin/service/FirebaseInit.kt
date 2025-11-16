package service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.File
import java.io.FileInputStream

object FirebaseInit {
    private val logger = mu.KotlinLogging.logger {}

    fun init() {
        if (FirebaseApp.getApps().isNotEmpty()) return

        val credentialsStream = getCredentialsStream()
            ?: throw IllegalStateException(
                "Service account credentials not found. " +
                        "Set GOOGLE_APPLICATION_CREDENTIALS or place serviceAccount.json in resources."
            )

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(credentialsStream))
            .build()

        FirebaseApp.initializeApp(options)
        logger.info { "Firebase initialized successfully" }
    }

    private fun getCredentialsStream(): java.io.InputStream? {
        // Try resources.
        val resourceStream = javaClass.getResourceAsStream("/serviceAccount.json")
        if (resourceStream != null) {
            logger.info { "Using service account from resources/serviceAccount.json" }
            return resourceStream
        }

        // Didn't work. Try environment.
        val envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        if (!envPath.isNullOrBlank()) {
            val file = File(envPath)
            if (file.exists()) {
                logger.info { "Using service account from environment variable path" }
                return FileInputStream(file)
            }
        }

        // Didn't work. I have nothing.
        return null
    }
}
