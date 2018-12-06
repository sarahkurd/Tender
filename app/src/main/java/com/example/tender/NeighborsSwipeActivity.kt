package com.example.tender

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neighbors_swipe)

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
    }
}
