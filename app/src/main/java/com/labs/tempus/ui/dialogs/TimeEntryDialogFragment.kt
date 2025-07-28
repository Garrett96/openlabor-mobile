package com.labs.tempus.ui.dialogs

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.labs.tempus.R
import com.labs.tempus.data.EmployeeRepository
import com.labs.tempus.model.TimeEntry
import java.time.LocalDate
import java.io.Serializable
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Dialog fragment for adding or editing a time entry
 */
class TimeEntryDialogFragment : DialogFragment() {
    
    private lateinit var datePickerButton: Button
    private lateinit var clockInButton: Button
    private lateinit var clockOutButton: Button
    private lateinit var breakSeekBar: SeekBar
    private lateinit var breakMinutesText: TextView
    
    private var selectedClockInDate = LocalDate.now()
    private var selectedClockOutDate = LocalDate.now()
    private var selectedClockInTime = LocalTime.of(9, 0) // Default to 9:00 AM
    private var selectedClockOutTime = LocalTime.of(17, 0) // Default to 5:00 PM
    private var selectedBreakMinutes = 30 // Default 30 minute break
    private var isNightShift = false
    
    private var timeEntry: TimeEntry? = null
    private var employeeId: String? = null
    private var employeeName: String = ""
    
    private var onSaveListener: ((LocalDateTime, LocalDateTime, Int) -> Unit)? = null
    private lateinit var employeeRepository: EmployeeRepository
    
