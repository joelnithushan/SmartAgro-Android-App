package com.example.smartagro.data.firebase

import com.example.smartagro.domain.model.IrrigationStatus
import com.example.smartagro.domain.model.SensorData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseDataSource {
    private val database = FirebaseDatabase.getInstance()
    
    private val sensorsRef = database.getReference("sensors")
    private val irrigationRef = database.getReference("irrigation")
    
    fun observeSensorData(): Flow<SensorData> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorData = snapshot.getValue(SensorData::class.java) 
                    ?: SensorData()
                trySend(sensorData)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        sensorsRef.addValueEventListener(listener)
        
        awaitClose {
            sensorsRef.removeEventListener(listener)
        }
    }
    
    suspend fun getSensorData(): SensorData {
        val snapshot = sensorsRef.get().await()
        return snapshot.getValue(SensorData::class.java) ?: SensorData()
    }
    
    fun observeIrrigationStatus(): Flow<IrrigationStatus> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(IrrigationStatus::class.java) 
                    ?: IrrigationStatus()
                trySend(status)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        irrigationRef.addValueEventListener(listener)
        
        awaitClose {
            irrigationRef.removeEventListener(listener)
        }
    }
    
    suspend fun getIrrigationStatus(): IrrigationStatus {
        val snapshot = irrigationRef.get().await()
        return snapshot.getValue(IrrigationStatus::class.java) ?: IrrigationStatus()
    }
    
    suspend fun togglePump(pumpId: Int, status: Boolean) {
        val pumpKey = if (pumpId == 1) "pump1Status" else "pump2Status"
        irrigationRef.child(pumpKey).setValue(status).await()
    }
    
    suspend fun setAutoMode(enabled: Boolean) {
        irrigationRef.child("autoMode").setValue(enabled).await()
        irrigationRef.child("manualMode").setValue(!enabled).await()
    }
    
    suspend fun setManualMode(enabled: Boolean) {
        irrigationRef.child("manualMode").setValue(enabled).await()
        irrigationRef.child("autoMode").setValue(!enabled).await()
    }
    
    suspend fun setThreshold(threshold: Double) {
        irrigationRef.child("threshold").setValue(threshold).await()
    }
    
    suspend fun setDuration(duration: Int) {
        irrigationRef.child("duration").setValue(duration).await()
    }
    
    suspend fun updateLastWatered() {
        irrigationRef.child("lastWatered").setValue(System.currentTimeMillis()).await()
    }
}
