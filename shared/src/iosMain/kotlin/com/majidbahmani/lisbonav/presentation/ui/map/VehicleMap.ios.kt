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
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKPointAnnotation
import platform.darwin.NSObject

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

/** An annotation that remembers its heading bucket, so the delegate can pick the icon. */
private class VehicleAnnotation : MKPointAnnotation() {
    var bearingBucket: Int? = null
}

/** Keeps one annotation per vehicle id, so a bus moves instead of blinking out and in. */
@OptIn(ExperimentalForeignApi::class)
private class MapKitVehicleController {

    // MKMapView holds its delegate weakly: keep a strong reference here.
    private val delegate = BusAnnotationDelegate()

    val mapView = MKMapView().apply {
        setRegion(
            MKCoordinateRegionMakeWithDistance(
                VehicleMapDefaults.center.toCoordinate(),
                VehicleMapDefaults.SPAN_METERS,
                VehicleMapDefaults.SPAN_METERS,
            ),
            animated = false,
        )
        // North stays up, so the heading pointers on the markers stay correct.
        setRotateEnabled(false)
        setPitchEnabled(false)
        setDelegate(this@MapKitVehicleController.delegate)
    }

    private val annotations = mutableMapOf<String, VehicleAnnotation>()

    fun show(vehicles: List<Vehicle>, title: (Vehicle) -> String) {
        val currentIds = vehicles.mapTo(mutableSetOf()) { it.id }
        val gone = annotations.filterKeys { it !in currentIds }
        mapView.removeAnnotations(gone.values.toList())
        annotations.keys.removeAll(gone.keys)

        vehicles.forEach { vehicle ->
            val bucket = BusMarkerStyle.bearingBucket(vehicle.bearingDegrees)
            val annotation = annotations[vehicle.id]
            if (annotation == null) {
                val new = VehicleAnnotation().apply {
                    setCoordinate(vehicle.position.toCoordinate())
                    setTitle(title(vehicle))
                    bearingBucket = bucket
                }
                annotations[vehicle.id] = new
                mapView.addAnnotation(new)
            } else {
                // MapKit observes the coordinate: the marker moves in place.
                annotation.setCoordinate(vehicle.position.toCoordinate())
                annotation.setTitle(title(vehicle))
                if (annotation.bearingBucket != bucket) {
                    annotation.bearingBucket = bucket
                    // Null when the marker is off screen; the delegate sets the icon when it appears.
                    mapView.viewForAnnotation(annotation)?.setImage(BusMarkerImages.image(bucket))
                }
            }
        }
    }
}

/** Gives each bus annotation a view with the bus icon instead of the default pin. */
private class BusAnnotationDelegate :
    NSObject(),
    MKMapViewDelegateProtocol {

    override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
        val bus = viewForAnnotation as? VehicleAnnotation ?: return null
        val view = mapView.dequeueReusableAnnotationViewWithIdentifier(REUSE_ID)
            ?: MKAnnotationView(annotation = bus, reuseIdentifier = REUSE_ID)
        view.setAnnotation(bus)
        view.setImage(BusMarkerImages.image(bus.bearingBucket))
        view.setCanShowCallout(true) // tap shows "Line …"
        return view
    }
}

// Not in a companion object: Kotlin/Native doesn't allow fields there for subclasses of ObjC types.
private const val REUSE_ID = "bus"

@OptIn(ExperimentalForeignApi::class)
private fun GeoPoint.toCoordinate() = CLLocationCoordinate2DMake(latitude, longitude)
