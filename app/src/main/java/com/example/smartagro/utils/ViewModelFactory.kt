package com.example.smartagro.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartagro.data.firebase.FirebaseDataSource
import com.example.smartagro.data.repository.AgroRepository
import com.example.smartagro.viewmodel.DashboardViewModel
import com.example.smartagro.viewmodel.IrrigationViewModel

class ViewModelFactory : ViewModelProvider.Factory {
    
    private val firebaseDataSource = FirebaseDataSource()
    private val repository = AgroRepository(firebaseDataSource)
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(repository) as T
            }
            modelClass.isAssignableFrom(IrrigationViewModel::class.java) -> {
                IrrigationViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
