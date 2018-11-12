package com.example.tender

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener{
            signIn(email_login_text.text.toString(), password_login_text.text.toString())
        }

        register_button.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = email_login_text.text.toString()
        if (TextUtils.isEmpty(email)) {
            email_login_text.error = "Required."
            valid = false
        } else {
            email_login_text.error = null
        }

        val password = password_login_text.text.toString()
        if (TextUtils.isEmpty(password)) {
            password_login_text.error = "Required."
            valid = false
        } else {
            password_login_text.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {

        if (user != null) {
            // start next activity
            val intent = Intent(this, MainActivity::class.java)
            // CLEAR THIS ACTIVITY FROM STACK (you can't go back to it)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }

    fun signOut() {
        auth.signOut()
        updateUI(null)
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        //showProgressDialog()

        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // [START_EXCLUDE]
                    if (!task.isSuccessful) {
                        status.setText("Auth failed")
                    }
                    //hideProgressDialog()
                    // [END_EXCLUDE]
                }
        // [END sign_in_with_email]
    }

    companion object {
        private const val TAG = "EmailPassword"
    }

}
