package com.example.tender

import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.maps.model.CameraPosition





class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val mCameraPosition: CameraPosition? = null

    protected var mGeoDataClient: GeoDataClient? = null
    protected var mPlaceDetectionClient: PlaceDetectionClient? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:Int = 1
    private var mLocationPermissionGranted: Boolean = false

    private val mDefaultLocation = LatLng(-33.8523341, 151.2106085)
    private val DEFAULT_ZOOM = 15f

    private lateinit var mLastKnownLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var locationCallback : LocationCallback
    private var userLat: Double = 0.0
    private var userLong: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null)

        // Prompt the user for permission.
        getLocationPermission()
        try {
            if (mLocationPermissionGranted) {
                buildLocationRequest()
                buildLocationCallBack()

                // create instance of Fused location client
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper())
            } else {
                Toast.makeText(this, "Location off", Toast.LENGTH_SHORT).show()
            }
        }catch (e:SecurityException){
            Log.e("Exception", "************")
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

    }

    // handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    // get runtime permissions on app
    // user can allow or deny location permission
    private fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    // set the location controls on the map
    // enable the My location layer if user granted permission
    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                mLastKnownLocation = p0!!.locations.get(p0.locations.size-1) // get last location
                userLat = mLastKnownLocation.latitude
                userLong = mLastKnownLocation.longitude
                mMap.addMarker(MarkerOptions().position(LatLng(userLat, userLong)).title("My Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(userLat, userLong), DEFAULT_ZOOM))
            }
        }

    }

    private fun buildLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5000
        mLocationRequest.fastestInterval = 3000
        mLocationRequest.smallestDisplacement = 10f

    }


    // use fused location provider to find devices last known location
    // use location to position map
//    private fun getDeviceLocation() {
//        /*
//     * Get the best and most recent location of the device, which may be null in rare
//     * cases when a location is not available.
//     */
//        try {
//            if (mLocationPermissionGranted) {
//                val locationResult = fusedLocationClient.lastLocation
//                locationResult.addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            // Set the map's camera position to the current location of the device.
//                            mLastKnownLocation = task.result
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                    LatLng(mLastKnownLocation!!.latitude,
//                                            mLastKnownLocation!!.longitude), DEFAULT_ZOOM))
//                        } else {
//                            Log.d(TAG, "Current location is null. Using defaults.")
//                            Log.e(TAG, "Exception: %s", task.exception)
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM))
//                            mMap.uiSettings.isMyLocationButtonEnabled = false
//                        }
//
//                    }
//
//                }
//        } catch (e: SecurityException) {
//            Log.e("Exception: %s", e.message)
//        }
//
//    }

    companion object {
        private const val TAG = "Maps Activity"
    }
}
