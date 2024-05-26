package no.uio.ifi.in2000.team40.havprosjekt.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.maps.MapView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team40.havprosjekt.data.bathinglocations.BathingLocationsRepository
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.BathingLocations

/**
 * ViewModel for managing dataflow between MapScreen and repositories.
 * Mainly for retrieving BathingLocations and initializing a new map instance.
 */
class MapScreenViewModel : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    private lateinit var mapView: MapView

    private val _bathingLocations = MutableStateFlow(emptyList<BathingLocations.Location>())
    val bathingLocations = _bathingLocations.asStateFlow()

    private val _bathingLocationRepository: BathingLocationsRepository = BathingLocationsRepository()

    private var initializeCalled = false

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchBathingLocations() {

        if (initializeCalled) return
        initializeCalled = true

        viewModelScope.launch {
            _bathingLocations.value = _bathingLocationRepository.getBathingLocations().locations
        }
    }

    /**
     * Returns the MapView instance.
     *
     * @return The MapView instance.
     */
    fun getMapView(): MapView {
        return mapView
    }

    /**
     * Initializes the MapView instance
     *
     * @param context The context from which to initialize upon.
     */
    fun initializeMapView(context: Context) {
        mapView = MapView(context)
    }
}