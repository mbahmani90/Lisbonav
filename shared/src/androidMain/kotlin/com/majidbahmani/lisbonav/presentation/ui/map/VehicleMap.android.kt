package com.majidbahmani.lisbonav.presentation.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.majidbahmani.lisbonav.domain.model.GeoPoint
import com.majidbahmani.lisbonav.domain.model.Vehicle

/** Google Maps (Maps Compose). Needs `MAPS_API_KEY` in local.properties; without it the tiles stay grey. */
@Composable
actual fun VehicleMap(
    vehicles: List<Vehicle>,
    markerTitle: (Vehicle) -> String,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(VehicleMapDefaults.center.toLatLng(), VehicleMapDefaults.ZOOM)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(mapToolbarEnabled = false),
    ) {
        vehicles.forEach { vehicle ->
            // key(id): the same Marker follows the same bus across refreshes.
            key(vehicle.id) {
                Marker(
                    state = rememberUpdatedMarkerState(position = vehicle.position.toLatLng()),
                    title = markerTitle(vehicle),
                )
            }
        }
    }
}

private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)
