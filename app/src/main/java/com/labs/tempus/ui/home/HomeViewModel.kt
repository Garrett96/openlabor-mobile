package com.labs.openlabor-mobile.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.labs.openlabor-mobile.data.EmployeeRepository
import com.labs.openlabor-mobile.model.Employee
import com.labs.openlabor-mobile.model.EmployeeType
import com.labs.openlabor-mobile.model.TimeEntry
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmployeeRepository.getInstance(application)

    private val _employees = MutableLiveData<List<Employee>>()
    val employees: LiveData<List<Employee>> = _employees

    private val _clockInResult = MutableLiveData<Boolean>()
    val clockInResult: LiveData<Boolean> = _clockInResult

    private val _clockOutResult = MutableLiveData<Boolean>()
    val clockOutResult: LiveData<Boolean> = _clockOutResult

    init {
        loadEmployees()
    }

    fun loadEmployees() {
        _employees.value = repository.getAllEmployees()
    }

    fun addEmployee(name: String, type: EmployeeType) {
        viewModelScope.launch {
            repository.addEmployee(name, type)
            loadEmployees()
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.updateEmployee(employee)
            loadEmployees()
        }
    }

    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch {
            repository.deleteEmployee(employeeId)
            loadEmployees()
        }
    }

    fun clockIn(employeeId: String) {
        viewModelScope.launch {
            val result = repository.clockIn(employeeId)
            _clockInResult.value = result != null
            loadEmployees()
        }
    }

    fun clockOut(employeeId: String, breakMinutes: Int) {
        viewModelScope.launch {
            val result = repository.clockOut(employeeId, breakMinutes)
            _clockOutResult.value = result != null
            loadEmployees()
        }
    }
}
