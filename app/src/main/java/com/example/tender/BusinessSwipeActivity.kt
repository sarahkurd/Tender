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
import android.R.attr.apiKey
import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log
import com.yelp.fusion.client.connection.YelpFusionApi
import com.yelp.fusion.client.connection.YelpFusionApiFactory
import com.yelp.fusion.client.models.SearchResponse
import okhttp3.OkHttpClient
import java.io.IOException
import kotlin.collections.HashMap


@Suppress("DEPRECATION")
class BusinessSwipeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var mFirestore: FirebaseFirestore
    lateinit var context: Context

    var users : ArrayList<User> = arrayListOf()
    lateinit var imageV : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_swipe)
        context = this


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

        // AsyncTask to dislpay images from Yelp api
        //FetchPictures().execute()
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

    // For downloading / making an API call while other code is running
    inner class FetchRestaurants: AsyncTask<String, String, String>() {

        lateinit var progressDialog : ProgressDialog

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Searching around you...")
            progressDialog.show()
        }

        override fun doInBackground(vararg parameters: String?): String? {
            val client = OkHttpClient()


            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            //handleJson(result)
        }

        // change ImageView here (be able to touch the main thread)
        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
        }


    }

    private fun handleJson(jsonString: String) {

    }



}
