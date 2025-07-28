package com.labs.tempus.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import com.labs.tempus.model.Employee
import com.labs.tempus.util.GsonUtils
import com.labs.tempus.model.EmployeeType
import com.labs.tempus.model.TimeEntry
import java.lang.reflect.Type
import java.time.LocalDateTime

/**
 * Repository for managing employee data and persistence
 */
class EmployeeRepository private constructor(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "tempus_preferences"
        private const val KEY_EMPLOYEES = "employees"
        
        @Volatile
        private var INSTANCE: EmployeeRepository? = null
        
        fun getInstance(context: Context): EmployeeRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = EmployeeRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = GsonUtils.createGson()
    private val employeeListType: Type = object : TypeToken<List<Employee>>() {}.type
    
    // In-memory cache of employees
    private val employees: MutableList<Employee> = loadEmployees()
    
    /**
     * Load employees from SharedPreferences
     */
    private fun loadEmployees(): MutableList<Employee> {
        val json = sharedPreferences.getString(KEY_EMPLOYEES, null)
        return if (json != null) {
            try {
                gson.fromJson(json, employeeListType)
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }
    }
    
    /**
     * Save employees to SharedPreferences
     */
    private fun saveEmployees() {
        val json = gson.toJson(employees)
        sharedPreferences.edit().putString(KEY_EMPLOYEES, json).apply()
    }
    
    /**
     * Get all employees
     */
    fun getAllEmployees(): List<Employee> {
        return employees.toList()
    }
    
    /**
     * Get employee by ID
     */
    fun getEmployeeById(id: String): Employee? {
        return employees.find { it.id == id }
    }
    
    /**
     * Add a new employee
     */
    fun addEmployee(name: String, type: EmployeeType): Employee {
        val employee = Employee(name = name, type = type)
        employees.add(employee)
        saveEmployees()
        return employee
    }
    
    /**
     * Add a new employee using an existing Employee object
     */
    fun addEmployee(employee: Employee): Employee {
        employees.add(employee)
        saveEmployees()
        return employee
    }
    
    /**
     * Update an employee
     */
    fun updateEmployee(employee: Employee) {
        val index = employees.indexOfFirst { it.id == employee.id }
        if (index != -1) {
            employees[index] = employee
            saveEmployees()
        }
    }
    
    /**
     * Delete an employee
     */
    fun deleteEmployee(employeeId: String) {
        employees.removeIf { it.id == employeeId }
        saveEmployees()
    }
    
    /**
     * Clock in an employee
     */
    fun clockIn(employeeId: String): TimeEntry? {
        val employee = getEmployeeById(employeeId) ?: return null
        
        // Check if employee is already clocked in
        if (employee.isClockedIn()) {
            return null
        }
        
        val timeEntry = TimeEntry(clockInTime = LocalDateTime.now())
        employee.timeEntries.add(timeEntry)
        saveEmployees()
        return timeEntry
    }
    
    /**
     * Clock out an employee
     */
    fun clockOut(employeeId: String, breakMinutes: Int = 0): TimeEntry? {
        val employee = getEmployeeById(employeeId) ?: return null
        val timeEntry = employee.getCurrentTimeEntry() ?: return null
        
        timeEntry.clockOutTime = LocalDateTime.now()
        timeEntry.breakMinutes = breakMinutes
        saveEmployees()
        return timeEntry
    }
    
    /**
     * Get total hours by employee type
     */
    fun getTotalHoursByType(): Map<EmployeeType, Float> {
        return employees.groupBy { it.type }
            .mapValues { entry -> 
                entry.value.sumOf { employee -> employee.getTotalHours().toDouble() }.toFloat()
            }
    }
    
    /**
     * Reset all data
     */
    fun resetAllData() {
        employees.clear()
        saveEmployees()
    }
}