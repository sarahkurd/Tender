package com.example.tender.models

import android.net.Uri

class Business {

    // get this data from Zomato JSON object
    data class BusinessInfo(
            var name: String = "",
            var url: String = "",
            var address: String = "",
            var latitude: Double = 0.0,
            var longitude: Double = 0.0,
            var featured_image: Uri?= null,
            var aggregate_rating: Double = 0.0,
            var cuisines: String = "",
            var phone_numbers: String = "",
            var photos_url: Uri ?= null
    )
}