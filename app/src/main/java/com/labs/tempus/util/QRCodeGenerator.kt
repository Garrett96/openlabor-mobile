package com.labs.tempus.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.EncodeHintType
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.EnumMap
import com.labs.tempus.model.Employee
import com.labs.tempus.model.TimeEntry

/**
 * Utility class for generating QR codes for the Tempus app
 */
object QRCodeGenerator {
    private const val TAG = "QRCodeGenerator"
    
    /**
     * Generate a QR code bitmap from a string
     * 
     * @param content The content to encode in the QR code
     * @param width The desired width of the QR code (default: 500px)
     * @param height The desired height of the QR code (default: 500px)
     * @return The generated QR code bitmap, or null if generation failed
     */
    fun generateQRCode(content: String, width: Int = 500, height: Int = 500): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 1 // Smaller margin for better scan results
            
            barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, width, height, hints)
        } catch (e: WriterException) {
            Log.e(TAG, "Error generating QR code: ${e.message}")
            null
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid input for QR code: ${e.message}")
            null
        }
    }
    
    /**
     * Generate a QR code for an employee's information
     * 
     * @param employee The employee to generate a QR code for
     * @param width The desired width of the QR code
     * @param height The desired height of the QR code
     * @return The generated QR code bitmap, or null if generation failed
     */
    fun generateEmployeeQRCode(employee: Employee, width: Int = 500, height: Int = 500): Bitmap? {
        // Create JSON representation of employee info (simplified for QR code)
        val content = """
            {
                "type": "employee",
                "id": "${employee.id}",
                "name": "${employee.name}",
                "employeeType": "${employee.type}"
            }
        """.trimIndent()
        
        return generateQRCode(content, width, height)
    }
    
    /**
     * Generate a QR code for a time entry
     * 
     * @param timeEntry The time entry to generate a QR code for
     * @param employeeName The name of the employee (for reference)
     * @param width The desired width of the QR code
     * @param height The desired height of the QR code
     * @return The generated QR code bitmap, or null if generation failed
     */
    fun generateTimeEntryQRCode(timeEntry: TimeEntry, employeeName: String, width: Int = 500, height: Int = 500): Bitmap? {
        // Format dates in ISO format for machine readability
        val clockInStr = timeEntry.clockInTime.toString()
        val clockOutStr = timeEntry.clockOutTime?.toString() ?: ""
        
        val content = """
            {
                "type": "timeEntry",
                "id": "${timeEntry.id}",
                "employeeName": "$employeeName",
                "clockIn": "$clockInStr",
                "clockOut": "$clockOutStr",
                "breakMinutes": ${timeEntry.breakMinutes},
                "hoursWorked": ${timeEntry.getHoursWorked()}
            }
        """.trimIndent()
        
        return generateQRCode(content, width, height)
    }
    
    /**
     * Generate a summary QR code for an employee's total hours
     * 
     * @param employee The employee to generate a summary QR code for
     * @param width The desired width of the QR code
     * @param height The desired height of the QR code
     * @return The generated QR code bitmap, or null if generation failed
     */
    fun generateSummaryQRCode(employee: Employee, width: Int = 500, height: Int = 500): Bitmap? {
        val content = """
            {
                "type": "summary",
                "employeeId": "${employee.id}",
                "employeeName": "${employee.name}",
                "totalHours": ${employee.getTotalHours()},
                "todayHours": ${employee.getTodayHours()},
                "entryCount": ${employee.timeEntries.size}
            }
        """.trimIndent()
        
        return generateQRCode(content, width, height)
    }
}