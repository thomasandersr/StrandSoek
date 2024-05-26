package no.uio.ifi.in2000.team40.havprosjekt.data.metalerts

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team40.havprosjekt.model.metalerts.Feature

/**
 * Class for administrating incoming raw data from API(s).
 * Used for retrieving the alerts for a given county.
 */
class MetAlertsDataSource {
    private val _client = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }

    /**
     * Suspended method for retrieving the alerts for a given county.
     *
     * @param countyCode The unique code of the county.
     *
     * @return The raw data incoming from the API.
     */
    suspend fun getFeature(countyCode: String): Feature {
        val url = "https://api.met.no/weatherapi/metalerts/2.0/all.json?county=${countyCode}"

        val features: Feature

        withContext(Dispatchers.IO) {
            features = _client.get(url).body()
        }
        return features
    }
}