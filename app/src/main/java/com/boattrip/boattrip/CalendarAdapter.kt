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

    private var startDate: Int? = null
    private var endDate: Int? = null

    class DayViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.textView.apply {
            text = if (day > 0) day.toString() else ""
            isSelected = when (day) {
                startDate -> true
                endDate -> true
                in (startDate ?: 0)..(endDate ?: 0) -> true
                else -> false
            }
            
            if (day == 0) {
                visibility = View.INVISIBLE
            } else {
                visibility = View.VISIBLE
                setOnClickListener {
                    when {
                        startDate == null -> {
                            startDate = day
                        }
                        endDate == null && day > startDate!! -> {
                            endDate = day
                        }
                        else -> {
                            startDate = day
                            endDate = null
                        }
                    }
                    onDayClick(day)
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getItemCount() = days.size
} 