package com.levy.danaloca.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.levy.danaloca.R
import com.levy.danaloca.utils.toBitmap
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

class MapLocationActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var zoomInButton: ImageButton
    private lateinit var zoomOutButton: ImageButton
    private lateinit var clearLocationButton: ImageButton
    private lateinit var confirmButton: com.google.android.material.button.MaterialButton
    private lateinit var mapView: MapView
    private var currentMarker: Point? = null
    private var pointAnnotationManager: com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_location)

        initViews()
        setupListeners()
        initializeMap()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        zoomInButton = findViewById(R.id.zoomInButton)
        zoomOutButton = findViewById(R.id.zoomOutButton)
        clearLocationButton = findViewById(R.id.clearLocationButton)
        confirmButton = findViewById(R.id.confirmButton)
        mapView = findViewById(R.id.mapView)
        
        // Initialize annotation manager
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

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

        clearLocationButton.setOnClickListener {
            clearMarker()
        }

        confirmButton.setOnClickListener {
            currentMarker?.let { point ->
                val intent = Intent().apply {
                    putExtra(EXTRA_LATITUDE, point.latitude())
                    putExtra(EXTRA_LONGITUDE, point.longitude())
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        mapView.getMapboxMap().apply {
            addOnMapClickListener { point ->
                addMarker(point)
                currentMarker = point
                updateVisibility()
                true
            }
        }
    }

    private fun updateVisibility() {
        val hasMarker = currentMarker != null
        clearLocationButton.visibility = if (hasMarker) View.VISIBLE else View.GONE
        confirmButton.visibility = if (hasMarker) View.VISIBLE else View.GONE
    }

    private fun clearMarker() {
        pointAnnotationManager?.deleteAll()
        currentMarker = null
        updateVisibility()
    }

    private fun initializeMap() {
        mapView.getMapboxMap().apply {
            loadStyleUri(Style.MAPBOX_STREETS)
            setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(108.2022, 16.0544)) // Da Nang coordinates
                    .zoom(12.0) // City level zoom
                    .build()
            )
        }
    }

    private fun addMarker(point: Point) {
        pointAnnotationManager?.let { manager ->
            // Clear existing markers
            manager.deleteAll()

            // Add new marker
            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(getDrawable(R.drawable.ic_location)!!.toBitmap())

            manager.create(pointAnnotationOptions)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    companion object {
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
    }
}