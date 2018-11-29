package com.example.tender

import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.tender.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_business_swipe.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card.view.*
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

class BusinessSwipeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var mFirestore: FirebaseFirestore

    var users : ArrayList<User> = arrayListOf()
    lateinit var imageV : ImageView
    lateinit var relativeLayoutContainer : RelativeLayout
    lateinit var layoutTVparams : ViewGroup.LayoutParams


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_swipe)

        getData()

        // Get the LayoutInflater from Context
        val layoutInflater: LayoutInflater = LayoutInflater.from(applicationContext)
        val card_view = layoutInflater.inflate(R.layout.card, main_layoutview)
        imageV = card_view.findViewById(R.id.userIMG) as ImageView

        bottomNavigationViewSwipe.setOnNavigationItemSelectedListener {

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

    private fun getData() {
        val user1 = User()
        user1.firstName = "Sally"
        users.add(user1)

        val user2 = User()
        user2.firstName = "Sarah"
        users.add(user2)

        val user3 = User()
        user3.firstName = "Kevin"
        users.add(user3)


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
