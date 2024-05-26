package no.uio.ifi.in2000.team40.havprosjekt.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.Facilities
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetSize
import com.skydoves.flexible.core.FlexibleSheetState
import com.skydoves.flexible.core.FlexibleSheetValue
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import kotlinx.coroutines.delay

/**
 * Composable function for showing FilterScreen.
 *
 * @param homeScreenViewModel ViewModel for accessing its methods and uiStates.
 * @param expandedHeight The amount of height the button should use.
 * @param onClick (Lambda) function to be called should SearchBar be clicked.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FilterScreen(homeScreenViewModel: HomeScreenViewModel, expandedHeight: FlexibleSheetState, onClick: () -> Unit) {
    // hjelpefunksjoner for å filtre på Locations
    fun areLocationPermissionsGranted(context: Context): Boolean =
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun launchPermissionSettings(context: Context) =
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    //

    val uiState by homeScreenViewModel.uiState.collectAsState()

    val filters = listOf("Avstand", "Fasiliteter", "Vanntemperatur")

    // facilities
    val filterStates = remember {
        mutableStateListOf<Pair<String, Boolean>>().apply {
            addAll(filters.map { it to false })
        }
    }

    val facilities = mutableSetOf<String>()
    uiState.bathingLocationsFixed.forEach { it -> facilities.addAll(it.facilities.map { it.toString() }) }

    val facilitiesCheckedState = remember { mutableStateListOf<Boolean>() }
    facilities.map { if (it in uiState.facilityFilterNames) facilitiesCheckedState.add(true) else facilitiesCheckedState.add(false) }

    val filteredFacilities: List<Facilities> = facilities.zip(facilitiesCheckedState)
        .filter { (_, keep) -> keep }
        .map { (facility, _) -> Facilities.entries.firstOrNull {it.name == facility}!! }

    val fac = facilities.toList()
    val facilitiesFixedSize: Int = fac.size
    //

    // location
    val context: Context = LocalContext.current
    homeScreenViewModel.getUserLocation(context)

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val chosenDist = if (uiState.positionCheckedState) uiState.sliderPosition.floatValue else 0f

    /**
     * Nested Composable function for displaying the available temperature range inside FilterScreen
     */
    @Composable
    fun TemperatureCard() {
        Column {
            RangeSlider(
                value = uiState.sliderPositionTemp,
                onValueChange = { range -> uiState.sliderPositionTemp = range },
                valueRange = 0f..30f
            )
            Text(text = "Minst: ${uiState.sliderPositionTemp.start.toInt()}\u00B0 C   -   Maks: ${uiState.sliderPositionTemp.endInclusive.toInt()}\u00B0 C")
        }
    }

    /**
     * Nested Composable function for displaying each available facility inside FilterScreen
     */
    @Composable
    fun FacilityCard() {
        for (i in 0 until facilitiesFixedSize) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(fac[i], fontSize = 20.sp)
                Checkbox(
                    checked = facilitiesCheckedState[i],
                    onCheckedChange = {
                        facilitiesCheckedState[i] = it
                    }
                )
            }
        }
    }

    /**
     * Nested Composable function for displaying the max distance of each BathingLocation inside FilterScreen
     */
    @Composable
    fun LocationCard() {
        if (areLocationPermissionsGranted(context)) {
            if (!locationPermissionsState.allPermissionsGranted) {
                Text("Tilnærmet posisjon er slått på. For å få bedre resultater er presis posisjon anbefalt")
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Request permissions")
                }
            }

            // hovedinnhold
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text("${uiState.sliderPosition.floatValue.toInt()} km", fontSize = 25.sp)
            }
            Slider(
                value = uiState.sliderPosition.floatValue,
                onValueChange = { uiState.sliderPosition.floatValue = it },
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Black,
                    inactiveTrackColor = Color.Black
                ),
                valueRange = 1f..50f
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bruk", fontSize = 20.sp)
                Checkbox(
                    checked = uiState.positionCheckedState,
                    onCheckedChange = {
                        uiState.positionCheckedState = it
                    }
                )
            }

        } else { // tillatelser ikke godkjent
            if (!locationPermissionsState.shouldShowRationale) {
                Text("Denne delen av appen krever tilgang til posisjon:")
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("Request permissions")
                }
            } else {
                Text("Du har nektet tilgang til posisjon til denne appen. Gå inn i innstillinger for å endre det:")
                Button(onClick = { launchPermissionSettings(context) }) {
                    Text(text = "Innstillinger")
                }
            }
        }
    }

    // variabler for justering av dynamisk høyde til FilterScreenBackground (FlexibleBottomSheet via expandedHeight)
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val dynamicHeight = remember { mutableIntStateOf(250) }
    val marginBottom = 150

    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .height(dynamicHeight.intValue.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // expandedHeight er ikke konstant, er et sted mellom 0-1f avhengig av FlexibleSheetValue
                if (expandedHeight.swipeableState.currentValue == FlexibleSheetValue.IntermediatelyExpanded)
                    dynamicHeight.intValue = (expandedHeight.flexibleSheetSize.intermediatelyExpanded * screenHeight - marginBottom).toInt()
                else if (expandedHeight.swipeableState.currentValue == FlexibleSheetValue.FullyExpanded)
                    dynamicHeight.intValue = (expandedHeight.flexibleSheetSize.fullyExpanded * screenHeight - marginBottom).toInt()

                filterStates.forEachIndexed { index, pair ->
                    val rotationState by animateFloatAsState(targetValue = if (pair.second) 180f else 0f)

                    Card(
                        modifier = Modifier
                            .padding(10.dp, 5.dp)
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = Ease
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        filterStates[index] = filterStates[index].copy(second = !pair.second)
                                    }) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            pair.first,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Icon(
                                            modifier = Modifier.rotate(rotationState),
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }

                            Divider(thickness = 2.dp)

                            if (pair.second) {
                                Spacer(modifier = Modifier.height(5.dp))

                                // content
                                when (pair.first) {
                                    "Fasiliteter" -> FacilityCard()
                                    "Avstand" -> LocationCard()
                                    "Vanntemperatur" -> TemperatureCard()
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = {
                        val sliderPositionTempInt: ClosedRange<Int> = uiState.sliderPositionTemp.start.toInt()..uiState.sliderPositionTemp.endInclusive.toInt()

                        uiState.facilityFilterNames = filteredFacilities.map { it.toString() }

                        homeScreenViewModel.updateLocationsByAllFilters(chosenDist, filteredFacilities, sliderPositionTempInt)

                        homeScreenViewModel.preventFilter()

                        onClick() //Lukker bottomsheet
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 15.dp)
                ) { Text(text = "Vis resultater", fontSize = 20.sp) }
            }
        }
    }
}

