package com.boattrip.boattrip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class SavedRouteAdapter(
    private val savedRoutes: List<SavedRoute>,
    private val onItemClick: (SavedRoute) -> Unit
) : RecyclerView.Adapter<SavedRouteAdapter.SavedRouteViewHolder>() {

    class SavedRouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val destinationText: TextView = itemView.findViewById(R.id.destinationText)
        val periodText: TextView = itemView.findViewById(R.id.periodText)
        val savedDateText: TextView = itemView.findViewById(R.id.savedDateText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedRouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_route, parent, false)
        return SavedRouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedRouteViewHolder, position: Int) {
        val savedRoute = savedRoutes[position]
        
        holder.destinationText.text = savedRoute.destination
        holder.periodText.text = savedRoute.period
        
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
        holder.savedDateText.text = "저장: ${dateFormat.format(Date(savedRoute.savedAt))}"
        
        holder.itemView.setOnClickListener {
            onItemClick(savedRoute)
        }
    }

    override fun getItemCount(): Int = savedRoutes.size
} 