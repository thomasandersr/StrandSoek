package no.uio.ifi.in2000.team40.havprosjekt.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import no.uio.ifi.in2000.team40.havprosjekt.R
import no.uio.ifi.in2000.team40.havprosjekt.model.bathinglocations.BathingLocations
import no.uio.ifi.in2000.team40.havprosjekt.ui.home.BottomBar

/**
 * Composable function for showing the map screen.
 *
 * @param viewModel ViewModel for accessing its methods and uiStates.
 * @param navController NavController for navigating between different screens.
 */
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    viewModel: MapScreenViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    viewModel.initializeMapView(context)
    viewModel.fetchBathingLocations() // Call the function to fetch bathing locations
    val mapView = viewModel.getMapView()
    val bathingLocations by viewModel.bathingLocations.collectAsState()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) {
        MapViewComposable(
            mapView = mapView,
            context = context,
            bathingLocations = bathingLocations,
            navController = navController
        )
    }
}

/**
 * Composable function for showing the map of all available BathingLocations.
 *
 * @param mapView The MapView instance.
 * @param context The context, needed for converting drawables to bitmaps.
 * @param bathingLocations The locations themselves, to be drawn and shown on the screen to the user.
 * @param navController NavController for navigating to LocationScreen of a given BathingLocation.
 */
@Composable
fun MapViewComposable(
    mapView: MapView,
    context: Context,
    bathingLocations: List<BathingLocations.Location>,
    navController: NavController
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { mv ->
                mv.getMapboxMap().apply {
                    loadStyleUri(Style.MAPBOX_STREETS) {
                        setCamera(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(10.71, 59.92))
                                .zoom(9.0)
                                .build()
                        )

                        val annotationManager = mv.annotations.createPointAnnotationManager()

                        annotationManager.addClickListener { annotation ->
                            val location = bathingLocations.find { loc ->
                                loc.latitude == annotation.point.latitude() &&
                                        loc.longitude == annotation.point.longitude()
                            }
                            if (location != null) {
                                navController.navigate("locationscreen/${location.name}")
                            }
                            true
                        }

                        for (location in bathingLocations) {
                            val markerCoordinates = Point.fromLngLat(location.longitude, location.latitude)
                            bitmapFromDrawableRes(context, R.drawable.red_marker)?.let { bitmap ->
                                val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                                    .withPoint(markerCoordinates)
                                    .withIconImage(bitmap)
                                annotationManager?.create(pointAnnotationOptions)
                            }
                        }
                    }
                }
            }
        )
        TopTextOverlay()
    }
}

/**
 * Composable function for showing a text box to the user, informing them they can explore the map freely.
 */
@Composable
private fun TopTextOverlay() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = 0.8f // adjust transparency as needed
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Utforsk nye badeplasser ved å trykke på markørene!",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Helper function for converting a drawable to a bitmap.
 *
 * @param context The context, needed for managing resources.
 * @param resourceId The ID of the drawable.
 *
 * @return The newly converted bitmap.
 */
private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
    convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

/**
 * Helper function for converting a drawable to a bitmap.
 *
 * @param sourceDrawable The drawable to be converted.
 *
 * @return The newly converted bitmap.
 */
private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
    if (sourceDrawable == null) {
        return null
    }

    return if (sourceDrawable is BitmapDrawable) {
        sourceDrawable.bitmap
    } else {
        val constantState = sourceDrawable.constantState ?: return null
        val drawable = constantState.newDrawable().mutate()
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}
