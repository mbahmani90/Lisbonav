package com.majidbahmani.lisbonav.feature.map.presentation.ui.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.majidbahmani.lisbonav.feature.map.domain.model.GeoPoint
import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle

/**
 * A native map with one marker per vehicle: Google Maps on Android, MapKit on iOS.
 * Markers are matched by vehicle id, so a bus moves instead of being removed and added again.
 * [contentPadding] keeps the map's logo and legal notice clear of views drawn on top of the map.
 */
@Composable
expect fun VehicleMap(
    vehicles: List<Vehicle>,
    markerTitle: (Vehicle) -> String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
)

/** The initial camera: the Lisbon metropolitan area, where Carris Metropolitana runs. */
internal object VehicleMapDefaults {
    val center = GeoPoint(latitude = 38.72, longitude = -9.14)

    /** Google Maps zoom level showing roughly the whole area. */
    const val ZOOM = 10f

    /** The same area for MapKit, as the height/width of the visible region. */
    const val SPAN_METERS = 60_000.0
}
