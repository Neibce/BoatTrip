package com.boattrip.boattrip

import android.app.TimePickerDialog
import android.content.Intent
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
    private lateinit var bottomText: TextView
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    private var selectedTheme: String? = null
    private var selectedTransport: String? = null

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
        bottomText = findViewById(R.id.bottomText)

        // 출발 시간 선택 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.btnSelectDepartureTime).setOnClickListener {
            showTimePickerDialog { selectedTime ->
                departureTimeText.text = selectedTime
                updateBottomText()
            }
        }

        // 도착 시간 선택 버튼 클릭 이벤트
        findViewById<ImageButton>(R.id.btnSelectArrivalTime).setOnClickListener {
            showTimePickerDialog { selectedTime ->
                arrivalTimeText.text = selectedTime
                updateBottomText()
            }
        }

        // 교통수단 선택 버튼들 초기화
        setupTransportButtons()
    }

    private fun setupTransportButtons() {
        findViewById<ImageButton>(R.id.btnCar).setOnClickListener {
            selectedTransport = "승용차"
            updateBottomText()
        }
        findViewById<ImageButton>(R.id.btnTrain).setOnClickListener {
            selectedTransport = "기차"
            updateBottomText()
        }
    }

    fun onEatingThemeClicked(view: android.view.View) {
        selectedTheme = "먹방"
        updateBottomText()
        navigateToRouteFetch("먹방")
    }

    fun onHealingThemeClicked(view: android.view.View) {
        selectedTheme = "힐링"
        updateBottomText()
        navigateToRouteFetch("힐링")
    }

    fun onHistoryThemeClicked(view: android.view.View) {
        selectedTheme = "역사 탐방"
        updateBottomText()
        navigateToRouteFetch("역사 탐방")
    }

    fun onSurfingThemeClicked(view: android.view.View) {
        selectedTheme = "액티비티"
        updateBottomText()
        navigateToRouteFetch("액티비티")
    }

    fun onCarClicked(view: android.view.View) {
        selectedTransport = "승용차"
        updateBottomText()
    }

    fun onTrainClicked(view: android.view.View) {
        selectedTransport = "기차"
        updateBottomText()
    }

    private fun updateBottomText() {
        val parts = mutableListOf<String>()
        
        selectedTheme?.let { parts.add(it) }
        
        if (departureTimeText.text != "시간을 선택해주세요" && 
            arrivalTimeText.text != "시간을 선택해주세요") {
            parts.add("${departureTimeText.text}/${arrivalTimeText.text}")
        }
        
        selectedTransport?.let { parts.add(it) }
        
        bottomText.text = if (parts.isEmpty()) {
            "어떤 여행을 즐기고 싶멍?"
        } else {
            "${parts.joinToString(", ")} 여행을 간다멍!"
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

    private fun navigateToRouteFetch(theme: String) {
        val intent = Intent(this, RouteFetchActivity::class.java).apply {
            // 이전 화면에서 받은 데이터
            putExtra("startDate", intent.getStringExtra("startDate"))
            putExtra("endDate", intent.getStringExtra("endDate"))
            putExtra("duration", intent.getIntExtra("duration", 0))
            putExtra("destination", intent.getStringExtra("destination"))
            
            // DetailInfo 화면에서 선택한 데이터
            putExtra("theme", theme)
            putExtra("departureTime", departureTimeText.text.toString())
            putExtra("arrivalTime", arrivalTimeText.text.toString())
            putExtra("transport", selectedTransport)
        }
        startActivity(intent)
    }
}