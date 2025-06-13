package com.boattrip.boattrip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StepAdapter(private val steps: List<StepItem>) : RecyclerView.Adapter<StepAdapter.StepViewHolder>() {

    inner class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stepCircle: TextView = itemView.findViewById(R.id.step_circle)
        val time : TextView = itemView.findViewById(R.id.time)
        val title: TextView = itemView.findViewById(R.id.textView6)
        val description: TextView = itemView.findViewById(R.id.textView7)
        val dottedLine: View = itemView.findViewById(R.id.view_dotted_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_step, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val item = steps[position]
        holder.stepCircle.text = (position + 1).toString() // Step number starts from 1
        holder.time.text = item.time
        holder.title.text = item.title
        holder.description.text = item.description

        // 마지막 아이템일 경우 점선 숨기기
        holder.dottedLine.visibility = if (position == steps.lastIndex) View.INVISIBLE else View.VISIBLE
    }

    override fun getItemCount(): Int = steps.size
}