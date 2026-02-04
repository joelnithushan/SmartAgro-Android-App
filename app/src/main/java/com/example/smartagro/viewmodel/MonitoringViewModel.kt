package com.example.smartagro.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagro.data.firebase.RtdbRepository
import com.example.smartagro.domain.model.SensorCardData
import com.example.smartagro.domain.model.SensorStatus
import com.example.smartagro.domain.model.rtdb.RelayCommand
import com.example.smartagro.domain.model.rtdb.RtdbSensorLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MonitoringViewModel(
    private val rtdbRepository: RtdbRepository
) : ViewModel() {

    private val TAG = "MonitoringViewModel"

    private val _loading = MutableLiveData<Boolean>(true)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sensorCards = MutableStateFlow<List<SensorCardData>>(emptyList())
    val sensorCards: StateFlow<List<SensorCardData>> = _sensorCards.asStateFlow()

    private val _lastSeen = MutableStateFlow<Long?>(null)
    val lastSeen: StateFlow<Long?> = _lastSeen.asStateFlow()

    private var observeJob: Job? = null

    fun observe(deviceIdFlow: StateFlow<String?>) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            deviceIdFlow
                .filterNotNull()
                .collect { deviceId ->
                    subscribe(deviceId)
                }
        }
    }

    private fun subscribe(deviceId: String) {
        viewModelScope.launch {
            _loading.postValue(true)
            _errorMessage.postValue(null)

            combine(
                rtdbRepository.observeSensorsLatest(deviceId),
                rtdbRepository.observeLastSeen(deviceId)
            ) { latest, lastSeen ->
                Pair(latest, lastSeen)
            }
                .catch { e ->
                    Log.e(TAG, "Error observing monitoring streams", e)
                    _errorMessage.postValue(e.message ?: "Error loading monitoring data")
                    _loading.postValue(false)
                }
                .collect { (latest, lastSeen) ->
                    _lastSeen.value = lastSeen
                    val snapshot = latest.toSnapshotDefaults()
                    if (snapshot.updatedAt > 0L) {
                        _sensorCards.value = convertToSensorCards(snapshot)
                    } else {
                        _sensorCards.value = emptyList()
                    }
                    _loading.postValue(false)
                }
        }
    }

    private fun convertToSensorCards(snapshot: com.example.smartagro.domain.model.SensorSnapshot): List<SensorCardData> {
        return listOf(
            SensorCardData("Soil Moisture", snapshot.soilMoisturePercent, "%", getStatus(snapshot.soilMoisturePercent, 30.0, 70.0), com.example.smartagro.R.drawable.ic_soil_moisture),
            SensorCardData("Soil Temperature", snapshot.soilTempC, "°C", getStatus(snapshot.soilTempC, 20.0, 32.0), com.example.smartagro.R.drawable.ic_temperature),
            SensorCardData("Air Temperature", snapshot.airTempC, "°C", getStatus(snapshot.airTempC, 20.0, 32.0), com.example.smartagro.R.drawable.ic_temperature),
            SensorCardData("Humidity", snapshot.humidityPercent, "%", getStatus(snapshot.humidityPercent, 40.0, 75.0), com.example.smartagro.R.drawable.ic_humidity),
            SensorCardData("Rain Level", snapshot.rainLevelPercent, "%", getStatus(snapshot.rainLevelPercent, 10.0, 50.0), com.example.smartagro.R.drawable.ic_rain),
            SensorCardData("CO2 Level", snapshot.gasPpm, "ppm", getStatus(snapshot.gasPpm, 350.0, 1000.0), com.example.smartagro.R.drawable.ic_gas),
            SensorCardData("Light", snapshot.lightPercent, "%", getStatus(snapshot.lightPercent, 30.0, 80.0), com.example.smartagro.R.drawable.ic_light)
        )
    }

    private fun getStatus(value: Double, lowThreshold: Double, highThreshold: Double): SensorStatus {
        return when {
            value < lowThreshold -> SensorStatus.LOW
            value > highThreshold -> SensorStatus.HIGH
            else -> SensorStatus.NORMAL
        }
    }
}

