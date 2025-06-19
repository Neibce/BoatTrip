package com.boattrip.boattrip

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*


class RouteViewActivity : AppCompatActivity() {
    var markerList: MutableList<Marker?> = ArrayList<Marker?>()
    var polylineList: MutableList<Polyline?> = ArrayList<Polyline?>()
    lateinit var mapFragment: SupportMapFragment
    lateinit var recyclerView: RecyclerView
    lateinit var route: Route
    private val db = FirebaseFirestore.getInstance()

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

        // 저장 버튼 클릭 리스너 설정
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            saveRouteToFirestore()
        }

        // 캘린더 추가 버튼 클릭 리스너 설정
        val addToCalendarButton = findViewById<Button>(R.id.addToCalendarButton)
        addToCalendarButton.setOnClickListener {
            addRouteToCalendar()
        }

        val dayButtonsHolder = findViewById<LinearLayout>(R.id.day_buttons_holder)
        route.itinerary.forEachIndexed { index, _ ->
            val button = TextView(this).apply {
                text = "Day " + (index + 1).toString()
                textSize = 16f
                isAllCaps = false
                isClickable = true
                isFocusable = true
                minHeight = 0
                minWidth = 0
                typeface = resources.getFont(R.font.scdream2)

                setTextColor(resources.getColor(R.color.gray, null))
                if (index == 0)
                    setBackgroundResource(R.drawable.round_button_selected)
                else
                    setBackgroundResource(R.drawable.round_button)

                setPadding(24, 16, 24, 16)

                setOnClickListener {
                    for (i in 0 until dayButtonsHolder.childCount) {
                        val child = dayButtonsHolder.getChildAt(i) as TextView
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
                //if (index > 0) {
                    rightMargin = 12 // 버튼 사이 간격 추가
                //}
            }
            
            dayButtonsHolder.addView(button, layoutParams)
        }

        //pin on map
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        changeDay(1)
    }

    private fun saveRouteToFirestore() {
        // 임의의 목적지와 기간 설정 (실제로는 intent나 다른 곳에서 가져와야 함)
        val destination = intent.getStringExtra("destination") ?: "[일본] 도쿄"
        val period = intent.getStringExtra("period") ?: "2025.05.06 - 2025.05.08"

        val savedRoute = SavedRoute(
            destination = destination,
            period = period,
            route = route,
            savedAt = System.currentTimeMillis()
        )

        db.collection("saved_routes")
            .add(savedRoute)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "경로가 저장되었습니다!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addRouteToCalendar() {
        val destination = intent.getStringExtra("destination") ?: "[일본] 도쿄"

        try {
            // 여행 전체 기간을 하나의 일정으로 추가
            addCompleteRouteToCalendar(destination)
        } catch (e: Exception) {
            Toast.makeText(this, "캘린더 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCompleteRouteToCalendar(destination: String) {
        try {
            if (route.itinerary.isEmpty()) {
                Toast.makeText(this, "일정 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startCalendar = Calendar.getInstance()
            val endCalendar = Calendar.getInstance()

            // 시작일 설정 (첫 번째 날짜)
            try {
                startCalendar.time = dateFormat.parse(route.itinerary.first().date) ?: Date()
            } catch (e: Exception) {
                startCalendar.time = Date()
            }
            startCalendar.set(Calendar.HOUR_OF_DAY, 9)
            startCalendar.set(Calendar.MINUTE, 0)
            startCalendar.set(Calendar.SECOND, 0)

            // 종료일 설정 (마지막 날짜)
            try {
                endCalendar.time = dateFormat.parse(route.itinerary.last().date) ?: Date()
            } catch (e: Exception) {
                endCalendar.time = Date()
                endCalendar.add(Calendar.DAY_OF_MONTH, route.itinerary.size - 1)
            }
            endCalendar.set(Calendar.HOUR_OF_DAY, 18)
            endCalendar.set(Calendar.MINUTE, 0)
            endCalendar.set(Calendar.SECOND, 0)

            val startTime = startCalendar.timeInMillis
            val endTime = endCalendar.timeInMillis

            // 일정 제목
            val title = "$destination 여행"

            // 전체 여행 일정을 상세하게 기록
            val description = StringBuilder()
            description.append("📍 여행지: $destination\n")
            description.append("📅 기간: ${route.itinerary.first().date} ~ ${route.itinerary.last().date}\n")
            description.append("🗓 총 ${route.itinerary.size}일 여행\n\n")
            description.append("=== 상세 일정 ===\n\n")

            route.itinerary.forEach { itinerary ->
                description.append("📆 Day ${itinerary.day} (${itinerary.date})\n")
                description.append("━━━━━━━━━━━━━━━━━━━━\n")

                itinerary.schedule.forEach { schedule ->
                    description.append("🕐 ${schedule.time}\n")
                    description.append("   ${schedule.activity}\n")
                    description.append("   📍 ${schedule.location}\n\n")
                }

                description.append("\n")
            }

            // 첫 번째 날의 첫 번째 위치를 기본 위치로 설정
            val location = if (route.itinerary.isNotEmpty() && route.itinerary.first().schedule.isNotEmpty()) {
                route.itinerary.first().schedule.first().location
            } else {
                destination
            }

            // 캘린더 인텐트 생성
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description.toString().trim())
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                putExtra(CalendarContract.Events.ALL_DAY, false)
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            }

            startActivity(intent)
            Toast.makeText(this, "캘린더에 여행 일정이 추가되었습니다.", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "캘린더 추가 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeDay(day: Int) {
        pinOnMap(route.itinerary.get(day - 1).schedule)
        updateRecyclerView(route.itinerary.get(day - 1).schedule)
    }

    private fun pinOnMap(schedule: List<Schedule>) {
        mapFragment.getMapAsync { googleMap ->
            // 기존 마커들 제거
            for (marker in markerList)
                marker?.remove()
            markerList.clear()

            // 기존 polyline들 제거
            for (polyline in polylineList)
                polyline?.remove()
            polylineList.clear()

            val latLngList = mutableListOf<com.google.android.gms.maps.model.LatLng>()

            schedule.forEachIndexed { idx, it ->
                val latLng = com.google.android.gms.maps.model.LatLng(
                    it.coordinates.lat,
                    it.coordinates.lng
                )
                latLngList.add(latLng)

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

            // 마커들 사이에 선 그리기
            if (latLngList.size > 1) {
                val polylineOptions = PolylineOptions()
                    .addAll(latLngList)
                    .color(0xFF4285F4.toInt()) // Google Maps 파란색
                    .width(8f)
                    .geodesic(true)

                val polyline = googleMap.addPolyline(polylineOptions)
                polylineList.add(polyline)
            }

            // Move the camera to the first location
            if (schedule.isNotEmpty()) {
                val firstLocation = com.google.android.gms.maps.model.LatLng(
                    schedule[0].coordinates.lat,
                    schedule[0].coordinates.lng)
                googleMap.moveCamera(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(firstLocation, 15f))
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