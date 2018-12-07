package com.example.tender

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.tender.models.Recipe
import com.squareup.picasso.Picasso

class CustomAdapter(val recipeList:ArrayList<Recipe>, val isFavorites: Boolean, val isDelete: Boolean, val ctx: Context) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    var selectedList : ArrayList<Recipe> = arrayListOf()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        lateinit var v:View
        if(isFavorites) {
            v = LayoutInflater.from(p0.context).inflate(R.layout.recycler_view_details_w_checkbox, p0, false)
        } else if(isDelete){
            v = LayoutInflater.from(p0.context).inflate(R.layout.recycler_view_details_w_delete, p0, false)
        } else {
            v = LayoutInflater.from(p0.context).inflate(R.layout.recycler_view_details, p0, false)
        }
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, position: Int) {

        val recipe : Recipe = recipeList[position]

        p0.tvName.text = recipe.title
        Picasso.get().load(recipe.photo).fit().into(p0.picture)
        p0.details.text = recipe.details
        p0.prepTime.text = recipe.prepTime

        // check if we need to deal with the check box
        if(isFavorites) {
            val checkBox = p0.itemView.findViewById(R.id.check_box) as CheckBox
            checkBox.tag = position
            checkBox.setOnClickListener {
                val pos: Int = Integer.parseInt(checkBox.tag.toString())
                Toast.makeText(ctx, recipeList.get(pos).title + " clicked!", Toast.LENGTH_SHORT).show()

                if (selectedList.contains(recipeList[pos])) {
                    selectedList.remove(recipeList[pos])
                } else {
                    selectedList.add(recipeList[pos])
                }
            }

            // check if we need to have the delete option in a row
        } else if(isDelete) {
            val delete = p0.itemView.findViewById(R.id.delete_recipe_button) as ImageView
            delete.tag = position
            delete.setOnClickListener {
                val pos: Int = Integer.parseInt(delete.tag.toString())
                recipeList.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, recipeList.size)
                Toast.makeText(ctx, "Recipe removed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById(R.id.recyclerViewName) as TextView
        val picture = itemView.findViewById(R.id.recyclerViewImage) as ImageView
        val details = itemView.findViewById(R.id.recyclerViewDetails) as TextView
        val prepTime = itemView.findViewById(R.id.recyclerViewPrepTime) as TextView
    }
}