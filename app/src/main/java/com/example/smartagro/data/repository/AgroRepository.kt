package com.example.smartagro.data.repository

import com.example.smartagro.data.firebase.FirebaseDataSource
import com.example.smartagro.domain.model.IrrigationStatus
import com.example.smartagro.domain.model.SensorData
import kotlinx.coroutines.flow.Flow

class AgroRepository(
    private val firebaseDataSource: FirebaseDataSource
) {
    fun observeSensorData(): Flow<SensorData> = firebaseDataSource.observeSensorData()
    
    suspend fun getSensorData(): SensorData = firebaseDataSource.getSensorData()
    
    fun observeIrrigationStatus(): Flow<IrrigationStatus> = 
        firebaseDataSource.observeIrrigationStatus()
    
    suspend fun getIrrigationStatus(): IrrigationStatus = 
        firebaseDataSource.getIrrigationStatus()
    
    suspend fun togglePump(pumpId: Int, status: Boolean) {
        firebaseDataSource.togglePump(pumpId, status)
        if (status) {
            firebaseDataSource.updateLastWatered()
        }
    }
    
    suspend fun setAutoMode(enabled: Boolean) {
        firebaseDataSource.setAutoMode(enabled)
    }
    
    suspend fun setManualMode(enabled: Boolean) {
        firebaseDataSource.setManualMode(enabled)
    }
    
    suspend fun setThreshold(threshold: Double) {
        firebaseDataSource.setThreshold(threshold)
    }
    
    suspend fun setDuration(duration: Int) {
        firebaseDataSource.setDuration(duration)
    }
}
