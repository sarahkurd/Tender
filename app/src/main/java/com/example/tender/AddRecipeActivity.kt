package com.example.tender

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.LinearLayout;
import kotlinx.android.synthetic.main.activity_add_recipe.*
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.view.LayoutInflater



class AddRecipeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)
    }

    fun onAddIngredient(v: View) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.ingredients_row, null)
        // Add the new row before the add field button.
        ingredient_linear_layout.addView(rowView, ingredient_linear_layout.childCount - 1)
    }

    fun onDelete(v: View) {
        ingredient_linear_layout.removeView(v.parent as View)
    }
}
