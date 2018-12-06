package com.example.tender.models

import android.net.Uri

data class Recipe (
        var userID: String = "",
        var photo: String = "",
        var title: String = "",
        var prepTime: String = "",
        var ingredientList: ArrayList<String> = arrayListOf(),
        var details: String = "",
        var cuisineType: String = "",
        var longitude: Double = 0.0,
        var latitude: Double = 0.0
)
