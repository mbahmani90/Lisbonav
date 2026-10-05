package com.majidbahmani.lisbonav.presentation.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.majidbahmani.lisbonav.domain.model.GeoPoint
import com.majidbahmani.lisbonav.domain.model.Vehicle

/** Google Maps (Maps Compose). Needs `MAPS_API_KEY` in local.properties; without it the map stays empty. */
@Composable
actual fun VehicleMap(
    vehicles: List<Vehicle>,
    markerTitle: (Vehicle) -> String,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(VehicleMapDefaults.center.toLatLng(), VehicleMapDefaults.ZOOM)
    }
    val density = LocalDensity.current.density

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
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
