package no.uio.ifi.in2000.team40.havprosjekt.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * Sealed Class for handling the status of whether a user is connected to the internet or not.
 */
sealed class ConnectionStatus {
    data object Available: ConnectionStatus()
    data object Unavailable: ConnectionStatus()
}

/**
 * Composable function checking the internet connectivity status.
 *
 * @return the appropriate state of whether it is connected or not.
 */
@RequiresApi(Build.VERSION_CODES.O)
@ExperimentalCoroutinesApi
@Composable
fun checkConnectivityStatus(): Boolean {
    val connection by connectivityStatus()

    return connection == ConnectionStatus.Available
}

/**
 * Helper method for retrieving the current status of connectivity.
 *
 * @return State representing the connectivityStatus.
 */
@Composable
fun connectivityStatus(): State<ConnectionStatus> {
    val mCtx = LocalContext.current

    return produceState(initialValue = mCtx.currentConnectivityStatus) {
        mCtx.observeConnectivityStateAsFlow().collect{ value = it }
    }
}

/**
 * Helper method for retrieving the current status of connectivity.
 *
 * @return ConnectivityManager to determine the connection status
 */
val Context.currentConnectivityStatus: ConnectionStatus
    get() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return getCurrentConnectivityStatus(connectivityManager)
    }

/**
 * Helper method for retrieving the current status of connectivity.
 *
 * @param connectivityManager ConnectivityManager to determine the connection status
 *
 * @return New ConnectionStatus.Available instance if the user is connected, otherwise ConnectionStatus.Unavailable
 */
private fun getCurrentConnectivityStatus(
    connectivityManager: ConnectivityManager
): ConnectionStatus {
    val connected = connectivityManager.allNetworks.any {network ->
        connectivityManager.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false
    }
    return if (connected) ConnectionStatus.Available
    else ConnectionStatus.Unavailable
}

/**
 * Helper method for retrieving the current status of connectivity.
 *
 * @return StateFlow representing the connectivity state value.
 */
fun Context.observeConnectivityStateAsFlow() = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = NetworkCallback { connectionState -> trySend(connectionState) }

    val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    connectivityManager.registerNetworkCallback(networkRequest, callback)

    val currentState = getCurrentConnectivityStatus(connectivityManager)
    trySend(currentState)

    awaitClose {
        try { connectivityManager.unregisterNetworkCallback(callback) }
        catch (_: Exception) { }
    }
}

/**
 * Helper method for retrieving the current status of connectivity.
 *
 * @param callback (Lamda) function for the callback to handle network changes should there be any.
 *
 * @return New ConnectionStatus.Available instance if the user is connected, otherwise ConnectionStatus.Unavailable
 */
fun NetworkCallback(callback: (ConnectionStatus) -> Unit): ConnectivityManager.NetworkCallback {
    return object : ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            callback(ConnectionStatus.Available)
        }

        override fun onLost(network: Network) {
            callback(ConnectionStatus.Unavailable)
        }
    }
}
