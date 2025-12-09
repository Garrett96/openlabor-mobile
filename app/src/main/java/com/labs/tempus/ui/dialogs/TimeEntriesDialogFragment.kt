package com.labs.openlabor-mobile.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.openlabor-mobile.R
import com.labs.openlabor-mobile.model.Employee
import com.labs.openlabor-mobile.model.TimeEntry
import com.labs.openlabor-mobile.ui.adapters.TimeEntryAdapter
import com.labs.openlabor-mobile.ui.home.HomeViewModel
import java.time.LocalDateTime
import java.io.Serializable

/**
 * Dialog fragment for displaying and managing time entries for an employee
 */
class TimeEntriesDialogFragment : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addEntryButton: Button
    private lateinit var noEntriesText: TextView
    private lateinit var employeeNameTitle: TextView

    private lateinit var timeEntryAdapter: TimeEntryAdapter
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var employee: Employee

    companion object {
        private const val ARG_EMPLOYEE = "employee"

        fun newInstance(employee: Employee): TimeEntriesDialogFragment {
            return TimeEntriesDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_EMPLOYEE, employee)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel = ViewModelProvider(requireParentFragment()).get(HomeViewModel::class.java)
        @Suppress("DEPRECATION")
        employee = arguments?.getSerializable(ARG_EMPLOYEE) as Employee
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create dialog view programmatically
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        // Create title text view
        employeeNameTitle = TextView(context).apply {
            text = "Time Entries for ${employee.name}"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 20)
            id = View.generateViewId()
        }
        layout.addView(employeeNameTitle)

        // Create "no entries" text view
        noEntriesText = TextView(context).apply {
            text = "No time entries recorded yet"
            gravity = Gravity.CENTER
            visibility = View.GONE
            id = View.generateViewId()
        }
        layout.addView(noEntriesText)

        // Create RecyclerView
        recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                800 // Fixed height for dialog, can be adjusted
            )
        }
        layout.addView(recyclerView)

        // Create Add Entry button
        addEntryButton = Button(context).apply {
            text = "Add Time Entry"
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
        }
        layout.addView(addEntryButton)

        // Set up RecyclerView
        setupRecyclerView()

        // Update UI based on entries
        updateEntryListUI()

        // Set up add button
        addEntryButton.setOnClickListener {
            showAddTimeEntryDialog()
        }

        // Create dialog
        return AlertDialog.Builder(requireContext())
            .setView(layout)
            .create()
    }

    private fun setupRecyclerView() {
        timeEntryAdapter = TimeEntryAdapter(
            onEditClick = { timeEntry ->
                showEditTimeEntryDialog(timeEntry)
            },
            onDeleteClick = { timeEntry ->
                showDeleteTimeEntryConfirmation(timeEntry)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timeEntryAdapter
        }

        // Submit current list of time entries
        timeEntryAdapter.submitList(employee.timeEntries.sortedByDescending { it.clockInTime })
    }

    private fun updateEntryListUI() {
        if (employee.timeEntries.isEmpty()) {
            noEntriesText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noEntriesText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            timeEntryAdapter.submitList(employee.timeEntries.sortedByDescending { it.clockInTime })
        }
    }

    private fun showAddTimeEntryDialog() {
        val dialog = TimeEntryDialogFragment.newInstance(employee.id)
        dialog.setOnSaveListener { clockInTime, clockOutTime, breakMinutes ->
            addNewTimeEntry(clockInTime, clockOutTime, breakMinutes)
        }
        dialog.show(childFragmentManager, "AddTimeEntry")
    }

    private fun showEditTimeEntryDialog(timeEntry: TimeEntry) {
        val dialog = TimeEntryDialogFragment.newInstance(employee.id, timeEntry)
        dialog.setOnSaveListener { clockInTime, clockOutTime, breakMinutes ->
            updateTimeEntry(timeEntry, clockInTime, clockOutTime, breakMinutes)
        }
        dialog.show(childFragmentManager, "EditTimeEntry")
    }

    private fun showDeleteTimeEntryConfirmation(timeEntry: TimeEntry) {
        AlertDialog.Builder(requireContext(), R.style.Theme_openlabor-mobile_Dialog)
            .setTitle("Delete Time Entry")
            .setMessage("Are you sure you want to delete this time entry?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTimeEntry(timeEntry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNewTimeEntry(clockInTime: LocalDateTime, clockOutTime: LocalDateTime, breakMinutes: Int) {
        // Create a new time entry
        val newTimeEntry = TimeEntry(
            clockInTime = clockInTime,
            clockOutTime = clockOutTime,
            breakMinutes = breakMinutes
        )

        // Add to employee's time entries
        employee.timeEntries.add(newTimeEntry)

        // Update the employee in the repository
        homeViewModel.updateEmployee(employee)

        // Update UI
        updateEntryListUI()
    }

    private fun updateTimeEntry(
        timeEntry: TimeEntry,
        clockInTime: LocalDateTime,
        clockOutTime: LocalDateTime,
        breakMinutes: Int
    ) {
        // Find the time entry in the employee's list
        val index = employee.timeEntries.indexOfFirst { it.id == timeEntry.id }
        if (index != -1) {
            // Create updated time entry
            val updatedTimeEntry = TimeEntry(
                id = timeEntry.id,
                clockInTime = clockInTime,
                clockOutTime = clockOutTime,
                breakMinutes = breakMinutes
            )

            // Replace the old entry with the updated one
            employee.timeEntries[index] = updatedTimeEntry

            // Update the employee in the repository
            homeViewModel.updateEmployee(employee)

            // Update UI
            updateEntryListUI()
        }
    }

    private fun deleteTimeEntry(timeEntry: TimeEntry) {
        // Remove time entry from employee's list
        employee.timeEntries.removeIf { it.id == timeEntry.id }

        // Update the employee in the repository
        homeViewModel.updateEmployee(employee)

        // Update UI
        updateEntryListUI()
    }
}
