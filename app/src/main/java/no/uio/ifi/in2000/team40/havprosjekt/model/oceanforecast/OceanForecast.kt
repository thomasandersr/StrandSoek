package no.uio.ifi.in2000.team40.havprosjekt.model.oceanforecast

import com.google.gson.annotations.SerializedName

/**
 * Data class for which attributes and names to contain for the raw data incoming from an API.
 */
data class Location(
    val properties: Properties
){
    data class Properties(
        val timeseries: List<TimeSeries>
    ) {
        data class TimeSeries(
            val time: String,
            val data: Data
        ) {
            data class Data(
                val instant: Instant
            ) {
                data class Instant(
                    val details: Details
                ) {
                    data class Details(
                        @SerializedName("sea_surface_wave_height")
                        val seaSurfaceWaveHeight: Double,
                        @SerializedName("sea_water_speed")
                        val seaWaterSpeed: Double,
                        @SerializedName("sea_water_temperature")
                        val seaWaterTemperature: Double
                    )
                }
            }
        }
    }
}







