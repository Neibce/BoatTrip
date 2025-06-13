package com.boattrip.boattrip

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson


class RouteViewActivity : AppCompatActivity() {
    var markerList: MutableList<Marker?> = ArrayList<Marker?>()
    lateinit var mapFragment: SupportMapFragment
    lateinit var recyclerView: RecyclerView
    lateinit var route: Route

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_route_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val standardBottomSheet = findViewById<LinearLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.halfExpandedRatio = 0.5f
        standardBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)

        val routeData = intent.getStringExtra("routeData")
        route = Gson().fromJson(routeData, Route::class.java)

        val dayButtonsHolder = findViewById<LinearLayout>(R.id.day_buttons_holder)
        route.itinerary.forEachIndexed { index, _ ->
            val button = Button(this).apply {
                text = "Day " + (index + 1).toString()
                textSize = 16f
                elevation = 0f

                stateListAnimator = null
                setTextColor(resources.getColor(R.color.gray, null))
                if (index == 0)
                    setBackgroundResource(R.drawable.round_button_selected)
                else
                    setBackgroundResource(R.drawable.round_button)

                setPadding(24, 12, 24, 12)

                setOnClickListener {
                    for (i in 0 until dayButtonsHolder.childCount) {
                        val child = dayButtonsHolder.getChildAt(i) as Button
                        child.setBackgroundResource(R.drawable.round_button)
                        child.setTextColor(resources.getColor(R.color.gray, null))
                    }
                    setBackgroundResource(R.drawable.round_button_selected)
                    changeDay(index + 1)
                }
            }
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (index > 0) {
                    marginStart = 12 // 버튼 사이 간격 추가
                }
            }
            
            dayButtonsHolder.addView(button, layoutParams)
        }

        //pin on map
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        changeDay(1)
    }

    private fun changeDay(day: Int) {
        pinOnMap(route.itinerary.get(day - 1).schedule)
        updateRecyclerView(route.itinerary.get(day - 1).schedule)
    }

    private fun pinOnMap(schedule: List<Schedule>) {
        mapFragment.getMapAsync { googleMap ->
            for (marker in markerList)
                marker?.remove()
            markerList.clear()

            schedule.forEachIndexed { idx, it ->
                val latLng = com.google.android.gms.maps.model.LatLng(
                    it.coordinates.lat,
                    it.coordinates.lng
                )
                var marker = googleMap.addMarker(
                    com.google.android.gms.maps.model.MarkerOptions()
                        .position(latLng)
                        .title(it.activity)
                        .snippet("${it.time} - ${it.location}")
                        .icon(BitmapDescriptorFactory.fromBitmap(
                                createCustomMarker(this, (idx + 1).toString())))
                )
                markerList.add(marker)
            }
            // Move the camera to the first location
            if (schedule.isNotEmpty()) {
                val firstLocation = com.google.android.gms.maps.model.LatLng(
                    schedule[0].coordinates.lat,
                    schedule[0].coordinates.lng)
                googleMap.moveCamera(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(firstLocation, 10f))
            }
        }
    }

    fun updateRecyclerView(schedule: List<Schedule>) {
        var stepList = mutableListOf<StepItem>()
        schedule.forEach { schedule ->
            stepList.add(
                StepItem(
                    schedule.time,
                    schedule.location,
                    schedule.activity
                )
            )
        }
        recyclerView.adapter = StepAdapter(stepList)
    }

    fun createCustomMarker(context: Context?, text: String?): Bitmap {
        val markerView: View = LayoutInflater.from(context).inflate(R.layout.custom_marker, null)

        val textView = markerView.findViewById<TextView?>(R.id.tv_marker)
        textView.text = text

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

        val bitmap = createBitmap(markerView.measuredWidth, markerView.measuredHeight)
        markerView.draw(Canvas(bitmap))

        return bitmap
    }
}