package com.labs.tempus.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.labs.tempus.data.EmployeeRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _isDarkMode = MutableLiveData<Boolean>()
    val isDarkMode: LiveData<Boolean> = _isDarkMode
    
    private val _defaultBreakTimeEnabled = MutableLiveData<Boolean>()
    val defaultBreakTimeEnabled: LiveData<Boolean> = _defaultBreakTimeEnabled
    
    private val _defaultBreakTime = MutableLiveData<Int>()
    val defaultBreakTime: LiveData<Int> = _defaultBreakTime
    
    // Available employee types
    private val _staffEnabled = MutableLiveData<Boolean>()
    val staffEnabled: LiveData<Boolean> = _staffEnabled
    
    private val _tempEnabled = MutableLiveData<Boolean>()
    val tempEnabled: LiveData<Boolean> = _tempEnabled
    
    private val _contractorEnabled = MutableLiveData<Boolean>()
    val contractorEnabled: LiveData<Boolean> = _contractorEnabled
    
    private val _managerEnabled = MutableLiveData<Boolean>()
    val managerEnabled: LiveData<Boolean> = _managerEnabled
    
    private val repository = EmployeeRepository.getInstance(application)
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        // In a real app, these would be loaded from SharedPreferences
        _isDarkMode.value = false
        _defaultBreakTimeEnabled.value = true
        _defaultBreakTime.value = 30
        
        // All employee types enabled by default
        _staffEnabled.value = true
        _tempEnabled.value = true
        _contractorEnabled.value = true
        _managerEnabled.value = true
    }
    
    fun saveSettings(
        isDarkMode: Boolean,
        defaultBreakTimeEnabled: Boolean,
        defaultBreakTime: Int,
        staffEnabled: Boolean,
        tempEnabled: Boolean,
        contractorEnabled: Boolean,
        managerEnabled: Boolean
    ) {
        viewModelScope.launch {
            // In a real app, these would be saved to SharedPreferences
            _isDarkMode.value = isDarkMode
            _defaultBreakTimeEnabled.value = defaultBreakTimeEnabled
            _defaultBreakTime.value = defaultBreakTime
            
            _staffEnabled.value = staffEnabled
            _tempEnabled.value = tempEnabled
            _contractorEnabled.value = contractorEnabled
            _managerEnabled.value = managerEnabled
        }
    }
}