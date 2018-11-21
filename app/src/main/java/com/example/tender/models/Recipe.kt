package com.example.tender.models

data class Recipe (
        var userID: String = "",
        var photo: String? = null,
        var title: String = "",
        var prepTime: String = "",
        var ingredientList: ArrayList<String>,
        var details: String = "",
        var cuisineType: String = "",
        var longitude: Double = 0.0,
        var latitude: Double = 0.0
)
