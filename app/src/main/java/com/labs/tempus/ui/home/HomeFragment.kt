package com.labs.tempus.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.labs.tempus.R
import com.labs.tempus.databinding.FragmentHomeBinding
import com.labs.tempus.model.Employee
import com.labs.tempus.model.EmployeeType
import com.labs.tempus.model.TimeEntry
import com.labs.tempus.ui.adapters.EmployeeAdapter
import com.labs.tempus.ui.adapters.TimeEntryAdapter
import java.util.Timer
import java.util.TimerTask

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var employeeAdapter: EmployeeAdapter
    
    private var refreshTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupObservers()
        setupFab()
        startAutoRefresh()

        return root
    }
    
    /**
     * Refreshes employee data from the repository
     */
    fun refreshData() {
        homeViewModel.loadEmployees()
    }
    
    /**
     * Starts a timer to refresh data every 2 seconds
     */
    private fun startAutoRefresh() {
        stopAutoRefresh() // Stop any existing timer
        
        refreshTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    activity?.runOnUiThread {
                        refreshData()
                    }
                }
            }, 0, 2000) // Initial delay 0ms, then every 2000ms (2 seconds)
        }
    }
    
    /**
     * Stops the auto-refresh timer
     */
    private fun stopAutoRefresh() {
        refreshTimer?.cancel()
        refreshTimer = null
    }
    
    private fun setupRecyclerView() {
        employeeAdapter = EmployeeAdapter(
            onClockInClick = { employee ->
                clockInEmployee(employee)
            },
            onClockOutClick = { employee ->
                showClockOutDialog(employee)
            },
            onEditClick = { employee ->
                showEditEmployeeDialog(employee)
            },
            onDeleteClick = { employee ->
                showDeleteConfirmationDialog(employee)
            },
            onViewTimeEntriesClick = { employee ->
                showTimeEntriesDialog(employee)
            }
        )
        
        binding.recyclerEmployees.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = employeeAdapter
        }
    }
    
    private fun setupObservers() {
        homeViewModel.employees.observe(viewLifecycleOwner) { employees ->
            employeeAdapter.submitList(employees)
            
            // Show "no employees" message if the list is empty
            binding.textNoEmployees.visibility = if (employees.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupFab() {
        binding.fabAddEmployee.setOnClickListener {
            showAddEmployeeDialog()
        }
    }
    
    private fun showAddEmployeeDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }
        
        // Name input
        val nameEditText = EditText(context).apply {
            hint = getString(R.string.label_name)
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
            .setTitle(R.string.dialog_add_employee)
            .setView(layout)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val name = nameEditText.text.toString().trim()
                if (name.isNotEmpty()) {
                    val selectedType = EmployeeType.values()[typeSpinner.selectedItemPosition]
                    homeViewModel.addEmployee(name, selectedType)
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
    
    private fun showEditEmployeeDialog(employee: Employee) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }
        
        // Name input
        val nameEditText = EditText(context).apply {
            hint = getString(R.string.label_name)
            setText(employee.name)
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
        typeSpinner.setSelection(EmployeeType.values().indexOf(employee.type))
        layout.addView(typeSpinner)
        
        // Create and show dialog
        AlertDialog.Builder(context, R.style.Theme_Tempus_Dialog)
            .setTitle(R.string.dialog_edit_employee)
            .setView(layout)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val name = nameEditText.text.toString().trim()
                if (name.isNotEmpty()) {
                    val updatedEmployee = employee.copy(
                        name = name,
                        type = EmployeeType.values()[typeSpinner.selectedItemPosition]
                    )
                    homeViewModel.updateEmployee(updatedEmployee)
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
    
    private fun showDeleteConfirmationDialog(employee: Employee) {
        AlertDialog.Builder(requireContext(), R.style.Theme_Tempus_Dialog)
            .setTitle("Delete Employee")
            .setMessage("Are you sure you want to delete ${employee.name}?")
            .setPositiveButton(R.string.action_delete) { _, _ ->
                homeViewModel.deleteEmployee(employee.id)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
    
    private fun clockInEmployee(employee: Employee) {
        homeViewModel.clockIn(employee.id)
    }
    
    private fun showClockOutDialog(employee: Employee) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }
        
        // Break time seek bar
        val breakMinutes = intArrayOf(30) // Default value, using array to make it mutable
        
        val breakTimeLabel = TextView(context).apply {
            text = "Break time: ${breakMinutes[0]} minutes"
        }
        layout.addView(breakTimeLabel)
        
        val breakTimeSeekBar = SeekBar(context).apply {
            max = 120 // Max 2 hours
            progress = breakMinutes[0]
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    breakMinutes[0] = progress
                    breakTimeLabel.text = "Break time: ${breakMinutes[0]} minutes"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        layout.addView(breakTimeSeekBar)
        
        // Create and show dialog
        AlertDialog.Builder(context, R.style.Theme_Tempus_Dialog)
            .setTitle(R.string.dialog_clock_out)
            .setView(layout)
            .setPositiveButton(R.string.action_clock_out) { _, _ ->
                homeViewModel.clockOut(employee.id, breakMinutes[0])
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
    
    /**
     * Shows a dialog with time entries for an employee
     */
    private fun showTimeEntriesDialog(employee: Employee) {
        val dialog = com.labs.tempus.ui.dialogs.TimeEntriesDialogFragment.newInstance(employee)
        dialog.show(childFragmentManager, "TimeEntriesDialog")
    }
    
    override fun onDestroyView() {
        stopAutoRefresh()
        super.onDestroyView()
        _binding = null
    }
    
    override fun onPause() {
        stopAutoRefresh()
        super.onPause()
    }
    
    override fun onResume() {
        super.onResume()
        startAutoRefresh()
    }
}