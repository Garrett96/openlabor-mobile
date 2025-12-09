package com.labs.openlabor-mobile.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.labs.openlabor-mobile.data.EmployeeRepository
import com.labs.openlabor-mobile.databinding.FragmentSummaryBinding
import com.labs.openlabor-mobile.model.Employee
import com.labs.openlabor-mobile.model.EmployeeType
import com.labs.openlabor-mobile.model.TimeEntry
import com.labs.openlabor-mobile.ui.adapters.EmployeeSummaryAdapter
import com.labs.openlabor-mobile.ui.dialogs.TimeEntryDialogFragment

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var summaryViewModel: SummaryViewModel
    private lateinit var employeeSummaryAdapter: EmployeeSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        summaryViewModel = ViewModelProvider(this).get(SummaryViewModel::class.java)

        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupObservers()

        return root
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    /**
     * Refreshes summary data from the repository
     */
    fun refreshData() {
        summaryViewModel.refreshData()
    }

    private fun setupRecyclerView() {
        employeeSummaryAdapter = EmployeeSummaryAdapter(
            onEditTimeEntry = { employee, timeEntry ->
                showEditTimeEntryDialog(employee, timeEntry)
            }
        )

        binding.recyclerEmployeeSummary.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = employeeSummaryAdapter
        }
    }

    /**
     * Shows dialog to edit a time entry
     */
    private fun showEditTimeEntryDialog(employee: Employee, timeEntry: TimeEntry) {
        val dialog = TimeEntryDialogFragment.newInstance(employee.id, timeEntry)
        dialog.setOnSaveListener { clockInTime, clockOutTime, breakMinutes ->
            updateTimeEntry(employee, timeEntry, clockInTime, clockOutTime, breakMinutes)
        }
        dialog.show(childFragmentManager, "EditTimeEntry")
    }

    /**
     * Updates a time entry and refreshes the data
     */
    private fun updateTimeEntry(
        employee: Employee,
        timeEntry: TimeEntry,
        clockInTime: java.time.LocalDateTime,
        clockOutTime: java.time.LocalDateTime,
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
            val repository = EmployeeRepository.getInstance(requireContext())
            repository.updateEmployee(employee)

            // Refresh data
            refreshData()
        }
    }

    private fun setupObservers() {
        // Observe employees
        summaryViewModel.employees.observe(viewLifecycleOwner) { employees ->
            employeeSummaryAdapter.submitList(employees)

            // Show "no employees" message if the list is empty
            binding.textNoEmployeesSummary.visibility = if (employees.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe total hours
        summaryViewModel.totalHours.observe(viewLifecycleOwner) { totalHours ->
            binding.textTotalHours.text = summaryViewModel.getFormattedTotalHours()
        }

        // Observe hours by type
        summaryViewModel.hoursByType.observe(viewLifecycleOwner) { hoursByType ->
            // Update hours by type
            updateHoursByType()
        }
    }

    private fun updateHoursByType() {
        binding.textStaffHours.text = "Staff: ${summaryViewModel.getFormattedHoursByType(EmployeeType.STAFF)}"
        binding.textTempHours.text = "Temporary: ${summaryViewModel.getFormattedHoursByType(EmployeeType.TEMP)}"
        binding.textContractorHours.text = "Contractor: ${summaryViewModel.getFormattedHoursByType(EmployeeType.CONTRACTOR)}"
        binding.textManagerHours.text = "Manager: ${summaryViewModel.getFormattedHoursByType(EmployeeType.MANAGER)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
