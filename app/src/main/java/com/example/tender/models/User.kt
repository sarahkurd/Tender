package com.example.tender.models

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val city: String = "",
    val profilePhoto: String = "",
    val bio: String = "",
    val tenderScore: Double = 0.0
)
