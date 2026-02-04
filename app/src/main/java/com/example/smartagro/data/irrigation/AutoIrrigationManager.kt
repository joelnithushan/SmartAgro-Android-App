package com.example.smartagro.data.irrigation

import android.util.Log
import com.example.smartagro.data.repository.AgroRepository
import com.example.smartagro.domain.model.IrrigationState
import com.example.smartagro.domain.model.SensorSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AutoIrrigationManager(
    private val repository: AgroRepository,
    private val farmId: String,
    private val scope: CoroutineScope
) {
    private val TAG = "AutoIrrigationManager"
    private var debounceJob: Job? = null
    private var lastCheckedMoisture: Double? = null
    private var lastCheckedState: Boolean? = null
    private val DEBOUNCE_DELAY_MS = 5000L
    
    fun start(
        sensorsFlow: kotlinx.coroutines.flow.Flow<SensorSnapshot>,
        irrigationFlow: kotlinx.coroutines.flow.Flow<IrrigationState>
    ) {
        scope.launch {
            combine(sensorsFlow, irrigationFlow) { sensors, irrigation ->
                Pair(sensors, irrigation)
            }.collect { (sensors, irrigation) ->
                processAutoLogic(sensors, irrigation)
            }
        }
    }
    
    private suspend fun processAutoLogic(
        sensors: SensorSnapshot,
        irrigation: IrrigationState
    ) {
        if (irrigation.mode != "AUTO") {
            debounceJob?.cancel()
            debounceJob = null
            lastCheckedMoisture = null
            lastCheckedState = null
            return
        }
        
        val soilMoisture = sensors.soilMoisturePercent
        val threshold = irrigation.moistureThreshold
        val isCurrentlyOn = irrigation.isOn
        
        val shouldBeOn = soilMoisture < threshold
        
        if (soilMoisture == lastCheckedMoisture && isCurrentlyOn == lastCheckedState) {
            return
        }
        
        lastCheckedMoisture = soilMoisture
        lastCheckedState = isCurrentlyOn
        
        if (shouldBeOn != isCurrentlyOn) {
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(DEBOUNCE_DELAY_MS)
                
                val currentSensors = try {
                    repository.getSensors(farmId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting sensors for auto logic", e)
                    return@launch
                }
                
                val currentIrrigation = try {
                    repository.getIrrigation(farmId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting irrigation for auto logic", e)
                    return@launch
                }
                
                if (currentIrrigation.mode != "AUTO") {
                    Log.d(TAG, "Mode changed to MANUAL, aborting auto logic")
                    return@launch
                }
                
                val currentMoisture = currentSensors.soilMoisturePercent
                val currentThreshold = currentIrrigation.moistureThreshold
                val currentIsOn = currentIrrigation.isOn
                val shouldTurnOn = currentMoisture < currentThreshold
                
                if (shouldTurnOn != currentIsOn) {
                    try {
                        Log.d(TAG, "Auto irrigation: ${if (shouldTurnOn) "turning ON" else "turning OFF"} (moisture: $currentMoisture%, threshold: $currentThreshold%)")
                        repository.setIrrigationOn(farmId, shouldTurnOn, "AUTO")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting irrigation in auto mode", e)
                    }
                }
            }
        } else {
            debounceJob?.cancel()
            debounceJob = null
        }
    }
    
    fun stop() {
        debounceJob?.cancel()
        debounceJob = null
    }
}
