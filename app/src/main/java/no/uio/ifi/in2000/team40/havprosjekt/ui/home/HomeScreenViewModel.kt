package no.uio.ifi.in2000.team40.havprosjekt.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team40.havprosjekt.data.bathinglocations.BathingLocationsRepository
import no.uio.ifi.in2000.team40.havprosjekt.data.locationforecast.LocationForecastRepository
import no.uio.ifi.in2000.team40.havprosjekt.data.oceanforecast.OceanForecastRepository
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.BathingLocations
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.Facilities
import no.uio.ifi.in2000.team40.havprosjekt.model.locationforecast.Forecast
import no.uio.ifi.in2000.team40.havprosjekt.model.metalerts.Feature.Alerts
import no.uio.ifi.in2000.team40.havprosjekt.model.oceanforecast.Location
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * ViewModel for managing dataflow between HomeScreen and repositories.
 * Mainly for displaying each BathingLocationsCard correctly, with up-to-date information from different API-services.
 * And methods and variables for filtering this data.
 */
@RequiresApi(Build.VERSION_CODES.O)
class HomeScreenViewModel: ViewModel() {
    private val _bathingLocationRepository: BathingLocationsRepository = BathingLocationsRepository()
    private val _locationForecastRepository: LocationForecastRepository = LocationForecastRepository()
    private val _oceanForecastRepository: OceanForecastRepository = OceanForecastRepository()

    private val _oceanForecastsMap = MutableStateFlow<Map<String, Location>>(emptyMap())
    val oceanForecastsMap = _oceanForecastsMap.asStateFlow()

    private val _airTemperatureMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val airTemperatureMap = _airTemperatureMap.asStateFlow()



