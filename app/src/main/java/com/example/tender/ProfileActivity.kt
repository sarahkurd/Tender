package com.example.tender

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.tender.R.id.profile
import com.example.tender.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        Picasso.get().load(R.drawable.tenders).fit().into(background_image)

        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        // click to edit profile
        edit_profile.setOnClickListener {
            editClick()
        }

        // view user saved recipes
        card_myrecipes.setOnClickListener {
            recipeBookClick()
        }

        // click on card view to view user map
        card_mymap.setOnClickListener{
            mapsClick()
        }

        bottomNavigationViewProfile.setOnNavigationItemSelectedListener {
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
    }


    override fun onStart() {
        super.onStart()
        setUserInfo()
    }

    // read from firestore and populate user profile information
    private fun setUserInfo() {
        var userReference: CollectionReference
        val user = auth.currentUser

        userReference = mFirestore.collection("Users")
        val docRef: DocumentReference = userReference.document(user!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if(documentSnapshot != null){
                val fname = documentSnapshot.get("firstName").toString()
                val lname = documentSnapshot.get("lastName").toString()
                val get_bio = documentSnapshot.get("bio").toString()
                val city = documentSnapshot.get("city").toString()
                val get_fave_cuisine = documentSnapshot.get("faveCuisine").toString()
                val posts = documentSnapshot.get("posts").toString()
                val tenderScore = documentSnapshot.get("tenderScore").toString()
                val photo = documentSnapshot.get("profilePhotoPath").toString()
                users_first_name.text = fname
                users_last_name.text = lname
                user_location.text = city
                if(photo != ""){
                    Picasso.get().load(photo).fit().into(profile_image)
                }
                user_tenderscore.text = "Tender Score: " + tenderScore
                bio.text = get_bio
                fave_cuisine.text = "Loves:" + get_fave_cuisine
                num_posts.text = "Posts: " + posts
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

        when(item?.itemId) {
            R.id.logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun editClick(){
        val intent = Intent(this, EditProfileActivity::class.java)
        startActivity(intent)
    }

    private fun mapsClick(){
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun recipeBookClick() {
        val intent = Intent(this, RecipeBookActivity::class.java)
        startActivity(intent)
    }
}
