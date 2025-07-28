package com.labs.tempus.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.labs.tempus.data.EmployeeRepository
import com.labs.tempus.model.Employee
import com.labs.tempus.model.EmployeeType
import com.labs.tempus.model.TimeEntry
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = EmployeeRepository.getInstance(application)
    
    private val _employees = MutableLiveData<List<Employee>>()
    val employees: LiveData<List<Employee>> = _employees
    
    private val _operationResult = MutableLiveData<Boolean>()
    val operationResult: LiveData<Boolean> = _operationResult
    
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
    
    fun addTimeEntry(employeeId: String, timeEntry: TimeEntry) {
        viewModelScope.launch {
            val employee = repository.getEmployeeById(employeeId)
            if (employee != null) {
                employee.timeEntries.add(timeEntry)
                repository.updateEmployee(employee)
                _operationResult.value = true
                loadEmployees()
            } else {
                _operationResult.value = false
            }
        }
    }
    
    fun findOrCreateEmployee(name: String, type: EmployeeType): String {
        val employee = repository.getAllEmployees().find { it.name.equals(name, ignoreCase = true) }
        return if (employee != null) {
            employee.id
        } else {
            val newEmployee = Employee(name = name, type = type)
            repository.addEmployee(name, type)
            newEmployee.id
        }
    }
}