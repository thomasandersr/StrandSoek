package no.uio.ifi.in2000.team40.havprosjekt.data.locationforecast

import no.uio.ifi.in2000.team40.havprosjekt.model.locationforecast.Forecast

/**
 * Class for administrating incoming data from DataSource(s).
 * Mainly for providing Weather Forecasts from specific coordinates.
 */
class LocationForecastRepository {
    private val _locationForecastDataSource = LocationForecastDataSource()

    /**
     * Suspended method for retrieving a forecast for a specific location.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     *
     * @return A weather forecast property for the specified location.
     */
    suspend fun getForecast(lat: Double, lon: Double): Forecast =
        _locationForecastDataSource.getForecast(lat, lon)
}