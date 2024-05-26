package no.uio.ifi.in2000.team40.havprosjekt.ui.bathinglocations

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team40.havprosjekt.R
import no.uio.ifi.in2000.team40.havprosjekt.model.metalerts.Feature
import no.uio.ifi.in2000.team40.havprosjekt.ui.home.LockScreenOrientation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Composable function for showing LocationScreen.
 *
 * @param navController NavController for navigating between different screens.
 * @param locationName Name of the location we want to show.
 * @param state Boolean indicating whether internet connection is available or not.
 * @param locationScreenViewModel ViewModel for accessing its methods and uiStates.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("QueryPermissionsNeeded", "UnusedMaterial3ScaffoldPaddingParameter",
    "StateFlowValueCalledInComposition", "CoroutineCreationDuringComposition"
)
@Composable
fun LocationScreen(navController: NavController, locationName: String, state: Boolean, locationScreenViewModel: LocationScreenViewModel = viewModel()) {
    val context = LocalContext.current

    locationScreenViewModel.updateState(state)

    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val uiState by locationScreenViewModel.uiState.collectAsState()
    locationScreenViewModel.requestLocation(locationName)

    val location = uiState.bathingLocations ?: return

    val nowDate = LocalDateTime.now()
    locationScreenViewModel.requestAlerts(location.latitude, location.longitude, nowDate)

    locationScreenViewModel.requestOceanProperties(location.latitude, location.longitude)
    locationScreenViewModel.requestForecastProperties(location.latitude, location.longitude)

    var showAlertCard by remember { mutableStateOf(true) }

    //dateTime
    val currentHour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"))
    val chosenHour = remember { mutableIntStateOf(currentHour.toInt()) }

    val hoursOptions = currentHour.toInt() until 24

    val oceanDetails: List<String> = filterOceanDetails(uiState, chosenHour)
    val (airTemperature, windSpeed, _, precipitationAmount) = filterForecastDetails(uiState, chosenHour)

    val nedborsmengde: Painter = painterResource(R.drawable.rainammount)
    val luftTemp: Painter = painterResource(R.drawable.lufttemp)
    val vannTemp: Painter = painterResource(R.drawable.vanntemp)
    val vindStyrke: Painter = painterResource(R.drawable.vindstyrke)
    val vannHastighet: Painter = painterResource(R.drawable.vannhastighet)

    val isLoading = remember { mutableStateOf(false) }
    val showPlaceHolderImg = remember { mutableStateOf(false) }

    val cnt = remember { mutableIntStateOf(0) }
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    coroutineScope.launch {
        while (!state) {
            delay(3000)
            cnt.intValue++
        }
    }
    Text("$cnt")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Hjem | ${location.name}")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        Column {
            Spacer(modifier = Modifier.height(70.dp))

            if (showAlertCard)
                AlertCard(navController, uiState.alerts, onClose = { showAlertCard = false }, location.latitude, location.longitude, state)
            else
                ReOpenAlert(onClick = { showAlertCard = true })

            Card(shape = RectangleShape, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = location.img,
                        contentDescription = null,
                        onLoading = {
                            isLoading.value = true
                        },
                        onSuccess = { isLoading.value = false; showPlaceHolderImg.value = false },
                        onError = {
                            showPlaceHolderImg.value = true
                        }
                    )
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(100.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                    // får app til å krasje... ?
                    /*if (showPlaceHolderImg.value) {
                        Image(
                            painter = painterResource(id = R.drawable.placeholder),
                            contentDescription = null
                        )
                    }*/
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                var expanded by remember { mutableStateOf(false) }
                val onDismissRequest = { expanded = false }

                ExposedDropdownMenuBox(
                    modifier = Modifier.padding(),
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        value = "${chosenHour.intValue}:00",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Velg tidspunkt") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = onDismissRequest,
                    ) {
                        hoursOptions.forEach { timeRange ->
                            DropdownMenuItem(
                                text = { Text("$timeRange:00", fontSize = 20.sp) },
                                onClick = {
                                    chosenHour.intValue = timeRange
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                IconButton(modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(40)),
                    onClick = { launchGoogleMaps(context, location.latitude, location.longitude, location.name) }
                ) {
                    Image(
                        modifier = Modifier.size(40.dp),
                        painter = painterResource(id = R.drawable.mapicon),
                        contentDescription = null
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(2.dp,5.dp,0.5.dp,2.dp),
            ){

                item {
                    Card(
                        modifier = Modifier.padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF8E8EFF)),
                        content = {
                            Row(modifier = Modifier.padding(12.dp),verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = luftTemp,
                                    contentDescription = "luftTemp",
                                    modifier = Modifier.size(45.dp),
                                )
                                if (airTemperature != "-") Text(
                                    text = "$airTemperature °C",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    textAlign = TextAlign.Start,
                                    fontSize = 5.em,
                                    color = Color(0xFFFFFFFF)
                                )
                                else CircularProgressIndicator(Modifier.size(20.dp))
                            }
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF8E8EFF)),
                        content = {
                            Row(modifier = Modifier.padding(12.dp),verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = vannTemp,
                                    contentDescription = "vannTemp",
                                    modifier = Modifier.size(45.dp),
                                )

                                if (oceanDetails[2]!="-") Text(
                                    text = "${oceanDetails[2]} °C",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    textAlign = TextAlign.Start,
                                    fontSize = 5.em,
                                    color = Color(0xFFFFFFFF)
                                )
                                else CircularProgressIndicator(Modifier.size(20.dp))
                            }
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF8E8EFF)),
                        content = {
                            Row(modifier = Modifier.padding(12.dp),verticalAlignment = Alignment.CenterVertically) {
                                if (oceanDetails[0]!="-") Text(
                                    text = "\uD83C\uDF0A ${oceanDetails[0]} m",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    textAlign = TextAlign.Start,
                                    fontSize = 5.em,
                                    color = Color(0xFFFFFFFF)
                                )
                                else CircularProgressIndicator(Modifier.size(20.dp))
                            }
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF8E8EFF)),
                        content = {
                            Row(modifier = Modifier.padding(12.dp),verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = vannHastighet,
                                    contentDescription = "vannHastighet",
                                    modifier = Modifier.size(45.dp),
                                )
                                if (oceanDetails[1]!="-") Text(
                                    text = "${oceanDetails[1]} m/s",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    textAlign = TextAlign.Start,
                                    fontSize = 5.em,
                                    color = Color(0xFFFFFFFF)
                                )
                                else CircularProgressIndicator(Modifier.size(20.dp))

                            }
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF8E8EFF)),
                        content = {
                            Row(modifier = Modifier.padding(12.dp),verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = nedborsmengde,
                                    contentDescription = "nedborsmengde",
                                    modifier = Modifier.size(45.dp),
                                )
                                if (precipitationAmount != "-") Text(
                                    text = "$precipitationAmount mm",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    textAlign = TextAlign.Start,
                                    fontSize = 5.em,
                                    color = Color(0xFFFFFFFF)
                                )
                                else CircularProgressIndicator(Modifier.size(20.dp))
                            }
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.padding(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF8E8EFF)),
                        content = {
                            Row(modifier = Modifier.padding(12.dp),verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = vindStyrke,
                                    contentDescription = "vindStyrke",
                                    modifier = Modifier.size(45.dp),
                                )
                                if (windSpeed != "-") Text(
                                    text = "$windSpeed m/s",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    textAlign = TextAlign.Start,
                                    fontSize = 5.em,
                                    color = Color(0xFFFFFFFF)
                                )
                                else CircularProgressIndicator(Modifier.size(20.dp))
                            }
                        }
                    )
                }
            }
        }
    }
}


