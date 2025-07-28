package com.labs.tempus.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.labs.tempus.R
import com.labs.tempus.model.Employee
import com.labs.tempus.model.TimeEntry

class EmployeeAdapter(
    private val onEditClick: (Employee) -> Unit,
    private val onDeleteClick: (Employee) -> Unit,
    private val onViewTimeEntriesClick: (Employee) -> Unit
) : ListAdapter<Employee, EmployeeAdapter.EmployeeViewHolder>(EmployeeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = getItem(position)
        holder.bind(employee)
    }

    inner class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_employee_name)
        private val typeTextView: TextView = itemView.findViewById(R.id.text_employee_type)
        private val totalHoursTextView: TextView = itemView.findViewById(R.id.text_total_hours)
        private val moreButton: ImageButton = itemView.findViewById(R.id.button_more)
        private val timeDetailsContainer: LinearLayout = itemView.findViewById(R.id.container_time_details)
        private val clockedInTextView: TextView = itemView.findViewById(R.id.text_clocked_in)
        private val clockedOutTextView: TextView = itemView.findViewById(R.id.text_clocked_out)

        fun bind(employee: Employee) {
            nameTextView.text = employee.name
            typeTextView.text = employee.type.toString()
            totalHoursTextView.text = "Total Hours: ${String.format("%.2f", employee.getTotalHours())}"

            val isClockedIn = employee.isClockedIn()

            // Show current time entry details if clocked in
            if (isClockedIn) {
                timeDetailsContainer.visibility = View.VISIBLE
                val currentEntry = employee.getCurrentTimeEntry()
                clockedInTextView.text = "Clocked in: ${currentEntry?.getFormattedClockInTime()}"
                clockedOutTextView.visibility = View.GONE
            } else {
                // Show last time entry if available
                val lastEntry = employee.timeEntries.lastOrNull()
                if (lastEntry != null && lastEntry.clockOutTime != null) {
                    timeDetailsContainer.visibility = View.VISIBLE
                    clockedInTextView.text = "Last in: ${lastEntry.getFormattedClockInTime()}"
                    clockedOutTextView.visibility = View.VISIBLE
                    clockedOutTextView.text = "Last out: ${lastEntry.getFormattedClockOutTime()}"
                } else {
                    timeDetailsContainer.visibility = View.GONE
                }
            }

            // Set up click listeners

            moreButton.setOnClickListener { 
                showPopupMenu(it, employee)
            }
        }
        
        private fun showPopupMenu(view: View, employee: Employee) {
            val popup = android.widget.PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.employee_actions, popup.menu)
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_view_entries -> {
                        onViewTimeEntriesClick(employee)
                        true
                    }
                    R.id.action_edit -> {
                        onEditClick(employee)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(employee)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }

    class EmployeeDiffCallback : DiffUtil.ItemCallback<Employee>() {
        override fun areItemsTheSame(oldItem: Employee, newItem: Employee): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Employee, newItem: Employee): Boolean {
            return oldItem == newItem
        }
    }
}