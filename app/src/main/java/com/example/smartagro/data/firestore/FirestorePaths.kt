package com.example.smartagro.data.firestore

object FirestorePaths {
    const val USERS = "users"
    const val DEVICE_REQUESTS = "deviceRequests"

    fun userDoc(uid: String) = "$USERS/$uid"
}

