package no.uio.ifi.in2000.team40.havprosjekt.ui.bathinglocations

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team40.havprosjekt.data.bathinglocations.BathingLocationsRepository
import no.uio.ifi.in2000.team40.havprosjekt.data.locationforecast.LocationForecastRepository
import no.uio.ifi.in2000.team40.havprosjekt.data.metalerts.MetAlertsRepository
import no.uio.ifi.in2000.team40.havprosjekt.data.oceanforecast.OceanForecastRepository
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.BathingLocations.Location
import no.uio.ifi.in2000.team40.havprosjekt.model.locationforecast.Forecast
import no.uio.ifi.in2000.team40.havprosjekt.model.metalerts.Feature
import no.uio.ifi.in2000.team40.havprosjekt.model.oceanforecast.Location.Properties
import java.time.LocalDateTime

/**
 * Data class for the different uiStates used by LocationScreen for
 * managing and preserving the different values
 */
data class UiState(
    var state: Boolean = false,
    val oceanProperties: Properties? = null,
    val bathingLocations: Location? = null,
    val locationForecast: Forecast? = null,
    val alerts: List<Feature.Alerts> = emptyList()
)

/**
 * ViewModel for managing dataflow between LocationScreen and repositories.
 * Mainly for retrieving and storing the different data from each API to the appropriate uiState.
 */
class LocationScreenViewModel: ViewModel() {
    private val _bathingLocationsRepository: BathingLocationsRepository = BathingLocationsRepository()
    private val _oceanForecastRepository: OceanForecastRepository = OceanForecastRepository()
    private val _locationForecastRepository: LocationForecastRepository = LocationForecastRepository()
    private val _alertRepository: MetAlertsRepository = MetAlertsRepository()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var initializeCalledAlerts = false
    private var initializeCalledForecast = false
    private var initializeCalledLocation = false
    private var initializeCalledOcean = false

    /**
     * Updates a state indicating whether the app currently has connection to internet or not.
     *
     * @param state Boolean value of the current connection state.
     */
    fun updateState(state: Boolean) { _uiState.value.state = state }

    /**
     * Updates the location to the first one that contains a given name. 'null' in case there is no match.
     *
     * @param name The name of the location (BathingLocation).
     */
    fun requestLocation(name: String) {
        if (initializeCalledLocation) return
        initializeCalledLocation = true

        _uiState.update {
            it.copy(
                bathingLocations = _bathingLocationsRepository.getLocationByName(name)
            )
        }
    }

    /**
     * Retrieves alerts from MetAlertsRepository with the given latitude, longitude and minimum date,
     * and saves it to the appropriate uiState.
     *
     * @param lat The latitude of a given location, relevant for showing the correct county.
     * @param lon The longitude of a given location, relevant for showing the correct county.
     * @param nowDate The minimum date required so that we can exclude expired alerts.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun requestAlerts(lat: Double, lon: Double, nowDate: LocalDateTime) {
        if (!_uiState.value.state) return

        if (initializeCalledAlerts) return
        initializeCalledAlerts = true

        viewModelScope.launch {
            _uiState.update {
                it.copy(alerts = _alertRepository.getFeature(lat, lon, nowDate))
            }
        }
    }

    /**
     * Retrieves Ocean Forecast from OceanForecastRepository with the given latitude and longitude,
     * and saves it to the appropriate uiState.
     *
     * @param latitude The latitude of a given location.
     * @param longitude The longitude of a given location.
     */
    fun requestOceanProperties(latitude: Double, longitude: Double) {
        if (!_uiState.value.state) return

        // begrenser API-kall til OceanForecast
        if (initializeCalledOcean) return
        initializeCalledOcean = true

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    oceanProperties = _oceanForecastRepository.getProperties(latitude, longitude).properties
                )
            }
        }
    }

    /**
     * Retrieves Weather Forecast from LocationForecastRepository with the given latitude and longitude,
     * and saves it to the appropriate uiState.
     *
     * @param latitude The latitude of a given location.
     * @param longitude The longitude of a given location.
     */
    fun requestForecastProperties(latitude: Double, longitude: Double) {
        if (!_uiState.value.state) return

        if (initializeCalledForecast) return
        initializeCalledForecast = true

        viewModelScope.launch {
            _uiState.update { it.copy(locationForecast = _locationForecastRepository.getForecast(latitude, longitude)) }
        }
    }
}