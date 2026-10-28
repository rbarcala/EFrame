package com.antigravity.eframe.data

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

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
    val stations: List<SubteStation>
)

class TransportRepository {

    private val lineaAStations = listOf(
        SubteStation("Plaza de Mayo", hasTransfer = listOf("Línea D", "Línea E")),
        SubteStation("Perú", hasTransfer = listOf("Línea D", "Línea E")),
        SubteStation("Piedras"),
        SubteStation("Lima", hasTransfer = listOf("Línea C")),
        SubteStation("Sáenz Peña"),
        SubteStation("Congreso"),
        SubteStation("Pasco", isClosed = true, closedReason = "Clausurada definitivamente"),
        SubteStation("Alberti", isClosed = true, closedReason = "Clausurada definitivamente"),
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

    private val allLines = listOf(
        SubteLineInfo(
            id = "linea_a",
            name = "Línea A",
            terminalA = "Plaza de Mayo",
            terminalB = "San Pedrito",
            color = Color(0xFF00A2DF),
            status = "Normal",
            frequencyMinutes = 3,
            stations = lineaAStations
        ),
        SubteLineInfo(
            id = "linea_b",
            name = "Línea B",
            terminalA = "L. N. Alem",
            terminalB = "J. M. de Rosas",
            color = Color(0xFFE2211C),
            status = "Normal",
            frequencyMinutes = 3,
            stations = lineaBStations
        ),
        SubteLineInfo(
            id = "linea_c",
            name = "Línea C",
            terminalA = "Retiro",
            terminalB = "Constitución",
            color = Color(0xFF0055A5),
            status = "Normal",
            frequencyMinutes = 4,
            stations = lineaCStations
        ),
        SubteLineInfo(
            id = "linea_d",
            name = "Línea D",
            terminalA = "Catedral",
            terminalB = "Congreso de Tucumán",
            color = Color(0xFF008053),
            status = "Normal",
            frequencyMinutes = 3,
            stations = lineaDStations
        ),
        SubteLineInfo(
            id = "linea_e",
            name = "Línea E",
            terminalA = "Retiro",
            terminalB = "Plaza de los Virreyes",
            color = Color(0xFF6B2D82),
            status = "Demoras de 5 min",
            frequencyMinutes = 6,
            stations = lineaEStations
        ),
        SubteLineInfo(
            id = "linea_h",
            name = "Línea H",
            terminalA = "Facultad de Derecho",
            terminalB = "Hospitales",
            color = Color(0xFFFFD200),
            status = "Normal",
            frequencyMinutes = 4,
            stations = lineaHStations
        )
    )

    fun getLineByName(name: String): SubteLineInfo {
        return allLines.find { it.name.equals(name, ignoreCase = true) }
            ?: allLines.find { it.name.contains(name, ignoreCase = true) }
            ?: allLines[3] // Default Línea D
    }

    fun getAllLines(): List<SubteLineInfo> = allLines

    suspend fun getEstimatedArrival(
        lineName: String,
        stationName: String,
        destinationTerminal: String
    ): Int = withContext(Dispatchers.IO) {
        val line = getLineByName(lineName)
        val stationIndex = line.stations.indexOfFirst { it.name.equals(stationName, ignoreCase = true) }
            .coerceAtLeast(0)

        val seed = (System.currentTimeMillis() / 60000 + stationIndex) % line.frequencyMinutes
        (seed.toInt() + 1).coerceAtLeast(1)
    }

    // Returns a detailed list of upcoming trains towards each direction
    fun getUpcomingTrainsForDirections(
        line: SubteLineInfo,
        waitingStationName: String
    ): Pair<DirectionSchedule, DirectionSchedule> {
        val baseFreq = line.frequencyMinutes
        val currentMin = Calendar.getInstance().get(Calendar.MINUTE)

        val firstA = (currentMin % baseFreq) + 1
        val trainsA = listOf(
            UpcomingTrain(1, firstA, "Aproximándose a la estación"),
            UpcomingTrain(2, firstA + baseFreq, "En trayecto"),
            UpcomingTrain(3, firstA + (baseFreq * 2), "Saliendo de cabecera")
        )

        val firstB = ((currentMin + 2) % baseFreq) + 1
        val trainsB = listOf(
            UpcomingTrain(1, firstB, "Próximo a arribar"),
            UpcomingTrain(2, firstB + baseFreq, "En trayecto"),
            UpcomingTrain(3, firstB + (baseFreq * 2), "En maniobras")
        )

        return Pair(
            DirectionSchedule(line.terminalA, trainsA),
            DirectionSchedule(line.terminalB, trainsB)
        )
    }
}
