package com.example.tender

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.example.tender.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_business_swipe.*
import kotlinx.android.synthetic.main.activity_neighbors_swipe.*

class NeighborsSwipeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    // Firebase Image Store
    internal var storageReference: StorageReference?= null
    internal var storage: FirebaseStorage?= null

    lateinit var imageV : ImageView
    lateinit var context: Context

    private var getLat : Double = 0.0
    private var getLong : Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neighbors_swipe)
        context = this

        // Initialize Firebase auth and Firestore database
        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        // Initialize storage
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        // Get the LayoutInflater from Context
        val layoutInflater: LayoutInflater = LayoutInflater.from(applicationContext)
        val card_view = layoutInflater.inflate(R.layout.card, main_layoutview_neighbors)
        imageV = card_view.findViewById(R.id.userIMG) as ImageView

        bottomNavigationViewNeighborsSwipe.setOnNavigationItemSelectedListener {

            when(it.itemId) {
                R.id.profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }  R.id.nav_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }   R.id.add_image -> {
                val intent = Intent(this, AddRecipeActivity::class.java)
                startActivity(intent)
                true
            }else ->
                true
            }
        }

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
                }
            }
        }

        GetUserRecipes().execute()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_nav, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.logout -> {
                // set online status to false and logout
                val user = auth.currentUser
                val usersReference: CollectionReference
                if(user != null) {
                    usersReference = mFirestore.collection("Users")
                    usersReference.document(user.uid).update("online", false)
                    auth.signOut()
                    Toast.makeText(this, "Logged Out", Toast.LENGTH_LONG).show()
                }
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    inner class GetUserRecipes : AsyncTask<Void, Void, ArrayList<Recipe>>() {

        lateinit var progressDialog : ProgressDialog
        var recipeList : ArrayList<Recipe> = arrayListOf()

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Searching recipes around you...")
            progressDialog.show()
        }

        // return list of all recipes around current user
        override fun doInBackground(vararg params: Void?): ArrayList<Recipe> {
            var userReference: CollectionReference = mFirestore.collection("Users")
            val user = auth.currentUser
            var RecipesReference: CollectionReference

            userReference.get().addOnSuccessListener { querySnapshot ->
                    // go though all users in database
                    for(document in querySnapshot){
                        RecipesReference = document.reference.collection("MyRecipes")
//                        var recipeQuery1 = RecipesReference.whereGreaterThan("latitude", getLat - 2)
//                        var recipeQuery2 = recipeQuery1.whereLessThan("latitude", getLat + 2)
//                        var recipeQuery3 = RecipesReference.whereLessThan("longitude", getLong + 2)
//                        recipeQuery3.whereGreaterThan("longitude", getLong - 2).get().addOnSuccessListener { recipes ->
//                            if(recipes != null) {
//                                for(recipe in recipes.documents){
//                                    val gotRecipe = recipe.toObject(Recipe::class.java) as Recipe
//                                    recipeList.add(gotRecipe)
//                                }
//                            }
//                        }.addOnFailureListener {
//                            Log.d("GettingData", "Error querying")
//                        }
                        RecipesReference.get().addOnSuccessListener { documents->
                            if(documents != null ){
                                for(doc in documents){
                                    val gotRecipe = doc.toObject(Recipe::class.java)
                                    recipeList.add(gotRecipe)
                                }
                            }

                        }
                    }
                }

            return recipeList
        }

        override fun onPostExecute(result: ArrayList<Recipe>?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
        }

    }
}
