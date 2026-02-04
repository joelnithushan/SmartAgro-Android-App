package com.example.smartagro.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagro.data.firebase.FirebaseProvider
import com.example.smartagro.data.firestore.FirestoreRepository
import com.example.smartagro.domain.model.firestore.UserDevice
import com.example.smartagro.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DeviceSelectionViewModel(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val TAG = "DeviceSelectionViewModel"

    private val _devices = MutableStateFlow<List<UserDevice>>(emptyList())
    val devices: StateFlow<List<UserDevice>> = _devices.asStateFlow()

    private val _selectedDeviceId = MutableStateFlow<String?>(null)
    val selectedDeviceId: StateFlow<String?> = _selectedDeviceId.asStateFlow()

    fun start() {
        val uid = FirebaseProvider.auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _devices.value = emptyList()
            _selectedDeviceId.value = null
            return
        }

        viewModelScope.launch {
            firestoreRepository.getUserDevices(uid)
                .catch { e ->
                    Log.e(TAG, "Error observing devices", e)
                    _devices.value = emptyList()
                }
                .collect { list ->
                    _devices.value = list
                }
        }

        viewModelScope.launch {
            firestoreRepository.getUserActiveDeviceId(uid)
                .catch { e ->
                    Log.e(TAG, "Error observing active device", e)
                    _selectedDeviceId.value = pickFallbackDeviceId(_devices.value)
                }
                .collect { active ->
                    if (!active.isNullOrBlank()) {
                        _selectedDeviceId.value = active
                    } else {
                        _selectedDeviceId.value = pickFallbackDeviceId(_devices.value)
                    }
                }
        }

        viewModelScope.launch {
            devices.collect { list ->
                val current = _selectedDeviceId.value
                if (current.isNullOrBlank()) {
                    _selectedDeviceId.value = pickFallbackDeviceId(list)
                }
            }
        }
    }

    fun selectDevice(deviceId: String) {
        val uid = FirebaseProvider.auth.currentUser?.uid
        _selectedDeviceId.value = deviceId
        if (Constants.ENABLE_WRITE_ACTIVE_DEVICE_ID && !uid.isNullOrBlank()) {
            viewModelScope.launch {
                try {
                    firestoreRepository.updateActiveDeviceId(uid, deviceId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating activeDeviceId", e)
                }
            }
        }
    }

    private fun pickFallbackDeviceId(list: List<UserDevice>): String? {
        return list.firstOrNull()?.deviceId
    }
}

