package no.uio.ifi.in2000.team40.havprosjekt.data.oceanforecast

import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.BathingLocations
import no.uio.ifi.in2000.team40.havprosjekt.model.oceanforecast.Location

/**
 * Class for administrating incoming data from DataSource(s).
 * Mainly for providing ocean forecasts in both modified, and un-modified states.
 */
class OceanForecastRepository {
    private val _oceanForecastDataSource = OceanForecastDataSource()
    suspend fun getProperties(lat: Double, lon: Double): Location =
        _oceanForecastDataSource.getProperties(lat, lon)

    /**
     * Suspended method for filtering BathingLocations given by the present forecast temperature.
     *
     * @param bathingLocations The BathingLocations of which to be returned after filtering.
     * @param sliderPositionTemp Range of which temperatures are acceptable.
     *
     * @return The newly filtered BathingLocations.
     */
    suspend fun getBathingLocationsByTemp(bathingLocations: List<BathingLocations.Location>, sliderPositionTemp: ClosedRange<Int>): List<BathingLocations.Location> =
        bathingLocations.filter {
            sliderPositionTemp.contains(
                // antar timeseries[0] er nåtid
                getProperties(it.latitude, it.longitude).properties.timeseries[0].data.instant.details.seaWaterTemperature.toInt()
            )
        }
}

