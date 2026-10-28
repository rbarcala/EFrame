package com.antigravity.eframe.ui.transport

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.antigravity.eframe.data.DirectionSchedule
import com.antigravity.eframe.data.SubteLineInfo
import com.antigravity.eframe.data.TransportRepository
import com.antigravity.eframe.data.UpcomingTrain
import kotlinx.coroutines.launch

@Composable
fun SubteDetailModal(
    repository: TransportRepository = remember { TransportRepository() },
    userConfiguredLineName: String,
    userWaitingStation: String,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allLines by TransportRepository.linesState.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Initial fetch on open
    LaunchedEffect(Unit) {
        isRefreshing = true
        repository.fetchLiveStatus(force = true)
        isRefreshing = false
    }

    // Default to the line the user has in configuration
    var selectedLineName by remember { mutableStateOf(userConfiguredLineName) }

    val currentLine: SubteLineInfo = remember(selectedLineName, allLines) {
        allLines.find { it.name.equals(selectedLineName, ignoreCase = true) }
            ?: repository.getLineByName(selectedLineName)
    }

    // Schedules for both directions
    val (scheduleA, scheduleB) = remember(currentLine, userWaitingStation) {
        repository.getUpcomingTrainsForDirections(currentLine, userWaitingStation)
    }

    val closedStationsInCurrentLine = remember(currentLine) {
        currentLine.stations.filter { it.isClosed }
    }

    val otherClosedStations = remember(allLines, currentLine) {
        allLines.filter { it.id != currentLine.id }
            .flatMap { line ->
                line.stations.filter { it.isClosed }.map { station ->
                    Pair(line, station)
                }
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(28.dp)),
            color = Color(0xFF141923)
        ) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(currentLine.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsSubway,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Estado de Subtes y Próximos Trenes",
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Estación habitual: $userWaitingStation",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF64B5F6),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isRefreshing = true
                                        repository.fetchLiveStatus(force = true)
                                        isRefreshing = false
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Actualizar ahora",
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            }
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
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = Color.White.copy(alpha = 0.15f)
                )

                // Subheader with Official Emova timestamp
                if (currentLine.lastUpdated.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📡 Datos en vivo • Emova (Oficial)",
                            color = Color(0xFF81C784),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = currentLine.lastUpdated,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                // 1. Selector de cada Línea (por default en la que está)
                Text(
                    text = "Seleccioná la línea para consultar trenes y estado:",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allLines.forEach { line ->
                        val isSelected = line.name == selectedLineName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLineName = line.name },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(line.color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = line.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (line.isAlert) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFB74D))
                                        )
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = line.color.copy(alpha = 0.35f),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF222836),
                                labelColor = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Estado detallado de la línea seleccionada
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2432)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (!currentLine.isAlert) Color(0xFF4CAF50) else Color(0xFFFFB74D))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${currentLine.name}: ${currentLine.status}",
                                    color = if (!currentLine.isAlert) Color(0xFF4CAF50) else Color(0xFFFFB74D),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (currentLine.frequencyMinutes > 0) {
                                Text(
                                    text = "Frecuencia: ${currentLine.frequencyMinutes} min",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Red general en tiempo real:",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            allLines.forEach { line ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(line.color)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${line.name.replace("Línea ", "")}: ${line.status}",
                                        color = if (!line.isAlert) Color.White.copy(alpha = 0.7f) else Color(0xFFFFB74D),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. LISTADO DE TRENES HACIA CADA DIRECCIÓN
                Text(
                    text = "🚆 Próximos Trenes en $userWaitingStation (${currentLine.name})",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DirectionTrainsCard(
                        modifier = Modifier.weight(1f),
                        schedule = scheduleA,
                        lineColor = currentLine.color
                    )
                    DirectionTrainsCard(
                        modifier = Modifier.weight(1f),
                        schedule = scheduleB,
                        lineColor = currentLine.color
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. ESTACIONES DESHABILITADAS / EN OBRA
                Text(
                    text = "⚠️ Estaciones Deshabilitadas o Cerradas",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2432)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (closedStationsInCurrentLine.isEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Todas las estaciones de ${currentLine.name} se encuentran operativas.",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            closedStationsInCurrentLine.forEach { st ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFEF5350),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Estación ${st.name} (${currentLine.name})",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = st.closedReason.ifBlank { "Cerrada por obras de renovación integral" },
                                            color = Color(0xFFFFB74D),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Si hay otras estaciones cerradas en el resto de la red, listarlas también
                        if (otherClosedStations.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            )
                            Text(
                                text = "Otras estaciones cerradas en la red de Subtes:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            otherClosedStations.forEach { (line, st) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(line.color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${line.name}: Estación ${st.name}",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${st.closedReason.ifBlank { "En obra" }})",
                                        color = Color(0xFFFFB74D),
                                        fontSize = 11.sp
                                    )
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
private fun DirectionTrainsCard(
    modifier: Modifier = Modifier,
    schedule: DirectionSchedule,
    lineColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2432)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, lineColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(lineColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hacia ${schedule.terminalName}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            schedule.trains.forEachIndexed { idx, train ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (idx == 0) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Tren #${train.orderNumber}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = train.currentPositionText,
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (idx == 0) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (train.minutesAway > 0) "en ${train.minutesAway} min" else "Interrumpido",
                            color = if (idx == 0 && train.minutesAway > 0) Color(0xFF81C784) else Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (idx < schedule.trains.lastIndex) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                }
            }
        }
    }
}
