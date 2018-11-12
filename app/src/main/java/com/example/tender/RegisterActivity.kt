package com.example.tender

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Button
        btn_register.setOnClickListener{
            createAccount(et_email.text.toString(), et_password.text.toString())
        }

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase.reference.child("Users")
        // Initialize Firebase auth
        auth = FirebaseAuth.getInstance()

    }

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        //showProgressDialog()

        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser

                        val userId = user!!.uid

                        val curDBReference = mDatabaseReference.child(userId)
                        curDBReference.child("firstName").setValue(et_first_name.text.toString())
                        curDBReference.child("lastName").setValue(et_last_name.text.toString())
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // [START_EXCLUDE]
                    //hideProgressDialog()
                    // [END_EXCLUDE]
                }
        // [END create_user_with_email]
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = et_email.text.toString()
        if (TextUtils.isEmpty(email)) {
            et_email.error = "Required."
            valid = false
        } else {
            et_email.error = null
        }

        val password = et_password.text.toString()
        if (TextUtils.isEmpty(password)) {
            et_password.error = "Required."
            valid = false
        } else {
            et_password.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        //hideProgressDialog()
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            // CLEAR THIS ACTIVITY FROM STACK (you can't go back to it)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } else {
            join_tender.setText("Missing fields")
            btn_register.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }


}
