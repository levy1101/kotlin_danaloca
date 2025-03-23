package com.levy.danaloca.view.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.levy.danaloca.R
import com.levy.danaloca.utils.toBitmap
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class LocationPreviewDialog : DialogFragment() {
    
    private lateinit var mapView: MapView
    private lateinit var zoomInButton: ImageButton
    private lateinit var zoomOutButton: ImageButton

    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        fun newInstance(latitude: Double, longitude: Double): LocationPreviewDialog {
            return LocationPreviewDialog().apply {
                arguments = Bundle().apply {
                    putDouble(ARG_LATITUDE, latitude)
                    putDouble(ARG_LONGITUDE, longitude)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_location_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById<MapView>(R.id.mapView)
        zoomInButton = view.findViewById<ImageButton>(R.id.zoomInButton)
        zoomOutButton = view.findViewById<ImageButton>(R.id.zoomOutButton)
        
        val latitude = arguments?.getDouble(ARG_LATITUDE) ?: return
        val longitude = arguments?.getDouble(ARG_LONGITUDE) ?: return
        
        setupMap(latitude, longitude)
        setupZoomButtons()
    }

    private fun setupMap(latitude: Double, longitude: Double) {
        val location = Point.fromLngLat(longitude, latitude)
        
        mapView.getMapboxMap().apply {
            loadStyleUri(Style.MAPBOX_STREETS)
            setCamera(
                CameraOptions.Builder()
                    .center(location)
                    .zoom(15.0)
                    .build()
            )
        }

        // Add marker
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(location)
            .withIconImage(requireContext().getDrawable(R.drawable.ic_location)!!.toBitmap())
        pointAnnotationManager.create(pointAnnotationOptions)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupZoomButtons() {
        zoomInButton.setOnClickListener {
            mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .zoom(mapView.getMapboxMap().cameraState.zoom + 1.0)
                    .build()
            )
        }

        zoomOutButton.setOnClickListener {
            mapView.getMapboxMap().setCamera(
                CameraOptions.Builder()
                    .zoom(mapView.getMapboxMap().cameraState.zoom - 1.0)
                    .build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }
}