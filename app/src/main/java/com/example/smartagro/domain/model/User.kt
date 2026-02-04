package com.example.smartagro.domain.model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val location: String = "",
    val nic: String = "",
    val farmId: String = ""
)
