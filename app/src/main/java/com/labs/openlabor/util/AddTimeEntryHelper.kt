package com.labs.openlabor.util

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.FragmentManager
import com.labs.openlabor.R
import com.labs.openlabor.data.EmployeeRepository
import com.labs.openlabor.model.Employee
import com.labs.openlabor.model.EmployeeType
import com.labs.openlabor.ui.dialogs.TimeEntryDialogFragment

object AddTimeEntryHelper {

    fun showAddTimeEntryDialog(context: Context, fragmentManager: FragmentManager, repository: EmployeeRepository) {
        val employees = repository.getAllEmployees()

        if (employees.isEmpty()) {
            showAddEmployeeDialog(context, fragmentManager, repository)
            return
        }

        val employeeNames = employees.map { it.name }.toTypedArray()

        AlertDialog.Builder(context, R.style.Theme_openlabor_Dialog)
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

    private fun showTimeEntryDialog(fragmentManager: FragmentManager, employee: Employee) {
        val dialog = TimeEntryDialogFragment.newInstance(employee.id)
        dialog.show(fragmentManager, "AddTimeEntry")
    }

    private fun showAddEmployeeDialog(context: Context, fragmentManager: FragmentManager, repository: EmployeeRepository) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val nameEditText = EditText(context).apply {
            hint = "Employee Name"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        layout.addView(nameEditText)

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

        AlertDialog.Builder(context, R.style.Theme_openlabor_Dialog)
            .setTitle("Add Employee")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                if (name.isNotEmpty()) {
                    val selectedType = EmployeeType.values()[typeSpinner.selectedItemPosition]
                    val newEmployee = repository.addEmployee(name, selectedType)

                    showTimeEntryDialog(fragmentManager, newEmployee)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                showAddTimeEntryDialog(context, fragmentManager, repository)
            }
            .show()
    }
}
