package com.labs.openlabor-mobile.ui.summary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.labs.openlabor-mobile.data.EmployeeRepository
import com.labs.openlabor-mobile.model.Employee
import com.labs.openlabor-mobile.model.EmployeeType

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmployeeRepository.getInstance(application)

    private val _employees = MutableLiveData<List<Employee>>()
    val employees: LiveData<List<Employee>> = _employees

    private val _totalHours = MutableLiveData<Float>()
    val totalHours: LiveData<Float> = _totalHours

    private val _hoursByType = MutableLiveData<Map<EmployeeType, Float>>()
    val hoursByType: LiveData<Map<EmployeeType, Float>> = _hoursByType

    init {
        refreshData()
    }

    fun refreshData() {
        val allEmployees = repository.getAllEmployees()
        _employees.value = allEmployees

        // Calculate total hours
        val total = allEmployees.sumOf { it.getTotalHours().toDouble() }.toFloat()
        _totalHours.value = total

        // Get hours by employee type
        _hoursByType.value = repository.getTotalHoursByType()
    }

    fun getFormattedTotalHours(): String {
        return String.format("%.2f hours", _totalHours.value ?: 0f)
    }

    fun getFormattedHoursByType(type: EmployeeType): String {
        val hours = _hoursByType.value?.get(type) ?: 0f
        return String.format("%.2f hours", hours)
    }
}
