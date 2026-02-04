package com.example.smartagro.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagro.R
import com.example.smartagro.data.repository.AgroRepository
import com.example.smartagro.domain.model.SensorCardData
import com.example.smartagro.domain.model.SensorSnapshot
import com.example.smartagro.domain.model.SensorStatus
import com.example.smartagro.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: AgroRepository,
    private var farmId: String = Constants.DEFAULT_FARM_ID
) : ViewModel() {
    
    private val TAG = "DashboardViewModel"
    private var observeJob: Job? = null
    
    private val _sensorSnapshot = MutableStateFlow<SensorSnapshot?>(null)
    val sensorSnapshot: StateFlow<SensorSnapshot?> = _sensorSnapshot.asStateFlow()
    
    private val _loading = MutableLiveData<Boolean>(true)
    val loading: LiveData<Boolean> = _loading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _sensorCards = MutableStateFlow<List<SensorCardData>>(emptyList())
    val sensorCards: StateFlow<List<SensorCardData>> = _sensorCards.asStateFlow()
    
    private val _farmName = MutableLiveData<String>(farmId)
    val farmName: LiveData<String> = _farmName
    
    init {
        observeSensors()
    }
    
    private fun observeSensors() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _loading.postValue(true)
            _errorMessage.postValue(null)
            
            repository.observeSensors(farmId)
                .catch { e ->
                    Log.e(TAG, "Error observing sensors", e)
                    _errorMessage.postValue(e.message ?: "Error fetching sensor data")
                    _loading.postValue(false)
                }
                .collect { snapshot ->
                    Log.d(TAG, "Sensor snapshot received: $snapshot")
                    _sensorSnapshot.value = snapshot
                    
                    if (hasValidData(snapshot)) {
                        val cards = convertToSensorCards(snapshot)
                        _sensorCards.value = cards
                        _errorMessage.postValue(null)
                    } else {
                        _sensorCards.value = emptyList()
                    }
                    
                    _loading.postValue(false)
                }
        }
    }
    
    private fun hasValidData(snapshot: SensorSnapshot): Boolean {
        return snapshot.updatedAt > 0
    }
    
    private fun convertToSensorCards(snapshot: SensorSnapshot): List<SensorCardData> {
        return listOf(
            SensorCardData(
                "Soil Moisture",
                snapshot.soilMoisturePercent,
                "%",
                getSoilMoistureStatus(snapshot.soilMoisturePercent),
                R.drawable.ic_soil_moisture
            ),
            SensorCardData(
                "Soil Temperature",
                snapshot.soilTempC,
                "°C",
                getAirTempStatus(snapshot.soilTempC),
                R.drawable.ic_temperature
            ),
            SensorCardData(
                "Air Temperature",
                snapshot.airTempC,
                "°C",
                getAirTempStatus(snapshot.airTempC),
                R.drawable.ic_temperature
            ),
            SensorCardData(
                "Humidity",
                snapshot.humidityPercent,
                "%",
                getHumidityStatus(snapshot.humidityPercent),
                R.drawable.ic_humidity
            ),
            SensorCardData(
                "Rain Level",
                snapshot.rainLevelPercent,
                "%",
                getRainLevelStatus(snapshot.rainLevelPercent),
                R.drawable.ic_rain
            ),
            SensorCardData(
                "CO2 Level",
                snapshot.gasPpm,
                "ppm",
                getCO2Status(snapshot.gasPpm),
                R.drawable.ic_gas
            ),
            SensorCardData(
                "Light",
                snapshot.lightPercent,
                "%",
                getLightStatus(snapshot.lightPercent),
                R.drawable.ic_light
            )
        )
    }
    
    private fun getSoilMoistureStatus(value: Double): SensorStatus {
        return when {
            value < 30.0 -> SensorStatus.LOW
            value > 70.0 -> SensorStatus.HIGH
            else -> SensorStatus.NORMAL
        }
    }
    
    private fun getAirTempStatus(value: Double): SensorStatus {
        return when {
            value < 20.0 -> SensorStatus.LOW
            value > 32.0 -> SensorStatus.HIGH
            else -> SensorStatus.NORMAL
        }
    }
    
    private fun getHumidityStatus(value: Double): SensorStatus {
        return when {
            value < 40.0 -> SensorStatus.LOW
            value > 75.0 -> SensorStatus.HIGH
            else -> SensorStatus.NORMAL
        }
    }
    
    private fun getRainLevelStatus(value: Double): SensorStatus {
        return when {
            value < 10.0 -> SensorStatus.LOW
            value > 50.0 -> SensorStatus.HIGH
            else -> SensorStatus.NORMAL
        }
    }
    
    private fun getCO2Status(value: Double): SensorStatus {
        return when {
            value < 350.0 -> SensorStatus.LOW
            value > 1000.0 -> SensorStatus.HIGH
            else -> SensorStatus.NORMAL
        }
    }
    
    private fun getLightStatus(value: Double): SensorStatus {
        return when {
            value < 30.0 -> SensorStatus.LOW
            value > 80.0 -> SensorStatus.HIGH
            else -> SensorStatus.NORMAL
        }
    }
    
    fun refreshData() {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val snapshot = repository.getSensors(farmId)
                _sensorSnapshot.value = snapshot
                
                if (hasValidData(snapshot)) {
                    val cards = convertToSensorCards(snapshot)
                    _sensorCards.value = cards
                }
                
                _errorMessage.postValue(null)
                Log.d(TAG, "Data refreshed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
                _errorMessage.postValue(e.message ?: "Error refreshing data")
            } finally {
                _loading.postValue(false)
            }
        }
    }
    
    fun updateFarmId(newFarmId: String) {
        if (newFarmId != farmId) {
            farmId = newFarmId
            _farmName.postValue(newFarmId)
            observeSensors()
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
