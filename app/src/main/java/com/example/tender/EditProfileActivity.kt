package com.example.tender

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.tender.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase auth and Firestore
        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        complete_profile_submit.setOnClickListener {
            saveUserInformation()
        }
    }

    private fun saveUserInformation(){
        if(validateForm()){
            val map: MutableMap<String, Any> = mutableMapOf()
            val first_name = edit_first_name.text.toString()
            val last_name = edit_last_name.text.toString()
            val city = edit_from_location.text.toString()
            val bio = edit_bio.text.toString()
            var usersReference: CollectionReference

            map.put("firstName", first_name)
            map.put("lastName", last_name)
            map.put("city", city)
            map.put("bio", bio)

            val user = auth.currentUser
            if(user != null) {
                usersReference = mFirestore.collection("Users")
                usersReference.document(user.uid).update(map)
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_LONG).show()
                updateUI()
            }
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()        }
    }

    private fun updateUI() {
        val user = auth.currentUser
        if (user != null) {
            val intent = Intent(this, ProfileActivity::class.java)
            // CLEAR THIS ACTIVITY FROM STACK (you can't go back to it)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } else {
            edit_text_view.setText("Missing fields")
            complete_profile_submit.visibility = View.VISIBLE
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val location = edit_from_location.text.toString()
        if (TextUtils.isEmpty(location)) {
            edit_from_location.error = "Required."
            valid = false
        } else {
            edit_from_location.error = null
        }

        val first_name = edit_first_name.text.toString()
        if (TextUtils.isEmpty(first_name)) {
            edit_first_name.error = "Required."
            valid = false
        } else {
            edit_first_name.error = null
        }

        val last_name = edit_last_name.text.toString()
        if (TextUtils.isEmpty(first_name)) {
            edit_last_name.error = "Required."
            valid = false
        } else {
            edit_last_name.error = null
        }

        return valid
    }
}