/**
 * Composable function for showing a card displaying the number of alerts given they are available.
 *
 * @param navController NavController for navigating between different screens.
 * @param alerts List of the alerts to be used for determining the amount of - and level of potential hazards.
 * @param onClose (Lambda) function to be used for when the card is closed.
 * @param lat The latitude of the location
 * @param lon The longitude of the location
 * @param state Boolean regarding whether an internet connection is established or not.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertCard(
    navController: NavController,
    alerts: List<Feature.Alerts>,
    onClose: () -> Unit,
    lat: Double,
    lon: Double,
    state: Boolean
){
    val alertColor = when {
        alerts.size > 5 -> Color(0xFF9E311E)
        alerts.size >= 3 -> Color(0xFFA78D02)
        else -> Color(0xFF169E16)
    }
    val containerColor = when (alertColor ) {
        Color(0xFF9E311E) -> Color(0xFFE9B9AF)
        Color(0xFFA78D02) -> Color(0xFFE4DBA1)
        else -> Color(0xFFBCD5BC)
    }

    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        border = BorderStroke(1.dp, alertColor),
        onClick = { if (alerts.isNotEmpty() && state) navController.navigate("alertscreen/$lat/$lon") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .fillMaxHeight(0.08f)
    ) {
        Row {
            Spacer(modifier = Modifier.width(20.dp))

            Icon(
                modifier = Modifier
                    .size(45.dp)
                    .align(alignment = Alignment.CenterVertically),
                imageVector = Icons.Default.Warning,
                contentDescription = "Awareness Level",
                tint = alertColor
            )

            Spacer(modifier = Modifier.width(15.dp))

            Divider(color = alertColor, thickness = 2.dp, modifier = Modifier
                .fillMaxHeight()
                .width(2.dp))

            Spacer(modifier = Modifier.width(8.dp))

            val prefix = if (alerts.size == 1) "Farevarsel" else "Farevarsler"

            var text = ""
            text = if (!state) "Kunne ikke hente farevarsler"
            else {
                if (alerts.isNotEmpty()) "${alerts.size} $prefix. Trykk for å se mer"
                else "Ingen farevarsler for idag"
            }

            Text(
                text = text,
                //text = if (alerts.isNotEmpty()) "${alerts.size} $prefix. Trykk for å se mer" else "Ingen farevarsler for idag",
                modifier = Modifier.padding(10.dp, 16.dp, 0.dp, 16.dp)
            )

            Spacer(Modifier.weight(0.8f))

            androidx.compose.material.IconButton(
                modifier = Modifier
                    .size(25.dp)
                    .align(alignment = Alignment.Top),
                onClick = { onClose() }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close button",
                    tint = Color.Black,
                    modifier = Modifier.padding(end = 5.dp)
                )
            }
        }
    }
}

/**
 * Composable function for showing an option of re-opening AlertCard if it was closed by the user.
 *
 * @param onClick (Lambda) function to be used for when the button is clicked.
 */
