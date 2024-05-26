package no.uio.ifi.in2000.team40.havprosjekt.ui.alert

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import no.uio.ifi.in2000.team40.havprosjekt.model.metalerts.Feature.Alerts
import no.uio.ifi.in2000.team40.havprosjekt.ui.home.LockScreenOrientation
import java.time.LocalDateTime

/**
 * Composable function for showing AlertScreen.
 *
 * @param navController NavController for navigating between different screens.
 * @param lat The latitude of a given location, relevant for showing the correct county.
 * @param lon The longitude of a given location, relevant for showing the correct county.
 * @param state Boolean indicating whether internet connection is available or not.
 * @param alertScreenViewModel ViewModel for accessing its methods and uiStates.
 */
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(navController: NavController, lat: Double, lon: Double, state: Boolean, alertScreenViewModel: AlertScreenViewModel = viewModel()) {
    alertScreenViewModel.updateState(state)

    // navController.navigateUp() blir kalt mer enn én gang, dette forhindrer at det skjer
    val navCheck = remember { mutableStateOf(false) }
    // ingen farevarsler å vise uansett, ettersom de trenger nettverksforbindelse
    if (!state && !navCheck.value) { navController.navigateUp(); navCheck.value = true; return }

    val nowDate = LocalDateTime.now()
    alertScreenViewModel.requestAlerts(lat, lon, nowDate)

    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val alerts: AlertsUIState by alertScreenViewModel.alertsUIState.collectAsState()

    LaunchedEffect(Unit){
        alertScreenViewModel.viewModelScope
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Hjem") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(70.dp))
            Text("Dette stedet er i ${alerts.county}.\nFarevasler som gjelder:", fontSize = 4.5.em, textAlign = TextAlign.Center)

            Spacer(Modifier.height(5.dp))
            Divider(thickness = 1.5.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0xFFFFFFFF)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(alerts.alerts) { alert -> AlertCard(alert) }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Composable function for showing an alert, presented on a card.
 *
 * @param alert Given alert content to be displayed to the user.
 */
@Composable
fun AlertCard(alert: Alerts) {
    val properties: Alerts.Property = alert.properties
    val textColor = when (properties.eventAwarenessName) {
        "High" -> Color.Red
        "Medium" -> Color.Yellow
        else -> Color.Green
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = textColor.copy(0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = properties.description,
                style = TextStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 20.sp),
                modifier = Modifier
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    tint = Color.Red,
                    contentDescription = null,
                )
                Text(text = " ${properties.area}")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Awareness Level",
                    tint = when (properties.eventAwarenessName) {
                        "High" -> Color.Red
                        "Medium" -> Color.Yellow
                        else -> Color.Green
                    })
                Spacer(modifier = Modifier.width(4.dp))

                Text(properties.eventAwarenessName)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.align(Alignment.Top),
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                )
                Text(" ${properties.consequences}")
            }
        }
    }
}
