package com.majidbahmani.lisbonav.feature.map.presentation.ui.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.majidbahmani.lisbonav.feature.map.domain.model.GeoPoint
import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle

/** Google Maps (Maps Compose). Needs `MAPS_API_KEY` in local.properties; without it the map stays empty. */
@Composable
actual fun VehicleMap(
    vehicles: List<Vehicle>,
    markerTitle: (Vehicle) -> String,
    modifier: Modifier,
    contentPadding: PaddingValues,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(VehicleMapDefaults.center.toLatLng(), VehicleMapDefaults.ZOOM)
    }
    val density = LocalDensity.current.density

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        // Dark map tiles in dark mode, like the rest of the app (MapKit does this by itself).
        mapColorScheme = if (isSystemInDarkTheme()) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT,
        // The Google logo must stay visible (Maps terms): keep it above the floating bar.
        contentPadding = contentPadding,
        // North stays up, so the heading pointers on the markers stay correct.
        uiSettings = MapUiSettings(
            mapToolbarEnabled = false,
            zoomControlsEnabled = false, // pinch to zoom; matches iOS, which has no zoom buttons
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false,
        ),
    ) {
        vehicles.forEach { vehicle ->
            // key(id): the same Marker follows the same bus across refreshes.
            key(vehicle.id) {
                Marker(
                    state = rememberUpdatedMarkerState(position = vehicle.position.toLatLng()),
                    title = markerTitle(vehicle),
                    icon = BusMarkerIcons.icon(BusMarkerStyle.bearingBucket(vehicle.bearingDegrees), density),
                    anchor = Offset(0.5f, 0.5f), // the circle's center is the bus position
                )
            }
        }
    }
}

private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)
