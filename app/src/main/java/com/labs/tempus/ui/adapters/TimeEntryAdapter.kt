package com.labs.tempus.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.labs.tempus.R
import com.labs.tempus.model.TimeEntry

/**
 * Adapter for displaying time entries in a RecyclerView
 */
class TimeEntryAdapter(
    private val onEditClick: (TimeEntry) -> Unit,
    private val onDeleteClick: (TimeEntry) -> Unit
) : ListAdapter<TimeEntry, TimeEntryAdapter.TimeEntryViewHolder>(TimeEntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_entry, parent, false)
        return TimeEntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeEntryViewHolder, position: Int) {
        val timeEntry = getItem(position)
        holder.bind(timeEntry)
    }

    inner class TimeEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.text_entry_date)
        private val clockInTextView: TextView = itemView.findViewById(R.id.text_clock_in_time)
        private val clockOutTextView: TextView = itemView.findViewById(R.id.text_clock_out_time)
        private val breakTimeTextView: TextView = itemView.findViewById(R.id.text_break_time)
        private val hoursWorkedTextView: TextView = itemView.findViewById(R.id.text_hours_worked)
        private val editButton: ImageButton = itemView.findViewById(R.id.button_edit_entry)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_entry)

        fun bind(timeEntry: TimeEntry) {
            // Set date
            dateTextView.text = timeEntry.getFormattedDate()
            
            // Set clock in time
            clockInTextView.text = "In: ${timeEntry.getFormattedClockInTime()}"
            
            // Set clock out time if available
            if (timeEntry.clockOutTime != null) {
                clockOutTextView.text = "Out: ${timeEntry.getFormattedClockOutTime()}"
                clockOutTextView.visibility = View.VISIBLE
            } else {
                clockOutTextView.text = "Out: Not clocked out"
                clockOutTextView.visibility = View.VISIBLE
            }
            
            // Set break time
            breakTimeTextView.text = "Break: ${timeEntry.breakMinutes} min"
            
            // Set hours worked
            hoursWorkedTextView.text = "Hours: ${String.format("%.2f", timeEntry.getHoursWorked())}"
            
            // Set click listeners
            editButton.setOnClickListener {
                onEditClick(timeEntry)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(timeEntry)
            }
        }
    }

    class TimeEntryDiffCallback : DiffUtil.ItemCallback<TimeEntry>() {
        override fun areItemsTheSame(oldItem: TimeEntry, newItem: TimeEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimeEntry, newItem: TimeEntry): Boolean {
            return oldItem.clockInTime == newItem.clockInTime &&
                   oldItem.clockOutTime == newItem.clockOutTime &&
                   oldItem.breakMinutes == newItem.breakMinutes
        }
    }
}