    companion object {
        private const val ARG_EMPLOYEE_ID = "employee_id"
        private const val ARG_TIME_ENTRY = "time_entry"
        
        fun newInstance(employeeId: String, timeEntry: TimeEntry? = null): TimeEntryDialogFragment {
            return TimeEntryDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_EMPLOYEE_ID, employeeId)
                    timeEntry?.let { putSerializable(ARG_TIME_ENTRY, it) }
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        employeeId = arguments?.getString(ARG_EMPLOYEE_ID)
        @Suppress("DEPRECATION")
        timeEntry = arguments?.getSerializable(ARG_TIME_ENTRY) as? TimeEntry
        
        // Initialize repository
        employeeRepository = EmployeeRepository.getInstance(requireContext())
        
        // Get employee name
        employeeId?.let { id ->
            employeeRepository.getEmployeeById(id)?.let { employee ->
                employeeName = employee.name
            }
        }
        
        // If we're editing an existing time entry, initialize with its values
        timeEntry?.let {
            selectedClockInDate = it.clockInTime.toLocalDate()
            selectedClockInTime = it.clockInTime.toLocalTime()
            
            it.clockOutTime?.let { clockOut ->
                selectedClockOutDate = clockOut.toLocalDate()
                selectedClockOutTime = clockOut.toLocalTime()
                
                // Check if this is a night shift (spans across days)
                isNightShift = !selectedClockInDate.isEqual(selectedClockOutDate)
            } ?: run {
                selectedClockOutDate = selectedClockInDate
                selectedClockOutTime = LocalTime.of(17, 0)
            }
            
            selectedBreakMinutes = it.breakMinutes
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create custom dialog layout programmatically since we don't have the XML yet
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
            setBackgroundColor(resources.getColor(R.color.dark_card_background, context?.theme))
        }
        
        // Clock-in date picker button
        datePickerButton = Button(context).apply {
            text = "Select Clock-In Date"
            id = View.generateViewId()
        }
        layout.addView(datePickerButton)
        
        // Clock-out date picker button (for night shifts)
        val clockOutDateButton = Button(context).apply {
            text = "Select Clock-Out Date"
            id = View.generateViewId()
            visibility = if (isNightShift) View.VISIBLE else View.GONE
            setOnClickListener { showClockOutDatePicker() }
        }
        layout.addView(clockOutDateButton)
        
        // Night shift toggle
        val nightShiftButton = Button(context).apply {
            text = if (isNightShift) "Night Shift: ON" else "Night Shift: OFF"
            setOnClickListener {
                isNightShift = !isNightShift
                text = if (isNightShift) "Night Shift: ON" else "Night Shift: OFF"
                clockOutDateButton.visibility = if (isNightShift) View.VISIBLE else View.GONE
                
                // If turning night shift on, default to next day for clock-out
                if (isNightShift && selectedClockOutDate.isEqual(selectedClockInDate)) {
                    selectedClockOutDate = selectedClockInDate.plusDays(1)
                } else if (!isNightShift) {
                    // If turning night shift off, make clock-out date same as clock-in date
                    selectedClockOutDate = selectedClockInDate
                }
                
                updateClockOutButtonText()
            }
        }
        layout.addView(nightShiftButton)
        
        // Clock in button
        clockInButton = Button(context).apply {
            text = "Clock In Time"
            id = View.generateViewId()
        }
        layout.addView(clockInButton)
        
        // Clock out button
        clockOutButton = Button(context).apply {
            text = "Clock Out Time" 
            id = View.generateViewId()
        }
        layout.addView(clockOutButton)
        
        // Break minutes text
        breakMinutesText = TextView(context).apply {
            text = "Break: 30 minutes"
            id = View.generateViewId()
            setPadding(0, 30, 0, 10)
        }
        layout.addView(breakMinutesText)
        
        // Break seekbar
        breakSeekBar = SeekBar(context).apply {
            max = 120
            progress = 30
            id = View.generateViewId()
        }
        layout.addView(breakSeekBar)
        
        // Setup initial values
        updateClockInDateButtonText()
        updateClockInButtonText()
        updateClockOutButtonText()
        
        breakSeekBar.max = 120 // Max 2 hours of break
        breakSeekBar.progress = selectedBreakMinutes
        updateBreakMinutesText()
        
        // Setup click listeners
        datePickerButton.setOnClickListener { showClockInDatePicker() }
        clockInButton.setOnClickListener { showClockInTimePicker() }
        clockOutButton.setOnClickListener { showClockOutTimePicker() }
        breakSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedBreakMinutes = progress
                updateBreakMinutesText()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Create dialog
        val title = if (timeEntry == null) "Add Time Entry for $employeeName" else "Edit Time Entry for $employeeName"
        return AlertDialog.Builder(requireContext(), R.style.Theme_Tempus_Dialog)
            .setTitle(title)
            .setView(layout as View)
            .setPositiveButton("Save") { _, _ ->
                val clockInDateTime = LocalDateTime.of(selectedClockInDate, selectedClockInTime)
                val clockOutDateTime = LocalDateTime.of(selectedClockOutDate, selectedClockOutTime)
                
                if (onSaveListener != null) {
                    // Use the custom listener if provided
                    onSaveListener?.invoke(clockInDateTime, clockOutDateTime, selectedBreakMinutes)
                } else {
                    // Default behavior: save to the repository
                    saveTimeEntry(clockInDateTime, clockOutDateTime, selectedBreakMinutes)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
    
    private fun showClockInDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.set(selectedClockInDate.year, selectedClockInDate.monthValue - 1, selectedClockInDate.dayOfMonth)
        
        DatePickerDialog(
            requireContext(), 
            R.style.Theme_Tempus_Dialog_DatePicker,
            { _, year, month, dayOfMonth ->
                selectedClockInDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateClockInDateButtonText()
                
                // If not a night shift, keep clock-out date in sync with clock-in date
                if (!isNightShift) {
                    selectedClockOutDate = selectedClockInDate
                    updateClockOutButtonText()
                } else if (selectedClockOutDate.isBefore(selectedClockInDate)) {
                    // If night shift and clock-out date is before clock-in date, adjust it
                    selectedClockOutDate = selectedClockInDate.plusDays(1)
                    updateClockOutButtonText()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun showClockOutDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.set(selectedClockOutDate.year, selectedClockOutDate.monthValue - 1, selectedClockOutDate.dayOfMonth)
        
        DatePickerDialog(
            requireContext(),
            R.style.Theme_Tempus_Dialog_DatePicker,
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                
                // Ensure clock-out date is not before clock-in date
                if (!newDate.isBefore(selectedClockInDate)) {
                    selectedClockOutDate = newDate
                    updateClockOutButtonText()
                } else {
                    // Show error message
                    val alertDialog = AlertDialog.Builder(requireContext(), R.style.Theme_Tempus_AlertDialog)
                            .setTitle("Invalid Date")
                            .setMessage("Clock-out date cannot be before clock-in date")
                            .setPositiveButton("OK", null)
                            .create()
                    alertDialog.show()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun showClockInTimePicker() {
        TimePickerDialog(
            requireContext(),
            R.style.Theme_Tempus_Dialog_TimePicker,
            { _, hourOfDay, minute ->
                selectedClockInTime = LocalTime.of(hourOfDay, minute)
                updateClockInButtonText()
                
                // If clock-in time is after clock-out time, adjust clock-out time
                if (selectedClockInTime.isAfter(selectedClockOutTime)) {
                    selectedClockOutTime = selectedClockInTime.plusHours(1)
                    updateClockOutButtonText()
                }
            },
            selectedClockInTime.hour,
            selectedClockInTime.minute,
            false // 12-hour format
        ).show()
    }
    
    private fun showClockOutTimePicker() {
        TimePickerDialog(
            requireContext(),
            R.style.Theme_Tempus_Dialog_TimePicker,
            { _, hourOfDay, minute ->
                val newTime = LocalTime.of(hourOfDay, minute)
                
                // For night shifts or different days, any time is valid
                if (isNightShift || !selectedClockInDate.isEqual(selectedClockOutDate)) {
                    selectedClockOutTime = newTime
                    updateClockOutButtonText()
                } else {
                    // Same day: only allow clock-out time to be after clock-in time
                    if (newTime.isAfter(selectedClockInTime)) {
                        selectedClockOutTime = newTime
                        updateClockOutButtonText()
                    } else {
                        // Show error message or handle invalid time
                        val alertDialog = AlertDialog.Builder(requireContext(), R.style.Theme_Tempus_AlertDialog)
                            .setTitle("Invalid Time")
                            .setMessage("Clock-out time must be after clock-in time for same-day shifts")
                            .setPositiveButton("OK", null)
                            .create()
                        alertDialog.show()
                    }
                }
            },
            selectedClockOutTime.hour,
            selectedClockOutTime.minute,
            false // 12-hour format
        ).show()
    }
    
    private fun updateClockInDateButtonText() {
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
        datePickerButton.text = "Clock In: ${selectedClockInDate.format(formatter)}"
        datePickerButton.setTextColor(resources.getColor(R.color.accent, context?.theme))
    }
    
    private fun updateClockInButtonText() {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        clockInButton.text = "Clock In: ${selectedClockInTime.format(formatter)}"
        clockInButton.setTextColor(resources.getColor(R.color.accent, context?.theme))
    }
    
    private fun updateClockOutButtonText() {
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d")
        
        val timeText = selectedClockOutTime.format(timeFormatter)
        
        // If night shift, include the date in the button text
        clockOutButton.text = if (isNightShift || !selectedClockInDate.isEqual(selectedClockOutDate)) {
            "Clock Out: $timeText (${selectedClockOutDate.format(dateFormatter)})"
        } else {
            "Clock Out: $timeText"
        }
    }
    
    private fun updateBreakMinutesText() {
        breakMinutesText.text = "Break: $selectedBreakMinutes minutes"
        breakMinutesText.setTextColor(resources.getColor(R.color.accent, context?.theme))
    }
    
    fun setOnSaveListener(listener: (LocalDateTime, LocalDateTime, Int) -> Unit) {
        onSaveListener = listener
    }
    
    /**
     * Save a time entry to the repository
     */
    private fun saveTimeEntry(clockInTime: LocalDateTime, clockOutTime: LocalDateTime, breakMinutes: Int) {
        employeeId?.let { id ->
            // Get the employee
            val employee = employeeRepository.getEmployeeById(id)
            
            if (employee != null) {
                // Store the employee name for future reference
                employeeName = employee.name
                
                if (timeEntry != null) {
                    // Editing existing entry
                    val index = employee.timeEntries.indexOfFirst { it.id == timeEntry!!.id }
                    if (index != -1) {
                        // Create updated entry
                        val updatedEntry = TimeEntry(
                            id = timeEntry!!.id,
                            clockInTime = clockInTime,
                            clockOutTime = clockOutTime,
                            breakMinutes = breakMinutes
                        )
                        
                        // Replace in list
                        employee.timeEntries[index] = updatedEntry
                    }
                } else {
                    // Creating new entry
                    val newEntry = TimeEntry(
                        clockInTime = clockInTime,
                        clockOutTime = clockOutTime,
                        breakMinutes = breakMinutes
                    )
                    
                    // Add to employee's entries
                    employee.timeEntries.add(newEntry)
                }
                
                // Update employee in repository
                employeeRepository.updateEmployee(employee)
                
                // Show success message
                Toast.makeText(requireContext(), 
                    "Time entry saved for ${employee.name}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}