package no.uio.ifi.in2000.team40.havprosjekt.data.metalerts

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team40.havprosjekt.model.metalerts.County

/**
 * Class for administrating incoming raw data from API(s).
 * Used for retrieving the specific county for a given location.
 */
class CountyDataSource {
    private val _client = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }

    /**
     * Suspended method for finding the county from the coordinates of a location.
     *
     * @param lat Latitude of the location.
     * @param lon Longitude of the location.
     *
     * @return The appropriate county based on the latitude and longitude.
     */
    suspend fun getCounty(lat: Double, lon: Double): County {
        val url = "https://api.kartverket.no/kommuneinfo/v1/punkt?nord=${lat}&ost=${lon}&koordsys=4258"

        val test: County

        withContext(Dispatchers.IO) {
            test = _client.get(url).body()
            Log.i("county", "$test")
        }
        return test
    }
}