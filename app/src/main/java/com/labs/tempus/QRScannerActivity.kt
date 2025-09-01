package com.labs.tempus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.labs.tempus.data.EmployeeRepository
import com.labs.tempus.databinding.ActivityQrScannerBinding
import com.labs.tempus.model.Employee
import com.labs.tempus.model.EmployeeType
import com.labs.tempus.model.TimeEntry
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.UUID

class QRScannerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var repository: EmployeeRepository
    private val gson = Gson()
    
    // Current employee being processed
    private var currentEmployee: Employee? = null
    private var scanPaused = false
    private var lastScannedContent = ""
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up action bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_qrcode)
        
        // Initialize repository
        repository = EmployeeRepository.getInstance(this)
        
        // Set up barcode scanner
        barcodeView = binding.barcodeScanner
        
        // Handle close button
        binding.buttonClose.setOnClickListener {
            finish()
        }
        
        // Handle clock-in button
        binding.buttonClockIn.setOnClickListener {
            currentEmployee?.let { employee ->
                clockInEmployee(employee)
            }
        }
        
        // Handle clock-out button
        binding.buttonClockOut.setOnClickListener {
            currentEmployee?.let { employee ->
                clockOutEmployee(employee)
            }
        }
        
        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, 
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            setupScanner()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupScanner()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to scan QR codes",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
    
    private fun setupScanner() {
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                // Only process if scanning is not paused and content is new
                if (!scanPaused && result.text != lastScannedContent) {
                    // Pause scanning temporarily to avoid multiple scans
                    scanPaused = true
                    lastScannedContent = result.text
                     
                    // Process the QR code content
                    processQRContent(result.text)
                     
                    // Resume scanning after a delay (3 seconds)
                    barcodeView.postDelayed({
                        scanPaused = false
                    }, 3000)
                }
            }
        })
        
        // Show initial status
        binding.textScanResult.text = getString(R.string.scan_ready)
    }
    
    private fun processQRContent(content: String) {
        try {
            // Parse the JSON content
            val jsonObject = gson.fromJson(content, JsonObject::class.java)
            
            // Get the type of QR code (employee, timeEntry, summary)
            when (jsonObject.get("type")?.asString) {
                "employee" -> processEmployeeQR(jsonObject)
                "timeEntry" -> processTimeEntryQR(jsonObject)
                "summary" -> processSummaryQR(jsonObject)
                else -> showError("Unknown QR code type")
            }
            
        } catch (e: JsonSyntaxException) {
            showError("Invalid QR code format: ${e.message}")
        } catch (e: NullPointerException) {
            showError("Missing required field in QR code")
        } catch (e: Exception) {
            showError("Error processing QR code: ${e.message}")
        }
    }
    
    private fun processEmployeeQR(jsonObject: JsonObject) {
        try {
            val employeeId = jsonObject.get("id").asString
            val name = jsonObject.get("name").asString
            val employeeTypeStr = jsonObject.get("employeeType").asString
            
            // Check if employee already exists
            val existingEmployee = repository.getEmployeeById(employeeId)
            
            if (existingEmployee != null) {
                // Employee exists, show confirmation and options
                showMessage("Employee found: ${existingEmployee.name}")
                
                // Store the current employee and show appropriate buttons
                currentEmployee = existingEmployee
                updateActionButtons(existingEmployee)
            } else {
                // Employee doesn't exist, ask to add
                val employeeType = when (employeeTypeStr) {
                    "Staff" -> EmployeeType.STAFF
                    "Temporary" -> EmployeeType.TEMP
                    "Contractor" -> EmployeeType.CONTRACTOR
                    "Manager" -> EmployeeType.MANAGER
                    else -> EmployeeType.STAFF
                }
                
                // Add employee and show confirmation
                val newEmployee = repository.addEmployee(name, employeeType)
                showMessage("Added new employee: $name")
                
                // Store the current employee and show appropriate buttons
                currentEmployee = newEmployee
                updateActionButtons(newEmployee)
            }
        } catch (e: Exception) {
            showError("Error processing employee data: ${e.message}")
        }
    }
    
    private fun processTimeEntryQR(jsonObject: JsonObject) {
        try {
            val timeEntryId = jsonObject.get("id").asString
            val employeeName = jsonObject.get("employeeName").asString
            val clockInStr = jsonObject.get("clockIn").asString
            val clockOutStr = jsonObject.get("clockOut")?.asString ?: ""
            val breakMinutes = jsonObject.get("breakMinutes").asInt
            
            // Find employee by name
            val employee = repository.getEmployeeByName(employeeName)
            
            if (employee != null) {
                // Check if time entry already exists
                val existingEntry = employee.timeEntries.find { it.id == timeEntryId }
                
                if (existingEntry == null) {
                    // Create and add the time entry
                    val clockInTime = LocalDateTime.parse(clockInStr)
                    val clockOutTime = if (clockOutStr.isNotEmpty()) 
                        LocalDateTime.parse(clockOutStr) else null
                    
                    val timeEntry = TimeEntry(
                        id = timeEntryId,
                        clockInTime = clockInTime,
                        clockOutTime = clockOutTime,
                        breakMinutes = breakMinutes
                    )
                    
                    // Add the time entry
                    employee.timeEntries.add(timeEntry)
                    repository.updateEmployee(employee)
                    
                    showMessage("Added time entry for $employeeName: ${timeEntry.getFormattedHours()}")
                } else {
                    showMessage("Time entry already exists for $employeeName")
                }
            } else {
                showError("Employee not found: $employeeName")
            }
        } catch (e: DateTimeParseException) {
            showError("Invalid date format in QR code: ${e.message}")
        } catch (e: Exception) {
            showError("Error processing time entry: ${e.message}")
        }
    }
    
    private fun processSummaryQR(jsonObject: JsonObject) {
        try {
            val employeeId = jsonObject.get("employeeId").asString
            val employeeName = jsonObject.get("employeeName").asString
            val totalHours = jsonObject.get("totalHours").asFloat
            val todayHours = jsonObject.get("todayHours").asFloat
            
            // Show summary information
            val message = "Summary for $employeeName:\n" +
                          "Total Hours: $totalHours\n" +
                          "Today's Hours: $todayHours"
            
            showMessage(message)
        } catch (e: Exception) {
            showError("Error processing summary: ${e.message}")
        }
    }
    
    private fun updateActionButtons(employee: Employee) {
        if (employee.isClockedIn()) {
            // Employee is clocked in, show clock-out button
            binding.buttonClockIn.visibility = View.GONE
            binding.buttonClockOut.visibility = View.VISIBLE
            
            val timeEntry = employee.getCurrentTimeEntry()
            val clockInTime = timeEntry?.getFormattedClockInTime() ?: ""
            showMessage("${employee.name} is currently clocked in (since $clockInTime)")
        } else {
            // Employee is not clocked in, show clock-in button
            binding.buttonClockIn.visibility = View.VISIBLE
            binding.buttonClockOut.visibility = View.GONE
            showMessage("${employee.name} is not clocked in")
        }
    }
    
    private fun clockInEmployee(employee: Employee) {
        // Check if already clocked in
        if (employee.isClockedIn()) {
            showMessage("${employee.name} is already clocked in")
            return
        }
        
        // Create a new time entry
        val timeEntry = TimeEntry(
            id = UUID.randomUUID().toString(),
            clockInTime = LocalDateTime.now()
        )
        
        // Add to employee and update
        employee.timeEntries.add(timeEntry)
        repository.updateEmployee(employee)
        
        showMessage("${employee.name} clocked in successfully at ${timeEntry.getFormattedClockInTime()}")
        
        // Update buttons
        updateActionButtons(employee)
    }
    
    private fun clockOutEmployee(employee: Employee) {
        // Check if clocked in
        if (!employee.isClockedIn()) {
            showMessage("${employee.name} is not clocked in")
            return
        }
        
        // Get the current time entry
        val timeEntry = employee.getCurrentTimeEntry()
        timeEntry?.let {
            // Set clock-out time
            it.clockOutTime = LocalDateTime.now()
            
            // Update repository
            repository.updateEmployee(employee)
            
            // Show confirmation
            val hours = it.getHoursWorked()
            showMessage("${employee.name} clocked out successfully\nHours: $hours")
            
            // Update buttons
            updateActionButtons(employee)
        }
    }
    
    private fun showMessage(message: String) {
        // Update scan result text
        binding.textScanResult.text = message
        
        // Also show toast for confirmation
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        
        // Vibrate to provide feedback (if we had permission)
        // Keeping this simple for now - would need VIBRATE permission
    }
    
    private fun showError(error: String) {
        // Update scan result text with error
        binding.textScanResult.text = "Error: $error"
        
        // Also show toast for confirmation
        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
    }
    
    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }
    
    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}