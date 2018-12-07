package com.example.tender.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
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
) : Parcelable

