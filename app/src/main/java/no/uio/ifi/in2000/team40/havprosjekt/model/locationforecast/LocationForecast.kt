package no.uio.ifi.in2000.team40.havprosjekt.model.locationforecast

import com.google.gson.annotations.SerializedName

/**
 * Data class for which attributes and names to contain for the raw data incoming from an API.
 */
data class Forecast(
    val properties: Properties
) {
    data class Properties(
        val timeseries: List<TimeSeries>
    ) {
        data class TimeSeries(
            val time: String,
            val data: Data
        ) {
            data class Data(
                val instant: Instant,
                @SerializedName("next_1_hours")
                val next1Hours: Next1Hours?
            ) {
                data class Instant(
                    val details: Details
                ) {
                    data class Details(
                        @SerializedName("air_temperature")
                        val airTemperature: Double,
                        @SerializedName("wind_speed")
                        val windSpeed: Double
                    )
                }

                data class Next1Hours(
                    val summary: Summary,
                    val details: Details?
                ) {
                    data class Summary(
                        @SerializedName("symbol_code")
                        val symbolCode: String
                    )

                    data class Details(
                        @SerializedName("precipitation_amount")
                        val precipitationAmount: Double?
                    )
                }
            }
        }
    }
}
