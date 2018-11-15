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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()

        setUserInfo()

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
                } else ->
                true
            }
        }
    }

    private fun setUserInfo() {
        var userReference: CollectionReference
        val user = auth.currentUser
        println(user)

        userReference = mFirestore.collection("Users")
        val docRef: DocumentReference = userReference.document(user!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if(documentSnapshot != null){
                val fname = documentSnapshot.get("firstName").toString()
                val lname = documentSnapshot.get("lastName").toString()
                println(fname)
                println(lname)
                users_first_name.text = fname
                users_last_name.text = lname
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
}
