package com.example.tender.models

import android.net.Uri

data class User(
        var userID: String = "",
        var firstName: String = "",
        var lastName: String = "",
        var city: String = "",
        var profilePhotoPath: String = "",
        var isOnline: Boolean = false,
        var backgroundPhotoPath: String? = null,
        var bio: String = "",
        var posts: Int = 0,
        var faveCuisine: String = "",
        var tenderScore: Double = 0.0,
        var latitude: Double = 0.0,
        var longitude: Double = 0.0)

