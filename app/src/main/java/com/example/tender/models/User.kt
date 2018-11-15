package com.example.tender.models

data class User(
        val userID: String = "",
        val firstName: String = "",
        val lastName: String = "",
        val city: String = "",
        val profilePhotoPath: String? = null,
        val isOnline: String = "True",
        val backgroundPhotoPath: String? = null,
        val bio: String = "",
        val posts: Int = 0,
        val faveCuisine: String = "",
        val tenderScore: Double = 0.0)

