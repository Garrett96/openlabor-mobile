package com.labs.tempus.ui.dialogs.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.DialogFragment
import com.labs.tempus.model.Employee
import com.labs.tempus.model.EmployeeType
import com.labs.tempus.model.TimeEntry
import com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.DatePickerDialog
import com.marosseleng.compose.material3.datetimepickers.time.ui.dialog.TimePickerDialog
import kotlinx.parcelize.IgnoredOnParcel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * A modern Material You based dialog for timesheet entries
 */
class TimesheetComposeDialog : DialogFragment() {

    @IgnoredOnParcel
    private var employee: Employee? = null
    
    @IgnoredOnParcel
    private var timeEntry: TimeEntry? = null
    
    // Callback for when the entry is saved
    @IgnoredOnParcel
    private var onSaveListener: ((String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit)? = null
    
    companion object {
        private const val ARG_EMPLOYEE = "employee"
        private const val ARG_TIME_ENTRY = "time_entry"
        
        fun newInstance(
            employee: Employee? = null,
            timeEntry: TimeEntry? = null
        ): TimesheetComposeDialog {
            return TimesheetComposeDialog().apply {
                arguments = Bundle().apply {
                    employee?.let { putParcelable(ARG_EMPLOYEE, it) }
                    timeEntry?.let { putParcelable(ARG_TIME_ENTRY, it) }
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, com.google.android.material.R.style.Theme_Material3_Light_NoActionBar)
        
        arguments?.let { args ->
            employee = args.getParcelable(ARG_EMPLOYEE)
            timeEntry = args.getParcelable(ARG_TIME_ENTRY)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TimesheetDialogContent(
                        employee = employee,
                        timeEntry = timeEntry,
                        onSave = { name, type, startTime, endTime, breakMinutes ->
                            onSaveListener?.invoke(name, type, startTime, endTime, breakMinutes)
                            dismiss()
                        },
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
    }
    
    fun setOnSaveListener(listener: (String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit) {
        onSaveListener = listener
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TimesheetDialogContent(
    employee: Employee?,
    timeEntry: TimeEntry?,
    onSave: (String, EmployeeType, LocalDateTime, LocalDateTime, Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Setup state
    var employeeName by remember { mutableStateOf(employee?.name ?: "") }
    var employeeType by remember { mutableStateOf(employee?.type ?: EmployeeType.STAFF) }
    var showEmployeeTypeMenu by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(timeEntry?.clockInTime?.toLocalDate() ?: LocalDate.now()) }
    var selectedStartTime by remember { mutableStateOf(timeEntry?.clockInTime?.toLocalTime() ?: LocalTime.of(9, 0)) }
    var selectedEndTime by remember { mutableStateOf(timeEntry?.clockOutTime?.toLocalTime() ?: LocalTime.of(17, 0)) }
    var breakMinutes by remember { mutableStateOf(timeEntry?.breakMinutes?.toFloat() ?: 30f) }
    
    // State for date and time pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top app bar
            TopAppBar(
                title = { 
                    Text(
                        text = if (timeEntry == null) "Add Timesheet Entry" else "Edit Timesheet Entry",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Employee section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Employee Information",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Employee name field
                        OutlinedTextField(
                            value = employeeName,
                            onValueChange = { employeeName = it },
                            label = { Text("Employee Name") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = employee == null,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        // Employee type selector
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = employeeType.toString(),
                                onValueChange = { },
                                label = { Text("Employee Type") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                enabled = employee == null,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select type",
                                        modifier = Modifier.clickable {
                                            if (employee == null) {
                                                showEmployeeTypeMenu = true
                                            }
                                        }
                                    )
                                }
                            )
                            
                            DropdownMenu(
                                expanded = showEmployeeTypeMenu,
                                onDismissRequest = { showEmployeeTypeMenu = false }
                            ) {
                                EmployeeType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.toString()) },
                                        onClick = {
                                            employeeType = type
                                            showEmployeeTypeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Time details section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Time Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Date selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showDatePicker = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = "Date",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Time selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Start time
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showStartTimePicker = true }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Column(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "Start",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = selectedStartTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // End time
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showEndTimePicker = true }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Column(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "End",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = selectedEndTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // Break duration
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Break Duration",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Text(
                                    text = "${breakMinutes.toInt()} minutes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Slider(
                                value = breakMinutes,
                                onValueChange = { breakMinutes = it },
                                valueRange = 0f..120f,
                                steps = 120,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0 min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = "120 min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Save button
                Button(
                    onClick = {
                        if (employeeName.isNotBlank()) {
                            val startDateTime = LocalDateTime.of(selectedDate, selectedStartTime)
                            var endDateTime = LocalDateTime.of(selectedDate, selectedEndTime)
                            
                            // Handle overnight shifts
                            if (endDateTime.isBefore(startDateTime)) {
                                endDateTime = LocalDateTime.of(selectedDate.plusDays(1), selectedEndTime)
                            }
                            
                            onSave(
                                employeeName,
                                employeeType,
                                startDateTime,
                                endDateTime,
                                breakMinutes.toInt()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = employeeName.isNotBlank()
                ) {
                    Text("Save Timesheet Entry")
                }
                
                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateChange = { 
                selectedDate = it 
                showDatePicker = false
            },
            initialDate = selectedDate
        )
    }
    
    // Start time picker dialog
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onTimeChange = {
                selectedStartTime = LocalTime.of(it.hour, it.minute)
                showStartTimePicker = false
            },
            initialTime = LocalTime.of(selectedStartTime.hour, selectedStartTime.minute)
        )
    }
    
    // End time picker dialog
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onTimeChange = {
                selectedEndTime = LocalTime.of(it.hour, it.minute)
                showEndTimePicker = false
            },
            initialTime = LocalTime.of(selectedEndTime.hour, selectedEndTime.minute)
        )
    }
}
