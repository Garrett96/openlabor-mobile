package com.labs.tempus.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.UUID

data class Employee(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    
    @SerializedName("name")
    var name: String = "",
    
    @SerializedName("type")
    var type: EmployeeType = EmployeeType.STAFF,
    
    @SerializedName("timeEntries")
    val timeEntries: MutableList<TimeEntry> = mutableListOf()
) : Serializable {
    
    fun getTotalHours(): Float {
        return timeEntries.sumOf { it.getHoursWorked().toDouble() }.toFloat()
    }
    
    fun getTodayHours(): Float {
        val today = java.time.LocalDate.now()
        return timeEntries
            .filter { 
                val entryDate = it.clockInTime.toLocalDate()
                entryDate.isEqual(today) || 
                (it.isNightShift() && it.clockOutTime?.toLocalDate()?.isEqual(today) == true)
            }
            .sumOf { it.getHoursWorked().toDouble() }
            .toFloat()
    }
    
    fun isClockedIn(): Boolean {
        return timeEntries.any { it.clockOutTime == null }
    }
    
    fun getNightShiftEntries(): List<TimeEntry> {
        return timeEntries.filter { it.isNightShift() }
    }
    
    fun getCurrentTimeEntry(): TimeEntry? {
        return timeEntries.find { it.clockOutTime == null }
    }
}

enum class EmployeeType : Serializable {
    STAFF,
    TEMP,
    CONTRACTOR,
    MANAGER;
    
    override fun toString(): String {
        return when(this) {
            STAFF -> "Staff"
            TEMP -> "Temporary"
            CONTRACTOR -> "Contractor"
            MANAGER -> "Manager"
        }
    }
}
