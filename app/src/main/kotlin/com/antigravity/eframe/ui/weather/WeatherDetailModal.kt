package com.antigravity.eframe.ui.weather

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.antigravity.eframe.data.WeatherData
import kotlinx.coroutines.launch

@Composable
fun WeatherDetailModal(
    weather: WeatherData?,
    lastUpdatedTime: String,
    onRefreshNow: suspend () -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "spin"
    )

    // Auto-trigger refresh if opened while null
    LaunchedEffect(weather) {
        if (weather == null) {
            isRefreshing = true
            onRefreshNow()
            isRefreshing = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(28.dp)),
            color = Color(0xFF141923)
        ) {
            if (weather == null) {
                // Loading View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF64B5F6),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Consultando pronóstico en tiempo real...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Conectando con Open-Meteo...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isRefreshing = true
                                onRefreshNow()
                                isRefreshing = false
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF90CAF9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reintentar",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reintentar conexión")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                    ) {
                        Text("Cerrar")
                    }
                }
            } else {
                // Full Weather Forecast View
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = weather.iconEmoji,
                                    fontSize = 32.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Pronóstico en ${weather.cityName}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${weather.conditionText} • Actual ${weather.temperatureCelsius.toInt()}°C (ST: ${weather.feelsLikeCelsius.toInt()}°C)",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                                if (lastUpdatedTime.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• Actualizado: $lastUpdatedTime",
                                        color = Color(0xFF81C784),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    if (!isRefreshing) {
                                        isRefreshing = true
                                        coroutineScope.launch {
                                            onRefreshNow()
                                            isRefreshing = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF90CAF9)),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Actualizar",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(if (isRefreshing) rotation else 0f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isRefreshing) "Actualizando..." else "Actualizar",
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    // Current detailed weather chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WeatherDetailChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.WaterDrop,
                            label = "Humedad",
                            value = "${weather.humidityPercent}%",
                            tint = Color(0xFF64B5F6)
                        )
                        WeatherDetailChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.WindPower,
                            label = "Viento",
                            value = "${weather.windSpeedKmh.toInt()} km/h",
                            tint = Color(0xFF81C784)
                        )
                        WeatherDetailChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.WbSunny,
                            label = "T. Máx / Mín",
                            value = "${weather.tempMax.toInt()}° / ${weather.tempMin.toInt()}°",
                            tint = Color(0xFFFFB74D)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // SECTION 1: Hourly Forecast (Strict next 24 hours from current time)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏰ Próximas 24 Horas (a partir de ahora)",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "En tiempo real",
                            color = Color(0xFF81C784),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        weather.hourlyList.forEach { hour ->
                            val isNow = hour.timeLabel.equals("Ahora", ignoreCase = true)
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isNow) Color(0xFF2E394E) else Color(0xFF222836)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .width(94.dp)
                                    .border(
                                        width = if (isNow) 1.5.dp else 1.dp,
                                        color = if (isNow) Color(0xFF64B5F6) else Color.White.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = hour.timeLabel,
                                        color = if (isNow) Color(0xFF64B5F6) else Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp,
                                        fontWeight = if (isNow) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = hour.iconEmoji,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${hour.tempCelsius.toInt()}°",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (hour.precipitationProb > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "💧${hour.precipitationProb}%",
                                            color = Color(0xFF90CAF9),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "ST ${hour.feelsLikeCelsius.toInt()}°",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SECTION 2: 7-Day Forecast
                    Text(
                        text = "📅 Pronóstico Extendido (7 Días)",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2432)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            weather.dailyList.forEachIndexed { index, day ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = day.dayName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.width(90.dp)
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = day.iconEmoji,
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = day.conditionText,
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 12.sp
                                        )
                                    }

                                    if (day.rainProbMax > 0) {
                                        Text(
                                            text = "💧${day.rainProbMax}%",
                                            color = Color(0xFF90CAF9),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                    }

                                    Row {
                                        Text(
                                            text = "${day.tempMax.toInt()}°",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${day.tempMin.toInt()}°",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                if (index < weather.dailyList.lastIndex) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2432)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.sp
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
