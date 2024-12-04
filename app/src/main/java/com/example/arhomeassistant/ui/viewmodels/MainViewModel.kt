package com.example.arhomeassistant.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arhomeassistant.data.models.*
import com.example.arhomeassistant.data.repository.ApplianceRepository
import com.example.arhomeassistant.data.repository.MaintenanceRepository
import com.example.arhomeassistant.data.repository.ARMarkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val applianceRepository: ApplianceRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val arMarkerRepository: ARMarkerRepository
) : ViewModel() {
    
    private val _selectedAppliance = MutableStateFlow<Appliance?>(null)
    val selectedAppliance: StateFlow<Appliance?> = _selectedAppliance.asStateFlow()
    
    private val _currentMaintenanceGuide = MutableStateFlow<MaintenanceGuide?>(null)
    val currentMaintenanceGuide: StateFlow<MaintenanceGuide?> = _currentMaintenanceGuide.asStateFlow()
    
    private val _scanMode = MutableStateFlow(false)
    val scanMode: StateFlow<Boolean> = _scanMode.asStateFlow()
    
    fun selectAppliance(appliance: Appliance) {
        viewModelScope.launch {
            _selectedAppliance.value = appliance
            loadMaintenanceGuide(appliance)
        }
    }
    
    private fun loadMaintenanceGuide(appliance: Appliance) {
        viewModelScope.launch {
            _currentMaintenanceGuide.value = maintenanceRepository.getMaintenanceGuide(
                applianceType = appliance.type
            )
        }
    }
    
    fun setScanMode(enabled: Boolean) {
        _scanMode.value = enabled
    }
    
    fun saveARMarker(marker: ARMarker) {
        viewModelScope.launch {
            arMarkerRepository.saveMarker(marker)
        }
    }
    
    fun captureARScene() {
        viewModelScope.launch {
            // Implement AR scene capture functionality
            // This could save the current view as an image
            // or save the AR marker positions for later reference
        }
    }
    
    fun startMaintenance(appliance: Appliance) {
        viewModelScope.launch {
            val record = MaintenanceRecord(
                id = generateUniqueId(),
                applianceId = appliance.id,
                date = getCurrentDate(),
                type = MaintenanceType.ROUTINE_CHECK,
                description = "Started maintenance check",
                cost = null,
                technician = null,
                notes = null
            )
            maintenanceRepository.createMaintenanceRecord(record)
        }
    }
    
    fun completeMaintenance(
        appliance: Appliance,
        cost: Double?,
        technician: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val record = maintenanceRepository.getLatestMaintenanceRecord(appliance.id)
            record?.let {
                val updatedRecord = it.copy(
                    cost = cost,
                    technician = technician,
                    notes = notes
                )
                maintenanceRepository.updateMaintenanceRecord(updatedRecord)
                
                // Update appliance last maintenance date
                val updatedAppliance = appliance.copy(
                    lastMaintenanceDate = getCurrentDate()
                )
                applianceRepository.updateAppliance(updatedAppliance)
            }
        }
    }
    
    private fun generateUniqueId(): String {
        return java.util.UUID.randomUUID().toString()
    }
    
    private fun getCurrentDate(): Date {
        return java.util.Date()
    }
}
