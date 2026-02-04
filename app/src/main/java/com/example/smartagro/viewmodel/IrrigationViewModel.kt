package com.example.smartagro.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagro.data.irrigation.AutoIrrigationManager
import com.example.smartagro.data.repository.AgroRepository
import com.example.smartagro.domain.model.IrrigationState
import com.example.smartagro.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class IrrigationViewModel(
    private val repository: AgroRepository,
    private val farmId: String = Constants.DEFAULT_FARM_ID
) : ViewModel() {
    
    private val TAG = "IrrigationViewModel"
    
    private val _irrigationState = MutableStateFlow<IrrigationState?>(null)
    val irrigationState: StateFlow<IrrigationState?> = _irrigationState.asStateFlow()
    
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _isAutoModeActive = MutableLiveData<Boolean>(false)
    val isAutoModeActive: LiveData<Boolean> = _isAutoModeActive
    
    private var autoIrrigationManager: AutoIrrigationManager? = null
    
    init {
        observeIrrigation()
        setupAutoIrrigation()
    }
    
    private fun setupAutoIrrigation() {
        viewModelScope.launch {
            irrigationState.collect { state ->
                if (state?.mode == "AUTO") {
                    if (autoIrrigationManager == null) {
                        autoIrrigationManager = AutoIrrigationManager(
                            repository,
                            farmId,
                            viewModelScope
                        )
                        autoIrrigationManager?.start(
                            repository.observeSensors(farmId),
                            repository.observeIrrigation(farmId)
                        )
                        _isAutoModeActive.postValue(true)
                        Log.d(TAG, "Auto irrigation manager started")
                    }
                } else {
                    autoIrrigationManager?.stop()
                    autoIrrigationManager = null
                    _isAutoModeActive.postValue(false)
                    Log.d(TAG, "Auto irrigation manager stopped")
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        autoIrrigationManager?.stop()
        autoIrrigationManager = null
    }
    
    private fun observeIrrigation() {
        viewModelScope.launch {
            _loading.postValue(true)
            _errorMessage.postValue(null)
            
            repository.observeIrrigation(farmId)
                .catch { e ->
                    Log.e(TAG, "Error observing irrigation", e)
                    _errorMessage.postValue(e.message ?: "Error fetching irrigation status")
                    _loading.postValue(false)
                }
                .collect { state ->
                    Log.d(TAG, "Irrigation state received: $state")
                    _irrigationState.value = state
                    _loading.postValue(false)
                    _errorMessage.postValue(null)
                }
        }
    }
    
    fun toggleIrrigation(confirmed: Boolean) {
        if (!confirmed) return
        
        val currentState = _irrigationState.value
        val newOnState = !(currentState?.isOn ?: false)
        
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                repository.setIrrigationOn(farmId, newOnState, "MANUAL")
                Log.d(TAG, "Irrigation toggled to: $newOnState")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling irrigation", e)
                _errorMessage.postValue(e.message ?: "Error controlling irrigation")
            } finally {
                _loading.postValue(false)
            }
        }
    }
    
    fun setMode(mode: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                repository.setMode(farmId, mode)
                Log.d(TAG, "Mode set to: $mode")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting mode", e)
                _errorMessage.postValue(e.message ?: "Error setting mode")
            } finally {
                _loading.postValue(false)
            }
        }
    }
    
    fun setThreshold(threshold: Double) {
        viewModelScope.launch {
            try {
                repository.setThreshold(farmId, threshold)
                Log.d(TAG, "Threshold set to: $threshold")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting threshold", e)
                _errorMessage.postValue(e.message ?: "Error setting threshold")
            }
        }
    }
    
    fun refreshStatus() {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val state = repository.getIrrigation(farmId)
                _irrigationState.value = state
                _errorMessage.postValue(null)
                Log.d(TAG, "Status refreshed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing status", e)
                _errorMessage.postValue(e.message ?: "Error refreshing status")
            } finally {
                _loading.postValue(false)
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
