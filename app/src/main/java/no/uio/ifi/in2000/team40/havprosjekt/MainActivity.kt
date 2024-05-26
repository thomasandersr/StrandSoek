package no.uio.ifi.in2000.team40.havprosjekt

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.ExperimentalCoroutinesApi
import no.uio.ifi.in2000.team40.havprosjekt.network.checkConnectivityStatus
import no.uio.ifi.in2000.team40.havprosjekt.ui.alert.AlertScreen
import no.uio.ifi.in2000.team40.havprosjekt.ui.bathinglocations.LocationScreen
import no.uio.ifi.in2000.team40.havprosjekt.ui.home.HomeScreen
import no.uio.ifi.in2000.team40.havprosjekt.ui.map.MapScreen
import no.uio.ifi.in2000.team40.havprosjekt.ui.theme.HavprosjektTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("lifecycle_l", "onCreate")

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContent {
            HavprosjektTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Run(checkConnectivityStatus())
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Run(state: Boolean) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "homescreen") {
        composable("homescreen") { HomeScreen(navController, state) }

        composable(
            "alertscreen/{lat}/{lon}",
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble()
            val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble()
            if (lat != null && lon != null) AlertScreen(navController, lat, lon, state)
        }

        composable(
            "locationscreen/{locationName}",
            arguments = listOf(navArgument("locationName") { type = NavType.StringType })
        ) { backStackEntry ->
            val locationName = backStackEntry.arguments?.getString("locationName")
            if (locationName != null) LocationScreen(navController, locationName, state)
        }

        composable("mapscreen") { MapScreen(navController = navController) }
    }
}