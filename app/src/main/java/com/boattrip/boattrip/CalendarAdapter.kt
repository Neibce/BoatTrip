package com.boattrip.boattrip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarAdapter(
    private val days: List<Int>,
    private val onDayClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    class DayViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false) as TextView
        return DayViewHolder(textView)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.textView.apply {
            text = if (day > 0) day.toString() else ""
            setOnClickListener {
                if (day > 0) onDayClick(day)
            }
            if (day == 0) {
                alpha = 0f
            }
        }
    }

    override fun getItemCount() = days.size
} 