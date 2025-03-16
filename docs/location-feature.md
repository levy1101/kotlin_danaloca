# Location Feature Implementation Guide for Posts

## Prerequisites
- Mapbox API key (already configured in AndroidManifest.xml)
- Android SDK 30 
- Firebase Realtime Database setup

## Data Structure in Firebase

```json
{
  "posts": {
    "postId": {
      "location": {
        "latitude": 10.123456,
        "longitude": 106.789012,
        "name": "Location Name (optional)"
      }
      // other post data
    }
  }
}
```

## Implementation Steps

### 1. Create Post with Location

1. Add map to create post layout:
```xml
<com.mapbox.maps.MapView
    android:id="@+id/mapView"
    android:layout_width="match_parent"
    android:layout_height="250dp"
    android:layout_margin="16dp"/>
```

2. Initialize map with zoom controls:
```kotlin
private lateinit var mapView: MapView
private lateinit var mapboxMap: MapboxMap
private var currentMarker: PointAnnotationManager? = null

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize map
    mapView = findViewById(R.id.mapView)
    mapboxMap = mapView.getMapboxMap()
    
    // Enable zoom controls
    mapView.gestures.pitchEnabled = true
    mapView.gestures.scrollEnabled = true
    mapView.gestures.pinchToZoomEnabled = true
    
    // Set initial zoom level
    mapboxMap.setCamera(
        CameraOptions.Builder()
            .zoom(15.0)
            .build()
    )
}
```

3. Handle map click to add/update marker:
```kotlin
private fun setupMapClickListener() {
    mapboxMap.addOnMapClickListener { point ->
        // Remove existing marker
        currentMarker?.deleteAll()
        
        // Add new marker
        val annotationApi = mapView?.annotations
        currentMarker = annotationApi?.createPointAnnotationManager()
        
        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(markerIconBitmap)
        
        currentMarker?.create(pointAnnotationOptions)
        
        // Save coordinates for post creation
        selectedLocation = LatLng(point.latitude(), point.longitude())
        
        true
    }
}
```

4. Save location with post:
```kotlin
private fun savePost(content: String, location: LatLng) {
    val postRef = FirebaseDatabase.getInstance().reference
        .child("posts")
        .push()
    
    val post = HashMap<String, Any>()
    post["content"] = content
    post["location"] = mapOf(
        "latitude" to location.latitude,
        "longitude" to location.longitude
    )
    
    postRef.setValue(post)
}
```

### 2. View Post Location

1. Display map in post detail view:
```xml
<com.mapbox.maps.MapView
    android:id="@+id/postLocationMap"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:layout_margin="16dp"/>
```

2. Load and display location:
```kotlin
private fun displayPostLocation(postId: String) {
    // Get post location from Firebase
    FirebaseDatabase.getInstance().reference
        .child("posts")
        .child(postId)
        .child("location")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("latitude").getValue(Double::class.java)
                val longitude = snapshot.child("longitude").getValue(Double::class.java)
                
                if (latitude != null && longitude != null) {
                    // Add marker
                    val point = Point.fromLngLat(longitude, latitude)
                    addMarkerToMap(point)
                    
                    // Center and zoom map
                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(15.0)
                            .build()
                    )
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
}
```

### 3. Helper Functions

1. Add marker to map:
```kotlin
private fun addMarkerToMap(point: Point) {
    // Remove existing marker
    currentMarker?.deleteAll()
    
    // Create new marker
    val annotationApi = mapView?.annotations
    currentMarker = annotationApi?.createPointAnnotationManager()
    
    val pointAnnotationOptions = PointAnnotationOptions()
        .withPoint(point)
        .withIconImage(markerIconBitmap)
    
    currentMarker?.create(pointAnnotationOptions)
}
```

2. Create marker bitmap:
```kotlin
private fun getBitmapFromVectorDrawable(drawable: Int): Bitmap {
    val vectorDrawable = ContextCompat.getDrawable(context, drawable)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return bitmap
}
```
