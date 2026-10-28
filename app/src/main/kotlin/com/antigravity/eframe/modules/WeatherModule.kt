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
import kotlinx.coroutines.flow.StateFlow

class WeatherModule(
    val repository: WeatherRepository = WeatherRepository(),
    private val getCityName: () -> String = { "Palermo" },
    private val getLatitude: () -> Double = { -34.5889 },
    private val getLongitude: () -> Double = { -58.4305 },
    private val getIsDetailed: () -> Boolean = { true },
    private val getUiScale: () -> Float = { 1.6f },
    override var isEnabled: Boolean = true
) : FrameModule {

    override val id: String = "module_weather"
    override val name: String = "Clima y Pronóstico"
    override val description: String = "Temperatura, estado del cielo y pronóstico horario en tiempo real"
    override val icon: ImageVector = Icons.Default.WbSunny
    override val defaultPosition: ModulePosition = ModulePosition.TOP_END

    val currentWeather: StateFlow<WeatherData?> = WeatherRepository.currentWeather
    val lastUpdatedTime: StateFlow<String> = WeatherRepository.lastUpdatedTime

    suspend fun refreshWeather() {
        repository.fetchWeather(
            cityName = getCityName(),
            latitude = getLatitude(),
            longitude = getLongitude()
        )
    }

    @Composable
    override fun Content(modifier: Modifier, onClick: (() -> Unit)?) {
        val scale = getUiScale()
        val isDetailed = getIsDetailed()
        val weather by WeatherRepository.currentWeather.collectAsState()

        // Background auto-refresh loop every 2 minutes
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
            if (weather != null) {
                val data = weather!!
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = data.iconEmoji,
                        fontSize = (28 * scale).sp,
                        modifier = Modifier.padding(end = (6 * scale).dp)
                    )
                    Text(
                        text = "${data.temperatureCelsius.toInt()}°C",
                        color = Color.White,
                        fontSize = (32 * scale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${data.conditionText} • ${data.cityName}",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = (13 * scale).sp,
                    fontWeight = FontWeight.Medium
                )

                if (isDetailed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.padding(top = (4 * scale).dp)
                    ) {
                        Text(
                            text = "ST: ${data.feelsLikeCelsius.toInt()}°",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = (11 * scale).sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width((8 * scale).dp))
                        Text(
                            text = "Max ${data.tempMax.toInt()}° / Min ${data.tempMin.toInt()}°",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = (11 * scale).sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.padding(top = (2 * scale).dp)
                    ) {
                        Text(
                            text = "💧 ${data.humidityPercent}%  •  💨 ${data.windSpeedKmh.toInt()} km/h",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = (10 * scale).sp
                        )
                    }
                }
            } else {
                // Loading / initial state (no fake numbers)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "🌤️",
                        fontSize = (28 * scale).sp,
                        modifier = Modifier.padding(end = (6 * scale).dp)
                    )
                    Text(
                        text = "--°C",
                        color = Color.White,
                        fontSize = (32 * scale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Consultando clima • ${getCityName()}",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = (12 * scale).sp
                )
            }

            Text(
                text = "Tocar para ver pronóstico detallado ↗",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = (9 * scale).sp,
                modifier = Modifier.padding(top = (4 * scale).dp)
            )
        }
    }
}
