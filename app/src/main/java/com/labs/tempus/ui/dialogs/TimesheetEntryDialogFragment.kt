package com.labs.tempus.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.labs.tempus.R
import com.labs.tempus.data.EmployeeRepository
import com.labs.tempus.model.Employee
import com.labs.tempus.model.EmployeeType
import com.labs.tempus.model.TimeEntry
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Dialog fragment for creating and editing timesheet entries manually
 */
class TimesheetEntryDialogFragment : DialogFragment() {
    
    private lateinit var employeeNameInput: EditText
    private lateinit var employeeTypeSpinner: Spinner
    private lateinit var datePickerButton: Button
    private lateinit var clockInButton: Button
    private lateinit var clockOutButton: Button
    private lateinit var breakSeekBar: SeekBar
    private lateinit var breakMinutesText: TextView
    
    // Extension function to add a custom view above the existing view
    private fun AlertDialog.Builder.setCustomView(topLayout: LinearLayout, bottomView: View): AlertDialog.Builder {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(topLayout)
            addView(bottomView)
        }
        return this.setView(container)
    }
    
    private var selectedDate = LocalDate.now()
    private var selectedClockInTime = LocalTime.of(9, 0) // Default to 9:00 AM
    private var selectedClockOutTime = LocalTime.of(17, 0) // Default to 5:00 PM
    private var selectedBreakMinutes = 30 // Default to 30 minutes
    
    private var employeeId: String? = null
    private var timeEntry: TimeEntry? = null
    private var existingEmployee: Employee? = null
    
    // Callback for when the entry is saved
    private var onSaveListener: ((String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit)? = null
    
    companion object {
        private const val ARG_EMPLOYEE_ID = "employee_id"
        private const val ARG_TIME_ENTRY = "time_entry"
        private const val ARG_EMPLOYEE = "employee"
        
        fun newInstance(
            employeeId: String? = null, 
            timeEntry: TimeEntry? = null,
            employee: Employee? = null
        ): TimesheetEntryDialogFragment {
            return TimesheetEntryDialogFragment().apply {
                arguments = Bundle().apply {
                    employeeId?.let { putString(ARG_EMPLOYEE_ID, it) }
                    timeEntry?.let { putSerializable(ARG_TIME_ENTRY, it) }
                    employee?.let { putSerializable(ARG_EMPLOYEE, it) }
                }
            }
        }

        private fun putSerializable(argTimeEntry: String, it: TimeEntry) {

        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        arguments?.let { args ->
            employeeId = args.getString(ARG_EMPLOYEE_ID)
            @Suppress("DEPRECATION")
            timeEntry = args.getSerializable(ARG_TIME_ENTRY) as? TimeEntry
            @Suppress("DEPRECATION")
            existingEmployee = args.getSerializable(ARG_EMPLOYEE) as? Employee
            
            // If we have a time entry, use its values as defaults
            timeEntry?.let { entry ->
                selectedDate = entry.clockInTime.toLocalDate()
                selectedClockInTime = entry.clockInTime.toLocalTime()
                entry.clockOutTime?.let { clockOutTime ->
                    selectedClockOutTime = clockOutTime.toLocalTime()
                }
                selectedBreakMinutes = entry.breakMinutes
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_time_entry, null)
        
        // Create layout for employee name and type inputs
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        
        // Add employee name field
        val nameLabel = TextView(requireContext()).apply {
            text = "Employee Name"
            textSize = 16f
            setTextColor(resources.getColor(R.color.accent, null))
        }
        layout.addView(nameLabel)
        
        employeeNameInput = EditText(requireContext()).apply {
            hint = "Enter employee name"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        layout.addView(employeeNameInput)
        
        // Add employee type spinner
        val typeLabel = TextView(requireContext()).apply {
            text = "Employee Type"
            textSize = 16f
            setTextColor(resources.getColor(R.color.accent, null))
            setPadding(0, 16, 0, 0)
        }
        layout.addView(typeLabel)
        
        employeeTypeSpinner = Spinner(requireContext())
        layout.addView(employeeTypeSpinner)
        
        // Add date and time fields from the dialog_edit_time_entry layout
        datePickerButton = view.findViewById(R.id.button_date)
        clockInButton = view.findViewById(R.id.button_clock_in_time)
        clockOutButton = view.findViewById(R.id.button_clock_out_time)
        breakSeekBar = view.findViewById(R.id.seekbar_break)
        breakMinutesText = view.findViewById(R.id.text_break_minutes)
        
        // Setup employee type spinner
        setupEmployeeTypeSpinner()
        
        // Setup date and time pickers
        setupDatePicker()
        setupTimePickers()
        
        // Setup break time seek bar
        setupBreakSeekBar()
        
        // Fill form with existing data if editing
        fillFormWithExistingData()
        
        // Create dialog with custom layout
        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.Theme_Tempus_Dialog)
            .setTitle(if (timeEntry == null) "Add Timesheet Entry" else "Edit Timesheet Entry")
            .setCustomView(layout, view)
            .setPositiveButton("Save") { dialog, which ->
                saveTimeEntry()
            }
            .setNegativeButton("Cancel", null)
        
        return dialogBuilder.create()
    }
    
    private fun setupEmployeeTypeSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            EmployeeType.values().map { it.toString() }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        employeeTypeSpinner.adapter = adapter
        
        // Set default selection based on existing employee
        existingEmployee?.let { employee ->
            val position = EmployeeType.values().indexOf(employee.type)
            if (position >= 0) {
                employeeTypeSpinner.setSelection(position)
            }
        }
    }
    
    private fun setupDatePicker() {
        updateDateButtonText()
        
        datePickerButton.setOnClickListener {
            val datePicker = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    updateDateButtonText()
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            )
            datePicker.show()
        }
    }
    
    private fun setupTimePickers() {
        updateTimeButtonsText()
        
        clockInButton.setOnClickListener {
            val timePicker = android.app.TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    selectedClockInTime = LocalTime.of(hourOfDay, minute)
                    updateTimeButtonsText()
                },
                selectedClockInTime.hour,
                selectedClockInTime.minute,
                false
            )
            timePicker.show()
        }
        
        clockOutButton.setOnClickListener {
            val timePicker = android.app.TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    selectedClockOutTime = LocalTime.of(hourOfDay, minute)
                    updateTimeButtonsText()
                },
                selectedClockOutTime.hour,
                selectedClockOutTime.minute,
                false
            )
            timePicker.show()
        }
    }
    
    private fun setupBreakSeekBar() {
        breakSeekBar.progress = selectedBreakMinutes
        updateBreakText()
        
        breakSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedBreakMinutes = progress
                updateBreakText()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun fillFormWithExistingData() {
        // Set employee name if available
        existingEmployee?.let { employee ->
            employeeNameInput.setText(employee.name)
            employeeNameInput.isEnabled = false // Don't allow changing the name when editing an existing employee
        }
        
        // If editing a time entry
        timeEntry?.let { entry ->
            // Update date and time fields
            selectedDate = entry.clockInTime.toLocalDate()
            selectedClockInTime = entry.clockInTime.toLocalTime()
            entry.clockOutTime?.let { clockOutTime ->
                selectedClockOutTime = clockOutTime.toLocalTime()
            }
            selectedBreakMinutes = entry.breakMinutes
            
            // Update UI
            updateDateButtonText()
            updateTimeButtonsText()
            breakSeekBar.progress = selectedBreakMinutes
            updateBreakText()
        }
    }
    
    private fun updateDateButtonText() {
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
        datePickerButton.text = "Date: ${selectedDate.format(formatter)}"
    }
    
    private fun updateTimeButtonsText() {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        clockInButton.text = "Clock In: ${selectedClockInTime.format(formatter)}"
        clockOutButton.text = "Clock Out: ${selectedClockOutTime.format(formatter)}"
    }
    
    private fun updateBreakText() {
        breakMinutesText.text = "$selectedBreakMinutes minutes"
    }
    
    private fun saveTimeEntry() {
        val employeeName = employeeNameInput.text.toString().trim()
        if (employeeName.isEmpty()) {
            return
        }
        
        val employeeType = EmployeeType.values()[employeeTypeSpinner.selectedItemPosition]
        
        val clockInDateTime = LocalDateTime.of(selectedDate, selectedClockInTime)
        val clockOutDateTime = LocalDateTime.of(selectedDate, selectedClockOutTime)
        
        // Handle overnight shifts properly
        val adjustedClockOutDateTime = if (clockOutDateTime.isBefore(clockInDateTime)) {
            // If clock out is earlier in the day than clock in, assume it's the next day
            clockOutDateTime.plusDays(1)
        } else {
            clockOutDateTime
        }
        
        onSaveListener?.invoke(employeeName, employeeType, clockInDateTime, adjustedClockOutDateTime, selectedBreakMinutes)
    }
    
    fun setOnSaveListener(listener: (String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit) {
        onSaveListener = listener
    }
}