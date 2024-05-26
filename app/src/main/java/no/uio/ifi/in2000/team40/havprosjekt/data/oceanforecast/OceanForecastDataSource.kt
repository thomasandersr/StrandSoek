package no.uio.ifi.in2000.team40.havprosjekt.data.oceanforecast

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team40.havprosjekt.model.oceanforecast.Location

/**
 * Class for administrating incoming raw data from API(s).
 * Used for retrieving ocean forecast for a specific location.
 */
class OceanForecastDataSource {
    private val client = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }

    /**
     * Suspended method for retrieving the ocean forecast for a given location.
     *
     * @param lat Latitude of the location.
     * @param lon Longitude of the location.
     *
     * @return The raw data incoming from the API.
     */
    suspend fun getProperties(lat: Double, lon: Double): Location {
        val url = "https://api.met.no/weatherapi/oceanforecast/2.0/complete?lat=$lat&lon=$lon"

        val properties : HttpResponse

        withContext(Dispatchers.IO) {
            properties = client.get(url) {
                headers {
                    append(HttpHeaders.Authorization, "5dbba469-a8c7-4985-bfd3-4949137ead38")
                }
            }
        }
        return properties.body()
    }
}