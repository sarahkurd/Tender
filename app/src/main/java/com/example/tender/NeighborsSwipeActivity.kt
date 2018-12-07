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
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_recipe.*
import kotlinx.android.synthetic.main.activity_business_swipe.*
import kotlinx.android.synthetic.main.activity_neighbors_swipe.*
import kotlinx.android.synthetic.main.card.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class NeighborsSwipeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Firebase Image Store
    internal var storageReference: StorageReference?= null
    internal var storage: FirebaseStorage?= null

    lateinit var imageV : ImageView
    var context: Context = this

    private var curr = 0
    private var images = arrayOf(R.drawable.chickentenders, R.drawable.tenderlogo,  R.drawable.tenders)
    var favoriteRecipes : ArrayList<Recipe> = arrayListOf()

    private var recipeList : ArrayList<Recipe> = arrayListOf()

    private var getLat : Double = 0.0
    private var getLong : Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neighbors_swipe)

        // Initialize Firebase auth and Firestore database
        auth = FirebaseAuth.getInstance()

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

        var userReference: CollectionReference = mFirestore.collection("Users")
        var myDocuments: ArrayList<QueryDocumentSnapshot> = arrayListOf()

        userReference.get().addOnSuccessListener { querySnapshot ->
            // go though all users in database
            for (document in querySnapshot) {
                myDocuments.add(document)
            }

            var recipesReference: CollectionReference
            for (doc in myDocuments) {
                recipesReference = doc.reference.collection("MyRecipes")
                recipesReference.get().addOnSuccessListener { recipes ->
                    for (recipe in recipes) {
                        val gotRecipe = recipe.toObject(Recipe::class.java)
                        recipeList.add(gotRecipe)
                    }
                    setInitialImage()
                    setImageRotateListener()
                }.addOnFailureListener {
                    Log.d("error", "EROROROROROROR")
                }
            }
        }
        //GetUserRecipes().execute()

    }

    private fun setInitialImage() {
        setCurrImage()
    }

    private fun setCurrImage() {
        Picasso.get().load(recipeList[curr].photo).fit().into(imageV)
    }

    private fun setImageRotateListener() {
        button_like_neighbors.setOnClickListener {
            favoriteRecipes.add(recipeList[curr])
            Toast.makeText(this, "Added to List", Toast.LENGTH_SHORT).show()
            curr++
            if(curr == recipeList.size){
                curr = 0
            }
            setCurrImage()
        }

        button_unlike_neighbors.setOnClickListener {
            curr++
            if(curr == recipeList.size){
                curr = 0
            }
            setCurrImage()
        }
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
        private var recipeList : ArrayList<Recipe> = arrayListOf()
        private var currImage : Int = 0


        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Searching recipes around you...")
            progressDialog.show()
        }

        // return list of all recipes around current user
        override fun doInBackground(vararg params: Void?): ArrayList<Recipe> {
            var userReference: CollectionReference = mFirestore.collection("Users")
            var myDocuments: ArrayList<QueryDocumentSnapshot> = arrayListOf()

            userReference.get().addOnSuccessListener { querySnapshot ->
                // go though all users in database
                for (document in querySnapshot) {
                    myDocuments.add(document)
                }

                for (doc in myDocuments) {
                    val recipesReference: CollectionReference = doc.reference.collection("MyRecipes")
                    recipesReference.get().addOnSuccessListener { recipes ->
                        for (recipe in recipes) {
                            val gotRecipe = recipe.toObject(Recipe::class.java)
                            recipeList.add(gotRecipe)
                        }
                    }.addOnFailureListener {
                        Log.d("error", "EROROROROROROR")
                    }
                }
            }
            return recipeList
        }
//                        val RecipesReference: CollectionReference = document.reference.collection("MyRecipes")
                        // query based on lat and long
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
//                        RecipesReference.get().addOnSuccessListener { documents->
//                            if(documents != null){
//                                for(doc in documents){
//                                    val gotRecipe = doc.toObject(Recipe::class.java)
//                                    recipeList.add(gotRecipe)
//                                }
//                            }
//                        }.addOnFailureListener {
//                            Log.d("failure", "FAILURE!!!!")
//                        }


        override fun onPostExecute(result: ArrayList<Recipe>?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
//            setInitialImage()
//            setImageRotateListener()
        }

        private fun setInitialImage() {
            setCurrImage()
        }

        private fun setCurrImage() {
            Picasso.get().load(recipeList[currImage].photo).fit().into(imageV)
        }

        private fun setImageRotateListener() {
            button_like_neighbors.setOnClickListener {
                currImage++
                if(currImage == recipeList.size){
                    currImage = 0
                }
                setCurrImage()
            }
            button_unlike_neighbors.setOnClickListener {
                currImage++
                if(currImage == recipeList.size){
                    currImage = 0
                }
                setCurrImage()
            }
        }
    }
}
