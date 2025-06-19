package com.boattrip.boattrip

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson

class ArchiveFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedRouteAdapter
    private val db = FirebaseFirestore.getInstance()
    private val savedRoutes = mutableListOf<SavedRoute>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_archive, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.archiveRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        adapter = SavedRouteAdapter(savedRoutes) { savedRoute ->
            openRouteView(savedRoute)
        }
        recyclerView.adapter = adapter
        
        loadSavedRoutes()
    }
    
    private fun loadSavedRoutes() {
        db.collection("saved_routes")
            .orderBy("savedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                savedRoutes.clear()
                for (document in documents) {
                    val savedRoute = document.toObject(SavedRoute::class.java)
                    savedRoute.id = document.id
                    savedRoutes.add(savedRoute)
                }
                adapter.notifyDataSetChanged()
            }
    }
    
    private fun openRouteView(savedRoute: SavedRoute) {
        val intent = Intent(context, RouteViewActivity::class.java)
        val routeJson = Gson().toJson(savedRoute.route)
        intent.putExtra("routeData", routeJson)
        intent.putExtra("destination", savedRoute.destination)
        intent.putExtra("period", savedRoute.period)
        startActivity(intent)
    }
} 