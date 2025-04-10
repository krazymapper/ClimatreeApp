package com.example.climatport.ui.components

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                
                // Add location overlay
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                locationOverlay.enableMyLocation()
                overlays.add(locationOverlay)
                
                // Set initial position to Paris
                controller.setZoom(18.0)
                controller.setCenter(GeoPoint(48.8566, 2.3522))
            }
        },
        update = { mapView ->
            onMapReady(mapView)
        }
    )
}

fun MapView.addTreeMarker(latitude: Double, longitude: Double, title: String) {
    val marker = Marker(this)
    marker.position = GeoPoint(latitude, longitude)
    marker.title = title
    overlays.add(marker)
    invalidate()
} 