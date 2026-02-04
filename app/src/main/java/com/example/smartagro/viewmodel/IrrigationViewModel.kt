package com.example.smartagro.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagro.data.repository.AgroRepository
import com.example.smartagro.domain.model.IrrigationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class IrrigationViewModel(
    private val repository: AgroRepository
) : ViewModel() {
    
    private val _irrigationStatus = MutableStateFlow<IrrigationStatus?>(null)
    val irrigationStatus: StateFlow<IrrigationStatus?> = _irrigationStatus.asStateFlow()
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _actionSuccess = MutableLiveData<String?>()
    val actionSuccess: LiveData<String?> = _actionSuccess
    
    init {
        observeIrrigationStatus()
    }
    
    private fun observeIrrigationStatus() {
        viewModelScope.launch {
            repository.observeIrrigationStatus()
                .catch { e ->
                    _error.postValue(e.message ?: "Error fetching irrigation status")
                }
                .collect { status ->
                    _irrigationStatus.value = status
                    _isLoading.postValue(false)
                }
        }
    }
    
    fun togglePump(pumpId: Int) {
        val currentStatus = _irrigationStatus.value
        val newStatus = when (pumpId) {
            1 -> !(currentStatus?.pump1Status ?: false)
            2 -> !(currentStatus?.pump2Status ?: false)
            else -> return
        }
        
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                repository.togglePump(pumpId, newStatus)
                _actionSuccess.postValue("Pump $pumpId ${if (newStatus) "turned ON" else "turned OFF"}")
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error controlling pump")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun setAutoMode(enabled: Boolean) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                repository.setAutoMode(enabled)
                _actionSuccess.postValue("Auto mode ${if (enabled) "enabled" else "disabled"}")
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error setting auto mode")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun setManualMode(enabled: Boolean) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                repository.setManualMode(enabled)
                _actionSuccess.postValue("Manual mode ${if (enabled) "enabled" else "disabled"}")
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error setting manual mode")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun setThreshold(threshold: Double) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                repository.setThreshold(threshold)
                _actionSuccess.postValue("Threshold updated to $threshold%")
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error setting threshold")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun setDuration(duration: Int) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                repository.setDuration(duration)
                _actionSuccess.postValue("Duration updated to $duration minutes")
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error setting duration")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun refreshStatus() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val status = repository.getIrrigationStatus()
                _irrigationStatus.value = status
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error refreshing status")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSuccess() {
        _actionSuccess.value = null
    }
}
