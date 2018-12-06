package com.example.tender

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_recipe_book.*

class RecipeBookActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_book)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        bottomNavigationViewRbook.setOnNavigationItemSelectedListener {
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
