package no.uio.ifi.in2000.team40.havprosjekt.ui.alert

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team40.havprosjekt.data.metalerts.MetAlertsRepository
import no.uio.ifi.in2000.team40.havprosjekt.model.metalerts.Feature.Alerts
import java.time.LocalDateTime

/**
 * Data class for the different uiStates used by AlertScreen for
 * managing and preserving the different values
 */
data class AlertsUIState(
    val alerts: List<Alerts> = listOf(),
    val county: String = "",
    var state: Boolean = false
)

/**
 * ViewModel for managing dataflow between AlertScreen and repositories.
 * Mainly for retrieving MetAlerts to be stored and displayed to the user in AlertScreen.
 */
class AlertScreenViewModel: ViewModel() {
    private val _repository: MetAlertsRepository = MetAlertsRepository()

    private val _alertsUIState = MutableStateFlow(AlertsUIState())
    val alertsUIState: StateFlow<AlertsUIState> = _alertsUIState.asStateFlow()

    private var initializeCalled = false

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
        if (!_alertsUIState.value.state) return

        if (initializeCalled) return
        initializeCalled = true

        viewModelScope.launch {
            _alertsUIState.update {
                it.copy(
                    alerts = _repository.getFeature(lat, lon, nowDate),
                    county = _repository.getCounty(lat, lon)
                )
            }
        }
    }

    /**
     * Updates a state indicating whether the app currently has connection to internet or not.
     *
     * @param state Boolean value of the current connection state.
     */
    fun updateState(state: Boolean) { _alertsUIState.value.state = state }
}