/**
 * Composable function for showing the configured FlexibleBottomSheet which holds the FilterScreen.
 *
 * @param homeScreenViewModel ViewModel for accessing its methods and uiStates.
 * @param onClick (Lambda) function to be called should SearchBar be clicked.
 */
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("CoroutineCreationDuringComposition", "StateFlowValueCalledInComposition")
@Composable
fun FilterScreenBackground(homeScreenViewModel: HomeScreenViewModel, onClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val controller = LocalSoftwareKeyboardController.current
    val sheetState = rememberFlexibleBottomSheetState(
        flexibleSheetSize = FlexibleSheetSize(
            fullyExpanded = 1f,
            intermediatelyExpanded = 0.5f,
            slightlyExpanded = 0f
        ),
        isModal = false,
        skipSlightlyExpanded = false,
        allowNestedScroll = false
    ).also { LaunchedEffect(Unit) {
        scope.launch { it.slightlyExpand() }
    } }

    if (homeScreenViewModel.uiState.value.filterRemainsHidden) {
        scope.launch {
            // sheetstate setter seg til intermediatelyExpanded automatisk, hva annet kan man gjøre?
            while (true) {
                if (!homeScreenViewModel.uiState.value.filterRemainsHidden) break
                if (sheetState.currentValue.toString() != "Hidden") sheetState.hide()

                delay(100)
            }
        }
    }

    FlexibleBottomSheet(
        onDismissRequest = { showBottomSheet = true },
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        if (sheetState.swipeableState.currentValue == FlexibleSheetValue.SlightlyExpanded) {
            Button(modifier = Modifier
                .padding(10.dp, 0.dp), onClick = {
                scope.launch { sheetState.intermediatelyExpand() }
            }) {
                Text("Vis filter", fontSize = 20.sp)
            }
        }
        FilterScreen(homeScreenViewModel, sheetState, onClick = {showBottomSheet = !showBottomSheet})
    }
    IconButton(
        onClick = {
            controller?.hide()
            onClick()

            if (!sheetState.hasIntermediatelyExpandedState) {
                scope.launch { sheetState.intermediatelyExpand() }
                homeScreenViewModel.allowFilter()
            } else {
                scope.launch { sheetState.slightlyExpand() }
                homeScreenViewModel.preventFilter()
            }
        },
    ) {
        Spacer(modifier = Modifier.size(5.dp))
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Filters",
            tint = Color.Gray,
            modifier = Modifier.size(38.dp)
        )
    }
}
