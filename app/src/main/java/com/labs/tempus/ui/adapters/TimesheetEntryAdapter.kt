package com.labs.openlabor-mobile.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.labs.openlabor-mobile.R
import com.labs.openlabor-mobile.model.TimeEntry

/**
 * Adapter for displaying time entries in the timesheet/summary view
 * Supports night shifts that span across midnight
 */
class TimesheetEntryAdapter(
    private val onEditClick: (TimeEntry) -> Unit
) : ListAdapter<TimeEntry, TimesheetEntryAdapter.TimesheetEntryViewHolder>(TimesheetEntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimesheetEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timesheet_entry, parent, false)
        return TimesheetEntryViewHolder(view, onEditClick)
    }

    override fun onBindViewHolder(holder: TimesheetEntryViewHolder, position: Int) {
        val timeEntry = getItem(position)
        holder.bind(timeEntry)
    }

    class TimesheetEntryViewHolder(
        itemView: View,
        private val onEditClick: (TimeEntry) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.text_timesheet_date)
        private val clockInTextView: TextView = itemView.findViewById(R.id.text_timesheet_clock_in)
        private val clockOutTextView: TextView = itemView.findViewById(R.id.text_timesheet_clock_out)
        private val breakTextView: TextView = itemView.findViewById(R.id.text_timesheet_break)
        private val hoursTextView: TextView = itemView.findViewById(R.id.text_timesheet_hours)
        private val editButton: ImageButton = itemView.findViewById(R.id.button_edit_timesheet)

        private var currentTimeEntry: TimeEntry? = null

        init {
            editButton.setOnClickListener {
                currentTimeEntry?.let { onEditClick(it) }
            }

            // Make the entire item clickable for editing
            itemView.setOnClickListener {
                currentTimeEntry?.let { onEditClick(it) }
            }
        }

        fun bind(timeEntry: TimeEntry) {
            currentTimeEntry = timeEntry

            // Display date differently for night shifts that span multiple days
            dateTextView.text = timeEntry.getFormattedDate()

            if (timeEntry.isNightShift()) {
                // For night shifts that span multiple days, show the day of month to avoid confusion
                val clockInDay = timeEntry.clockInTime.dayOfMonth
                clockInTextView.text = "In: ${timeEntry.getFormattedClockInTime()} (${clockInDay})"

                if (timeEntry.clockOutTime != null) {
                    val clockOutDay = timeEntry.clockOutTime!!.dayOfMonth
                    clockOutTextView.text = "Out: ${timeEntry.getFormattedClockOutTime()} (${clockOutDay})"
                } else {
                    clockOutTextView.text = "Out: Not clocked out"
                }
            } else {
                // Regular shifts (same day)
                clockInTextView.text = "In: ${timeEntry.getFormattedClockInTime()}"

                if (timeEntry.clockOutTime != null) {
                    clockOutTextView.text = "Out: ${timeEntry.getFormattedClockOutTime()}"
                } else {
                    clockOutTextView.text = "Out: Not clocked out"
                }
            }

            breakTextView.text = "Break: ${timeEntry.breakMinutes} min"
            hoursTextView.text = timeEntry.getFormattedHours()
        }
    }

    class TimesheetEntryDiffCallback : DiffUtil.ItemCallback<TimeEntry>() {
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
