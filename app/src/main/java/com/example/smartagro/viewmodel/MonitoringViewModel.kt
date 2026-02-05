package com.example.smartagro.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagro.R
import com.example.smartagro.data.firebase.RtdbRepository
import com.example.smartagro.domain.model.ChartData
import com.example.smartagro.domain.model.SensorCardData
import com.example.smartagro.domain.model.SensorStatus
import com.example.smartagro.domain.model.rtdb.RtdbSensorLatest
import com.example.smartagro.utils.DeviceConfig
import com.example.smartagro.utils.IconMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MonitoringViewModel(
    private val rtdbRepository: RtdbRepository = RtdbRepository()
) : ViewModel() {

    private val TAG = "MonitoringViewModel"

    private val _loading = MutableLiveData<Boolean>(true)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Initialize with offline cards immediately so they show right away
    private val _sensorCards = MutableStateFlow<List<SensorCardData>>(
        createOfflineCards()
    )
    val sensorCards: StateFlow<List<SensorCardData>> = _sensorCards.asStateFlow()

    private val _lastSeen = MutableStateFlow<Long?>(null)
    val lastSeen: StateFlow<Long?> = _lastSeen.asStateFlow()

    private val _online = MutableStateFlow(false)
    val online: StateFlow<Boolean> = _online.asStateFlow()

    private val _lastReceivedTime = MutableStateFlow<Long?>(null)
    val lastReceivedTime: StateFlow<Long?> = _lastReceivedTime.asStateFlow()

    private val _deviceUptime = MutableStateFlow<Long?>(null)
    val deviceUptime: StateFlow<Long?> = _deviceUptime.asStateFlow()

    // Rolling buffer for chart data (last 60 samples)
    private val MAX_CHART_SAMPLES = 60
    private val tempHumidityBuffer = mutableListOf<Pair<Long, Pair<Float, Float>>>() // (timestamp, (temp, humidity))
    private val soilAqiBuffer = mutableListOf<Pair<Long, Pair<Float, Float>>>() // (timestamp, (soilMoisture, aqi))
    
    private val _chartData = MutableStateFlow<ChartData>(
        ChartData(emptyList(), emptyList())
    )
    val chartData: StateFlow<ChartData> = _chartData.asStateFlow()

    private var lastHeartbeatAt: Long? = null

    init {
        viewModelScope.launch {
            _loading.postValue(true)
            _errorMessage.postValue(null)

            DeviceConfig.deviceIdFlow
                .flatMapLatest { deviceId ->
                    combine(
                        rtdbRepository.observeSensorsLatest(deviceId),
                        rtdbRepository.observeLastSeen(deviceId)
                    ) { latest, lastSeen ->
                        Pair(latest, lastSeen)
                    }
                }
                .catch { e ->
                    Log.e(TAG, "Error observing monitoring streams", e)
                    _errorMessage.postValue(e.message ?: "Error loading monitoring data")
                    _loading.postValue(false)
                }
                .collect { (latest, lastSeen) ->
                    val now = System.currentTimeMillis()
                    
                    if (lastSeen != null) {
                        _lastSeen.value = lastSeen
                        lastHeartbeatAt = now
                        // Device uptime is the lastSeen value (millis from ESP32)
                        _deviceUptime.value = lastSeen
                    }
                    
                    // Track when we received the data
                    if (latest.timestamp != null && latest.timestamp!! > 0L) {
                        _lastReceivedTime.value = now
                        
                        // Update chart buffers
                        updateChartBuffers(latest, now)
                    }
                    
                    // Always show sensor cards, even when offline
                    val isOnline = latest.timestamp != null && latest.timestamp!! > 0L
                    _sensorCards.value = convertToSensorCards(latest, isOnline)
                    _loading.postValue(false)
                }
        }

        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val last = lastHeartbeatAt
                _online.value = last != null && now - last <= 30_000L
                kotlinx.coroutines.delay(5_000L)
            }
        }
    }

    private fun createOfflineCards(): List<SensorCardData> {
        return listOf(
            SensorCardData("Air Temp", 0.0, "°C", SensorStatus.NORMAL, IconMapper.getIconResId("Air Temp"), extra = "Offline"),
            SensorCardData("Humidity", 0.0, "%", SensorStatus.NORMAL, IconMapper.getIconResId("Humidity"), extra = "Offline"),
            SensorCardData("Soil Moisture", 0.0, "%", SensorStatus.NORMAL, IconMapper.getIconResId("Soil Moisture"), extra = "Offline"),
            SensorCardData("Soil Moist Raw", 0.0, "", SensorStatus.NORMAL, IconMapper.getIconResId("Soil Moist Raw"), extra = "Offline"),
            SensorCardData("Soil Temp", 0.0, "°C", SensorStatus.NORMAL, IconMapper.getIconResId("Soil Temp"), extra = "Offline"),
            SensorCardData("AQI", 0.0, "", SensorStatus.NORMAL, IconMapper.getIconResId("AQI"), extra = "Offline"),
            SensorCardData("CO2", 0.0, "ppm", SensorStatus.NORMAL, IconMapper.getIconResId("CO2"), extra = "Offline"),
            SensorCardData("NH3", 0.0, "ppm", SensorStatus.NORMAL, IconMapper.getIconResId("NH3"), extra = "Offline"),
            SensorCardData("Light", 0.0, "", SensorStatus.NORMAL, IconMapper.getIconResId("Light"), extra = "Offline"),
            SensorCardData("Rain", 0.0, "", SensorStatus.NORMAL, IconMapper.getIconResId("Rain"), extra = "Offline"),
            SensorCardData("Relay", 0.0, "", SensorStatus.NORMAL, IconMapper.getIconResId("Relay"), extra = "Offline")
        )
    }

    private fun convertToSensorCards(latest: RtdbSensorLatest, isOnline: Boolean): List<SensorCardData> {
        if (!isOnline) {
            return createOfflineCards()
        }

        // Extract all sensor values
        val airTemp = latest.airTemperature ?: 0.0
        val humidity = latest.airHumidity ?: 0.0
        val soilMoisturePct = (latest.soilMoisturePct ?: 0).toDouble()
        val soilMoistureRaw = (latest.soilMoistureRaw ?: 0).toDouble()
        val soilTemp = latest.soilTemperature ?: 0.0
        val airQualityIndex = (latest.airQualityIndex ?: 0).toDouble()
        val co2Ppm = latest.getCO2Ppm().toDouble()
        val nh3Ppm = latest.getNH3Ppm().toDouble()
        val lightDetected = latest.lightDetected ?: 0
        val rainStatus = latest.rainStatus ?: "Unknown"
        val rainLevelRaw = (latest.rainLevelRaw ?: 0).toDouble()
        val relayStatus = latest.relayStatus ?: "off"

        // Status calculations
        val airTempStatus = getStatus(airTemp, 20.0, 32.0)
        val humidityStatus = getStatus(humidity, 40.0, 75.0)
        val soilMoistureStatus = getStatus(soilMoisturePct, 30.0, 70.0)
        val soilTempStatus = getStatus(soilTemp, 20.0, 32.0)
        
        // AQI: Normal < 300, Poor >= 300
        val aqiStatus = if (airQualityIndex < 300) SensorStatus.NORMAL else SensorStatus.HIGH
        
        // CO2: Normal < 1000, High >= 1000
        val co2Status = if (co2Ppm < 1000) SensorStatus.NORMAL else SensorStatus.HIGH
        
        // NH3: Normal < 25, High >= 25
        val nh3Status = if (nh3Ppm < 25) SensorStatus.NORMAL else SensorStatus.HIGH
        
        // Rain: "Rain Detected" -> High, else Normal
        val rainStatusEnum = if (rainStatus.contains("Rain", ignoreCase = true)) SensorStatus.HIGH else SensorStatus.NORMAL
        
        // Relay: ON/OFF (always Normal status, just show state)
        val relayStatusEnum = SensorStatus.NORMAL

        return listOf(
            SensorCardData(
                "Air Temp",
                airTemp,
                "°C",
                airTempStatus,
                IconMapper.getIconResId("Air Temp"),
                extra = null
            ),
            SensorCardData(
                "Humidity",
                humidity,
                "%",
                humidityStatus,
                IconMapper.getIconResId("Humidity"),
                extra = null
            ),
            SensorCardData(
                "Soil Moisture",
                soilMoisturePct,
                "%",
                soilMoistureStatus,
                IconMapper.getIconResId("Soil Moisture"),
                extra = null
            ),
            SensorCardData(
                "Soil Moist Raw",
                soilMoistureRaw,
                "",
                SensorStatus.NORMAL,
                IconMapper.getIconResId("Soil Moist Raw"),
                extra = null
            ),
            SensorCardData(
                "Soil Temp",
                soilTemp,
                "°C",
                soilTempStatus,
                IconMapper.getIconResId("Soil Temp"),
                extra = null
            ),
            SensorCardData(
                "AQI",
                airQualityIndex,
                "",
                aqiStatus,
                IconMapper.getIconResId("AQI"),
                extra = if (aqiStatus == SensorStatus.HIGH) "Poor" else "Normal"
            ),
            SensorCardData(
                "CO2",
                co2Ppm,
                "ppm",
                co2Status,
                IconMapper.getIconResId("CO2"),
                extra = null
            ),
            SensorCardData(
                "NH3",
                nh3Ppm,
                "ppm",
                nh3Status,
                IconMapper.getIconResId("NH3"),
                extra = null
            ),
            SensorCardData(
                "Light",
                if (lightDetected == 1) 1.0 else 0.0,
                "",
                SensorStatus.NORMAL,
                IconMapper.getIconResId("Light"),
                extra = if (lightDetected == 1) "Detected" else "Dark"
            ),
            SensorCardData(
                "Rain",
                rainLevelRaw,
                "",
                rainStatusEnum,
                IconMapper.getIconResId("Rain"),
                extra = rainStatus
            ),
            SensorCardData(
                "Relay",
                if (relayStatus == "on") 1.0 else 0.0,
                "",
                relayStatusEnum,
                IconMapper.getIconResId("Relay"),
                extra = relayStatus.uppercase()
            )
        )
    }

    private fun getStatus(value: Double, lowThreshold: Double, highThreshold: Double): SensorStatus {
        return when {
            value < lowThreshold -> SensorStatus.LOW
            value > highThreshold -> SensorStatus.HIGH
            else -> SensorStatus.NORMAL
        }
    }

    private fun updateChartBuffers(latest: RtdbSensorLatest, timestamp: Long) {
        // Add to temp/humidity buffer
        val temp = latest.airTemperature?.toFloat() ?: 0f
        val humidity = latest.airHumidity?.toFloat() ?: 0f
        tempHumidityBuffer.add(Pair(timestamp, Pair(temp, humidity)))
        
        // Keep only last MAX_CHART_SAMPLES
        if (tempHumidityBuffer.size > MAX_CHART_SAMPLES) {
            tempHumidityBuffer.removeAt(0)
        }

        // Add to soil/AQI buffer
        val soilMoisture = (latest.soilMoisturePct ?: 0).toFloat()
        val aqi = (latest.airQualityIndex ?: 0).toFloat()
        soilAqiBuffer.add(Pair(timestamp, Pair(soilMoisture, aqi)))
        
        // Keep only last MAX_CHART_SAMPLES
        if (soilAqiBuffer.size > MAX_CHART_SAMPLES) {
            soilAqiBuffer.removeAt(0)
        }

        // Convert to chart data format (timeIndex, values)
        val tempHumidityData = tempHumidityBuffer.mapIndexed { index, (_, values) ->
            Pair(index.toFloat(), values)
        }
        
        val soilAqiData = soilAqiBuffer.mapIndexed { index, (_, values) ->
            Pair(index.toFloat(), values)
        }

        _chartData.value = ChartData(tempHumidityData, soilAqiData)
    }
}

