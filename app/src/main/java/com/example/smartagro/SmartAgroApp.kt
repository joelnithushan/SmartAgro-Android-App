package com.example.smartagro

import android.app.Application
import com.example.smartagro.data.firebase.FirebaseProvider
import com.example.smartagro.utils.DeviceConfig

class SmartAgroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseProvider.init(this)
        DeviceConfig.init(this)
    }
}

