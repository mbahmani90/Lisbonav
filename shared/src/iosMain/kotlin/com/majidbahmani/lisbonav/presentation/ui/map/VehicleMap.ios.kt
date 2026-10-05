package com.majidbahmani.lisbonav.presentation.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.majidbahmani.lisbonav.domain.model.GeoPoint
import com.majidbahmani.lisbonav.domain.model.Vehicle
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

/** Apple MapKit inside Compose. No API key needed. */
@Composable
actual fun VehicleMap(
    vehicles: List<Vehicle>,
    markerTitle: (Vehicle) -> String,
    modifier: Modifier,
) {
    // An object, not screen state: it owns the native view and its annotations.
    val controller = remember { MapKitVehicleController() }

    LaunchedEffect(controller, vehicles) {
        controller.show(vehicles, markerTitle)
    }

    UIKitView(
        factory = { controller.mapView },
        modifier = modifier,
    )
}

/** Keeps one annotation per vehicle id, so a bus moves instead of blinking out and in. */
@OptIn(ExperimentalForeignApi::class)
private class MapKitVehicleController {

    val mapView = MKMapView().apply {
        setRegion(
            MKCoordinateRegionMakeWithDistance(
                VehicleMapDefaults.center.toCoordinate(),
                VehicleMapDefaults.SPAN_METERS,
                VehicleMapDefaults.SPAN_METERS,
            ),
            animated = false,
        )
    }

    private val annotations = mutableMapOf<String, MKPointAnnotation>()

    fun show(vehicles: List<Vehicle>, title: (Vehicle) -> String) {
        val currentIds = vehicles.mapTo(mutableSetOf()) { it.id }
        val gone = annotations.filterKeys { it !in currentIds }
        mapView.removeAnnotations(gone.values.toList())
        annotations.keys.removeAll(gone.keys)

        vehicles.forEach { vehicle ->
            val annotation = annotations[vehicle.id]
            if (annotation == null) {
                val new = MKPointAnnotation().apply {
                    setCoordinate(vehicle.position.toCoordinate())
                    setTitle(title(vehicle))
                }
                annotations[vehicle.id] = new
                mapView.addAnnotation(new)
            } else {
                // MapKit observes the coordinate: the marker moves in place.
                annotation.setCoordinate(vehicle.position.toCoordinate())
                annotation.setTitle(title(vehicle))
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun GeoPoint.toCoordinate() = CLLocationCoordinate2DMake(latitude, longitude)
