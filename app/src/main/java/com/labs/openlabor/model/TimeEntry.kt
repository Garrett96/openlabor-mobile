package com.labs.openlabor.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class TimeEntry(
    @SerializedName("id")
    val id: String = UUID.randomUUID().toString(),

    @SerializedName("clockInTime")
    var clockInTime: LocalDateTime = LocalDateTime.now(),

    @SerializedName("clockOutTime")
    var clockOutTime: LocalDateTime? = null,

    @SerializedName("breakMinutes")
    var breakMinutes: Int = 0
) : Serializable {

    companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
    }

    fun getHoursWorked(): Float {
        if (clockOutTime == null) {
            return 0f
        }

        val totalMinutes = Duration.between(clockInTime, clockOutTime).toMinutes()

        val adjustedMinutes = if (totalMinutes < 0) 0L else totalMinutes
        val workMinutes = adjustedMinutes - breakMinutes

        return workMinutes / 60f
    }

    fun isNightShift(): Boolean {
        return clockOutTime != null &&
               !clockInTime.toLocalDate().isEqual(clockOutTime!!.toLocalDate())
    }

    fun getFormattedClockInTime(): String {
        return clockInTime.format(TIME_FORMATTER)
    }

    fun getFormattedClockOutTime(): String {
        return clockOutTime?.format(TIME_FORMATTER) ?: ""
    }

    fun getFormattedDate(): String {
        if (isNightShift()) {
            val clockInDate = clockInTime.format(DATE_FORMATTER)
            val clockOutDate = clockOutTime!!.format(DATE_FORMATTER)
            return "$clockInDate → $clockOutDate"
        }
        return clockInTime.format(DATE_FORMATTER)
    }

    fun getFormattedHours(): String {
        val hours = getHoursWorked()
        return if (isNightShift()) {
            String.format("%.2f hrs ⏱️", hours)
        } else {
            String.format("%.2f hrs", hours)
        }
    }

    fun getFormattedClockInDateTime(): String {
        return clockInTime.format(DATE_TIME_FORMATTER)
    }

    fun getFormattedClockOutDateTime(): String {
        return clockOutTime?.format(DATE_TIME_FORMATTER) ?: ""
    }
}
