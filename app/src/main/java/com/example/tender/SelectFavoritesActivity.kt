package com.example.tender

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import android.widget.Toast
import com.example.tender.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_recipe_book.*
import kotlinx.android.synthetic.main.activity_select_favorites.*

class SelectFavoritesActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var auth: FirebaseAuth
    private var mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    lateinit var getFavoritesList: ArrayList<Recipe>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_favorites)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // get favorite Recipes list passed from NeighborsSwipeActivity
        // and place in recyclerView
        getFavoritesList = intent.getParcelableArrayListExtra("list")

        linearLayoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        favoritesRecyclerView.layoutManager = linearLayoutManager

        val adapter = CustomAdapter(getFavoritesList, true, this)
        favoritesRecyclerView.adapter = adapter

        // take list of clicked items from adapter
        // and add to firestore database under MyFavorites collection
        var userReference: CollectionReference = mFirestore.collection("Users")
        val recipeReference: CollectionReference = userReference.document(user!!.uid).collection("MyFavorites")
        button_add_favorites.setOnClickListener {
            for(recipe in adapter.selectedList) {
                recipeReference.document(recipe.title).set(recipe)
            }
            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
