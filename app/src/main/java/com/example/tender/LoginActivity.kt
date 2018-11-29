package com.example.tender

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private var mProgressBar: ProgressDialog? = null
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:Int = 1
    private var mLocationPermissionGranted: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mLastKnownLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var locationCallback : LocationCallback
    private var userLat: Double = 0.0
    private var userLong: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mProgressBar = ProgressDialog(this)

        login_button.setOnClickListener{
            signIn(email_login_text.text.toString(), password_login_text.text.toString())
        }

        register_button.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

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

        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
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

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = email_login_text.text.toString()
        if (TextUtils.isEmpty(email)) {
            email_login_text.error = "Required."
            valid = false
        } else {
            email_login_text.error = null
        }

        val password = password_login_text.text.toString()
        if (TextUtils.isEmpty(password)) {
            password_login_text.error = "Required."
            valid = false
        } else {
            password_login_text.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {

        if (user != null) {
            // start next activity
            val intent = Intent(this, MainActivity::class.java)
            // CLEAR THIS ACTIVITY FROM STACK (you can't go back to it)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }


    private fun signIn(email: String, password: String) {

        var userReference: CollectionReference
        val map: MutableMap<String, Any> = mutableMapOf()

        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        mProgressBar!!.setMessage("Logging in...")
        mProgressBar!!.show()

        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    mProgressBar!!.hide()
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        map.put("latitude", userLat)
                        map.put("longitude", userLong)
                        userReference = mFirestore.collection("Users")
                        userReference.document(user!!.uid).update(map)
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }


                    // [START_EXCLUDE]
                    if (!task.isSuccessful) {
                        status.setText("Auth failed")
                    }
                    //hideProgressDialog()
                    // [END_EXCLUDE]
                }
        // [END sign_in_with_email]
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                } else {
                    Toast.makeText(this, "Location permissions recommended", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }

}
