package com.weatherdesk.repository

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.weatherdesk.config.ConfigManager
import com.weatherdesk.model.ForecastRating
import com.weatherdesk.model.UserPreferences
import com.weatherdesk.model.TemperatureUnit
import com.weatherdesk.model.WindSpeedUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

/**
 * Repository for Firebase Firestore operations
 */
class FirebaseRepository {

    private var firestore: Firestore? = null
    private var isInitialized = false

    init {
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            if (!ConfigManager.isFirebaseConfigured()) {
                logger.warn { "Firebase not configured. Using local storage fallback." }
                return
            }

            val credentialsPath = ConfigManager.getFirebaseCredentialsPath()
            val credentialsFile = File(credentialsPath)

            if (!credentialsFile.exists()) {
                logger.warn { "Firebase credentials file not found at: $credentialsPath" }
                return
            }

            val credentials = GoogleCredentials.fromStream(FileInputStream(credentialsFile))

            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .also { builder ->
                    ConfigManager.getFirebaseProjectId()?.let { projectId ->
                        builder.setProjectId(projectId)
                    }
                }
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }

            firestore = FirestoreClient.getFirestore()
            isInitialized = true
            logger.info { "Firebase initialized successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize Firebase" }
            isInitialized = false
        }
    }

    /**
     * Save user preferences to Firestore
     */
    suspend fun saveUserPreferences(userId: String, preferences: UserPreferences) {
        if (!isInitialized) {
            logger.warn { "Firebase not initialized, cannot save preferences" }
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "lastSearchedCity" to preferences.lastSearchedCity,
                    "lastSearchedLatitude" to preferences.lastSearchedLatitude,
                    "lastSearchedLongitude" to preferences.lastSearchedLongitude,
                    "preferredTempUnit" to preferences.preferredTempUnit.name,
                    "preferredWindUnit" to preferences.preferredWindUnit.name,
                    "updatedAt" to System.currentTimeMillis()
                )

                firestore?.collection("users")
                    ?.document(userId)
                    ?.set(data)
                    ?.get()

                logger.info { "User preferences saved for user: $userId" }
            } catch (e: Exception) {
                logger.error(e) { "Error saving user preferences" }
                throw RepositoryException("Failed to save user preferences", e)
            }
        }
    }

    /**
     * Load user preferences from Firestore
     */
    suspend fun loadUserPreferences(userId: String): UserPreferences? {
        if (!isInitialized) {
            logger.warn { "Firebase not initialized, cannot load preferences" }
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                val document = firestore?.collection("users")
                    ?.document(userId)
                    ?.get()
                    ?.get()

                if (document?.exists() == true) {
                    val data = document.data ?: return@withContext null

                    UserPreferences(
                        lastSearchedCity = data["lastSearchedCity"] as? String,
                        lastSearchedLatitude = (data["lastSearchedLatitude"] as? Number)?.toDouble(),
                        lastSearchedLongitude = (data["lastSearchedLongitude"] as? Number)?.toDouble(),
                        preferredTempUnit = try {
                            TemperatureUnit.valueOf(data["preferredTempUnit"] as? String ?: "CELSIUS")
                        } catch (e: Exception) {
                            TemperatureUnit.CELSIUS
                        },
                        preferredWindUnit = try {
                            WindSpeedUnit.valueOf(data["preferredWindUnit"] as? String ?: "KILOMETERS_PER_HOUR")
                        } catch (e: Exception) {
                            WindSpeedUnit.KILOMETERS_PER_HOUR
                        }
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                logger.error(e) { "Error loading user preferences" }
                null
            }
        }
    }

    /**
     * Save forecast rating to Firestore
     */
    suspend fun saveForecastRating(rating: ForecastRating) {
        if (!isInitialized) {
            logger.warn { "Firebase not initialized, cannot save rating" }
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val data = mapOf(
                    "city" to rating.city,
                    "rating" to rating.rating,
                    "date" to rating.date.toString(),
                    "userId" to rating.userId,
                    "timestamp" to System.currentTimeMillis()
                )

                firestore?.collection("ratings")
                    ?.add(data)
                    ?.get()

                logger.info { "Forecast rating saved: ${rating.rating} stars for ${rating.city}" }
            } catch (e: Exception) {
                logger.error(e) { "Error saving forecast rating" }
                throw RepositoryException("Failed to save rating", e)
            }
        }
    }

    /**
     * Get average rating for a city
     */
    suspend fun getAverageRating(city: String): Double? {
        if (!isInitialized) {
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                val documents = firestore?.collection("ratings")
                    ?.whereEqualTo("city", city)
                    ?.get()
                    ?.get()

                val ratings = documents?.documents
                    ?.mapNotNull { it.getLong("rating")?.toInt() }
                    ?: emptyList()

                if (ratings.isEmpty()) null else ratings.average()
            } catch (e: Exception) {
                logger.error(e) { "Error getting average rating" }
                null
            }
        }
    }

    fun isFirebaseAvailable(): Boolean = isInitialized
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
