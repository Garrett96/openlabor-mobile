package com.labs.openlabor

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
import com.labs.openlabor.data.EmployeeRepository
import com.labs.openlabor.databinding.ActivityQrScannerBinding
import com.labs.openlabor.model.Employee
import com.labs.openlabor.model.EmployeeType
import com.labs.openlabor.model.TimeEntry
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.UUID

class QRScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var repository: EmployeeRepository
    private val gson = Gson()

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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_qrcode)

        repository = EmployeeRepository.getInstance(this)

        barcodeView = binding.barcodeScanner

        binding.buttonClose.setOnClickListener {
            finish()
        }

        binding.buttonClockIn.setOnClickListener {
            currentEmployee?.let { employee ->
                clockInEmployee(employee)
            }
        }

        binding.buttonClockOut.setOnClickListener {
            currentEmployee?.let { employee ->
                clockOutEmployee(employee)
            }
        }

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
                if (!scanPaused && result.text != lastScannedContent) {
                    scanPaused = true
                    lastScannedContent = result.text

                    processQRContent(result.text)

                    barcodeView.postDelayed({
                        scanPaused = false
                    }, 3000)
                }
            }
        })

        binding.textScanResult.text = getString(R.string.scan_ready)
    }

    private fun processQRContent(content: String) {
        try {
            val jsonObject = gson.fromJson(content, JsonObject::class.java)

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

            val existingEmployee = repository.getEmployeeById(employeeId)

            if (existingEmployee != null) {
                showMessage("Employee found: ${existingEmployee.name}")

                currentEmployee = existingEmployee
                updateActionButtons(existingEmployee)
            } else {
                val employeeType = when (employeeTypeStr) {
                    "Staff" -> EmployeeType.STAFF
                    "Temporary" -> EmployeeType.TEMP
                    "Contractor" -> EmployeeType.CONTRACTOR
                    "Manager" -> EmployeeType.MANAGER
                    else -> EmployeeType.STAFF
                }

                val newEmployee = repository.addEmployee(name, employeeType)
                showMessage("Added new employee: $name")

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

            val employee = repository.getEmployeeByName(employeeName)

            if (employee != null) {
                val existingEntry = employee.timeEntries.find { it.id == timeEntryId }

                if (existingEntry == null) {
                    val clockInTime = LocalDateTime.parse(clockInStr)
                    val clockOutTime = if (clockOutStr.isNotEmpty())
                        LocalDateTime.parse(clockOutStr) else null

                    val timeEntry = TimeEntry(
                        id = timeEntryId,
                        clockInTime = clockInTime,
                        clockOutTime = clockOutTime,
                        breakMinutes = breakMinutes
                    )

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
            binding.buttonClockIn.visibility = View.GONE
            binding.buttonClockOut.visibility = View.VISIBLE

            val timeEntry = employee.getCurrentTimeEntry()
            val clockInTime = timeEntry?.getFormattedClockInTime() ?: ""
            showMessage("${employee.name} is currently clocked in (since $clockInTime)")
        } else {
            binding.buttonClockIn.visibility = View.VISIBLE
            binding.buttonClockOut.visibility = View.GONE
            showMessage("${employee.name} is not clocked in")
        }
    }

    private fun clockInEmployee(employee: Employee) {
        if (employee.isClockedIn()) {
            showMessage("${employee.name} is already clocked in")
            return
        }

        val timeEntry = TimeEntry(
            id = UUID.randomUUID().toString(),
            clockInTime = LocalDateTime.now()
        )

        employee.timeEntries.add(timeEntry)
        repository.updateEmployee(employee)

        showMessage("${employee.name} clocked in successfully at ${timeEntry.getFormattedClockInTime()}")

        updateActionButtons(employee)
    }

    private fun clockOutEmployee(employee: Employee) {
        if (!employee.isClockedIn()) {
            showMessage("${employee.name} is not clocked in")
            return
        }

        val timeEntry = employee.getCurrentTimeEntry()
        timeEntry?.let {
            it.clockOutTime = LocalDateTime.now()

            repository.updateEmployee(employee)

            val hours = it.getHoursWorked()
            showMessage("${employee.name} clocked out successfully\nHours: $hours")

            updateActionButtons(employee)
        }
    }

    private fun showMessage(message: String) {
        binding.textScanResult.text = message

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

    private fun showError(error: String) {
        binding.textScanResult.text = "Error: $error"

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
