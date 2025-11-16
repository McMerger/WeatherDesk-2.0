package com.weatherdesk.config

import mu.KotlinLogging
import java.io.File
import java.util.Properties

private val logger = KotlinLogging.logger {}

/**
 * Configuration manager for handling API keys and app configuration
 * Loads from config.properties file or environment variables
 */
object ConfigManager {

    private val properties = Properties()
    private const val CONFIG_FILE = "config.properties"

    init {
        loadConfiguration()
    }

    private fun loadConfiguration() {
        // Try to load from config.properties file
        val configFile = File(CONFIG_FILE)
        if (configFile.exists()) {
            try {
                configFile.inputStream().use { properties.load(it) }
                logger.info { "Configuration loaded from $CONFIG_FILE" }
            } catch (e: Exception) {
                logger.error(e) { "Error loading configuration from $CONFIG_FILE" }
            }
        } else {
            logger.warn { "$CONFIG_FILE not found, will use environment variables" }
        }
    }

    /**
     * Get OpenWeatherMap API key from config or environment (DEPRECATED - kept for backward compatibility)
     * OpenMeteo API does not require API keys
     */
    @Deprecated("Application now uses OpenMeteo API which doesn't require API keys")
    fun getOpenWeatherMapApiKey(): String? {
        return properties.getProperty("openweathermap.api.key")
            ?: System.getenv("OPENWEATHERMAP_API_KEY")
    }

    /**
     * Get Firebase credentials file path
     */
    fun getFirebaseCredentialsPath(): String {
        return properties.getProperty("firebase.credentials.path")
            ?: System.getenv("FIREBASE_CREDENTIALS_PATH")
            ?: "firebase-credentials.json"
    }

    /**
     * Get Firebase project ID
     */
    fun getFirebaseProjectId(): String? {
        return properties.getProperty("firebase.project.id")
            ?: System.getenv("FIREBASE_PROJECT_ID")
    }

    /**
     * Check if Firebase is configured
     */
    fun isFirebaseConfigured(): Boolean {
        val credentialsPath = getFirebaseCredentialsPath()
        val credentialsFile = File(credentialsPath)
        return credentialsFile.exists()
    }
}

class ConfigurationException(message: String) : Exception(message)
