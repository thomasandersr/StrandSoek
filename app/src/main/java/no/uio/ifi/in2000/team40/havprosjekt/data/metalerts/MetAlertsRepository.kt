package no.uio.ifi.in2000.team40.havprosjekt.data.metalerts

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.team40.havprosjekt.model.metalerts.Feature
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Class for administrating incoming data from DataSource(s).
 * Mainly for providing a usable interface for retrieving data in both filtered and un-filtered states.
 */
class MetAlertsRepository {
    private val _metAlertsDataSource = MetAlertsDataSource()
    private val _countyDataSource = CountyDataSource()

    /**
     * Suspended method for retrieving the county of a location.
     *
     * @param lat Latitude of the location.
     * @param lon Longitude of the location.
     *
     * @return The name of the county of the location.
     */
    suspend fun getCounty(lat: Double, lon: Double): String =
        _countyDataSource.getCounty(lat, lon).fylkesnavn

    /**
     * Retrieves a list of alerts for a given location's county, and only if it's after or during
     * the current day.
     *
     * @param lat Latitude of the location.
     * @param lon Longitude of the location.
     * @param nowDate Current LocalDate.
     *
     * @return List of active alerts for the county of the given location.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getFeature(lat: Double, lon: Double, nowDate: LocalDateTime): List<Feature.Alerts> {
        val countyCode = _countyDataSource.getCounty(lat, lon).fylkesnummer

        // opplevde at appen krasjet når sammenliknet med LocalDateTime direkte, så gjør om til
        // LocalDate ved '.split("T")[0]' og sammenligner kun yyyy-MM-dd istedenfor hele date
        return _metAlertsDataSource.getFeature(countyCode).alerts.filter {
            LocalDate.parse(
                it.whenn.interval[1].toInstant().toString().split("T")[0]) >= LocalDate.parse(nowDate.toString().split("T")[0])
        }
    }
}