package com.labs.openlabor-mobile.ui.qrcode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.labs.openlabor-mobile.data.EmployeeRepository
import com.labs.openlabor-mobile.model.Employee

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {

    private val employeeRepository = EmployeeRepository.getInstance(application)

    // LiveData for employees list
    private val _employees = MutableLiveData<List<Employee>>()
    val employees: LiveData<List<Employee>> = _employees

    // LiveData for the selected employee
    private val _selectedEmployee = MutableLiveData<Employee?>()
    val selectedEmployee: LiveData<Employee?> = _selectedEmployee

    // LiveData for the selected time entry position
    private val _selectedTimeEntryPosition = MutableLiveData<Int>()
    val selectedTimeEntryPosition: LiveData<Int> = _selectedTimeEntryPosition

    // LiveData for QR code type (0 = Employee Info, 1 = Time Entry, 2 = Summary)
    private val _qrCodeType = MutableLiveData<Int>()
    val qrCodeType: LiveData<Int> = _qrCodeType

    init {
        _qrCodeType.value = 0 // Default to Employee Info
        _selectedTimeEntryPosition.value = 0 // Default to first time entry
    }

    /**
     * Load employees from the repository
     */
    fun loadEmployees() {
        val allEmployees = employeeRepository.getAllEmployees()
        _employees.value = allEmployees

        // Select the first employee if available
        if (allEmployees.isNotEmpty()) {
            if (_selectedEmployee.value == null) {
                _selectedEmployee.value = allEmployees[0]
            } else {
                // Update the currently selected employee (it might have changed)
                val currentId = _selectedEmployee.value?.id
                _selectedEmployee.value = allEmployees.find { it.id == currentId } ?: allEmployees[0]
            }
        } else {
            _selectedEmployee.value = null
        }
    }

    /**
     * Select an employee by position in the list
     * @param position The position of the employee in the list
     */
    fun selectEmployee(position: Int) {
        _employees.value?.let { employeeList ->
            if (position >= 0 && position < employeeList.size) {
                _selectedEmployee.value = employeeList[position]
                _selectedTimeEntryPosition.value = 0 // Reset time entry selection
            }
        }
    }

    /**
     * Select a time entry by position
     * @param position The position of the time entry in the list
     */
    fun selectTimeEntry(position: Int) {
        _selectedTimeEntryPosition.value = position
    }

    /**
     * Set the QR code type
     * @param type The QR code type (0 = Employee Info, 1 = Time Entry, 2 = Summary)
     */
    fun setQRCodeType(type: Int) {
        if (type in 0..2) {
            _qrCodeType.value = type
        }
    }
}
