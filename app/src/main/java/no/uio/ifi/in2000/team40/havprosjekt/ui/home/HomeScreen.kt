package no.uio.ifi.in2000.team40.havprosjekt.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.BathingLocations
import no.uio.ifi.in2000.team40.havprosjekt.ui.home.HomeScreenViewModel.HomeScreenUiState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.runtime.DisposableEffect


/**
 * Sets screen orientation of current context to a specified [orientation].
 *
 * @param orientation The specific orientation.
 */
@Composable
fun LockScreenOrientation(orientation: Int) {
    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose { activity.requestedOrientation = originalOrientation }
    }
}

/**
 * Shows a SnackBar to indicate 'no internet connection'.
 */
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun NoInternetSnackBar() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = { Spacer(modifier = Modifier.height(50.dp))}
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Ingen internettforbindelse. Applikasjonen vil ha begrenset funksjonalitet.",
                    duration = Indefinite
                )
            }
        }
    }
}

/**
 * Composable function for showing HomeScreen.
 *
 * @param navController NavController for navigating between different screens.
 * @param state Boolean indicating whether internet connection is available or not.
 * @param homeScreenViewModel ViewModel for accessing its methods and uiStates.
 */
@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController,
    state: Boolean,
    homeScreenViewModel: HomeScreenViewModel = viewModel()
) {
    homeScreenViewModel.updateState(state)
    if (!state) NoInternetSnackBar()

    Log.i("statestate", "$state")

    //Applikasjonen er ikke ment for å kunne fungere i landskapsmodus
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val uiState: HomeScreenUiState by homeScreenViewModel.uiState.collectAsState()

    // variabler nødvendig for søkefelt
    val searchText by homeScreenViewModel.searchText.collectAsState()
    val isSearching by homeScreenViewModel.isSearching.collectAsState()
    val isLoadingFilters by homeScreenViewModel.isLoadingFilters.collectAsState()
    var searchBarSelected by remember { mutableStateOf(false) }
    val locations: List<BathingLocations.Location> = homeScreenViewModel.getSearchedLocs()

    Column(
        modifier =
        if (state) Modifier
            .fillMaxWidth()
            .fillMaxHeight()
        else Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar()

            Spacer(Modifier.height(17.dp))

            if(!searchBarSelected) {
                IconButton(onClick = {
                    searchBarSelected = true
                    homeScreenViewModel.preventFilter()
                }) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon", modifier = Modifier.size(30.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "Vis søkefelt", fontSize = 18.sp)
                    }
                }
            } else SearchBar(homeScreenViewModel) { searchBarSelected = false }

            Spacer(Modifier.height(15.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight(0.1f)
                    .fillMaxWidth(0.93f)
            ) {
                val prefix = if (uiState.bathingLocationsChangeable.size != 1) "steder" else "sted"
                val isLoadingOrShowsLocations =
                    if (!isLoadingFilters && !isSearching)
                        "Det er ${homeScreenViewModel.sizedSearch()} $prefix tilgjengelig"
                    else "Laster inn data ..."

                Text(text = isLoadingOrShowsLocations, fontSize = 20.sp, modifier = Modifier.padding(start = 10.dp))

                Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                    Divider( modifier = Modifier
                        .fillMaxHeight(0.95f)
                        .width(1.dp) )

                    Spacer( modifier = Modifier.width(15.dp) )

                    Column(modifier = Modifier
                        .width(50.dp),
                        Arrangement.Center,
                        Alignment.CenterHorizontally
                    ) {
                        Text("Filter")
                        FilterScreenBackground(homeScreenViewModel, onClick = {searchBarSelected = false})
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Divider(modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(bottom = 10.dp),
                thickness = 2.dp
            )

            if (!isLoadingFilters && !isSearching) {
                if (searchText.isNotBlank()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        locations.forEachIndexed { ind, it ->
                            // kommenter inn denne linjen og den litt lenger nede om app krasjer i HomeScreen:
                            // if (it != null && navController != null && homeScreenViewModel != null && state)
                            item { BathingLocationsCard(location = it, navController, state, homeScreenViewModel, ind) }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        locations.forEachIndexed { ind, it ->
                            //if (it != null && navController != null && homeScreenViewModel != null && state)
                            item { BathingLocationsCard(location = it, navController, state, homeScreenViewModel, ind) }
                        }
                    }
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.width(100.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }

        if (!state) NoInternetSpacer()

        BottomBar(navController = navController)
    }
}

/**
 * Composable function for displaying SearchBar.
 *
 * @param homeScreenViewModel ViewModel for accessing its methods and uiStates.
 * @param onClick (Lambda) function to be called should SearchBar be clicked.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SearchBar(
    homeScreenViewModel: HomeScreenViewModel,
    onClick: () -> Unit
){
    val localFocusManager = LocalFocusManager.current
    val searchText by homeScreenViewModel.searchText.collectAsState()

    OutlinedTextField(
        leadingIcon = {
            IconButton(onClick = {
                localFocusManager.clearFocus()
                onClick()
            }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "")
            }
            Divider(Modifier
                .width(1.dp)
                .height(45.dp)
                .offset(20.dp))
        },
        value = searchText,
        onValueChange = homeScreenViewModel::onSearchTextChange,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp, 0.dp),
        placeholder = { Text(text = "Søk etter badesteder")},
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(onSearch = {
            localFocusManager.clearFocus()
        })
    )
}

/**
 * Composable function for displaying TopBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    CenterAlignedTopAppBar(
        modifier = Modifier.fillMaxHeight(0.05f),
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.White),
        title = {
            Text(
                text = "StrandSøk \uD83C\uDFD6\uFE0F",
                fontSize = 23.sp,
                modifier = Modifier.padding(3.dp)
            )
        }
    )
    Divider(
        thickness = 1.5.dp,
    )
}

/**
 * Composable function for showing a singular BathingLocationsCard.
 *
 * @param location The specific data for this BathingLocationsCard.
 * @param navController NavController for navigating between different screens.
 * @param state Boolean indicating whether an internet connection is available or not.
 * @param viewModel ViewModel for accessing its methods and uiStates.
 * @param ind Index of which BathingLocationsCard within a loop from which it's called.
 */
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("StateFlowValueCalledInComposition", "DiscouragedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BathingLocationsCard(
    location: BathingLocations.Location,
    navController: NavController,
    state: Boolean,
    viewModel: HomeScreenViewModel,
    ind: Int
) {
    Log.i("LocationForecastCall", "card: $ind, loaded: ${viewModel.uiState.value.cardLoaded[ind]}")

    if (state && (!(viewModel.uiState.value.cardLoaded.containsKey(ind)) || viewModel.uiState.value.cardLoaded[ind] == false || viewModel.uiState.value.cardLoaded[ind] == null)) {
        LaunchedEffect(location.name) {
            viewModel.fetchForecastForLocation(location.name, location.latitude, location.longitude)
            viewModel.fetchOceanForecastForLocation(location.name, location.latitude, location.longitude)
        }
        viewModel.uiState.value.cardLoaded[ind] = true
    }


    val oceanForecast by viewModel.oceanForecastsMap.map { forecastsMap ->
        forecastsMap[location.name]
    }.collectAsState(null)

    val airTemperature by viewModel.airTemperatureMap.map { temperatureMap ->
        temperatureMap[location.name]
    }.collectAsState(null)

    val symbolCode by viewModel.locationForecastsMap.map { forecastsMap ->
        forecastsMap[location.name]
    }.collectAsState(null)

    val iconId = symbolCode?.let {
        val context = LocalContext.current
        context.resources.getIdentifier(it, "drawable", context.packageName)
    } ?: 0

    val waterTemperature = oceanForecast?.properties?.timeseries?.firstOrNull { timeseries ->
        val forecastDay = timeseries.time.split("T")[0]
        val forecastHour = timeseries.time.split("T")[1].split(":")[0].toInt()
        val currentDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val currentHour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH")).toInt()

        forecastDay == currentDay && forecastHour == currentHour
    }?.data?.instant?.details?.seaWaterTemperature

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        onClick = { navController.navigate("locationscreen/${location.name}") },
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = location.name,
                    fontSize = 25.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (airTemperature != null) {
                        Text(
                            text = "\uD83D\uDCA8: $airTemperature°C",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    if (waterTemperature != null) {
                        Text(
                            text = "\uD83C\uDF0A: $waterTemperature°C",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            if (iconId != 0) {
                Image(
                    painter = painterResource(id = iconId),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(52.dp)
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun NoInternetSpacer() {
    BottomNavigation(
        backgroundColor = Color.Transparent,
        contentColor = Color.Transparent
    ) {
        Column {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Composable function for showing BottomBar.
 *
 * @param navController NavController for navigating between different screens.
 */
@Composable
fun BottomBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black
    ) {
        BottomNavigationItem(
            selected = currentRoute == "homescreen",
            onClick = { navController.navigate("homescreen") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Hjem") }
        )
        BottomNavigationItem(
            selected = currentRoute == "mapscreen",
            onClick = { navController.navigate("mapscreen") },
            icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
            label = { Text("Kart") }
        )
    }
}