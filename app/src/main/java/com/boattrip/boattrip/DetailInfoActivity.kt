package com.boattrip.boattrip

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailInfoActivity : AppCompatActivity() {
    private lateinit var departureTimeText: TextView
    private lateinit var arrivalTimeText: TextView
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // View 초기화
        departureTimeText = findViewById(R.id.departureTime)
        arrivalTimeText = findViewById(R.id.arrivalTime)

        // 출발 시간 선택 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.btnSelectDepartureTime).setOnClickListener {
            showTimePickerDialog { selectedTime ->
                departureTimeText.text = selectedTime
            }
        }

        // 도착 시간 선택 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.btnSelectArrivalTime).setOnClickListener {
            showTimePickerDialog { selectedTime ->
                arrivalTimeText.text = selectedTime
            }
        }
    }

    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                onTimeSelected(timeFormat.format(calendar.time))
            },
            currentHour,
            currentMinute,
            true // 24시간 형식 사용
        ).show()
    }
}