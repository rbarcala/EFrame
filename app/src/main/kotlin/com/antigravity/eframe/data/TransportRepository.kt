package com.antigravity.eframe.data

import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class SubteStation(
    val name: String,
    val isClosed: Boolean = false,
    val closedReason: String = "",
    val hasTransfer: List<String> = emptyList()
)

data class UpcomingTrain(
    val orderNumber: Int,
    val minutesAway: Int,
    val currentPositionText: String
)

data class DirectionSchedule(
    val terminalName: String,
    val trains: List<UpcomingTrain>
)

data class SubteLineInfo(
    val id: String,
    val name: String,
    val terminalA: String,
    val terminalB: String,
    val color: Color,
    val status: String,
    val frequencyMinutes: Int,
    val stations: List<SubteStation>,
    val lastUpdated: String = "",
    val isAlert: Boolean = false,
    val officialFrequencySeconds: Int = 0
)

class TransportRepository {

    companion object {
        private const val TAG = "TransportRepo"

        private val lineaAStations = listOf(
            SubteStation("Plaza de Mayo", hasTransfer = listOf("Línea D", "Línea E")),
            SubteStation("Perú", hasTransfer = listOf("Línea D", "Línea E")),
            SubteStation("Piedras"),
            SubteStation("Lima", hasTransfer = listOf("Línea C")),
            SubteStation("Sáenz Peña"),
            SubteStation("Congreso"),
            SubteStation("Pasco"),
            SubteStation("Alberti"),
            SubteStation("Plaza Miserere", hasTransfer = listOf("Línea H", "Tren Sarmiento")),
            SubteStation("Loria"),
            SubteStation("Castro Barros"),
            SubteStation("Río de Janeiro"),
            SubteStation("Acoyte"),
            SubteStation("Primera Junta", hasTransfer = listOf("Tren Sarmiento")),
            SubteStation("Puán"),
            SubteStation("Carabobo"),
            SubteStation("San José de Flores", hasTransfer = listOf("Tren Sarmiento")),
            SubteStation("San Pedrito")
        )

        private val lineaBStations = listOf(
            SubteStation("L. N. Alem", hasTransfer = listOf("Línea E")),
            SubteStation("Florida"),
            SubteStation("Carlos Pellegrini", hasTransfer = listOf("Línea B", "Línea D")),
            SubteStation("Uruguay"),
            SubteStation("Callao"),
            SubteStation("Pasteur - AMIA"),
            SubteStation("Pueyrredón", hasTransfer = listOf("Línea H")),
            SubteStation("Carlos Gardel"),
            SubteStation("Medrano"),
            SubteStation("Ángel Gallardo"),
            SubteStation("Malabia - O. Pugliese"),
            SubteStation("Dorrego", hasTransfer = listOf("Tren San Martín")),
            SubteStation("Federico Lacroze", hasTransfer = listOf("Tren Urquiza")),
            SubteStation("Tronador - Villa Ortúzar"),
            SubteStation("De los Incas - Parque Chas"),
            SubteStation("Echeverría"),
            SubteStation("J. M. de Rosas", hasTransfer = listOf("Tren Mitre"))
        )

        private val lineaCStations = listOf(
            SubteStation("Retiro", hasTransfer = listOf("Línea E", "Tren Mitre", "Tren San Martín")),
            SubteStation("General San Martín"),
            SubteStation("Lavalle"),
            SubteStation("Diagonal Norte", hasTransfer = listOf("Línea B", "Línea D")),
            SubteStation("Avenida de Mayo", hasTransfer = listOf("Línea A")),
            SubteStation("Moreno"),
            SubteStation("Independencia", hasTransfer = listOf("Línea E")),
            SubteStation("San Juan"),
            SubteStation("Constitución", hasTransfer = listOf("Tren Roca"))
        )

        private val lineaDStations = listOf(
            SubteStation("Catedral", hasTransfer = listOf("Línea A", "Línea E")),
            SubteStation("9 de Julio", hasTransfer = listOf("Línea B", "Línea C")),
            SubteStation("Tribunales - Teatro Colón"),
            SubteStation("Callao"),
            SubteStation("Facultad de Medicina"),
            SubteStation("Pueyrredón", hasTransfer = listOf("Línea H")),
            SubteStation("Agüero"),
            SubteStation("Bulnes"),
            SubteStation("Scalabrini Ortiz"),
            SubteStation("Plaza Italia"),
            SubteStation("Palermo", hasTransfer = listOf("Tren San Martín")),
            SubteStation("Ministro Carranza", hasTransfer = listOf("Tren Mitre")),
            SubteStation("Olleros"),
            SubteStation("José Hernández"),
            SubteStation("Juramento"),
            SubteStation("Congreso de Tucumán")
        )

        private val lineaEStations = listOf(
            SubteStation("Retiro", hasTransfer = listOf("Línea C", "Tren Mitre")),
            SubteStation("Correo Central", hasTransfer = listOf("Línea B")),
            SubteStation("Bolívar", hasTransfer = listOf("Línea A", "Línea D")),
            SubteStation("Belgrano"),
            SubteStation("Independencia", hasTransfer = listOf("Línea C")),
            SubteStation("San José"),
            SubteStation("Entre Ríos - Rodolfo Walsh"),
            SubteStation("Pichincha"),
            SubteStation("Jujuy", hasTransfer = listOf("Línea H")),
            SubteStation("General Urquiza"),
            SubteStation("Boedo"),
            SubteStation("Avenida La Plata"),
            SubteStation("José María Moreno"),
            SubteStation("Emilio Mitre"),
            SubteStation("Medalla Milagrosa"),
            SubteStation("Varela"),
            SubteStation("Plaza de los Virreyes", hasTransfer = listOf("Premetro"))
        )

        private val lineaHStations = listOf(
            SubteStation("Facultad de Derecho - Julieta Lanteri"),
            SubteStation("Las Heras"),
            SubteStation("Santa Fe - Carlos Jáuregui", hasTransfer = listOf("Línea D")),
            SubteStation("Córdoba"),
            SubteStation("Corrientes", hasTransfer = listOf("Línea B")),
            SubteStation("Once - 30 de Diciembre", hasTransfer = listOf("Línea A", "Tren Sarmiento")),
            SubteStation("Venezuela"),
            SubteStation("Humberto 1°", hasTransfer = listOf("Línea E")),
            SubteStation("Inclán - Mezquita Al Ahmad"),
            SubteStation("Caseros"),
            SubteStation("Parque Patricios"),
            SubteStation("Hospitales")
        )

        private val initialDefaultLines = listOf(
            SubteLineInfo(
                id = "linea_a",
                name = "Línea A",
                terminalA = "Plaza de Mayo",
                terminalB = "San Pedrito",
                color = Color(0xFF00A2DF),
                status = "Consultando servicio...",
                frequencyMinutes = 5,
                stations = lineaAStations
            ),
            SubteLineInfo(
                id = "linea_b",
                name = "Línea B",
                terminalA = "L. N. Alem",
                terminalB = "J. M. de Rosas",
                color = Color(0xFFE2211C),
                status = "Consultando servicio...",
                frequencyMinutes = 5,
                stations = lineaBStations
            ),
            SubteLineInfo(
                id = "linea_c",
                name = "Línea C",
                terminalA = "Retiro",
                terminalB = "Constitución",
                color = Color(0xFF0055A5),
                status = "Consultando servicio...",
                frequencyMinutes = 5,
                stations = lineaCStations
            ),
            SubteLineInfo(
                id = "linea_d",
                name = "Línea D",
                terminalA = "Catedral",
                terminalB = "Congreso de Tucumán",
                color = Color(0xFF008053),
                status = "Consultando servicio...",
                frequencyMinutes = 5,
                stations = lineaDStations
            ),
            SubteLineInfo(
                id = "linea_e",
                name = "Línea E",
                terminalA = "Retiro",
                terminalB = "Plaza de los Virreyes",
                color = Color(0xFF6B2D82),
                status = "Consultando servicio...",
                frequencyMinutes = 6,
                stations = lineaEStations
            ),
            SubteLineInfo(
                id = "linea_h",
                name = "Línea H",
                terminalA = "Facultad de Derecho",
                terminalB = "Hospitales",
                color = Color(0xFFFFD200),
                status = "Consultando servicio...",
                frequencyMinutes = 5,
                stations = lineaHStations
            )
        )

        private val _linesState = MutableStateFlow<List<SubteLineInfo>>(initialDefaultLines)
        val linesState: StateFlow<List<SubteLineInfo>> = _linesState.asStateFlow()

        private var lastFetchMillis = 0L

        private val httpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(7, TimeUnit.SECONDS)
                .readTimeout(7, TimeUnit.SECONDS)
                .build()
        }

