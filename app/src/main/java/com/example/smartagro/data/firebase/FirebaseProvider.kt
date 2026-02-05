package com.example.smartagro.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseProvider {

    private const val TAG = "FirebaseProvider"

    @Volatile
    private var initialized = false

    @Volatile
    private var initFailed = false

    @Volatile
    private var firebaseApp: FirebaseApp? = null

    fun init(context: Context) {
        if (initialized || initFailed) return
        synchronized(this) {
            if (initialized || initFailed) return
            try {
                val app = try {
                    FirebaseApp.getInstance()
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "Default FirebaseApp not found. Initializing manually...")
                    val options = FirebaseOptions.Builder()
                        .setProjectId("smartagro-solution")
                        .setApplicationId("1:109717618865:android:78930aa68a7760bc8ce290")
                        .setApiKey("AIzaSyBTZGPwz_WciBdwFreCJ30QUZlAwtgg3TY")
                        .setDatabaseUrl("https://smartagro-solution-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .setStorageBucket("smartagro-solution.firebasestorage.app")
                        .build()
                    
                    FirebaseApp.initializeApp(context.applicationContext, options)
                }
                
                firebaseApp = app
                initialized = true
                Log.d(TAG, "Firebase initialized successfully")
                Log.d(TAG, "FirebaseApp instance: ${app.name}, projectId: ${app.options.projectId}")
                
                val testRtdb = rtdb
                if (testRtdb != null) {
                    Log.d(TAG, "RTDB instance verified during initialization")
                } else {
                    Log.w(TAG, "RTDB instance is null after initialization")
                }
            } catch (e: Exception) {
                initFailed = true
                Log.e(TAG, "Failed to initialize Firebase. App will continue without Firebase features.", e)
                e.printStackTrace()
            }
        }
    }

    val auth: FirebaseAuth? by lazy {
        if (!initialized) return@lazy null
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FirebaseAuth instance", e)
            null
        }
    }

    val firestore: FirebaseFirestore? by lazy {
        if (!initialized) return@lazy null
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firestore instance", e)
            null
        }
    }

    @Volatile
    private var rtdbInstance: FirebaseDatabase? = null

    val rtdb: FirebaseDatabase?
        get() {
            if (!initialized) {
                Log.w(TAG, "Firebase not initialized yet, RTDB will be null")
                return null
            }
            if (rtdbInstance != null) {
                return rtdbInstance
            }
            synchronized(this) {
                if (rtdbInstance != null) {
                    return rtdbInstance
                }
                try {
                    val app = firebaseApp ?: try {
                        FirebaseApp.getInstance()
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "FirebaseApp not available. Error: ${e.message}")
                        return null
                    }
                    
                    Log.d(TAG, "FirebaseApp found: ${app.name}, projectId: ${app.options.projectId}")
                    
                    val dbUrl = "https://smartagro-solution-default-rtdb.asia-southeast1.firebasedatabase.app"
                    Log.d(TAG, "Getting RTDB instance with URL: $dbUrl")
                    val db = FirebaseDatabase.getInstance(app, dbUrl)
                    db.setPersistenceEnabled(true)
                    Log.d(TAG, "RTDB instance created successfully")
                    Log.d(TAG, "Database root reference: ${db.reference}")
                    rtdbInstance = db
                    return db
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "FirebaseApp not initialized. Error: ${e.message}")
                    return null
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get RTDB instance", e)
                    e.printStackTrace()
                    return null
                }
            }
        }
}