@Composable
private fun ReOpenAlert(onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.9f)) {
            FilledTonalButton(
                onClick = { onClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
            ) {
                Text("Se varsler")
            }
        }
    }
}

/**
 * Filtering data from Ocean Forecast from a chosen hour.
 *
 * @param uiState Information of the full (unfiltered) ocean properties.
 * @param chosenHour The specific hour to be included exclusively.
 *
 * @return List containing the newly filtered data, specifically the wave height, water speed and water temperature.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun filterOceanDetails(uiState: UiState, chosenHour: MutableState<Int>): List<String> {
    // Om data feilet med å laste inn kan vi trygt returnere tomme verdier.
    var data = listOf("-", "-", "-")

    val oceanProperties = uiState.oceanProperties ?: return data
    val currentDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    // slik det er satt opp nå, vil vi kun hente data fra OceanForecast for samme dag
    oceanProperties.timeseries.forEach {
        // datoen fra oceanForecast er på formatet: 'yyyy-MM-ddTHH:mm:ssZ', eks: '2024-03-15T16:00:00Z'
        val forecastDay = it.time.split("T")[0]
        val forecastHour = it.time.split("T")[1].split(":")[0]

        if (forecastDay == currentDay && chosenHour.value == forecastHour.toInt()){
            data = listOf(
                it.data.instant.details.seaSurfaceWaveHeight.toString(),
                it.data.instant.details.seaWaterSpeed.toString(),
                it.data.instant.details.seaWaterTemperature.toString()
            )
        }
    }

    return data
}

/**
 * Filtering data from Weather Forecast from a chosen hour.
 *
 * @param uiState Information of the full (unfiltered) ocean properties.
 * @param chosenHour The specific hour to be included exclusively.
 *
 * @return List containing the newly filtered data, specifically the air temperature, wind speed, symbol code and precipitation amount.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun filterForecastDetails(uiState: UiState, chosenHour: MutableState<Int>): List<String> {
    var data = listOf("-", "-", "-", "-")

    val locationForecast = uiState.locationForecast ?: return data
    val currentDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    locationForecast.properties.timeseries.forEach { timeseries ->
        val forecastDay = timeseries.time.split("T")[0]
        val forecastHour = timeseries.time.split("T")[1].split(":")[0]

        if (forecastDay == currentDay && chosenHour.value == forecastHour.toInt()) {
            data = listOf(
                timeseries.data.instant.details.airTemperature.toString(),
                timeseries.data.instant.details.windSpeed.toString(),
                timeseries.data.next1Hours?.summary?.symbolCode ?: "-",
                timeseries.data.next1Hours?.details?.precipitationAmount.toString()
            )
        }
    }

    return data
}

/**
 * Helper function for launching Google Maps.
 *
 * @param context The given context from where the intent is started.
 * @param lat The latitude of the location.
 * @param lon The longitude of the location.
 * @param name The name of the location.
 */
fun launchGoogleMaps(context: Context, lat: Double, lon: Double, name: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lon?q=$name"))

    intent.setPackage("com.google.android.apps.maps")
    context.startActivity(intent)
}