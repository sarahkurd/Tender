package com.example.tender

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.tender.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import java.io.IOException
import java.util.*
import java.util.jar.Manifest

class EditProfileActivity : AppCompatActivity(), View.OnClickListener {

    private var filePath: Uri ?= null

    // Firebase Image Store
    internal var storageReference: StorageReference ?= null
    internal var storage: FirebaseStorage ?= null

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    private val PICK_IMAGE_REQUEST = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase auth and Firestore
        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        // Initialize storage
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        edit_profile_image_view.setOnClickListener(this)


        // preset any fields that are already in database
        var usersReference: CollectionReference
        usersReference = mFirestore.collection("Users")
        val docRef: DocumentReference = usersReference.document(user!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if(documentSnapshot != null) {
                val fname = documentSnapshot.get("firstName").toString()
                val lname = documentSnapshot.get("lastName").toString()
                val bio = documentSnapshot.get("bio").toString()
                val city = documentSnapshot.get("city").toString()
                edit_first_name.setText(fname)
                edit_last_name.setText(lname)
                edit_bio.setText(bio)
                edit_from_location.setText(city)
            }
        }

        complete_profile_submit.setOnClickListener {
            saveUserInformation()
        }
    }

    override fun onClick(v: View) {
        if(v == edit_profile_image_view){
            showFileChooser()
        }
    }

    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST)
    }

    private fun uploadFile(){
        if(filePath != null){
            val progressDialogue = ProgressDialog(this)
            progressDialogue.setTitle("Uploading profile image ...")
            progressDialogue.show()

            val imageRef = storageReference!!.child("images/" + UUID.randomUUID().toString())
            imageRef.putFile(filePath!!)
                    .addOnSuccessListener {
                        progressDialogue.dismiss()
                        Toast.makeText(applicationContext, "Image Uploaded", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        progressDialogue.dismiss()
                        Toast.makeText(applicationContext, "Error Uploading Image", Toast.LENGTH_SHORT).show()

                    }
                    .addOnProgressListener { taskSnapshot ->  
                        val progress = 100.0 * taskSnapshot.bytesTransferred/taskSnapshot.totalByteCount
                        progressDialogue.setMessage("Uploaded " + progress.toInt() + "% ...")
                    }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode ==  PICK_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK && data != null && data.data != null){
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                edit_profile_image_view.setImageBitmap(bitmap)
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveUserInformation(){
        if(validateForm()){
            val map: MutableMap<String, Any> = mutableMapOf()
            val profile_photo_path = filePath.toString()
            val first_name = edit_first_name.text.toString()
            val last_name = edit_last_name.text.toString()
            val city = edit_from_location.text.toString()
            val bio = edit_bio.text.toString()
            var usersReference: CollectionReference

            map.put("profilePhotoPath", profile_photo_path)
            map.put("firstName", first_name)
            map.put("lastName", last_name)
            map.put("city", city)
            map.put("bio", bio)

            val user = auth.currentUser
            if(user != null) {
                usersReference = mFirestore.collection("Users")
                uploadFile()
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
        if (TextUtils.isEmpty(last_name)) {
            edit_last_name.error = "Required."
            valid = false
        } else {
            edit_last_name.error = null
        }

        return valid
    }



}
