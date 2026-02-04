package com.example.smartagro

import android.app.Application
import com.example.smartagro.data.firebase.FirebaseProvider
import com.google.firebase.database.FirebaseDatabase

class SmartAgroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseProvider.init(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}

