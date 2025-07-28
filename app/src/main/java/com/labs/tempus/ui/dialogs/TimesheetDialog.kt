package com.labs.tempus.ui.dialogs

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.labs.tempus.R
import com.labs.tempus.model.Employee
import com.labs.tempus.model.EmployeeType
import com.labs.tempus.model.TimeEntry
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Dialog for creating or editing timesheet entries
 * This is a simplified version that lets users manually create entries
 */
class TimesheetDialog : DialogFragment() {
    
    private lateinit var employeeNameInput: EditText
    private lateinit var employeeTypeSpinner: Spinner
    private lateinit var dateButton: Button
    private lateinit var startTimeButton: Button
    private lateinit var endTimeButton: Button
    private lateinit var breakSeekBar: SeekBar
    private lateinit var breakMinutesText: TextView
    
    private var selectedDate = LocalDate.now()
    private var selectedStartTime = LocalTime.of(9, 0) // Default to 9:00 AM
    private var selectedEndTime = LocalTime.of(17, 0) // Default to 5:00 PM
    private var selectedBreakMinutes = 30 // Default break time
    
    private var employee: Employee? = null
    private var timeEntry: TimeEntry? = null
    
    // Callback for when the entry is saved
    private var onSaveListener: ((String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit)? = null
    
    companion object {
        private const val ARG_EMPLOYEE = "employee"
        private const val ARG_TIME_ENTRY = "time_entry"
        
        fun newInstance(
            employee: Employee? = null,
            timeEntry: TimeEntry? = null
        ): TimesheetDialog {
            return TimesheetDialog().apply {
                arguments = Bundle().apply {
                    employee?.let { putSerializable(ARG_EMPLOYEE, it) }
                    timeEntry?.let { putSerializable(ARG_TIME_ENTRY, it) }
                }
            }
        }

        private fun putSerializable(argTimeEntry: String, it: TimeEntry) {

        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        arguments?.let { args ->
            @Suppress("DEPRECATION")
            employee = args.getSerializable(ARG_EMPLOYEE) as? Employee
            @Suppress("DEPRECATION")
            timeEntry = args.getSerializable(ARG_TIME_ENTRY) as? TimeEntry
            
            // If editing an existing time entry, use its values
            timeEntry?.let { entry ->
                selectedDate = entry.clockInTime.toLocalDate()
                selectedStartTime = entry.clockInTime.toLocalTime()
                entry.clockOutTime?.let { endTime ->
                    selectedEndTime = endTime.toLocalTime()
                }
                selectedBreakMinutes = entry.breakMinutes
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val dialogLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }
        
        // Employee Name field
        val nameLabel = TextView(context).apply {
            text = "Employee Name"
            setTextColor(ContextCompat.getColor(context, R.color.accent))
            textSize = 16f
            setPadding(0, 0, 0, 8)
        }
        dialogLayout.addView(nameLabel)
        
        employeeNameInput = EditText(context).apply {
            hint = "Enter employee name"
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(24, 16, 24, 16)
            employee?.let {
                setText(it.name)
                isEnabled = false // Don't allow changing name if employee exists
            }
        }
        dialogLayout.addView(employeeNameInput)
        
        // Employee Type spinner
        val typeLabel = TextView(context).apply {
            text = "Employee Type"
            setTextColor(ContextCompat.getColor(context, R.color.accent))
            textSize = 16f
            setPadding(0, 16, 0, 8)
        }
        dialogLayout.addView(typeLabel)
        
        employeeTypeSpinner = Spinner(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                EmployeeType.values().map { it.toString() }
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            
            // Set selected type if employee exists
            employee?.let {
                val position = EmployeeType.values().indexOf(it.type)
                if (position >= 0) {
                    setSelection(position)
                }
            }
        }
        dialogLayout.addView(employeeTypeSpinner)
        
        // Date selector
        val dateLabel = TextView(context).apply {
            text = "Date"
            setTextColor(ContextCompat.getColor(context, R.color.accent))
            textSize = 16f
            setPadding(0, 24, 0, 8)
        }
        dialogLayout.addView(dateLabel)
        
        dateButton = Button(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.btn_outline)
            setTextColor(ContextCompat.getColor(context, R.color.accent))
            text = formatDate(selectedDate) // Initialize with formatted date text
            
            setOnClickListener {
                showDatePicker()
            }
        }
        dialogLayout.addView(dateButton)
        
        // Time selectors
        val timeLabel = TextView(context).apply {
            text = "Times"
            setTextColor(ContextCompat.getColor(context, R.color.accent))
            textSize = 16f
            setPadding(0, 24, 0, 8)
        }
        dialogLayout.addView(timeLabel)
        
        val timeButtonsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }
        
        startTimeButton = Button(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.btn_outline)
            setTextColor(ContextCompat.getColor(context, R.color.accent))
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { marginEnd = 8 }
            text = formatTime("Start", selectedStartTime)
            
            setOnClickListener {
                showStartTimePicker()
            }
        }
        timeButtonsLayout.addView(startTimeButton)
        
        endTimeButton = Button(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.btn_outline)
            setTextColor(ContextCompat.getColor(context, R.color.accent))
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { marginStart = 8 }
            text = formatTime("End", selectedEndTime)
            
            setOnClickListener {
                showEndTimePicker()
            }
        }
        timeButtonsLayout.addView(endTimeButton)
        dialogLayout.addView(timeButtonsLayout)
        
