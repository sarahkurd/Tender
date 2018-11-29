package com.example.tender

import android.app.Activity
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
import android.net.Uri
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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.ingredients_row.view.*


class AddRecipeActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private var image_uri: Uri ?= null

    var ingredients = arrayListOf<String>()
    lateinit var prepTime: String
    lateinit var minHours: String
    lateinit var dishCategory: String
    lateinit var measurement: String
    lateinit var cups_tbsps: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Initialize Firebase auth and Firestore database
        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        button_add_recipe_image.setOnClickListener {
            getCameraPermission()
        }

        add_recipe_button.setOnClickListener {
            saveRecipe()
        }

        // spinner adapters
        prep_time_spinner.onItemSelectedListener = this
        min_hour_spinner.onItemSelectedListener = this
        dish_category_spinner.onItemSelectedListener = this
        ingredient_measure_spinner.onItemSelectedListener = this
        tbsp_cup_spinner.onItemSelectedListener = this

    }

    // Interface
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
            val userReference: CollectionReference = mFirestore.collection("Users")
            val emptyIngredients: ArrayList<String> = arrayListOf()

            if (user != null) {
                // get edit text view fields
                val title = et_recipe_title.text.toString()
                val details = et_recipe_details.text.toString()

                // get spinner view values
                val pTime = prepTime + minHours
                val category = dishCategory

                val buildRecipe = Recipe("", null,
                        "", "", emptyIngredients,
                        "", "",
                        0.0, 0.0)
                buildRecipe.userID = user.uid
                buildRecipe.photo = image_uri
                buildRecipe.title = title
                buildRecipe.details = details

                // get users lat and long
                val docRef: DocumentReference = userReference.document(user.uid)
                docRef.get().addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot != null){
                        val lat = documentSnapshot.get("latitude") as Double
                        val long = documentSnapshot.get("longitude") as Double
                        buildRecipe.latitude = lat
                        buildRecipe.longitude = long
                    } else {
                        Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                buildRecipe.prepTime = pTime
                buildRecipe.ingredientList = ingredients
                buildRecipe.cuisineType = category
                var myRecipesReference: CollectionReference = userReference.document(user.uid).collection("MyRecipes")
                myRecipesReference.document(title).set(buildRecipe)
            }
        }
    }


    // dynamic ingredient adder
    fun onAddIngredient(v: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.ingredients_row, null)

        // get fields from spinner and add to ArrayList
        //val lastRow = ingredient_linear_layout.getChildAt(ingredient_linear_layout.childCount - 1)
        val text_ingredient = et_text_ingredient.text.toString()
        ingredients.add("$measurement $cups_tbsps $text_ingredient")

        // Add the new row before the add field button.
        ingredient_linear_layout.addView(rowView, ingredient_linear_layout.childCount - 1)

    }

    fun onDelete(v: View) {
        ingredient_linear_layout.removeView(v.parent as View)
    }

    // return Intent after taking photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            add_recipe_image_view.setImageURI(image_uri)
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
}
