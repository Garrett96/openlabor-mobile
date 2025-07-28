package com.labs.tempus.util

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.FragmentManager
import com.labs.tempus.R
import com.labs.tempus.data.EmployeeRepository
import com.labs.tempus.model.Employee
import com.labs.tempus.model.EmployeeType
import com.labs.tempus.ui.dialogs.TimeEntryDialogFragment

/**
 * Helper class to handle showing the add time entry dialog
 */
object AddTimeEntryHelper {

    /**
     * Shows a dialog to select an employee and add a time entry
     * 
     * @param context The context to show the dialog in
     * @param fragmentManager The fragment manager to use for showing the dialog
     * @param repository The employee repository to get employees from
     */
    fun showAddTimeEntryDialog(context: Context, fragmentManager: FragmentManager, repository: EmployeeRepository) {
        // Get all employees
        val employees = repository.getAllEmployees()
        
        // If no employees, go directly to create one
        if (employees.isEmpty()) {
            showAddEmployeeDialog(context, fragmentManager, repository)
            return
        }
        
        // Create array of employee names
        val employeeNames = employees.map { it.name }.toTypedArray()
        
        // Show dialog to select employee
        AlertDialog.Builder(context, R.style.Theme_Tempus_Dialog)
            .setTitle("Select Employee")
            .setItems(employeeNames) { _, which ->
                val selectedEmployee = employees[which]
                showTimeEntryDialog(fragmentManager, selectedEmployee)
            }
            .setPositiveButton("Add New Employee") { _, _ ->
                showAddEmployeeDialog(context, fragmentManager, repository)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Shows the time entry dialog for an employee
     * 
     * @param fragmentManager The fragment manager to use for showing the dialog
     * @param employee The employee to add a time entry for
     */
    private fun showTimeEntryDialog(fragmentManager: FragmentManager, employee: Employee) {
        val dialog = TimeEntryDialogFragment.newInstance(employee.id)
        dialog.show(fragmentManager, "AddTimeEntry")
    }
    
    /**
     * Shows dialog to add a new employee
     * 
     * @param context The context to show the dialog in
     * @param fragmentManager The fragment manager for showing the time entry dialog after
     * @param repository The employee repository to add the employee to
     */
    private fun showAddEmployeeDialog(context: Context, fragmentManager: FragmentManager, repository: EmployeeRepository) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }
        
        // Name input
        val nameEditText = EditText(context).apply {
            hint = "Employee Name"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        layout.addView(nameEditText)
        
        // Employee type spinner
        val typeSpinner = Spinner(context)
        val typeAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            EmployeeType.values().map { it.toString() }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        typeSpinner.adapter = typeAdapter
        layout.addView(typeSpinner)
        
        // Create and show dialog
        AlertDialog.Builder(context, R.style.Theme_Tempus_Dialog)
            .setTitle("Add Employee")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                if (name.isNotEmpty()) {
                    val selectedType = EmployeeType.values()[typeSpinner.selectedItemPosition]
                    val newEmployee = repository.addEmployee(name, selectedType)
                    
                    // After adding employee, proceed to time entry dialog
                    showTimeEntryDialog(fragmentManager, newEmployee)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // If employee creation is cancelled, go back to the employee selection dialog
                showAddTimeEntryDialog(context, fragmentManager, repository)
            }
            .show()
    }
}