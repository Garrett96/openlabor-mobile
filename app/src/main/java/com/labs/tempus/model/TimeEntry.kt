package com.labs.tempus.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Data class representing a time entry for an employee in the timesheet tracker
 */
@Parcelize
data class TimeEntry(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),
    
    @SerializedName("clockInTime")
    var clockInTime: LocalDateTime = LocalDateTime.now(),
    
    @SerializedName("clockOutTime")
    var clockOutTime: LocalDateTime? = null,
    
    @SerializedName("breakMinutes")
    var breakMinutes: Int = 0
) : Parcelable {
    
    companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
    }
    
    /**
     * Calculate hours worked for this time entry, excluding break time
     * Properly handles night shifts that span across midnight
     * @return Hours worked (in decimal format, e.g., 8.5 for 8 hours and 30 minutes)
     */
    fun getHoursWorked(): Float {
        if (clockOutTime == null) {
            return 0f
        }
        
        // Duration.between handles date changes automatically
        // For example, if clockIn is 10:00 PM and clockOut is 6:00 AM the next day
        // it will correctly calculate 8 hours
        val totalMinutes = Duration.between(clockInTime, clockOutTime).toMinutes()
        
        // Ensure we don't return negative values in case of data errors
        val adjustedMinutes = if (totalMinutes < 0) 0L else totalMinutes
        val workMinutes = adjustedMinutes - breakMinutes
        
        return workMinutes / 60f
    }
    
    /**
     * Determines if this time entry spans across midnight (night shift)
     * @return True if the clock-in date is different from the clock-out date
     */
    fun isNightShift(): Boolean {
        return clockOutTime != null && 
               !clockInTime.toLocalDate().isEqual(clockOutTime!!.toLocalDate())
    }
    
    /**
     * @return Formatted string of the clock-in time (e.g., "9:00 AM")
     */
    fun getFormattedClockInTime(): String {
        return clockInTime.format(TIME_FORMATTER)
    }
    
    /**
     * @return Formatted string of the clock-out time (e.g., "5:30 PM") or empty if not clocked out
     */
    fun getFormattedClockOutTime(): String {
        return clockOutTime?.format(TIME_FORMATTER) ?: ""
    }
    
    /**
     * @return Formatted string of the date (e.g., "Jan 15, 2023" or "Jan 15, 2023 → Jan 16, 2023" for night shifts)
     */
    fun getFormattedDate(): String {
        if (isNightShift()) {
            val clockInDate = clockInTime.format(DATE_FORMATTER)
            val clockOutDate = clockOutTime!!.format(DATE_FORMATTER)
            return "$clockInDate → $clockOutDate"
        }
        return clockInTime.format(DATE_FORMATTER)
    }
    
    /**
     * @return Formatted string of hours worked (e.g., "8.5 hrs")
     */
    fun getFormattedHours(): String {
        val hours = getHoursWorked()
        return if (isNightShift()) {
            // For night shifts, mark with a symbol for clarity
            String.format("%.2f hrs ⏱️", hours)
        } else {
            String.format("%.2f hrs", hours)
        }
    }
    
    /**
     * @return Formatted string of the full clock-in datetime (e.g., "Jan 15, 2023 at 10:30 PM")
     */
    fun getFormattedClockInDateTime(): String {
        return clockInTime.format(DATE_TIME_FORMATTER)
    }
    
    /**
     * @return Formatted string of the full clock-out datetime (e.g., "Jan 16, 2023 at 6:30 AM")
     */
    fun getFormattedClockOutDateTime(): String {
        return clockOutTime?.format(DATE_TIME_FORMATTER) ?: ""
    }
}
