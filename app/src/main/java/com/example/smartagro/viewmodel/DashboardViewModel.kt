package com.example.smartagro.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagro.data.repository.AgroRepository
import com.example.smartagro.domain.model.SensorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: AgroRepository
) : ViewModel() {
    
    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        observeSensorData()
    }
    
    private fun observeSensorData() {
        viewModelScope.launch {
            repository.observeSensorData()
                .catch { e ->
                    _error.postValue(e.message ?: "Error fetching sensor data")
                }
                .collect { data ->
                    _sensorData.value = data
                    _isLoading.postValue(false)
                }
        }
    }
    
    fun refreshData() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val data = repository.getSensorData()
                _sensorData.value = data
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error refreshing data")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
