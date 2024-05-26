package no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations

/**
 * Data class for which attributes and names to contain for the raw data incoming from an API.
 */
data class BathingLocations(
    val locations: List<Location>
) {
    data class Location(
        val name: String,
        val facilities: List<Facilities>,
        val img: String,

        // koordinater
        val latitude: Double,
        val longitude: Double
    )
}

/**
 * Enum class for which facilities for the above data class is available.
 */
enum class Facilities {
    TOALETT,
    BRYGGE,
    KIOSK,
    BARNEVENNLIG,
    BADEVAKT,
    GRILL,
    TILPASSET
}
