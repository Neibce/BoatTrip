package com.boattrip.boattrip

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson

class RouteViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_route)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val standardBottomSheet = findViewById<LinearLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.halfExpandedRatio = 0.5f
        standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)

        //get intent data
        val routeData = intent.getStringExtra("routeData")
        // Parse the routeData JSON string into a Schedule object
        val route = Gson().fromJson(routeData, Route::class.java)
        //pin on map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            route.itinerary.get(2).schedule.forEach { schedule ->
                val latLng = com.google.android.gms.maps.model.LatLng(
                    schedule.coordinates.lat,
                    schedule.coordinates.lng
                )
                googleMap.addMarker(
                    com.google.android.gms.maps.model.MarkerOptions()
                        .position(latLng)
                        .title(schedule.activity)
                        .snippet("${schedule.time} - ${schedule.location}")
                )
            }
            // Move the camera to the first location
            if (route.itinerary.get(2).schedule.isNotEmpty()) {
                val firstLocation = com.google.android.gms.maps.model.LatLng(
                    route.itinerary.get(2).schedule[0].coordinates.lat,
                    route.itinerary.get(2).schedule[0].coordinates.lng
                )
                googleMap.moveCamera(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                        firstLocation,
                        10f
                    )
                )
            }
        }


    }
}