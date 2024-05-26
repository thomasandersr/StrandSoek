package no.uio.ifi.in2000.team40.havprosjekt.model.metalerts

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Data class for which attributes and names to contain for the raw data incoming from an API.
 */
data class Feature(
    @SerializedName("features")
    val alerts: List<Alerts>
) {
    data class Alerts(
        val properties: Property,
        @SerializedName("when")
        val whenn: When
    ) {
        data class Property(
            val area: String,
            val awarenessType: String,
            val certainty: String,
            val consequences: String,
            val description: String,
            val eventAwarenessName: String,
            val instruction: String,
            val riskMatrixColor: String,
            val severity: String,
            val title: String
        )
        data class When(
            val interval: List<Date>
        )
    }
}