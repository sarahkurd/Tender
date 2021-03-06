package com.example.tender

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.location.LocationListener
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.tender.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.places.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private var mProgressBar: ProgressDialog? = null
    private var mGeoDataClient: GeoDataClient? = null
    private var mPlaceDetectionClient: PlaceDetectionClient? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:Int = 1
    private var mLocationPermissionGranted: Boolean = false

    private lateinit var mLastKnownLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var locationCallback : LocationCallback
    private var userLat: Double = 0.0
    private var userLong: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Button to register
        btn_register.setOnClickListener{
            createAccount(et_email.text.toString(), et_password.text.toString())
        }

        mProgressBar = ProgressDialog(this)

        // Initialize Firebase auth and Firestore database
        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()


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


        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null)



    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                mLastKnownLocation = p0!!.locations.get(p0.locations.size-1) // get last location
                userLat = mLastKnownLocation.latitude
                userLong = mLastKnownLocation.longitude
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

    override fun onStart() {
        super.onStart()
    }

    private fun createAccount(email: String, password: String) {

        val firstName = et_first_name.text.toString()
        val lastName = et_last_name.text.toString()

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)
                && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mProgressBar!!.setMessage("Registering User...")
            mProgressBar!!.show()

            Log.d(TAG, "createAccount:$email")
            if (!validateForm()) {
                return
            }

            // [START create_user_with_email]
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        mProgressBar!!.hide()
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = auth.currentUser
                            val userId = user!!.uid

                            // verify valid email
                            //verifyEmail()

                            //getUserLocation()
                            saveUserInformation()
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
            // [END create_user_with_email]


        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }

    }


    private fun saveUserInformation(){
        val first_Name = et_first_name.text.toString()
        val last_Name = et_last_name.text.toString()
        var usersReference: CollectionReference

        val user = auth.currentUser
        if(user != null) {
            val buildUser = User("","", "", "", "", false, null,
                    "", 0, "", 0.0, 0.0, 0.0)
            usersReference = mFirestore.collection("Users")
            buildUser.firstName = first_Name
            buildUser.lastName = last_Name
            buildUser.userID = user.uid
            buildUser.latitude = userLat
            buildUser.longitude = userLong
            buildUser.isOnline = true

            usersReference.document(user.uid).set(buildUser)
        }
    }



    private fun verifyEmail() {
        val mUser = auth.currentUser
        mUser!!.sendEmailVerification()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@RegisterActivity,
                                "Verification email sent to " + mUser.email,
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.exception)
                        Toast.makeText(this@RegisterActivity,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = et_email.text.toString()
        if (TextUtils.isEmpty(email)) {
            et_email.error = "Required."
            valid = false
        } else {
            et_email.error = null
        }

        val password = et_password.text.toString()
        if (TextUtils.isEmpty(password)) {
            et_password.error = "Required."
            valid = false
        } else {
            et_password.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        //hideProgressDialog()
        if (user != null) {
            val intent = Intent(this, EditProfileActivity::class.java)
            // CLEAR THIS ACTIVITY FROM STACK (you can't go back to it)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } else {
            join_tender.setText("Missing fields")
            btn_register.visibility = View.VISIBLE
        }
    }

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

    // handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                } else {
                    Toast.makeText(this, "Location permissions recommended", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    override fun onStop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    companion object {
        private const val TAG = "EmailPassword"
    }


}
