package com.antigravity.eframe.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class HourlyForecast(
    val timeLabel: String,
    val tempCelsius: Double,
    val feelsLikeCelsius: Double,
    val precipitationProb: Int,
    val iconEmoji: String,
    val conditionText: String
)

data class DailyForecast(
    val dayName: String,
    val tempMin: Double,
    val tempMax: Double,
    val iconEmoji: String,
    val conditionText: String,
    val rainProbMax: Int
)

data class WeatherData(
    val cityName: String,
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val conditionText: String,
    val iconEmoji: String,
    val tempMax: Double,
    val tempMin: Double,
    val windSpeedKmh: Double,
    val humidityPercent: Int,
    val precipitationProbCurrent: Int,
    val hourlyList: List<HourlyForecast> = emptyList(),
    val dailyList: List<DailyForecast> = emptyList()
)

class WeatherRepository {

    companion object {
        private const val TAG = "WeatherRepo"

        private val _currentWeather = MutableStateFlow<WeatherData?>(null)
        val currentWeather: StateFlow<WeatherData?> = _currentWeather.asStateFlow()

        private val _lastUpdatedTime = MutableStateFlow<String>("")
        val lastUpdatedTime: StateFlow<String> = _lastUpdatedTime.asStateFlow()

        private val client: OkHttpClient by lazy {
            HttpClientFactory.createClient()
        }
    }

