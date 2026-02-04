package com.example.smartagro.data.repository

import com.example.smartagro.data.firebase.FirebaseDataSource
import com.example.smartagro.domain.model.IrrigationState
import com.example.smartagro.domain.model.SensorSnapshot
import kotlinx.coroutines.flow.Flow

class AgroRepository(
    private val firebaseDataSource: FirebaseDataSource
) {
    fun observeSensors(farmId: String): Flow<SensorSnapshot> {
        return firebaseDataSource.observeSensors(farmId)
    }
    
    suspend fun getSensors(farmId: String): SensorSnapshot {
        return firebaseDataSource.getSensors(farmId)
    }
    
    fun observeIrrigation(farmId: String): Flow<IrrigationState> {
        return firebaseDataSource.observeIrrigation(farmId)
    }
    
    suspend fun getIrrigation(farmId: String): IrrigationState {
        return firebaseDataSource.getIrrigation(farmId)
    }
    
    suspend fun setIrrigationOn(farmId: String, on: Boolean, changedBy: String) {
        firebaseDataSource.setIrrigationOn(farmId, on, changedBy)
    }
    
    suspend fun setMode(farmId: String, mode: String) {
        firebaseDataSource.setMode(farmId, mode)
    }
    
    suspend fun setThreshold(farmId: String, value: Double) {
        firebaseDataSource.setThreshold(farmId, value)
    }
}
