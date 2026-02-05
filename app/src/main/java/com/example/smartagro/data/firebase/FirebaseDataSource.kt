package com.example.smartagro.data.firebase

import android.util.Log
import com.example.smartagro.domain.model.IrrigationState
import com.example.smartagro.domain.model.SensorSnapshot
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseDataSource {
    private fun getDatabase() = FirebaseProvider.rtdb
    private val TAG = "FirebaseDataSource"
    
    fun observeSensors(farmId: String): Flow<SensorSnapshot> = callbackFlow {
        val database = getDatabase()
        if (database == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Returning empty sensor data.")
            trySend(SensorSnapshot())
            close()
            return@callbackFlow
        }

        val path = FirebasePaths.sensorsPath(farmId)
        val sensorsRef = database.getReference(path)
        
        Log.d(TAG, "Observing sensors at path: $path")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val sensorData = snapshot.getValue(SensorSnapshot::class.java)
                    val result = sensorData ?: SensorSnapshot()
                    Log.d(TAG, "Sensor data received: $result")
                    trySend(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing sensor data", e)
                    trySend(SensorSnapshot())
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Sensor listener cancelled: ${error.message}")
                close(error.toException())
            }
        }
        
        sensorsRef.addValueEventListener(listener)
        
        awaitClose {
            Log.d(TAG, "Removing sensor listener from path: $path")
            sensorsRef.removeEventListener(listener)
        }
    }
    
    suspend fun getSensors(farmId: String): SensorSnapshot {
        val database = getDatabase()
        if (database == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Returning empty sensor data.")
            return SensorSnapshot()
        }

        val path = FirebasePaths.sensorsPath(farmId)
        val sensorsRef = database.getReference(path)
        
        Log.d(TAG, "Getting sensors from path: $path")
        
        return try {
            val snapshot = sensorsRef.get().await()
            val sensorData = snapshot.getValue(SensorSnapshot::class.java)
            val result = sensorData ?: SensorSnapshot()
            Log.d(TAG, "Sensor data fetched: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sensor data", e)
            SensorSnapshot()
        }
    }
    
    fun observeIrrigation(farmId: String): Flow<IrrigationState> = callbackFlow {
        val database = getDatabase()
        if (database == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Returning empty irrigation state.")
            trySend(IrrigationState())
            close()
            return@callbackFlow
        }

        val path = FirebasePaths.irrigationPath(farmId)
        val irrigationRef = database.getReference(path)
        
        Log.d(TAG, "Observing irrigation at path: $path")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val irrigationData = snapshot.getValue(IrrigationState::class.java)
                    val result = irrigationData ?: IrrigationState()
                    Log.d(TAG, "Irrigation data received: $result")
                    trySend(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing irrigation data", e)
                    trySend(IrrigationState())
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Irrigation listener cancelled: ${error.message}")
                close(error.toException())
            }
        }
        
        irrigationRef.addValueEventListener(listener)
        
        awaitClose {
            Log.d(TAG, "Removing irrigation listener from path: $path")
            irrigationRef.removeEventListener(listener)
        }
    }
    
    suspend fun getIrrigation(farmId: String): IrrigationState {
        val database = getDatabase()
        if (database == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Returning empty irrigation state.")
            return IrrigationState()
        }

        val path = FirebasePaths.irrigationPath(farmId)
        val irrigationRef = database.getReference(path)
        
        Log.d(TAG, "Getting irrigation from path: $path")
        
        return try {
            val snapshot = irrigationRef.get().await()
            val irrigationData = snapshot.getValue(IrrigationState::class.java)
            val result = irrigationData ?: IrrigationState()
            Log.d(TAG, "Irrigation data fetched: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching irrigation data", e)
            IrrigationState()
        }
    }
    
    suspend fun setIrrigationOn(farmId: String, on: Boolean, changedBy: String) {
        val database = getDatabase()
        if (database == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Cannot set irrigation state.")
            throw IllegalStateException("Firebase RTDB is not initialized. Please configure google-services.json")
        }

        val path = FirebasePaths.irrigationPath(farmId)
        val irrigationRef = database.getReference(path)
        
        Log.d(TAG, "Setting irrigation ON=$on for farmId=$farmId, changedBy=$changedBy")
        
        try {
            val updates = mapOf(
                "isOn" to on,
                "lastChangedAt" to System.currentTimeMillis(),
                "lastChangedBy" to changedBy
            )
            irrigationRef.updateChildren(updates).await()
            Log.d(TAG, "Irrigation state updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating irrigation state", e)
            throw e
        }
    }
    
    suspend fun setMode(farmId: String, mode: String) {
        val database = getDatabase()
        if (database == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Cannot set mode.")
            throw IllegalStateException("Firebase RTDB is not initialized. Please configure google-services.json")
        }

        val path = FirebasePaths.irrigationPath(farmId)
        val irrigationRef = database.getReference(path)
        
        Log.d(TAG, "Setting mode=$mode for farmId=$farmId")
        
        try {
            irrigationRef.child("mode").setValue(mode).await()
            Log.d(TAG, "Mode updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating mode", e)
            throw e
        }
    }
    
    suspend fun setThreshold(farmId: String, value: Double) {
        val database = getDatabase()
        if (database == null) {
            Log.w(TAG, "Firebase RTDB not initialized. Cannot set threshold.")
            throw IllegalStateException("Firebase RTDB is not initialized. Please configure google-services.json")
        }

        val path = FirebasePaths.irrigationPath(farmId)
        val irrigationRef = database.getReference(path)
        
        Log.d(TAG, "Setting threshold=$value for farmId=$farmId")
        
        try {
            irrigationRef.child("moistureThreshold").setValue(value).await()
            Log.d(TAG, "Threshold updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating threshold", e)
            throw e
        }
    }
}
