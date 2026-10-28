package com.antigravity.eframe.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.antigravity.eframe.data.SubteLineInfo
import com.antigravity.eframe.data.TransportRepository
import kotlinx.coroutines.delay

class TransportModule(
    val repository: TransportRepository = TransportRepository(),
    private val getSelectedLine: () -> String = { "Línea D" },
    private val getWaitingStation: () -> String = { "Palermo" },
    private val getDestination: () -> String = { "Congreso de Tucumán" },
    private val getIsDetailed: () -> Boolean = { true },
    private val getUiScale: () -> Float = { 1.2f },
    override var isEnabled: Boolean = true
) : FrameModule {

    override val id: String = "module_transport"
    override val name: String = "Transporte y Subte"
    override val description: String = "Próximo tren, estación y dirección de viaje"
    override val icon: ImageVector = Icons.Default.DirectionsSubway
    override val defaultPosition: ModulePosition = ModulePosition.BOTTOM_START

    @Composable
    override fun Content(modifier: Modifier, onClick: (() -> Unit)?) {
        val scale = getUiScale()
        val isDetailed = getIsDetailed()

        val lineInfo = remember(getSelectedLine()) {
            repository.getLineByName(getSelectedLine())
        }

        var arrivalMinutes by remember { mutableIntStateOf(2) }

        LaunchedEffect(getSelectedLine(), getWaitingStation(), getDestination()) {
            while (true) {
                arrivalMinutes = repository.getEstimatedArrival(
                    lineName = getSelectedLine(),
                    stationName = getWaitingStation(),
                    destinationTerminal = getDestination()
                )
                delay(20000L) // Refresh ETA every 20 seconds
            }
        }

        Row(
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
                .padding(horizontal = (16 * scale).dp, vertical = (12 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subway badge with line color
            Box(
                modifier = Modifier
                    .size((38 * scale).dp)
                    .clip(CircleShape)
                    .background(lineInfo.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsSubway,
                    contentDescription = lineInfo.name,
                    tint = Color.White,
                    modifier = Modifier.size((22 * scale).dp)
                )
            }

            Spacer(modifier = Modifier.width((12 * scale).dp))

            Column(verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lineInfo.name,
                        color = Color.White,
                        fontSize = (15 * scale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width((6 * scale).dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = (6 * scale).dp, vertical = (2 * scale).dp)
                    ) {
                        Text(
                            text = lineInfo.status,
                            color = if (lineInfo.status.contains("Normal")) Color(0xFF4CAF50) else Color(0xFFFFB74D),
                            fontSize = (10 * scale).sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "Próximo tren: en $arrivalMinutes min",
                    color = Color.White,
                    fontSize = (13 * scale).sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (isDetailed) {
                    Text(
                        text = "En: ${getWaitingStation()} → Hacia: ${getDestination()}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = (11 * scale).sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Text(
                    text = "Tocar para ver estaciones y líneas ↗",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = (9 * scale).sp,
                    modifier = Modifier.padding(top = (2 * scale).dp)
                )
            }
        }
    }
}