    suspend fun fetchWeather(
        cityName: String,
        latitude: Double,
        longitude: Double
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,precipitation" +
                "&hourly=temperature_2m,apparent_temperature,precipitation_probability,weather_code" +
                "&daily=temperature_2m_max,temperature_2m_min,weather_code,precipitation_probability_max" +
                "&timezone=auto&forecast_days=7"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "EFrame/1.0 (Android; Linux)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP error ${response.code}"))
                }

                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                val current = json.getJSONObject("current")
                val hourly = json.optJSONObject("hourly")
                val daily = json.optJSONObject("daily")

                val temp = current.getDouble("temperature_2m")
                val feelsLike = current.optDouble("apparent_temperature", temp)
                val code = current.getInt("weather_code")
                val wind = current.getDouble("wind_speed_10m")
                val humidity = current.getInt("relative_humidity_2m")
                val precipProbNow = current.optInt("precipitation", 0)

                val (condition, emoji) = mapWmoCode(code)

                val maxTemp = daily?.optJSONArray("temperature_2m_max")?.optDouble(0, temp + 4) ?: (temp + 4)
                val minTemp = daily?.optJSONArray("temperature_2m_min")?.optDouble(0, temp - 4) ?: (temp - 4)

                // Parse STRICTLY next 24 hours starting from current time
                val hourlyList = mutableListOf<HourlyForecast>()
                if (hourly != null) {
                    val times = hourly.optJSONArray("time")
                    val temps = hourly.optJSONArray("temperature_2m")
                    val appTemps = hourly.optJSONArray("apparent_temperature")
                    val probs = hourly.optJSONArray("precipitation_probability")
                    val codes = hourly.optJSONArray("weather_code")

                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
                    val nowMillis = System.currentTimeMillis() - (45 * 60 * 1000L) // Include current ongoing hour

                    if (times != null && temps != null) {
                        var startIndex = -1
                        for (i in 0 until times.length()) {
                            val parsedTime = try {
                                isoFormat.parse(times.getString(i))?.time ?: 0L
                            } catch (e: Exception) {
                                0L
                            }
                            if (parsedTime >= nowMillis) {
                                startIndex = i
                                break
                            }
                        }

                        // Fallback if timestamp parsing had timezone offset: match local hour of day
                        if (startIndex < 0) {
                            val curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            startIndex = curHour.coerceIn(0, times.length() - 1)
                        }

                        val endIndex = (startIndex + 24).coerceAtMost(times.length())
                        for (i in startIndex until endIndex) {
                            val timeStr = times.getString(i)
                            val hourRaw = timeStr.substringAfter("T").take(5)
                            val isCurrentHour = (i == startIndex)
                            val label = if (isCurrentHour) "Ahora" else hourRaw

                            // Crucial: strictly align "Ahora" with current observations for 100% UI consistency
                            val hTemp = if (isCurrentHour) temp else temps.getDouble(i)
                            val hApp = if (isCurrentHour) feelsLike else (appTemps?.optDouble(i, hTemp) ?: hTemp)
                            val hProb = probs?.optInt(i, 0) ?: 0
                            val hCode = codes?.optInt(i, 0) ?: 0
                            val (hCond, hEmoji) = if (isCurrentHour) Pair(condition, emoji) else mapWmoCode(hCode)

                            hourlyList.add(
                                HourlyForecast(
                                    timeLabel = label,
                                    tempCelsius = hTemp,
                                    feelsLikeCelsius = hApp,
                                    precipitationProb = hProb,
                                    iconEmoji = hEmoji,
                                    conditionText = hCond
                                )
                            )
                        }
                    }
                }

                // Parse 7 days
                val dailyList = mutableListOf<DailyForecast>()
                if (daily != null) {
                    val days = daily.optJSONArray("time")
                    val maxs = daily.optJSONArray("temperature_2m_max")
                    val mins = daily.optJSONArray("temperature_2m_min")
                    val dCodes = daily.optJSONArray("weather_code")
                    val dProbs = daily.optJSONArray("precipitation_probability_max")

                    val dayParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val dayNameFormat = SimpleDateFormat("EEE d", Locale("es", "AR"))

                    if (days != null && maxs != null && mins != null) {
                        for (i in 0 until days.length().coerceAtMost(7)) {
                            val rawDate = days.getString(i)
                            val dName = try {
                                val d = dayParser.parse(rawDate)
                                if (i == 0) "Hoy" else dayNameFormat.format(d ?: Date()).replaceFirstChar { it.uppercase() }
                            } catch (e: Exception) {
                                if (i == 0) "Hoy" else "Día ${i + 1}"
                            }

                            val dMax = maxs.getDouble(i)
                            val dMin = mins.getDouble(i)
                            val dCode = dCodes?.optInt(i, 0) ?: 0
                            val dProb = dProbs?.optInt(i, 0) ?: 0
                            val (dCond, dEmoji) = mapWmoCode(dCode)

                            dailyList.add(
                                DailyForecast(
                                    dayName = dName,
                                    tempMin = dMin,
                                    tempMax = dMax,
                                    iconEmoji = dEmoji,
                                    conditionText = dCond,
                                    rainProbMax = dProb
                                )
                            )
                        }
                    }
                }

                val resultData = WeatherData(
                    cityName = cityName,
                    temperatureCelsius = temp,
                    feelsLikeCelsius = feelsLike,
                    conditionText = condition,
                    iconEmoji = emoji,
                    tempMax = maxTemp,
                    tempMin = minTemp,
                    windSpeedKmh = wind,
                    humidityPercent = humidity,
                    precipitationProbCurrent = precipProbNow,
                    hourlyList = hourlyList,
                    dailyList = dailyList
                )

                // Update single source of truth StateFlow
                _currentWeather.value = resultData
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                _lastUpdatedTime.value = timeStr

                Log.d(TAG, "Weather updated for $cityName: ${temp.toInt()}°C, $condition ($timeStr)")
                Result.success(resultData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun mapWmoCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> Pair("Despejado", "☀️")
            1 -> Pair("Mayormente despejado", "🌤️")
            2 -> Pair("Parcialmente nublado", "⛅")
            3 -> Pair("Nublado", "☁️")
            45, 48 -> Pair("Niebla", "🌫️")
            51, 53, 55 -> Pair("Llovizna", "🌦️")
            56, 57 -> Pair("Llovizna helada", "🌧️")
            61 -> Pair("Lluvia leve", "🌧️")
            63 -> Pair("Lluvia moderada", "🌧️")
            65 -> Pair("Lluvia fuerte", "🌧️")
            66, 67 -> Pair("Lluvia helada", "🌧️")
            71, 73, 75 -> Pair("Nevada", "❄️")
            77 -> Pair("Granizo fino", "🌨️")
            80, 81, 82 -> Pair("Chaparrones", "🌦️")
            85, 86 -> Pair("Chaparrones de nieve", "🌨️")
            95 -> Pair("Tormenta eléctrica", "⛈️")
            96, 99 -> Pair("Tormenta con granizo", "⛈️")
            else -> Pair("Parcialmente nublado", "⛅")
        }
    }
}
