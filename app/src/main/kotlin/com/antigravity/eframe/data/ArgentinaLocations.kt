package com.antigravity.eframe.data

data class GeoLocation(
    val country: String,
    val province: String,
    val cityOrNeighborhood: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = "$cityOrNeighborhood, $province"
}

object ArgentinaLocations {

    val countries = listOf("Argentina")

    val provincesByCountry = mapOf(
        "Argentina" to listOf(
            "CABA (Buenos Aires)",
            "Buenos Aires (GBA / Interior)",
            "Córdoba",
            "Santa Fe",
            "Mendoza",
            "Tucumán",
            "Salta",
            "Entre Ríos",
            "Río Negro",
            "Neuquén",
            "Misiones",
            "San Juan",
            "Corrientes",
            "Chaco",
            "Jujuy",
            "San Luis",
            "Chubut",
            "Tierra del Fuego"
        )
    )

    val locations = listOf(
        // CABA Barrios
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Palermo", -34.5889, -58.4305),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Belgrano", -34.5627, -58.4566),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Recoleta", -34.5895, -58.3974),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Caballito", -34.6186, -58.4439),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Almagro", -34.6097, -58.4214),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Villa Urquiza", -34.5728, -58.4878),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Villa Crespo", -34.5986, -58.4411),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Flores", -34.6288, -58.4633),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "San Telmo", -34.6214, -58.3731),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Puerto Madero", -34.6118, -58.3636),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Núñez", -34.5447, -58.4647),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Colegiales", -34.5752, -58.4485),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Devoto", -34.5997, -58.5133),
        GeoLocation("Argentina", "CABA (Buenos Aires)", "Microcentro / Plaza de Mayo", -34.6083, -58.3712),

        // Buenos Aires GBA e Interior
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "Vicente López / Olivos", -34.5268, -58.4822),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "San Isidro", -34.4717, -58.5286),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "Tigre", -34.4260, -58.5796),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "San Martín", -34.5775, -58.5372),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "Morón / Haedo", -34.6534, -58.6198),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "Quilmes", -34.7242, -58.2527),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "Avellaneda", -34.6611, -58.3653),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "La Plata", -34.9214, -57.9545),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "Mar del Plata", -38.0055, -57.5562),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "Bahía Blanca", -38.7183, -62.2663),
        GeoLocation("Argentina", "Buenos Aires (GBA / Interior)", "Tandil", -37.3217, -59.1332),

        // Córdoba
        GeoLocation("Argentina", "Córdoba", "Córdoba Capital", -31.4201, -64.1888),
        GeoLocation("Argentina", "Córdoba", "Villa Carlos Paz", -31.4241, -64.4978),
        GeoLocation("Argentina", "Córdoba", "Río Cuarto", -33.1307, -64.3499),
        GeoLocation("Argentina", "Córdoba", "Alta Gracia", -31.6529, -64.4283),
        GeoLocation("Argentina", "Córdoba", "Villa General Belgrano", -31.9772, -64.5564),

        // Santa Fe
        GeoLocation("Argentina", "Santa Fe", "Rosario", -32.9468, -60.6393),
        GeoLocation("Argentina", "Santa Fe", "Santa Fe Capital", -31.6333, -60.7000),
        GeoLocation("Argentina", "Santa Fe", "Rafaela", -31.2503, -61.4867),

        // Mendoza
        GeoLocation("Argentina", "Mendoza", "Mendoza Capital", -32.8895, -68.8458),
        GeoLocation("Argentina", "Mendoza", "Godoy Cruz", -32.9255, -68.8417),
        GeoLocation("Argentina", "Mendoza", "San Rafael", -34.6177, -68.3301),
        GeoLocation("Argentina", "Mendoza", "Luján de Cuyo / Chacras", -33.0039, -68.8786),

        // Tucumán
        GeoLocation("Argentina", "Tucumán", "San Miguel de Tucumán", -26.8083, -65.2176),
        GeoLocation("Argentina", "Tucumán", "Yerba Buena", -26.8167, -65.3167),
        GeoLocation("Argentina", "Tucumán", "Tafí del Valle", -26.8531, -65.7097),

        // Salta
        GeoLocation("Argentina", "Salta", "Salta Capital", -24.7821, -65.4232),
        GeoLocation("Argentina", "Salta", "San Lorenzo", -24.7356, -65.4889),
        GeoLocation("Argentina", "Salta", "Cafayate", -26.0731, -65.9761),

        // Entre Ríos
        GeoLocation("Argentina", "Entre Ríos", "Paraná", -31.7333, -60.5333),
        GeoLocation("Argentina", "Entre Ríos", "Concordia", -31.3929, -58.0209),
        GeoLocation("Argentina", "Entre Ríos", "Gualeguaychú", -33.0131, -58.5172),

        // Río Negro & Neuquén
        GeoLocation("Argentina", "Río Negro", "Bariloche", -41.1335, -71.3103),
        GeoLocation("Argentina", "Río Negro", "Las Grutas", -40.8089, -65.0939),
        GeoLocation("Argentina", "Neuquén", "Neuquén Capital", -38.9516, -68.0591),
        GeoLocation("Argentina", "Neuquén", "San Martín de los Andes", -40.1578, -71.3533),
        GeoLocation("Argentina", "Neuquén", "Villa La Angostura", -40.7631, -71.6444),

        // Misiones
        GeoLocation("Argentina", "Misiones", "Posadas", -27.3671, -55.8961),
        GeoLocation("Argentina", "Misiones", "Puerto Iguazú", -25.5991, -54.5735),

        // Otras
        GeoLocation("Argentina", "Jujuy", "San Salvador de Jujuy", -24.1858, -65.2995),
        GeoLocation("Argentina", "Corrientes", "Corrientes Capital", -27.4692, -58.8306),
        GeoLocation("Argentina", "Chaco", "Resistencia", -27.4514, -58.9867),
        GeoLocation("Argentina", "San Juan", "San Juan Capital", -31.5375, -68.5364),
        GeoLocation("Argentina", "San Luis", "San Luis Capital", -33.3017, -66.3378),
        GeoLocation("Argentina", "Chubut", "Puerto Madryn", -42.7692, -65.0385),
        GeoLocation("Argentina", "Tierra del Fuego", "Ushuaia", -54.8019, -68.3030)
    )

    fun getCities(country: String, province: String): List<GeoLocation> {
        return locations.filter { it.country == country && it.province == province }
    }

    val defaultLocation = locations.first() // Palermo, CABA
}

