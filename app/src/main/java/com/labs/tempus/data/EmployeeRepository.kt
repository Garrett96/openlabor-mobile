package com.labs.openlabor-mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import com.labs.openlabor-mobile.model.Employee
import com.labs.openlabor-mobile.util.GsonUtils
import com.labs.openlabor-mobile.model.EmployeeType
import com.labs.openlabor-mobile.model.TimeEntry
import java.lang.reflect.Type
import java.time.LocalDateTime

class EmployeeRepository private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "openlabor-mobile_preferences"
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

    private val employees: MutableList<Employee> = loadEmployees()

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

    private fun saveEmployees() {
        val json = gson.toJson(employees)
        sharedPreferences.edit().putString(KEY_EMPLOYEES, json).apply()
    }

    fun getAllEmployees(): List<Employee> {
        return employees.toList()
    }

    fun getEmployeeById(id: String): Employee? {
        return employees.find { it.id == id }
    }

    fun getEmployeeByName(name: String): Employee? {
        return employees.find { it.name == name }
    }

    fun addEmployee(name: String, type: EmployeeType): Employee {
        val employee = Employee(name = name, type = type)
        employees.add(employee)
        saveEmployees()
        return employee
    }

    fun updateEmployee(employee: Employee) {
        val index = employees.indexOfFirst { it.id == employee.id }
        if (index != -1) {
            employees[index] = employee
            saveEmployees()
        }
    }

    fun deleteEmployee(employeeId: String) {
        employees.removeIf { it.id == employeeId }
        saveEmployees()
    }

    fun clockIn(employeeId: String): TimeEntry? {
        val employee = getEmployeeById(employeeId) ?: return null

        if (employee.isClockedIn()) {
            return null
        }

        val timeEntry = TimeEntry(clockInTime = LocalDateTime.now())
        employee.timeEntries.add(timeEntry)
        saveEmployees()
        return timeEntry
    }

    fun clockOut(employeeId: String, breakMinutes: Int = 0): TimeEntry? {
        val employee = getEmployeeById(employeeId) ?: return null
        val timeEntry = employee.getCurrentTimeEntry() ?: return null

        timeEntry.clockOutTime = LocalDateTime.now()
        timeEntry.breakMinutes = breakMinutes
        saveEmployees()
        return timeEntry
    }

    fun getTotalHoursByType(): Map<EmployeeType, Float> {
        return employees.groupBy { it.type }
            .mapValues { entry ->
                entry.value.sumOf { employee -> employee.getTotalHours().toDouble() }.toFloat()
            }
    }

    fun resetAllData() {
        employees.clear()
        saveEmployees()
    }
}