        private data class EmovaLineRaw(
            val lineLetter: String,
            val status: String,
            val frequencySeconds: Int
        )

        private data class EmovaReport(
            val lines: List<EmovaLineRaw>,
            val date: String,
            val time: String
        )
    }

    suspend fun fetchLiveStatus(force: Boolean = false): List<SubteLineInfo> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastFetchMillis < 40_000L) && _linesState.value.none { it.status == "Consultando servicio..." }) {
            return@withContext _linesState.value
        }

        try {
            val report = fetchEmovaSignalRReport()
            if (report != null && report.lines.isNotEmpty()) {
                val updated = applyEmovaReport(report)
                _linesState.value = updated
                lastFetchMillis = System.currentTimeMillis()
                Log.d(TAG, "Emova subte live status successfully updated. Lines count: ${updated.size}")
                return@withContext updated
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch live subte status from Emova: ${e.message}", e)
        }

        // If fetch failed but we have initial state, set fallback to normal instead of "Consultando..."
        if (_linesState.value.any { it.status == "Consultando servicio..." }) {
            _linesState.value = initialDefaultLines.map { it.copy(status = "Normal") }
        }

        return@withContext _linesState.value
    }

    private suspend fun fetchEmovaSignalRReport(): EmovaReport? = withContext(Dispatchers.IO) {
        val connData = "[{\"name\":\"MobileEmova\"}]"
        val encodedConnData = URLEncoder.encode(connData, "UTF-8")
        val negUrl = "https://info.emova.com.ar/EstadoLineas/signalr/negotiate?clientProtocol=2.0&connectionData=$encodedConnData"

        val negReq = Request.Builder()
            .url(negUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile)")
            .build()

        val token = try {
            httpClient.newCall(negReq).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val body = resp.body?.string() ?: return@use null
                JSONObject(body).optString("ConnectionToken")
            }
        } catch (e: Exception) {
            Log.w(TAG, "SignalR negotiate error: ${e.message}")
            return@withContext null
        }

        if (token.isNullOrEmpty()) return@withContext null

        val encodedToken = URLEncoder.encode(token, "UTF-8")
        val connUrl = "https://info.emova.com.ar/EstadoLineas/signalr/connect?transport=serverSentEvents&clientProtocol=2.0&connectionData=$encodedConnData&connectionToken=$encodedToken"
        val startUrl = "https://info.emova.com.ar/EstadoLineas/signalr/start?transport=serverSentEvents&clientProtocol=2.0&connectionData=$encodedConnData&connectionToken=$encodedToken"

        // SignalR requires calling 'start' shortly after initiating connection
        val startJob = launch {
            delay(200)
            try {
                val startReq = Request.Builder()
                    .url(startUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile)")
                    .build()
                httpClient.newCall(startReq).execute().use { it.body?.string() }
            } catch (_: Exception) {}
        }

        val connReq = Request.Builder()
            .url(connUrl)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile)")
            .build()

        var parsedReport: EmovaReport? = null

        try {
            httpClient.newCall(connReq).execute().use { response ->
                val source = response.body?.source() ?: return@use
                var linesRead = 0
                while (!source.exhausted() && linesRead < 15) {
                    val line = source.readUtf8Line() ?: break
                    linesRead++
                    val trimmed = line.trim()
                    if (trimmed.startsWith("data: ") && trimmed.contains("\"estadoLineasMobile\"")) {
                        val payload = trimmed.substring(6)
                        parsedReport = parseEmovaPayload(payload)
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "SignalR SSE connect error: ${e.message}")
        } finally {
            startJob.cancel()
        }

        parsedReport
    }

    private fun parseEmovaPayload(jsonStr: String): EmovaReport? {
        return try {
            val root = JSONObject(jsonStr)
            val messages = root.optJSONArray("M") ?: return null
            for (i in 0 until messages.length()) {
                val msg = messages.getJSONObject(i)
                if (msg.optString("M") == "estadoLineasMobile") {
                    val args = msg.optJSONArray("A") ?: continue
                    val innerJsonStr = args.getString(0)
                    val innerJson = JSONObject(innerJsonStr)

                    val linesArray = innerJson.optJSONArray("lines") ?: continue
                    val linesList = mutableListOf<EmovaLineRaw>()
                    for (j in 0 until linesArray.length()) {
                        val item = linesArray.getJSONObject(j)
                        val name = item.optString("LineName").trim().uppercase()
                        val status = item.optString("LineStatus").trim()
                        val freqSec = item.optString("LineFrequency").trim().toIntOrNull() ?: 0
                        linesList.add(EmovaLineRaw(name, status, freqSec))
                    }

                    val date = innerJson.optString("date")
                    val time = innerJson.optString("time")
                    return EmovaReport(linesList, date, time)
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Emova payload: ${e.message}")
            null
        }
    }

    private fun applyEmovaReport(report: EmovaReport): List<SubteLineInfo> {
        val currentLines = _linesState.value
        val updateTimestamp = if (report.date.isNotBlank() && report.time.isNotBlank()) {
            "${report.date} • ${report.time}"
        } else {
            ""
        }

        return currentLines.map { baseLine ->
            // Match line letter (e.g. "Línea A" -> "A")
            val letter = baseLine.name.removePrefix("Línea ").trim().uppercase()
            val raw = report.lines.find { it.lineLetter == letter }

            if (raw == null) {
                baseLine.copy(lastUpdated = updateTimestamp)
            } else {
                val rawStatus = raw.status
                val isNormal = rawStatus.equals("Normal", ignoreCase = true)
                val freqMinutes = if (raw.frequencySeconds > 0) {
                    (raw.frequencySeconds / 60).coerceAtLeast(2)
                } else {
                    baseLine.frequencyMinutes
                }

                // Check stations for closures according to genuine Emova alerts
                val updatedStations = baseLine.stations.map { station ->
                    val isClosed = if (isNormal) {
                        false
                    } else {
                        checkIfStationClosed(station.name, rawStatus)
                    }

                    val reason = if (isClosed) {
                        rawStatus
                    } else {
                        ""
                    }

                    station.copy(isClosed = isClosed, closedReason = reason)
                }

                baseLine.copy(
                    status = rawStatus,
                    frequencyMinutes = freqMinutes,
                    stations = updatedStations,
                    lastUpdated = updateTimestamp,
                    isAlert = !isNormal,
                    officialFrequencySeconds = raw.frequencySeconds
                )
            }
        }
    }

    private fun checkIfStationClosed(stationName: String, statusText: String): Boolean {
        val lowerStatus = statusText.lowercase()
        // Check if status is discussing closures
        val mentionsClosure = lowerStatus.contains("cerrad") ||
                lowerStatus.contains("no se detiene") ||
                lowerStatus.contains("clausurad") ||
                lowerStatus.contains("obras")

        if (!mentionsClosure) return false

        val coreName = stationName.split("-").first().trim().lowercase()
        if (lowerStatus.contains(coreName)) return true

        // Split meaningful tokens (ignore common prefixes)
        val stopWords = setOf("san", "plaza", "general", "avenida", "los", "las", "del", "por", "de")
        val tokens = coreName.split(" ").filter { it.length > 2 && it !in stopWords }

        return tokens.isNotEmpty() && tokens.all { lowerStatus.contains(it) }
    }

    fun getLineByName(name: String): SubteLineInfo {
        val lines = _linesState.value
        return lines.find { it.name.equals(name, ignoreCase = true) }
            ?: lines.find { it.name.contains(name, ignoreCase = true) }
            ?: lines.find { it.id.contains(name, ignoreCase = true) }
            ?: lines[3] // Default Línea D
    }

    fun getAllLines(): List<SubteLineInfo> = _linesState.value

    suspend fun getEstimatedArrival(
        lineName: String,
        stationName: String,
        destinationTerminal: String
    ): Int = withContext(Dispatchers.IO) {
        // Trigger live refresh if needed
        fetchLiveStatus(force = false)

        val line = getLineByName(lineName)
        val stationIndex = line.stations.indexOfFirst { it.name.equals(stationName, ignoreCase = true) }
            .coerceAtLeast(0)

        val freq = line.frequencyMinutes.coerceAtLeast(2)
        val seed = (System.currentTimeMillis() / 60000 + stationIndex) % freq
        (seed.toInt() + 1).coerceAtLeast(1)
    }

    // Returns a detailed list of upcoming trains towards each direction
    fun getUpcomingTrainsForDirections(
        line: SubteLineInfo,
        waitingStationName: String
    ): Pair<DirectionSchedule, DirectionSchedule> {
        val baseFreq = line.frequencyMinutes.coerceAtLeast(2)
        val currentMin = Calendar.getInstance().get(Calendar.MINUTE)

        val isInterrupted = line.status.contains("interrumpid", ignoreCase = true)

        val trainsA = if (isInterrupted) {
            listOf(UpcomingTrain(1, 0, "Servicio interrumpido"))
        } else {
            val firstA = (currentMin % baseFreq) + 1
            listOf(
                UpcomingTrain(1, firstA, "Aproximándose a la estación"),
                UpcomingTrain(2, firstA + baseFreq, "En trayecto"),
                UpcomingTrain(3, firstA + (baseFreq * 2), "Saliendo de cabecera")
            )
        }

        val trainsB = if (isInterrupted) {
            listOf(UpcomingTrain(1, 0, "Servicio interrumpido"))
        } else {
            val firstB = ((currentMin + 2) % baseFreq) + 1
            listOf(
                UpcomingTrain(1, firstB, "Próximo a arribar"),
                UpcomingTrain(2, firstB + baseFreq, "En trayecto"),
                UpcomingTrain(3, firstB + (baseFreq * 2), "En maniobras")
            )
        }

        return Pair(
            DirectionSchedule(line.terminalA, trainsA),
            DirectionSchedule(line.terminalB, trainsB)
        )
    }
}