        // Break time
        val breakLabel = TextView(context).apply {
            text = "Break Duration"
            setTextColor(ContextCompat.getColor(context, R.color.accent))
            textSize = 16f
            setPadding(0, 24, 0, 8)
        }
        dialogLayout.addView(breakLabel)
        
        breakMinutesText = TextView(context).apply {
            text = "$selectedBreakMinutes minutes"
            setTextColor(ContextCompat.getColor(context, R.color.accent))
            textSize = 14f
            setPadding(0, 0, 0, 8)
        }
        dialogLayout.addView(breakMinutesText)
        
        breakSeekBar = SeekBar(context).apply {
            max = 120 // Max 2 hours break
            progress = selectedBreakMinutes
            setPadding(0, 8, 0, 16)
            
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    selectedBreakMinutes = progress
                    breakMinutesText.text = "$selectedBreakMinutes minutes"
                }
                
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        dialogLayout.addView(breakSeekBar)
        
        // Create and return the dialog
        return AlertDialog.Builder(context, R.style.Theme_Tempus_Dialog)
            .setTitle(if (timeEntry == null) "Add Timesheet Entry" else "Edit Timesheet Entry")
            .setView(dialogLayout)
            .setPositiveButton("Save") { _, _ ->
                saveTimeEntry()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
    
    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                // Initialize the button text after the button is created
                dateButton.text = formatDate(selectedDate)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }
    
    private fun showStartTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedStartTime = LocalTime.of(hourOfDay, minute)
                updateStartTimeButtonText()
            },
            selectedStartTime.hour,
            selectedStartTime.minute,
            false
        ).show()
    }
    
    private fun showEndTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedEndTime = LocalTime.of(hourOfDay, minute)
                updateEndTimeButtonText()
            },
            selectedEndTime.hour,
            selectedEndTime.minute,
            false
        ).show()
    }
    
    private fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
        return date.format(formatter)
    }

    private fun updateDateButtonText() {
        try {
            if (::dateButton.isInitialized) {
                dateButton.text = formatDate(selectedDate)
            }
        } catch (e: Exception) {
            // Ignore if not initialized yet
        }
    }
    
    private fun formatTime(prefix: String, time: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return "$prefix: ${time.format(formatter)}"
    }
    
    private fun updateStartTimeButtonText() {
        try {
            if (::startTimeButton.isInitialized) {
                startTimeButton.text = formatTime("Start", selectedStartTime)
            }
        } catch (e: Exception) {
            // Ignore if not initialized yet
        }
    }
    
    private fun updateEndTimeButtonText() {
        try {
            if (::endTimeButton.isInitialized) {
                endTimeButton.text = formatTime("End", selectedEndTime)
            }
        } catch (e: Exception) {
            // Ignore if not initialized yet
        }
    }
    
    private fun saveTimeEntry() {
        val employeeName = employeeNameInput.text.toString().trim()
        if (employeeName.isEmpty()) {
            return
        }
        
        val employeeType = EmployeeType.values()[employeeTypeSpinner.selectedItemPosition]
        
        val startDateTime = LocalDateTime.of(selectedDate, selectedStartTime)
        var endDateTime = LocalDateTime.of(selectedDate, selectedEndTime)
        
        // Handle overnight shifts
        if (endDateTime.isBefore(startDateTime)) {
            endDateTime = LocalDateTime.of(selectedDate.plusDays(1), selectedEndTime)
        }
        
        onSaveListener?.invoke(employeeName, employeeType, startDateTime, endDateTime, selectedBreakMinutes)
    }
    
    fun setOnSaveListener(listener: (String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit) {
        onSaveListener = listener
    }
}