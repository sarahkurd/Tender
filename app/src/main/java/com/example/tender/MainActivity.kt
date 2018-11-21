package com.example.tender

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

// LOGIN: You can click "Register Here" to create an account
// Make sure to give a valid email, since it sends an email verification
// Fill in user information when prompted
// Should end up in ProfileActivity once registered
// Clicking on "My Map" in profile activity opens up a Map of pinned places
// MainActivity: have not implemented clicks on the card views yet
// AddRecipe Activity not yet implemented
// but it will implement a card swiping feature like Tinder
// SDKS/APIS used:
// Google Places SDK for Android
// Database: Firestore
// Firebase Authentication
// Google Geolocation API


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        bottomNavigationView.setOnNavigationItemSelectedListener {

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

    override fun onStart() {
        super.onStart()
        var userReference: CollectionReference = mFirestore.collection("Users")
        val docRef: DocumentReference = userReference.document(user!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null) {
                val fname = documentSnapshot.get("firstName").toString()
                users_name.text = fname + getString(R.string.question_mark)
            } else {
                Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show()
            }
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
    }


