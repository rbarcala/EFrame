package com.antigravity.eframe.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.eframe.data.WeatherData
import com.antigravity.eframe.data.WeatherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherModule(
    val repository: WeatherRepository = WeatherRepository(),
    private val getCityName: () -> String = { "Palermo" },
    private val getLatitude: () -> Double = { -34.5889 },
    private val getLongitude: () -> Double = { -58.4305 },
    private val getIsDetailed: () -> Boolean = { true },
    private val getUiScale: () -> Float = { 1.2f },
    override var isEnabled: Boolean = true
) : FrameModule {

    override val id: String = "module_weather"
    override val name: String = "Clima y Pronóstico"
    override val description: String = "Temperatura, estado del cielo y pronóstico horario en tiempo real"
    override val icon: ImageVector = Icons.Default.WbSunny
    override val defaultPosition: ModulePosition = ModulePosition.TOP_END

    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    val currentWeather: StateFlow<WeatherData?> = _currentWeather.asStateFlow()

    private val _lastUpdatedTime = MutableStateFlow<String>("")
    val lastUpdatedTime: StateFlow<String> = _lastUpdatedTime.asStateFlow()

    suspend fun refreshWeather() {
        val result = repository.fetchWeather(
            cityName = getCityName(),
            latitude = getLatitude(),
            longitude = getLongitude()
        )
        if (result.isSuccess) {
            val data = result.getOrThrow()
            _currentWeather.value = data
            _lastUpdatedTime.value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        }
    }

    @Composable
    override fun Content(modifier: Modifier, onClick: (() -> Unit)?) {
        val scale = getUiScale()
        val isDetailed = getIsDetailed()
        val weatherState by currentWeather.collectAsState()

        val weather = weatherState ?: remember {
            WeatherData(
                cityName = getCityName(),
                temperatureCelsius = 22.0,
                feelsLikeCelsius = 23.0,
                conditionText = "Soleado",
                iconEmoji = "☀️",
                tempMax = 25.0,
                tempMin = 16.0,
                windSpeedKmh = 12.0,
                humidityPercent = 50,
                precipitationProbCurrent = 5
            )
        }

        // Live automatic background refresh every 2 minutes for real-time accuracy
        LaunchedEffect(getCityName(), getLatitude(), getLongitude()) {
            while (true) {
                refreshWeather()
                delay(2 * 60 * 1000L) // 2 minutes
            }
        }

        Column(
            modifier = modifier
                .clip(RoundedCornerShape((18 * scale).dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Black.copy(alpha = 0.40f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape((18 * scale).dp)
                )
                .clickable { onClick?.invoke() }
                .padding(horizontal = (18 * scale).dp, vertical = (12 * scale).dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = weather.iconEmoji,
                    fontSize = (28 * scale).sp,
                    modifier = Modifier.padding(end = (6 * scale).dp)
                )
                Text(
                    text = "${weather.temperatureCelsius.toInt()}°C",
                    color = Color.White,
                    fontSize = (32 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${weather.conditionText} • ${weather.cityName}",
                color = Color.White.copy(alpha = 0.95f),
                fontSize = (13 * scale).sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = (2 * scale).dp)
            ) {
                Text(
                    text = "Mín ${weather.tempMin.toInt()}° / Máx ${weather.tempMax.toInt()}°",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = (11 * scale).sp
                )
                if (isDetailed) {
                    Spacer(modifier = Modifier.width((6 * scale).dp))
                    Text(
                        text = "• ST: ${weather.feelsLikeCelsius.toInt()}°",
                        color = Color(0xFFFFCC80),
                        fontSize = (11 * scale).sp
                    )
                    if (weather.precipitationProbCurrent > 0) {
                        Spacer(modifier = Modifier.width((6 * scale).dp))
                        Text(
                            text = "• 🌧️ ${weather.precipitationProbCurrent}%",
                            color = Color(0xFF90CAF9),
                            fontSize = (11 * scale).sp
                        )
                    }
                }
            }

            Text(
                text = "Tocar para ver próximas horas ↗",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = (9 * scale).sp,
                modifier = Modifier.padding(top = (3 * scale).dp)
            )
        }
    }
}
