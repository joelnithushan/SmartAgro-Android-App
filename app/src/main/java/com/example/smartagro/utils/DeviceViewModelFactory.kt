package com.example.smartagro.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartagro.data.firebase.RtdbRepository
import com.example.smartagro.data.firestore.FirestoreRepository
import com.example.smartagro.viewmodel.DeviceSelectionViewModel
import com.example.smartagro.viewmodel.IrrigationRtdbViewModel
import com.example.smartagro.viewmodel.MonitoringViewModel

class DeviceViewModelFactory : ViewModelProvider.Factory {

    private val firestoreRepository = FirestoreRepository()
    private val rtdbRepository = RtdbRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DeviceSelectionViewModel::class.java) -> {
                DeviceSelectionViewModel(firestoreRepository) as T
            }
            modelClass.isAssignableFrom(MonitoringViewModel::class.java) -> {
                MonitoringViewModel(rtdbRepository) as T
            }
            modelClass.isAssignableFrom(IrrigationRtdbViewModel::class.java) -> {
                IrrigationRtdbViewModel(rtdbRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

