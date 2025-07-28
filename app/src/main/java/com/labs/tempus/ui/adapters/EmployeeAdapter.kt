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
    private val onViewTimeEntriesClick: (Employee) -> Unit,
    private val onAddTimeEntryClick: (Employee) -> Unit
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
        private val addEntryButton: Button = itemView.findViewById(R.id.button_add_entry)
        private val timeDetailsContainer: LinearLayout = itemView.findViewById(R.id.container_time_details)

        fun bind(employee: Employee) {
            nameTextView.text = employee.name
            typeTextView.text = employee.type.toString()
            totalHoursTextView.text = "Total Hours: ${String.format("%.2f", employee.getTotalHours())}"

            // Show last entry summary if available
            val lastEntry = employee.timeEntries.lastOrNull()
            if (lastEntry != null) {
                timeDetailsContainer.visibility = View.VISIBLE
            } else {
                timeDetailsContainer.visibility = View.GONE
            }

            // Set up click listeners
            addEntryButton.setOnClickListener {
                onAddTimeEntryClick(employee)
            }

            moreButton.setOnClickListener { 
                showPopupMenu(it, employee)
            }
        }
        
        private fun showPopupMenu(view: View, employee: Employee) {
            val popup = android.widget.PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.timesheet_actions, popup.menu)
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "View Timesheet Entries" -> {
                        onViewTimeEntriesClick(employee)
                        true
                    }
                    "Add Timesheet Entry" -> {
                        onAddTimeEntryClick(employee)
                        true
                    }
                    "Edit Employee" -> {
                        onEditClick(employee)
                        true
                    }
                    "Delete Employee" -> {
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