package com.labs.openlabor.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.labs.openlabor.R
import com.labs.openlabor.model.Employee
import com.labs.openlabor.model.TimeEntry
import com.labs.openlabor.ui.dialogs.TimeEntryDialogFragment

/**
 * Adapter for displaying employee summary information
 */
class EmployeeSummaryAdapter(
    private val onEditTimeEntry: (Employee, TimeEntry) -> Unit = { _, _ -> }
) : ListAdapter<Employee, EmployeeSummaryAdapter.SummaryViewHolder>(SummaryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employee_summary, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val employee = getItem(position)
        holder.bind(employee)
    }

    class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_summary_employee_name)
        private val typeTextView: TextView = itemView.findViewById(R.id.text_summary_employee_type)
        private val hoursTextView: TextView = itemView.findViewById(R.id.text_summary_hours)
        private val timeEntriesRecyclerView: RecyclerView = itemView.findViewById(R.id.recycler_time_entries)
        private val expandButton: ImageButton = itemView.findViewById(R.id.button_expand_entries)

        private var isExpanded = false
        private var timesheetAdapter: TimesheetEntryAdapter? = null
        private var boundEmployee: Employee? = null

        init {
            expandButton.setOnClickListener {
                toggleExpansion()
            }

            // Set up time entries recycler view
            timeEntriesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            timeEntriesRecyclerView.visibility = View.GONE
        }

        fun bind(employee: Employee) {
            boundEmployee = employee
            nameTextView.text = employee.name
            typeTextView.text = employee.type.toString()
            hoursTextView.text = String.format("%.2f hrs", employee.getTotalHours())

            // Create timesheet adapter if needed
            if (timesheetAdapter == null) {
                timesheetAdapter = TimesheetEntryAdapter { timeEntry ->
                    boundEmployee?.let { emp ->
                        (itemView.context as? androidx.fragment.app.FragmentActivity)?.let { activity ->
                            val dialog = TimeEntryDialogFragment.newInstance(emp.id, timeEntry)
                            dialog.setOnSaveListener { clockInTime, clockOutTime, breakMinutes ->
                                // Find the time entry in the employee's list
                                val index = emp.timeEntries.indexOfFirst { it.id == timeEntry.id }
                                if (index != -1) {
                                    // Create updated time entry
                                    val updatedTimeEntry = TimeEntry(
                                        id = timeEntry.id,
                                        clockInTime = clockInTime,
                                        clockOutTime = clockOutTime,
                                        breakMinutes = breakMinutes
                                    )

                                    // Replace the old entry with the updated one
                                    emp.timeEntries[index] = updatedTimeEntry

                                    // Refresh the timesheet view
                                    refreshTimeEntries()
                                }
                            }
                            dialog.show(activity.supportFragmentManager, "EditTimeEntry")
                        }
                    }
                }
                timeEntriesRecyclerView.adapter = timesheetAdapter
            }

            // Reset expansion state
            isExpanded = false
            timeEntriesRecyclerView.visibility = View.GONE
            expandButton.setImageResource(android.R.drawable.arrow_down_float)

            // Update the time entries
            refreshTimeEntries()
        }

        private fun toggleExpansion() {
            isExpanded = !isExpanded
            timeEntriesRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE
            expandButton.setImageResource(
                if (isExpanded) android.R.drawable.arrow_up_float
                else android.R.drawable.arrow_down_float
            )
        }

        private fun refreshTimeEntries() {
            boundEmployee?.let { employee ->
                timesheetAdapter?.submitList(employee.timeEntries.sortedByDescending { it.clockInTime })
            }
        }
    }

    class SummaryDiffCallback : DiffUtil.ItemCallback<Employee>() {
        override fun areItemsTheSame(oldItem: Employee, newItem: Employee): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Employee, newItem: Employee): Boolean {
            return oldItem.name == newItem.name &&
                   oldItem.type == newItem.type &&
                   oldItem.getTotalHours() == newItem.getTotalHours()
        }
    }
}