    @RequiresApi(Build.VERSION_CODES.O)
    private val _chosenHour = MutableStateFlow(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH")).toInt())
    @RequiresApi(Build.VERSION_CODES.O)


    private val _uiState = MutableStateFlow(HomeScreenUiState())
    @RequiresApi(Build.VERSION_CODES.O)
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isLoadingFilters = MutableStateFlow(false)
    val isLoadingFilters = _isLoadingFilters.asStateFlow()

    private var initializeCalledOcean = false
    private var initializeCalledForecast = false

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    /**
     * Updating SearchBar with new value.
     *
     * @param text The given text which is typed in the SearchBar.
     */
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    /**
     * Updates a state indicating whether the app currently has connection to internet or not.
     *
     * @param state Boolean value of the current connection state.
     */
    fun updateState(state: Boolean) { _uiState.value.state = state }

    /**
     * Returns a list of BathingLocations which only contains letters from searchText.
     */
    fun getSearchedLocs(): List<BathingLocations.Location> {
        return uiState.value.bathingLocationsChangeable.filter {
            it.name.contains(_searchText.value, ignoreCase = true)
        }
    }

    /**
     * Returns the number of BathingLocations which only contains letters from searchText.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun sizedSearch(): Int =
        uiState.value.bathingLocationsChangeable.filter {
            it.name.contains(_searchText.value, ignoreCase = true)
        }.size

    /**
     * Fetches the users current location and saves it to the uiState should it succeed.
     *
     * @param context The given context to check whether it has permission to retrieve or not.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserLocation(context: Context) {
        if (uiState.value.coords != null) return

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000; fastestInterval = 5000
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {

                    _uiState.update { it.copy(coords =
                        mapOf("lat" to location.latitude, "lon" to location.longitude))
                    }

                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    /**
     * Updates the list of suitable BathingLocations based on different filters applied by the user.
     *
     * @param chosenDist The max distance to determine which BathingLocations are to be included.
     * @param facilities The facilities each BathingLocation must contain.
     * @param sliderPositionTemp The range of temperature in the ocean each BathingLocation must fall within.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLocationsByAllFilters(chosenDist: Float, facilities: List<Facilities>, sliderPositionTemp: ClosedRange<Int>) {
        // if (_uiState.value.state == false) return

        _isLoadingFilters.value = true
        fun intersectIgnoreNull(first: List<BathingLocations.Location>?, last: List<BathingLocations.Location>?): List<BathingLocations.Location> =
            when {
                first==null && last==null -> emptyList()
                first==null -> last!!; last==null -> first
                else -> first.intersect(last).toList()
            }

        Log.i("usercoords", "${_uiState.value.coords}")

        viewModelScope.launch {
            val bathingRepo = BathingLocationsRepository()
            val oceanRepo = OceanForecastRepository()

            val locationsByFacilities = bathingRepo.getBathinglocationsByFacilities(facilities)
            val locationsByDistance = bathingRepo.getBathingLocationsByDistance(chosenDist, uiState.value.coords)
            //val locationsByTemperature = oceanRepo.getBathingLocationsByTemp(bathingRepo.getBathingLocations().locations, sliderPositionTemp)
            var locationsByTemperature: List<BathingLocations.Location>? = null
            if (_uiState.value.state) locationsByTemperature = oceanRepo.getBathingLocationsByTemp(bathingRepo.getBathingLocations().locations, sliderPositionTemp)

            _uiState.update {
                it.copy(
                    bathingLocationsChangeable = intersectIgnoreNull(intersectIgnoreNull(locationsByFacilities, locationsByDistance), locationsByTemperature)
                )
            }
            _isLoadingFilters.value = false
        }
    }
                
    private val _locationForecastsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val locationForecastsMap = _locationForecastsMap.asStateFlow()

    /**
     * Fetches the Ocean Forecast for a given location
     *
     * @param locationName The name of the location
     * @param lat The latitude of the location
     * @param lon The longitude of the location
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchOceanForecastForLocation(locationName: String, lat: Double, lon: Double) {
        if (_uiState.value.state == false) return

        initializeCalledOcean = true

        viewModelScope.launch {
            try {
                val forecast = _oceanForecastRepository.getProperties(lat, lon)
                _oceanForecastsMap.update { currentMap ->
                    currentMap.plus(locationName to forecast)
                }
            } catch (_: Exception) { }
        }
    }

    /**
     * Fetches the Weather Forecast for a given location
     *
     * @param locationName The name of the location
     * @param lat The latitude of the location
     * @param lon The longitude of the location
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchForecastForLocation(locationName: String, lat: Double, lon: Double) {
        if (_uiState.value.state == false) return

        initializeCalledForecast = true

        viewModelScope.launch {
            try {
                val forecast = _locationForecastRepository.getForecast(lat, lon)
                val (airTemperature, windSpeed, symbolCode, precipitationAmount) =
                    filterForecastDetails(forecast, _chosenHour.value)

                _locationForecastsMap.update { currentMap ->
                    currentMap.plus(locationName to symbolCode)
                }

                _airTemperatureMap.update { currentMap ->
                    currentMap.plus(locationName to airTemperature)
                }
            } catch (_: Exception) { }
        }
    }

    /**
     * Waits for an internet connection to be available, then initializes data asynchronously.
     */
    init {
        viewModelScope.launch {
            while (_uiState.value.state == false) {
                delay(100)
            }
            _uiState.update {
                it.copy(
                    // alerts = _alertRepository.getFeature().alerts,
                    bathingLocationsFixed = _bathingLocationRepository.getBathingLocations().locations,
                    bathingLocationsChangeable = _bathingLocationRepository.getBathingLocations().locations,
                    locationForecasts = _locationForecastRepository.getForecast(59.9404992,10.7189548) // koordinater plassert på UiO
                )
            }
            _bathingLocationRepository.getBathingLocations().locations.map { location ->
                fetchOceanForecastForLocation(
                    location.name,
                    location.latitude,
                    location.longitude
                )
            }
        }
    }

    /**
     * Allows the filter in HomeScreen to be opened.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun allowFilter() = _uiState.update { it.copy(filterRemainsHidden = false) }

    /**
     * Prevents the filter in HomeScreen from being opened.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun preventFilter() = _uiState.update { it.copy(filterRemainsHidden = true) }

    /**
     * Data class for the different uiStates used by HomeScreen and FilterScreen for
     * managing and preserving the different values
     */
    data class HomeScreenUiState(
        var state: Boolean = false,

        var bathingLocationsFixed: List<BathingLocations.Location> = emptyList(),
        var bathingLocationsChangeable: List<BathingLocations.Location> = emptyList(),
        val alerts: List<Alerts> = emptyList(),
        val locationForecasts: Forecast? = null,
        val coords: Map<String, Double>? = null,

        var filterRemainsHidden: Boolean = true,

        val cardLoaded: MutableMap<Int, Boolean> = mutableMapOf(),

        // filter
        var sliderPosition: MutableFloatState = mutableFloatStateOf(15f),
        var positionCheckedState: Boolean = false,

        var facilityFilterNames: List<String> = listOf(),

        var sliderPositionTemp: ClosedFloatingPointRange<Float> = 0f .. 30f
    )

    /**
     * Filters the Weather Forecast for a specific hour of choice
     *
     * @param locationForecast The data itself, from which the filtering may be used upon.
     * @param chosenHour The specific hour to be used for the filtering.
     *
     * @return The new filtered data.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterForecastDetails(locationForecast: Forecast?, chosenHour: Int): List<String> {
        val data = mutableListOf("-", "-", "-", "-")

        locationForecast?.properties?.timeseries?.forEach { timeseries ->
            val forecastDay = timeseries.time.split("T")[0]
            val forecastHour = timeseries.time.split("T")[1].split(":")[0].toInt()
            val currentDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            if (forecastDay == currentDay && chosenHour == forecastHour) {
                data[0] = timeseries.data.instant.details.airTemperature.toString()
                data[1] = timeseries.data.instant.details.windSpeed.toString()
                data[2] = timeseries.data.next1Hours?.summary?.symbolCode ?: "-"
                data[3] = timeseries.data.next1Hours?.details?.precipitationAmount.toString()
            }
        }

        return data
    }
}

