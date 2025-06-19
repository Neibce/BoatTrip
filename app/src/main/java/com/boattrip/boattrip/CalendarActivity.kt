package com.boattrip.boattrip

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CalendarActivity : AppCompatActivity() {
    private lateinit var calendarGrid: RecyclerView
    private lateinit var monthText: TextView
    private lateinit var yearText: TextView
    private lateinit var selectedDateInfo: TextView
    private val calendar = Calendar.getInstance()
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var destination: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // 전달받은 destination 데이터 저장
        destination = intent.getStringExtra("destination") ?: ""

        monthText = findViewById(R.id.monthText)
        yearText = findViewById(R.id.yearText)
        calendarGrid = findViewById(R.id.calendarGrid)
        selectedDateInfo = findViewById(R.id.selectedDateInfo)

        setupCalendarGrid()
        updateMonthText()
        updateYearText()

        findViewById<View>(R.id.prevMonth).setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthText()
            updateYearText()
            setupCalendarGrid()
        }

        findViewById<View>(R.id.nextMonth).setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthText()
            updateYearText()
            setupCalendarGrid()
        }
    }

    private fun setupCalendarGrid() {
        calendarGrid.layoutManager = GridLayoutManager(this, 7)
        val adapter = CalendarAdapter(getDaysInMonth()) { day ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, day)
            }

            when {
                startDate == null -> {
                    startDate = selectedCalendar
            selectedDateInfo.visibility = View.VISIBLE
                    selectedDateInfo.text = "출발 날짜를 선택했멍!\n이제 도착 날짜를 선택해달라멍!"
                }
                endDate == null -> {
                    if (selectedCalendar.before(startDate)) {
                        selectedDateInfo.text = "도착 날짜는 출발 날짜보다 이후여야 한다멍!"
                        return@CalendarAdapter
                    }
                    endDate = selectedCalendar
                    calculateDuration()
            }
                else -> {
                    startDate = selectedCalendar
                    endDate = null
                    selectedDateInfo.text = "출발 날짜를 선택했멍! 이제 도착 날짜를 선택해달라멍!"
                }
            }
        }
        calendarGrid.adapter = adapter
    }

    private fun calculateDuration() {
        val diffInMillis = endDate!!.timeInMillis - startDate!!.timeInMillis
        val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
        val nights = diffInDays
        val days = diffInDays + 1

        selectedDateInfo.text = "${nights}박 ${days}일 여행이다멍!"
        
        // DetailInfoActivity로 이동
        val intent = Intent(this, DetailInfoActivity::class.java).apply {
            putExtra("startDate", "${startDate!!.get(Calendar.YEAR)}-${startDate!!.get(Calendar.MONTH) + 1}-${startDate!!.get(Calendar.DAY_OF_MONTH)}")
            putExtra("endDate", "${endDate!!.get(Calendar.YEAR)}-${endDate!!.get(Calendar.MONTH) + 1}-${endDate!!.get(Calendar.DAY_OF_MONTH)}")
            putExtra("duration", days)
            putExtra("destination", destination)
        }
        startActivity(intent)
    }

    private fun updateMonthText() {
        monthText.text = "${calendar.get(Calendar.MONTH) + 1}월"
    }

    private fun updateYearText() {
        yearText.text = "${calendar.get(Calendar.YEAR)}년"
    }

    private fun getDaysInMonth(): List<Int> {
        val days = mutableListOf<Int>()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // 첫 날의 요일만큼 빈 칸 추가
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        for (i in 1 until firstDayOfWeek) {
            days.add(0)
        }
        
        // 실제 날짜 추가
        for (i in 1..maxDays) {
            days.add(i)
        }
        
        return days
    }
} 