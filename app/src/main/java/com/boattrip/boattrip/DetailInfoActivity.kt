package com.boattrip.boattrip

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
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
    
    
    private var selectedThemeButton: ImageButton? = null
    private var selectedTransportButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        
        departureTimeText = findViewById(R.id.departureTime)
        arrivalTimeText = findViewById(R.id.arrivalTime)
        bottomText = findViewById(R.id.bottomText)

        
        findViewById<ImageButton>(R.id.btnSelectDepartureTime).setOnClickListener {
            showTimePickerDialog { selectedTime ->
                departureTimeText.text = selectedTime
                updateBottomText()
            }
        }

        
        findViewById<ImageButton>(R.id.btnSelectArrivalTime).setOnClickListener {
            showTimePickerDialog { selectedTime ->
                arrivalTimeText.text = selectedTime
                updateBottomText()
            }
        }

        
        setupTransportButtons()
    }

    private fun setupTransportButtons() {
        findViewById<ImageButton>(R.id.btnCar).setOnClickListener {
            selectTransportButton(it as ImageButton, "승용차")
        }
        findViewById<ImageButton>(R.id.btnTrain).setOnClickListener {
            selectTransportButton(it as ImageButton, "대중교통")
        }
    }

    private fun selectThemeButton(button: ImageButton, theme: String) {
        
        selectedThemeButton?.foreground = ContextCompat.getDrawable(this, R.drawable.rounded_border)
        
        
        button.foreground = ContextCompat.getDrawable(this, R.drawable.rounded_border_selected)
        
        
        selectedThemeButton = button
        selectedTheme = theme
        updateBottomText()
    }

    private fun selectTransportButton(button: ImageButton, transport: String) {
        
        selectedTransportButton?.foreground = ContextCompat.getDrawable(this, R.drawable.rounded_border)
        
        
        button.foreground = ContextCompat.getDrawable(this, R.drawable.rounded_border_selected)
        
        
        selectedTransportButton = button
        selectedTransport = transport
        updateBottomText()
    }

    fun onEatingThemeClicked(view: View) {
        val button = findViewById<ImageButton>(R.id.btnEatingTheme)
        selectThemeButton(button, "먹방")
    }

    fun onHealingThemeClicked(view: View) {
        val button = findViewById<ImageButton>(R.id.btnHealingTheme)
        selectThemeButton(button, "힐링")
    }

    fun onHistoryThemeClicked(view: View) {
        val button = findViewById<ImageButton>(R.id.btnHistoryTheme)
        selectThemeButton(button, "역사 탐방")
    }

    fun onSurfingThemeClicked(view: View) {
        val button = findViewById<ImageButton>(R.id.btnSurfingTheme)
        selectThemeButton(button, "액티비티")
    }

    fun onCarClicked(view: View) {
        val button = findViewById<ImageButton>(R.id.btnCar)
        selectTransportButton(button, "승용차")
    }

    fun onTrainClicked(view: View) {
        val button = findViewById<ImageButton>(R.id.btnTrain)
        selectTransportButton(button, "대중교통")
    }

    private fun updateBottomText() {
        val parts = mutableListOf<String>()

        selectedTheme?.let { parts.add(it) }

        if (departureTimeText.text != "시간을 선택해주세요" &&
            arrivalTimeText.text != "시간을 선택해주세요"
        ) {
            parts.add("${departureTimeText.text}/${arrivalTimeText.text}")
        }

        selectedTransport?.let { parts.add(it) }

        bottomText.text = if (parts.isEmpty()) {
            "어떤 여행을 즐기고 싶멍?"
        } else {
            "${parts.joinToString(", ")}\n여행을 간다멍!"
        }
    }

    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setTitleText("시간 선택")
            .setInputMode(INPUT_MODE_CLOCK)
            .build()

        picker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, picker.hour)
            calendar.set(Calendar.MINUTE, picker.minute)
            onTimeSelected(timeFormat.format(calendar.time))
        }

        picker.show(supportFragmentManager, "MaterialTimePicker")
    }

    fun navigateToRouteFetch(view: View) {
        
        if (selectedTheme == null ||
            departureTimeText.text == "시간을 선택해주세요" ||
            arrivalTimeText.text == "시간을 선택해주세요" ||
            selectedTransport == null) {
            bottomText.text = "모든 정보를 입력해달라멍!"
            return
        }
        val intent = Intent(this, RouteFetchActivity::class.java).apply {
            putExtra("startDate", intent.getStringExtra("startDate"))
            putExtra("endDate", intent.getStringExtra("endDate"))
            putExtra("duration", intent.getLongExtra("duration", 0))
            putExtra("destination", intent.getStringExtra("destination"))

            putExtra("theme", selectedTheme)
            putExtra("departureTime", departureTimeText.text.toString())
            putExtra("arrivalTime", arrivalTimeText.text.toString())
            putExtra("transport", selectedTransport)
        }
        startActivity(intent)
        finish()
    }

    fun goBack(view: View) {
        onBackPressedDispatcher.onBackPressed()
    }
}