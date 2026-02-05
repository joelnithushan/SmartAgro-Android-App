package com.example.smartagro.data.firebase

import android.util.Log
import com.example.smartagro.domain.model.rtdb.RtdbSensorLatest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RtdbRepository {
    private val TAG = "RtdbRepository"

    private fun getDatabase(): FirebaseDatabase? = FirebaseProvider.rtdb

    fun observeSensorsLatest(deviceId: String): Flow<RtdbSensorLatest> = callbackFlow {
        val db = getDatabase()
        if (db == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Returning empty sensor data.")
            trySend(RtdbSensorLatest())
            close()
            return@callbackFlow
        }

        val path = DeviceRtdbPaths.sensorsLatest(deviceId)
        val ref = db.getReference(path)

        Log.d(TAG, "Observing sensors latest at path: $path")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) {
                        Log.w(TAG, "No data at path: $path (snapshot does not exist)")
                        trySend(RtdbSensorLatest())
                        return
                    }
                    
                    val rawValue = snapshot.value
                    Log.d(TAG, "Data received at $path: $rawValue")
                    
                    val sensorData = snapshot.getValue(RtdbSensorLatest::class.java)
                    if (sensorData != null) {
                        Log.d(TAG, "Parsed sensor data - soilMoisture: ${sensorData.soilMoisturePct}%, airTemp: ${sensorData.airTemperature}°C, timestamp: ${sensorData.timestamp}")
                        trySend(sensorData)
                    } else {
                        Log.w(TAG, "Failed to parse sensor data from snapshot")
                        trySend(RtdbSensorLatest())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing sensor latest data", e)
                    trySend(RtdbSensorLatest())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Sensor latest listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            Log.d(TAG, "Removing sensor latest listener from path: $path")
            ref.removeEventListener(listener)
        }
    }

    fun observeLastSeen(deviceId: String): Flow<Long?> = callbackFlow {
        val db = getDatabase()
        if (db == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Returning null lastSeen.")
            trySend(null)
            close()
            return@callbackFlow
        }

        val path = DeviceRtdbPaths.metaLastSeen(deviceId)
        val ref = db.getReference(path)

        Log.d(TAG, "Observing lastSeen at path: $path")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) {
                        Log.w(TAG, "No data at path: $path (snapshot does not exist)")
                        trySend(null)
                        return
                    }
                    
                    val lastSeen = snapshot.getValue(Long::class.java)
                    Log.d(TAG, "LastSeen received: $lastSeen at path: $path")
                    trySend(lastSeen)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing lastSeen", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "LastSeen listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            Log.d(TAG, "Removing lastSeen listener from path: $path")
            ref.removeEventListener(listener)
        }
    }

    fun observeRelayStatus(deviceId: String): Flow<String?> = callbackFlow {
        val db = getDatabase()
        if (db == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Returning null relayStatus.")
            trySend(null)
            close()
            return@callbackFlow
        }

        val path = DeviceRtdbPaths.controlsRelayStatus(deviceId)
        val ref = db.getReference(path)

        Log.d(TAG, "Observing relay status at path: $path")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val status = snapshot.getValue(String::class.java)
                    trySend(status)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing relay status", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Relay status listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)

        awaitClose {
            Log.d(TAG, "Removing relay status listener from path: $path")
            ref.removeEventListener(listener)
        }
    }

    suspend fun setRelayCommand(deviceId: String, command: String) {
        val db = getDatabase()
        if (db == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Cannot write relay command.")
            throw IllegalStateException("Firebase RTDB is not initialized. Please configure google-services.json")
        }

        val path = DeviceRtdbPaths.controlsRelayCommand(deviceId)
        val ref = db.getReference(path)

        Log.d(TAG, "Writing relay command=$command to path: $path")

        try {
            ref.setValue(command).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error writing relay command", e)
            throw e
        }
    }
}
