package com.weatherdesk.repository

import com.weatherdesk.model.*
import com.weatherdesk.service.OpenMeteoService
import com.weatherdesk.service.WeatherServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Repository pattern for weather data access
 * Coordinates between API service and Firebase repository
 */
class WeatherRepository(
    private val weatherService: OpenMeteoService,
    private val firebaseRepository: FirebaseRepository
) {

    private val userId = "default_user" // In a real app, this would come from authentication

    /**
     * Fetch weather data and update user preferences
     */
    suspend fun getWeather(location: LocationInput): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                logger.info { "Fetching weather for location: $location" }
                val weatherData = weatherService.getWeather(location)

                // Save last searched location
                saveLastSearchedLocation(location, weatherData.current.city)

                Result.Success(weatherData)
            } catch (e: WeatherServiceException) {
                logger.error(e) { "Weather service error" }
                Result.Error("Failed to fetch weather: ${e.message}", e)
            } catch (e: Exception) {
                logger.error(e) { "Unexpected error fetching weather" }
                Result.Error("Unexpected error: ${e.message}", e)
            }
        }
    }

    /**
     * Get user preferences from Firebase
     */
    suspend fun getUserPreferences(): UserPreferences {
        return firebaseRepository.loadUserPreferences(userId) ?: UserPreferences()
    }

    /**
     * Save user preferences to Firebase
     */
    suspend fun saveUserPreferences(preferences: UserPreferences) {
        try {
            firebaseRepository.saveUserPreferences(userId, preferences)
        } catch (e: Exception) {
            logger.error(e) { "Failed to save preferences" }
            // Don't throw, preferences are not critical
        }
    }

    /**
     * Submit a forecast rating
     */
    suspend fun submitRating(city: String, rating: Int) {
        require(rating in 1..5) { "Rating must be between 1 and 5" }

        try {
            val forecastRating = ForecastRating(
                city = city,
                rating = rating,
                date = java.time.LocalDate.now(),
                userId = userId
            )
            firebaseRepository.saveForecastRating(forecastRating)
        } catch (e: Exception) {
            logger.error(e) { "Failed to submit rating" }
            throw RepositoryException("Failed to submit rating", e)
        }
    }

    /**
     * Get average rating for a city
     */
    suspend fun getAverageRating(city: String): Double? {
        return try {
            firebaseRepository.getAverageRating(city)
        } catch (e: Exception) {
            logger.error(e) { "Failed to get average rating" }
            null
        }
    }

    /**
     * Save last searched location to preferences
     */
    private suspend fun saveLastSearchedLocation(location: LocationInput, cityName: String) {
        try {
            val currentPrefs = getUserPreferences()
            val updatedPrefs = when (location) {
                is LocationInput.City -> currentPrefs.copy(
                    lastSearchedCity = cityName,
                    lastSearchedLatitude = null,
                    lastSearchedLongitude = null
                )
                is LocationInput.Coordinates -> currentPrefs.copy(
                    lastSearchedCity = cityName,
                    lastSearchedLatitude = location.latitude,
                    lastSearchedLongitude = location.longitude
                )
            }
            saveUserPreferences(updatedPrefs)
        } catch (e: Exception) {
            logger.error(e) { "Failed to save last searched location" }
        }
    }

    fun isFirebaseAvailable(): Boolean {
        return firebaseRepository.isFirebaseAvailable()
    }
}
