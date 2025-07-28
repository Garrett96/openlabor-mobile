package com.labs.tempus.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.io.Serializable
import java.time.LocalDate
import java.util.UUID

/**
 * Data class representing an employee in the timesheet tracker
 */
@Parcelize
data class Employee(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    
    @SerializedName("name")
    var name: String = "",
    
    @SerializedName("type")
    var type: EmployeeType = EmployeeType.STAFF,
    
    @SerializedName("timeEntries")
    val timeEntries: @RawValue MutableList<TimeEntry> = mutableListOf()
) : Parcelable, Serializable {
    
    /**
     * Calculate total hours worked for this employee
     * @return Total hours worked, not including break times
     */
    fun getTotalHours(): Float {
        return timeEntries.sumOf { it.getHoursWorked().toDouble() }.toFloat()
    }
    
    /**
     * Calculate hours for the current day
     * @return Hours worked today
     */
    fun getTodayHours(): Float {
        val today = LocalDate.now()
        return timeEntries
            .filter { 
                val entryDate = it.clockInTime.toLocalDate()
                // Include entries that started today or are overnight shifts that end today
                entryDate.isEqual(today) || 
                (it.isNightShift() && it.clockOutTime?.toLocalDate()?.isEqual(today) == true)
            }
            .sumOf { it.getHoursWorked().toDouble() }
            .toFloat()
    }
    
    /**
     * Checks if the employee is currently clocked in
     * @return True if the employee has an active time entry without a clock-out time
     */
    fun isClockedIn(): Boolean {
        return timeEntries.any { it.clockOutTime == null }
    }
    
    /**
     * Get all night shift entries (spanning multiple days)
     * @return List of time entries that cross midnight
     */
    fun getNightShiftEntries(): List<TimeEntry> {
        return timeEntries.filter { it.isNightShift() }
    }
    
    /**
     * Get the current active time entry if the employee is clocked in
     * @return The current active time entry or null if not clocked in
     */
    fun getCurrentTimeEntry(): TimeEntry? {
        return timeEntries.find { it.clockOutTime == null }
    }
}

/**
 * Enum representing different employee types
 */
@Parcelize
enum class EmployeeType : Parcelable {
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
