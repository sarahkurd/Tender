package com.example.tender

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.tender.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_neighbors_swipe.*
import kotlinx.android.synthetic.main.activity_recipe_book.*

class RecipeBookActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var auth: FirebaseAuth
    private var mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Firebase Image Store
    internal var storageReference: StorageReference?= null
    internal var storage: FirebaseStorage?= null

    var context: Context = this
    var recipeList: ArrayList<Recipe> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_book)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Initialize storage
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        linearLayoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager

        bottomNavigationViewRbook.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                } R.id.nav_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            } R.id.add_image -> {
                val intent = Intent(this, AddRecipeActivity::class.java)
                startActivity(intent)
                true
            } else ->
                true
            }
        }

        var userReference: CollectionReference = mFirestore.collection("Users")

        // load initital recycler view with MyRecipes documents
        val recipeReference: CollectionReference = userReference.document(user!!.uid).collection("MyRecipes")
        recipeReference.get().addOnSuccessListener { querySnapshot ->
            // go though all docs in collection
            for (document in querySnapshot) {
                val gotRecipe = document.toObject(Recipe::class.java)
                recipeList.add(gotRecipe)
            }
            val adapter = CustomAdapter(recipeList, false, true,this)
            recyclerView.adapter = adapter
        }

        // listen for View my recipes button click
        button_view_myrecipes.setOnClickListener {
            var getMyRecipes : ArrayList<Recipe> = arrayListOf()
            val myRecipeReference: CollectionReference = userReference.document(user!!.uid).collection("MyRecipes")
            myRecipeReference.get().addOnSuccessListener { querySnapshot ->
                // go though all docs in collection
                for (document in querySnapshot) {
                    val gotRecipe = document.toObject(Recipe::class.java)
                    getMyRecipes.add(gotRecipe)
                }
                val adapter = CustomAdapter(getMyRecipes, false, true, this)
                recyclerView.adapter = adapter
            }
        }

        // query recipes from MyFavorites Collection on this button clikc
        // and display in recycler view
        button_view_favorites.setOnClickListener {
            var getFavorites : ArrayList<Recipe> = arrayListOf()
            val favoriteReference: CollectionReference = userReference.document(user!!.uid).collection("MyFavorites")
            favoriteReference.get().addOnSuccessListener { querySnapshot ->
                // go though all docs in collection
                for (document in querySnapshot) {
                    val gotRecipe = document.toObject(Recipe::class.java)
                    getFavorites.add(gotRecipe)
                }
                val adapter = CustomAdapter(getFavorites, false, true,this)
                recyclerView.adapter = adapter
            }
        }


        //GetMyRecipes().execute()

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    inner class GetMyRecipes : AsyncTask<Void, Void, ArrayList<Recipe>>() {

        lateinit var progressDialog: ProgressDialog
        var recipeList: ArrayList<Recipe> = arrayListOf()


        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Searching recipes around you...")
            progressDialog.show()
        }

        // return list of all recipes around current user
        override fun doInBackground(vararg params: Void?): ArrayList<Recipe> {
            var userReference: CollectionReference = mFirestore.collection("Users")
            var RecipesReference: CollectionReference
            val user = auth.currentUser

            val recipeReference: CollectionReference = userReference.document(user!!.uid).collection("MyRecipes")
            recipeReference.get().addOnSuccessListener { querySnapshot ->
                // go though all users in database
                for (document in querySnapshot) {
                    val gotRecipe = document.toObject(Recipe::class.java)
                    recipeList.add(gotRecipe)
                }
            }

            return recipeList
        }

        override fun onPostExecute(result: ArrayList<Recipe>?) {
            super.onPostExecute(result)
            //progressDialog.dismiss()

//            val adapter = CustomAdapter(result!!, false, this)
//            recyclerView.adapter = adapter

        }

    }

}
