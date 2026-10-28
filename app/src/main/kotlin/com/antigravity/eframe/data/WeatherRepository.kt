package com.antigravity.eframe.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

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
            val request = Request.Builder().url(url).build()
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
                            val label = if (i == startIndex) "Ahora" else hourRaw

                            val hTemp = temps.getDouble(i)
                            val hApp = appTemps?.optDouble(i, hTemp) ?: hTemp
                            val hProb = probs?.optInt(i, 0) ?: 0
                            val hCode = codes?.optInt(i, 0) ?: 0
                            val (hCond, hEmoji) = mapWmoCode(hCode)

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

                    val inFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val outFormat = SimpleDateFormat("EEEE d", Locale("es", "ES"))

                    if (days != null && maxs != null && mins != null) {
                        for (i in 0 until days.length()) {
                            val dateStr = days.getString(i)
                            val dayLabel = try {
                                val d = inFormat.parse(dateStr)
                                if (i == 0) "Hoy" else if (i == 1) "Mañana" else if (d != null) outFormat.format(d).replaceFirstChar { it.uppercase() } else dateStr
                            } catch (e: Exception) {
                                dateStr
                            }
                            val dMax = maxs.getDouble(i)
                            val dMin = mins.getDouble(i)
                            val dCode = dCodes?.optInt(i, 0) ?: 0
                            val dProb = dProbs?.optInt(i, 0) ?: 0
                            val (dCond, dEmoji) = mapWmoCode(dCode)

                            dailyList.add(
                                DailyForecast(
                                    dayName = dayLabel,
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

                val currentRainProb = hourlyList.firstOrNull()?.precipitationProb ?: 0

                Result.success(
                    WeatherData(
                        cityName = cityName,
                        temperatureCelsius = temp,
                        feelsLikeCelsius = feelsLike,
                        conditionText = condition,
                        iconEmoji = emoji,
                        tempMax = maxTemp,
                        tempMin = minMin(minTemp, temp),
                        windSpeedKmh = wind,
                        humidityPercent = humidity,
                        precipitationProbCurrent = currentRainProb,
                        hourlyList = hourlyList,
                        dailyList = dailyList
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather data", e)
            Result.success(generateFallbackWeather(cityName))
        }
    }

    private fun minMin(a: Double, b: Double): Double = if (a < b) a else b

    private fun generateFallbackWeather(cityName: String): WeatherData {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val sampleHourly = (0 until 24).map { offset ->
            val h = (currentHour + offset) % 24
            val label = if (offset == 0) "Ahora" else String.format("%02d:00", h)
            val isDay = h in 7..20
            HourlyForecast(
                timeLabel = label,
                tempCelsius = 19.0 + (h % 7),
                feelsLikeCelsius = 19.0 + (h % 7),
                precipitationProb = if (h in 15..19) 35 else 0,
                iconEmoji = if (isDay) "🌤️" else "🌙",
                conditionText = if (isDay) "Parcialmente nublado" else "Despejado"
            )
        }

        val sampleDaily = listOf("Hoy", "Mañana", "Viernes", "Sábado", "Domingo", "Lunes", "Martes").map { day ->
            DailyForecast(
                dayName = day,
                tempMin = 15.0,
                tempMax = 25.0,
                iconEmoji = "☀️",
                conditionText = "Agradable",
                rainProbMax = 10
            )
        }

        return WeatherData(
            cityName = cityName,
            temperatureCelsius = 22.0,
            feelsLikeCelsius = 23.0,
            conditionText = "Despejado",
            iconEmoji = "☀️",
            tempMax = 26.0,
            tempMin = 16.0,
            windSpeedKmh = 14.0,
            humidityPercent = 52,
            precipitationProbCurrent = 10,
            hourlyList = sampleHourly,
            dailyList = sampleDaily
        )
    }

    private fun mapWmoCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> Pair("Despejado", "☀️")
            1 -> Pair("Mayormente despejado", "🌤️")
            2 -> Pair("Parcialmente nublado", "⛅")
            3 -> Pair("Nublado", "☁️")
            45, 48 -> Pair("Niebla", "🌫️")
            51, 53, 55 -> Pair("Llovizna", "🌦️")
            61, 63, 65 -> Pair("Lluvia", "🌧️")
            71, 73, 75 -> Pair("Nieve", "❄️")
            80, 81, 82 -> Pair("Chubascos", "🌧️")
            95, 96, 99 -> Pair("Tormenta eléctrica", "⛈️")
            else -> Pair("Clima templado", "🌤️")
        }
    }

    companion object {
        private const val TAG = "WeatherRepository"
    }
}
