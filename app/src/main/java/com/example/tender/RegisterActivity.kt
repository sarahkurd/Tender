package com.example.tender

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.location.LocationListener
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore
    private var mProgressBar: ProgressDialog? = null
    protected var mGeoDataClient: GeoDataClient? = null
    protected var mPlaceDetectionClient: PlaceDetectionClient? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:Int = 1
    private var mLocationPermissionGranted: Boolean = false
    private var mLastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Button to register
        btn_register.setOnClickListener{
            createAccount(et_email.text.toString(), et_password.text.toString())
        }

        mProgressBar = ProgressDialog(this)

        // Initialize Firebase auth and Firestore database
        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        // create instance of Fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null)

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null)


    }

    override fun onStart() {
        super.onStart()

        getLocationPermission()
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
                            verifyEmail()

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
        // create instance of Fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val user = auth.currentUser
        if(user != null) {
            val buildUser = User("","", "", "", null, "True", null,
                    "", 0, "", 0.0, 0.0, 0.0)
            usersReference = mFirestore.collection("Users")
            buildUser.firstName = first_Name
            buildUser.lastName = last_Name
            buildUser.userID = user.uid
            // Define a listener that responds to location updates
            try{
                if(mLocationPermissionGranted) {
                    Toast.makeText(this, "Location permissions granted", Toast.LENGTH_LONG).show()
                    fusedLocationClient.lastLocation.addOnSuccessListener{ location: Location? ->
                        if (location != null) {
                            buildUser.latitude = location.latitude
                            buildUser.longitude = location.longitude
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Toast.makeText(this, "Location is null", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: SecurityException){
                Log.e("Exception: %s", e.message)
            }
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
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
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
                    finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }


}
