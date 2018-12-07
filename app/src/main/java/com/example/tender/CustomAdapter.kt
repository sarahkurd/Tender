package com.example.tender

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.tender.models.Recipe
import com.squareup.picasso.Picasso

class CustomAdapter(val recipeList:ArrayList<Recipe>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.recycler_view_details, p0, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        val recipe : Recipe = recipeList[p1]

        p0.tvName.text = recipe.title
        Picasso.get().load(recipe.photo).fit().into(p0.picture)
        p0.details.text = recipe.details
        p0.prepTime.text = recipe.prepTime
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById(R.id.recyclerViewName) as TextView
        val picture = itemView.findViewById(R.id.recyclerViewImage) as ImageView
        val details = itemView.findViewById(R.id.recyclerViewDetails) as TextView
        val prepTime = itemView.findViewById(R.id.recyclerViewPrepTime) as TextView

    }
}