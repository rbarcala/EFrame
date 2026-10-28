package com.antigravity.eframe.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FrameConfig(
    val driveFolderUrl: String = "",
    val driveApiKey: String = "",
    val slideDurationSeconds: Int = 8,
    val inactivityTimeoutSeconds: Int = 10,
    val proximityWakeEnabled: Boolean = true,
    val isClockEnabled: Boolean = true,
    val isWeatherEnabled: Boolean = true,
    val isTransportEnabled: Boolean = true,
    val isDetailedMode: Boolean = true,
    val uiScale: Float = 1.6f, // 1.0f a 2.2f
    val weatherCountry: String = "Argentina",
    val weatherProvince: String = "CABA (Buenos Aires)",
    val weatherCity: String = "Palermo",
    val weatherLatitude: Double = -34.5889,
    val weatherLongitude: Double = -58.4305,
    val transportLine: String = "Línea D",
    val transportStation: String = "Palermo",
    val transportDirection: String = "Congreso de Tucumán"
)

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("eframe_preferences", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<FrameConfig> = _config.asStateFlow()

    private fun loadConfig(): FrameConfig {
        return FrameConfig(
            driveFolderUrl = prefs.getString(KEY_DRIVE_URL, "") ?: "",
            driveApiKey = prefs.getString(KEY_DRIVE_API_KEY, "") ?: "",
            slideDurationSeconds = prefs.getInt(KEY_SLIDE_DURATION, 8),
            inactivityTimeoutSeconds = prefs.getInt(KEY_INACTIVITY_TIMEOUT, 10),
            proximityWakeEnabled = prefs.getBoolean(KEY_PROXIMITY_WAKE, true),
            isClockEnabled = prefs.getBoolean(KEY_CLOCK_ENABLED, true),
            isWeatherEnabled = prefs.getBoolean(KEY_WEATHER_ENABLED, true),
            isTransportEnabled = prefs.getBoolean(KEY_TRANSPORT_ENABLED, true),
            isDetailedMode = prefs.getBoolean(KEY_DETAILED_MODE, true),
            uiScale = prefs.getFloat(KEY_UI_SCALE, 1.6f),
            weatherCountry = prefs.getString(KEY_WEATHER_COUNTRY, "Argentina") ?: "Argentina",
            weatherProvince = prefs.getString(KEY_WEATHER_PROVINCE, "CABA (Buenos Aires)") ?: "CABA (Buenos Aires)",
            weatherCity = prefs.getString(KEY_WEATHER_CITY, "Palermo") ?: "Palermo",
            weatherLatitude = prefs.getFloat(KEY_WEATHER_LAT, -34.5889f).toDouble(),
            weatherLongitude = prefs.getFloat(KEY_WEATHER_LON, -58.4305f).toDouble(),
            transportLine = prefs.getString(KEY_TRANSPORT_LINE, "Línea D") ?: "Línea D",
            transportStation = prefs.getString(KEY_TRANSPORT_STATION, "Palermo") ?: "Palermo",
            transportDirection = prefs.getString(KEY_TRANSPORT_DIRECTION, "Congreso de Tucumán") ?: "Congreso de Tucumán"
        )
    }

    fun updateConfig(newConfig: FrameConfig) {
        prefs.edit()
            .putString(KEY_DRIVE_URL, newConfig.driveFolderUrl)
            .putString(KEY_DRIVE_API_KEY, newConfig.driveApiKey)
            .putInt(KEY_SLIDE_DURATION, newConfig.slideDurationSeconds)
            .putInt(KEY_INACTIVITY_TIMEOUT, newConfig.inactivityTimeoutSeconds)
            .putBoolean(KEY_PROXIMITY_WAKE, newConfig.proximityWakeEnabled)
            .putBoolean(KEY_CLOCK_ENABLED, newConfig.isClockEnabled)
            .putBoolean(KEY_WEATHER_ENABLED, newConfig.isWeatherEnabled)
            .putBoolean(KEY_TRANSPORT_ENABLED, newConfig.isTransportEnabled)
            .putBoolean(KEY_DETAILED_MODE, newConfig.isDetailedMode)
            .putFloat(KEY_UI_SCALE, newConfig.uiScale)
            .putString(KEY_WEATHER_COUNTRY, newConfig.weatherCountry)
            .putString(KEY_WEATHER_PROVINCE, newConfig.weatherProvince)
            .putString(KEY_WEATHER_CITY, newConfig.weatherCity)
            .putFloat(KEY_WEATHER_LAT, newConfig.weatherLatitude.toFloat())
            .putFloat(KEY_WEATHER_LON, newConfig.weatherLongitude.toFloat())
            .putString(KEY_TRANSPORT_LINE, newConfig.transportLine)
            .putString(KEY_TRANSPORT_STATION, newConfig.transportStation)
            .putString(KEY_TRANSPORT_DIRECTION, newConfig.transportDirection)
            .apply()

        _config.value = newConfig
    }

    companion object {
        private const val KEY_DRIVE_URL = "drive_folder_url"
        private const val KEY_DRIVE_API_KEY = "drive_api_key"
        private const val KEY_SLIDE_DURATION = "slide_duration"
        private const val KEY_INACTIVITY_TIMEOUT = "inactivity_timeout"
        private const val KEY_PROXIMITY_WAKE = "proximity_wake"
        private const val KEY_CLOCK_ENABLED = "clock_enabled"
        private const val KEY_WEATHER_ENABLED = "weather_enabled"
        private const val KEY_TRANSPORT_ENABLED = "transport_enabled"
        private const val KEY_DETAILED_MODE = "detailed_mode"
        private const val KEY_UI_SCALE = "ui_scale"
        private const val KEY_WEATHER_COUNTRY = "weather_country"
        private const val KEY_WEATHER_PROVINCE = "weather_province"
        private const val KEY_WEATHER_CITY = "weather_city"
        private const val KEY_WEATHER_LAT = "weather_lat"
        private const val KEY_WEATHER_LON = "weather_lon"
        private const val KEY_TRANSPORT_LINE = "transport_line"
        private const val KEY_TRANSPORT_STATION = "transport_station"
        private const val KEY_TRANSPORT_DIRECTION = "transport_direction"

        @Volatile
        private var instance: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return instance ?: synchronized(this) {
                instance ?: AppSettings(context.applicationContext).also { instance = it }
            }
        }
    }
}
