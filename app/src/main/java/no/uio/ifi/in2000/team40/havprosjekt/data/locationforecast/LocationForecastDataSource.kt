package no.uio.ifi.in2000.team40.havprosjekt.data.locationforecast

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
import no.uio.ifi.in2000.team40.havprosjekt.model.locationforecast.Forecast

/**
 * Class for administrating incoming raw data from API(s).
 * Used for retrieving weather forecast for a specific location.
 */
class LocationForecastDataSource {
    private val client = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }

    /**
     * Suspended method for retrieving weather forecast for a specific location from API.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     *
     * @return The raw data incoming from the API.
     */
    suspend fun getForecast(lat: Double, lon: Double): Forecast {
        val url = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=$lat&lon=$lon"

        var properties: HttpResponse

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


