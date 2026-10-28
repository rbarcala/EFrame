package com.antigravity.eframe.ui.settings

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.antigravity.eframe.data.ArgentinaLocations
import com.antigravity.eframe.data.FrameConfig
import com.antigravity.eframe.data.TransportRepository

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    initialConfig: FrameConfig,
    transportRepository: TransportRepository = remember { TransportRepository() },
    onDismiss: () -> Unit,
    onSaveConfig: (FrameConfig) -> Unit,
    onRefreshPhotos: () -> Unit
) {
    val context = LocalContext.current
    var driveUrl by remember { mutableStateOf(initialConfig.driveFolderUrl) }
    var slideDuration by remember { mutableIntStateOf(initialConfig.slideDurationSeconds) }
    var inactivityTimeout by remember { mutableIntStateOf(initialConfig.inactivityTimeoutSeconds) }
    var proximityWake by remember { mutableStateOf(initialConfig.proximityWakeEnabled) }

    // Module toggles
    var clockEnabled by remember { mutableStateOf(initialConfig.isClockEnabled) }
    var weatherEnabled by remember { mutableStateOf(initialConfig.isWeatherEnabled) }
    var transportEnabled by remember { mutableStateOf(initialConfig.isTransportEnabled) }

    // Visual options
    var isDetailedMode by remember { mutableStateOf(initialConfig.isDetailedMode) }
    var uiScale by remember { mutableStateOf(initialConfig.uiScale) }

    // Weather Geographic Hierarchy
    var weatherCountry by remember { mutableStateOf(initialConfig.weatherCountry) }
    var weatherProvince by remember { mutableStateOf(initialConfig.weatherProvince) }
    var weatherCity by remember { mutableStateOf(initialConfig.weatherCity) }
    var weatherLat by remember { mutableStateOf(initialConfig.weatherLatitude) }
    var weatherLon by remember { mutableStateOf(initialConfig.weatherLongitude) }

    var expandedCountryMenu by remember { mutableStateOf(false) }
    var expandedProvinceMenu by remember { mutableStateOf(false) }
    var expandedCityMenu by remember { mutableStateOf(false) }

    // Subte settings
    var transportLine by remember { mutableStateOf(initialConfig.transportLine) }
    var transportStation by remember { mutableStateOf(initialConfig.transportStation) }
    var transportDirection by remember { mutableStateOf(initialConfig.transportDirection) }

    var expandedLineMenu by remember { mutableStateOf(false) }
    var expandedStationMenu by remember { mutableStateOf(false) }
    var expandedDirectionMenu by remember { mutableStateOf(false) }

    val currentLineInfo = remember(transportLine) {
        transportRepository.getLineByName(transportLine)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(28.dp)),
            color = Color(0xFF141923)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Configuración del Marco Inteligente",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personalizá fotos, escala de widgets, reposo, clima y transporte",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp
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

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.15f)
                )

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. Google Drive Section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2634)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = Color(0xFF64B5F6)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Carpeta de Google Drive (Pública)",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pegá el link a una carpeta pública de Drive con tus fotos.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = driveUrl,
                                onValueChange = { driveUrl = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "https://drive.google.com/drive/folders/...",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                        if (!clip.isNullOrBlank()) {
                                            driveUrl = clip.trim()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = "Pegar",
                                            tint = Color(0xFF90CAF9)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF64B5F6),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onSaveConfig(
                                            initialConfig.copy(
                                                driveFolderUrl = driveUrl,
                                                slideDurationSeconds = slideDuration,
                                                inactivityTimeoutSeconds = inactivityTimeout,
                                                proximityWakeEnabled = proximityWake,
                                                isClockEnabled = clockEnabled,
                                                isWeatherEnabled = weatherEnabled,
                                                isTransportEnabled = transportEnabled,
                                                isDetailedMode = isDetailedMode,
                                                uiScale = uiScale,
                                                weatherCountry = weatherCountry,
                                                weatherProvince = weatherProvince,
                                                weatherCity = weatherCity,
                                                weatherLatitude = weatherLat,
                                                weatherLongitude = weatherLon,
                                                transportLine = transportLine,
                                                transportStation = transportStation,
                                                transportDirection = transportDirection
                                            )
                                        )
                                        onRefreshPhotos()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF90CAF9))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar y Recargar Fotos")
                                }
                            }
                        }
                    }

                    // 2. UI Scale & Detailed Mode Section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2634)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = null,
                                    tint = Color(0xFF81C784)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Escala de Widgets y Modo de Información",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Agrandá los rectángulos para leerlos fácilmente desde lejos en la tablet:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    1.0f to "1.0x Normal",
                                    1.2f to "1.2x Grande (Recomendado)",
                                    1.4f to "1.4x Extra Grande",
                                    1.6f to "1.6x Tablet Lejana"
                                ).forEach { (scaleVal, label) ->
                                    FilterChip(
                                        selected = kotlin.math.abs(uiScale - scaleVal) < 0.05f,
                                        onClick = { uiScale = scaleVal },
                                        label = { Text(label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF4CAF50),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF2C3446),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Detailed mode switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFF64B5F6),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Modo de Información Detallado",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "Muestra sensación térmica, probabilidad de lluvia, estación de espera y destino de subte.",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Switch(
                                    checked = isDetailedMode,
                                    onCheckedChange = { isDetailedMode = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2196F3)
                                    )
                                )
                            }
                        }
                    }

                    // 3. Location (Weather) Hierarchy
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2634)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB74D)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ubicación Geográfica (Clima y Pronóstico)",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Elegí País, Provincia y Barrio/Ciudad de Argentina:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // 1. Country Selector
                            ExposedDropdownMenuBox(
                                expanded = expandedCountryMenu,
                                onExpandedChange = { expandedCountryMenu = !expandedCountryMenu }
                            ) {
                                OutlinedTextField(
                                    value = weatherCountry,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("País") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountryMenu) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF64B5F6),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCountryMenu,
                                    onDismissRequest = { expandedCountryMenu = false }
                                ) {
                                    ArgentinaLocations.countries.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c) },
                                            onClick = {
                                                weatherCountry = c
                                                expandedCountryMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Province Selector
                            val provinces = ArgentinaLocations.provincesByCountry[weatherCountry] ?: emptyList()
                            ExposedDropdownMenuBox(
                                expanded = expandedProvinceMenu,
                                onExpandedChange = { expandedProvinceMenu = !expandedProvinceMenu }
                            ) {
                                OutlinedTextField(
                                    value = weatherProvince,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Provincia") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvinceMenu) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF64B5F6),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedProvinceMenu,
                                    onDismissRequest = { expandedProvinceMenu = false }
                                ) {
                                    provinces.forEach { prov ->
                                        DropdownMenuItem(
                                            text = { Text(prov) },
                                            onClick = {
                                                weatherProvince = prov
                                                val cities = ArgentinaLocations.getCities(weatherCountry, prov)
                                                if (cities.isNotEmpty()) {
                                                    val first = cities.first()
                                                    weatherCity = first.cityOrNeighborhood
                                                    weatherLat = first.latitude
                                                    weatherLon = first.longitude
                                                }
                                                expandedProvinceMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. City / Neighborhood Selector
                            val cities = ArgentinaLocations.getCities(weatherCountry, weatherProvince)
                            ExposedDropdownMenuBox(
                                expanded = expandedCityMenu,
                                onExpandedChange = { expandedCityMenu = !expandedCityMenu }
                            ) {
                                OutlinedTextField(
                                    value = weatherCity,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Barrio / Localidad") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCityMenu) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF64B5F6),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCityMenu,
                                    onDismissRequest = { expandedCityMenu = false }
                                ) {
                                    cities.forEach { loc ->
                                        DropdownMenuItem(
                                            text = { Text(loc.cityOrNeighborhood) },
                                            onClick = {
                                                weatherCity = loc.cityOrNeighborhood
                                                weatherLat = loc.latitude
                                                weatherLon = loc.longitude
                                                expandedCityMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Transport & Subway Settings (Line, Station, Direction)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2634)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsSubway,
                                    contentDescription = null,
                                    tint = Color(0xFF00A2DF)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Transporte: Línea, Estación y Sentido de Viaje",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Configurá la estación donde solés esperar y hacia qué cabecera viajás:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // 1. Line
                            val lines = transportRepository.getAllLines()
                            ExposedDropdownMenuBox(
                                expanded = expandedLineMenu,
                                onExpandedChange = { expandedLineMenu = !expandedLineMenu }
                            ) {
                                OutlinedTextField(
                                    value = transportLine,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Línea de Subte") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLineMenu) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF64B5F6),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedLineMenu,
                                    onDismissRequest = { expandedLineMenu = false }
                                ) {
                                    lines.forEach { l ->
                                        DropdownMenuItem(
                                            text = { Text(l.name) },
                                            onClick = {
                                                transportLine = l.name
                                                if (l.stations.none { it.name == transportStation }) {
                                                    transportStation = l.stations.first().name
                                                }
                                                transportDirection = l.terminalB
                                                expandedLineMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Waiting Station
                            ExposedDropdownMenuBox(
                                expanded = expandedStationMenu,
                                onExpandedChange = { expandedStationMenu = !expandedStationMenu }
                            ) {
                                OutlinedTextField(
                                    value = transportStation,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Estación donde estás esperando") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStationMenu) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF64B5F6),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedStationMenu,
                                    onDismissRequest = { expandedStationMenu = false }
                                ) {
                                    currentLineInfo.stations.forEach { st ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(st.name)
                                                    if (st.isClosed) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("(Cerrada)", color = Color.Red, fontSize = 11.sp)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                transportStation = st.name
                                                expandedStationMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. Direction
                            ExposedDropdownMenuBox(
                                expanded = expandedDirectionMenu,
                                onExpandedChange = { expandedDirectionMenu = !expandedDirectionMenu }
                            ) {
                                OutlinedTextField(
                                    value = "Hacia $transportDirection",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Sentido de Viaje (Cabecera)") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDirectionMenu) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF64B5F6),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedDirectionMenu,
                                    onDismissRequest = { expandedDirectionMenu = false }
                                ) {
                                    listOf(currentLineInfo.terminalA, currentLineInfo.terminalB).forEach { term ->
                                        DropdownMenuItem(
                                            text = { Text("Hacia $term") },
                                            onClick = {
                                                transportDirection = term
                                                expandedDirectionMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Inactivity & Proximity Timing
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2634)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB74D)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tiempo de Inactividad y Reposo de Pantalla",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "La pantalla se oscurece y apaga el brillo suavemente si no se detecta actividad:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val timeouts = listOf(
                                    5 to "5 seg",
                                    10 to "10 seg (predeterminado)",
                                    30 to "30 seg",
                                    60 to "1 min",
                                    300 to "5 min",
                                    0 to "Nunca"
                                )
                                timeouts.forEach { (sec, label) ->
                                    FilterChip(
                                        selected = inactivityTimeout == sec,
                                        onClick = { inactivityTimeout = sec },
                                        label = { Text(label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFF9800),
                                            selectedLabelColor = Color.Black,
                                            containerColor = Color(0xFF2C3446),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Proximity sensor switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Sensors,
                                            contentDescription = null,
                                            tint = Color(0xFF81C784),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Despertar con sensor de proximidad",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "Al pasar la mano por encima del sensor frontal, la pantalla retoma brillo y color suavemente.",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Switch(
                                    checked = proximityWake,
                                    onCheckedChange = { proximityWake = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF4CAF50)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Slide duration
                            Text(
                                text = "Tiempo de cada foto en pantalla:",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(5, 8, 10, 15, 30, 60).forEach { sec ->
                                    FilterChip(
                                        selected = slideDuration == sec,
                                        onClick = { slideDuration = sec },
                                        label = { Text("$sec seg", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF2196F3),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF2C3446),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 6. Module Toggles
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2634)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Módulos Integrados y Widgets Activos",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            ModuleToggleRow(
                                icon = Icons.Default.Schedule,
                                title = "Reloj y Fecha",
                                description = "Hora digital con segundero y día en español",
                                isChecked = clockEnabled,
                                onCheckedChange = { clockEnabled = it }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.1f))

                            ModuleToggleRow(
                                icon = Icons.Default.WbSunny,
                                title = "Clima y Pronóstico",
                                description = "Temperatura actual, estado del cielo y pronóstico horario",
                                isChecked = weatherEnabled,
                                onCheckedChange = { weatherEnabled = it }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.1f))

                            ModuleToggleRow(
                                icon = Icons.Default.DirectionsSubway,
                                title = "Transporte Público / Subte",
                                description = "Próximo tren, estación de espera y estado",
                                isChecked = transportEnabled,
                                onCheckedChange = { transportEnabled = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val newConfig = initialConfig.copy(
                                driveFolderUrl = driveUrl,
                                slideDurationSeconds = slideDuration,
                                inactivityTimeoutSeconds = inactivityTimeout,
                                proximityWakeEnabled = proximityWake,
                                isClockEnabled = clockEnabled,
                                isWeatherEnabled = weatherEnabled,
                                isTransportEnabled = transportEnabled,
                                isDetailedMode = isDetailedMode,
                                uiScale = uiScale,
                                weatherCountry = weatherCountry,
                                weatherProvince = weatherProvince,
                                weatherCity = weatherCity,
                                weatherLatitude = weatherLat,
                                weatherLongitude = weatherLon,
                                transportLine = transportLine,
                                transportStation = transportStation,
                                transportDirection = transportDirection
                            )
                            onSaveConfig(newConfig)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Aplicar Cambios", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2196F3)
            )
        )
    }
}
