package com.example.tender

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.LinearLayout;
import kotlinx.android.synthetic.main.activity_add_recipe.*
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.support.v4.content.ContextCompat.getSystemService
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import com.example.tender.models.Recipe
import com.example.tender.models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.ingredients_row.view.*
import java.io.IOException
import java.util.*


class AddRecipeActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private var image_uri: Uri ?= null

    // Firebase Image Store
    internal var storageReference: StorageReference?= null
    internal var storage: FirebaseStorage?= null

    var ingredients = arrayListOf<String>()
    lateinit var prepTime: String
    lateinit var minHours: String
    lateinit var dishCategory: String
    lateinit var measurement: String
    lateinit var cups_tbsps: String

    private var getLat : Double = 0.0
    private var getLong : Double = 0.0
    private var getPosts : Long = 0
    private var downloadURL : String ?= null

    private val PICK_IMAGE_REQUEST = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Initialize Firebase auth and Firestore database
        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        // Initialize storage
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        button_add_recipe_image.setOnClickListener {
            getCameraPermission()
        }

        add_recipe_button.setOnClickListener {
            saveRecipe()
        }

        button_upload_recipe_image.setOnClickListener {
            showFileChooser()
        }

        // spinner adapters
        prep_time_spinner.onItemSelectedListener = this
        min_hour_spinner.onItemSelectedListener = this
        dish_category_spinner.onItemSelectedListener = this
        ingredient_measure_spinner.onItemSelectedListener = this
        tbsp_cup_spinner.onItemSelectedListener = this

        MyAsyncTask().execute()
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST)
    }

    // Interface for Spinners
    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        // The parent object passed into the method is the spinner in which the item was selected
        val selection = parent.getItemAtPosition(pos) as? String
        Log.d("ItemSelected", parent.getItemAtPosition(pos) as? String)

        // get spinner id
        when(parent) {
            prep_time_spinner ->
                prepTime = parent.getItemAtPosition(pos) as String
            min_hour_spinner ->
                minHours = parent.getItemAtPosition(pos) as String
            dish_category_spinner ->
                dishCategory = parent.getItemAtPosition(pos) as String
            ingredient_measure_spinner ->
                measurement = parent.getItemAtPosition(pos) as String
            tbsp_cup_spinner ->
                cups_tbsps = parent.getItemAtPosition(pos) as String
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }


    private fun saveRecipe() {
        // check if all fields are filled
        if(validateForm()) {
            val user = auth.currentUser
            var userReference: CollectionReference = mFirestore.collection("Users")
            val emptyIngredients: ArrayList<String> = arrayListOf()

            if (user != null) {
                // get edit text view fields
                val title = et_recipe_title.text.toString()
                val details = et_recipe_details.text.toString()

                // get spinner view values
                val pTime = prepTime + minHours
                val category = dishCategory

                val buildRecipe = Recipe("", "",
                        "", "", emptyIngredients,
                        "", "",
                        0.0, 0.0)
                buildRecipe.userID = user.uid

                if(downloadURL != null) {
                    buildRecipe.photo = downloadURL!!
                }
                buildRecipe.title = title
                buildRecipe.details = details

                buildRecipe.prepTime = pTime
                buildRecipe.ingredientList = ingredients
                buildRecipe.cuisineType = category

                buildRecipe.latitude = getLat
                buildRecipe.longitude = getLong
                userReference.document(user.uid).update("posts", getPosts + 1)

                // Uploaded: now go back to Home UI
                Toast.makeText(this, "Recipe added", Toast.LENGTH_LONG).show()
                val myRecipesReference: CollectionReference
                myRecipesReference = userReference.document(user.uid).collection("MyRecipes")
                myRecipesReference.document(title).set(buildRecipe)

                updateUI()
            }
        } else {
            Toast.makeText(this, "Missing Fields", Toast.LENGTH_SHORT).show()
        }
    }


    // dynamic ingredient adder
    fun onAddIngredient(v: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.ingredients_row, null)

        // get fields from spinner and add to ArrayList
        val lastRow = ingredient_linear_layout.getChildAt(ingredient_linear_layout.childCount - 2)
        val text_ingredient = lastRow.et_text_ingredient.text.toString()
        ingredients.add("$measurement $cups_tbsps $text_ingredient")

        // Add the new row before the add field button.
        ingredient_linear_layout.addView(rowView, ingredient_linear_layout.childCount - 1)

    }

    fun onDelete(v: View) {
        ingredient_linear_layout.removeView(v.parent as View)
    }

    // return Intent after taking photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // if taking picture
        if(requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            uploadFile()
        } else if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // if uploading image
            image_uri = data.data
            uploadFile()
        }
    }

    fun getCameraPermission() {
        if(ContextCompat.checkSelfPermission(this.applicationContext, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this.applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED
                ) {
            val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            // show pop up to request permissions
            ActivityCompat.requestPermissions(this, permission, PERMISSION_CODE)
        } else {
            // permission already granted
            openCamera()
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        // camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)

    }

    private fun uploadFile(){
        if(image_uri != null){
            val progressDialogue = ProgressDialog(this)
            progressDialogue.setTitle("Uploading profile image ...")
            progressDialogue.show()

            val imageRef = storageReference!!.child("images/" + UUID.randomUUID().toString())
            imageRef.putFile(image_uri!!)
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

            // after uploading image to storage, get upload URL and store in recipe
            val uploadTask = imageRef.putFile(image_uri!!)
            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation imageRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadURL = task.result.toString()
                    Picasso.get().load(downloadURL).fit().into(add_recipe_image_view)
                }
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode) {
            PERMISSION_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission from popup was granted
                    openCamera()
                } else {
                    // not granted
                    Toast.makeText(this, "Camera Permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun validateForm(): Boolean {
        var valid = true

        val title = et_recipe_title.text.toString()
        if (TextUtils.isEmpty(title)) {
            et_recipe_title.error = "Required."
            valid = false
        } else {
            et_recipe_title.error = null
        }

        val details = et_recipe_details.text.toString()
        if (TextUtils.isEmpty(details)) {
            et_recipe_details.error = "Required."
            valid = false
        } else {
            et_recipe_details.error = null
        }

        // figure out how to check if image uploaded

        return valid
    }

    private fun updateUI() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    inner class MyAsyncTask: AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            val user = auth.currentUser
            if(user!=null) {
                val userReference: CollectionReference
                userReference = mFirestore.collection("Users")
                val docRef: DocumentReference = userReference.document(user!!.uid)
                // get users lat and long
                docRef.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        getLat = documentSnapshot.get("latitude") as Double
                        getLong = documentSnapshot.get("longitude") as Double
                        getPosts = documentSnapshot.get("posts") as Long
                    }
                }
                return ""
            } else {
                return ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }
    }
}
