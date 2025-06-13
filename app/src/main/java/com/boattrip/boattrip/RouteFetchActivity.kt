package com.boattrip.boattrip

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.boattrip.boattrip.llm.LLMRouteRequest
import com.boattrip.boattrip.llm.LLMService
import com.boattrip.boattrip.llm.Message
import com.boattrip.boattrip.llm.Tool
import com.bumptech.glide.Glide
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RouteFetchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_route_fetch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val img = findViewById<ImageView>(R.id.loadingGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_dog)
            .into(img)

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃: 30초
            .readTimeout(60, TimeUnit.SECONDS) // 읽기 타임아웃: 30초
            .writeTimeout(60, TimeUnit.SECONDS) // 쓰기 타임아웃: 30초
            .build()

        val retrofit = Retrofit.Builder().baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()).build();
        val service = retrofit.create(LLMService::class.java)

        val request = LLMRouteRequest(
            model = "gpt-4.1-mini",
            tools = listOf(Tool("web_search_preview")),
            input = listOf(
                Message(
                    role = "system",
                    content = "You are an exceptionally competent tour guide for a major travel agency.\n" +
                            "\n" +
                            "Please create a `4-day, 3-night` itinerary for `삿포로` for customers.\n" +
                            "The schedule must be fully packed without any idle time.\n" +
                            "You must also consider walking and public transportation routes.\n" +
                            "Approximate times must be included.\n" +
                            "When listing restaurants, you must *never* make up fictitious names.\n" +
                            "Do not include any accommodation-related items.\n" +
                            "Time must be written strictly in 'HH:mm' format (e.g., 09:00)\n" +
                            "\n" +
                            "The response must be in **JSON format only**.\n" +
                            "Each place must include accurate coordinates (lat, lng).\n" +
                            "Make sure the locations and coordinates are exact.\n" +
                            "\n" +
                            "You must operate strictly like a REST API and **must not return anything other than JSON**.\n" +
                            "\n" +
                            "All content must be written in **Korean**.\n" +
                            "\n" +
                            "You must strictly follow the example JSON response format.\n" +
                            "The `coordinates` field is mandatory." +
                            "\n" +
                            "{\n" +
                            "  \"itinerary\": [\n" +
                            "    {\n" +
                            "      \"day\": 1,\n" +
                            "      \"date\": \"2025-06-08\",\n" +
                            "      \"schedule\": [\n" +
                            "        {\n" +
                            "          \"time\": \"09:00\",\n" +
                            "          \"activity\": \"도쿄역 방문\",\n" +
                            "          \"location\": \"도쿄역\",\n" +
                            "          \"coordinates\": {\n" +
                            "            \"lat\": 35.6812361123112,\n" +
                            "            \"lng\": 139.7671251231312\n" +
                            "          }\n" +
                            "        }\n" +
                            "      ]\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}\n"
                ),
                //The location is inaccurate. Correct it now and return the exact coordinates without any mistakes.
                Message(role = "user", content = "일정을 짜달라!! 저번에 짜준건 위치 좌표가 다 틀려서 제대로 여행을 못했잖아!! 짤리고싶어?!"),
            ),
            stream = false
        )

        service.getStructuredAnswer(
            "Bearer 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",
            request
        )
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        // 정상적으로 통신이 성고된 경우
                        var result: String? = response.body()?.string()
                        var a =
                            JsonParser.parseString(result).asJsonObject["output"].asJsonArray.get(0)
                                .asJsonObject["content"].asJsonArray.get(0).asJsonObject["text"].asString
                        Log.d("YMC", "onResponse 성공: " + result?.toString() + a.toString())
                        val start = a.indexOfFirst { it == '{' }
                        val end = a.indexOfLast { it == '}' } + 1
                        val jsonString =
                            if (start >= 0 && end > start) a.substring(start, end) else "{}"
                        val b = JsonParser.parseString(jsonString).asJsonObject
                        Log.d("YMC", "onResponse 성공: " + result?.toString() + b.toString())

                        //load RouteActivity and pass the data
                        val intent = Intent(this@RouteFetchActivity, RouteViewActivity::class.java)
                        intent.putExtra("routeData", b.toString())
                        startActivity(intent)
                    } else {
                        // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                        Log.d(
                            "YMC",
                            "onResponse 실패 응답 코드: " + response.code()
                                .toString() + ", 메시지: " + response.body()
                        );
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    // 통신 실패 (인터넷 끊킴, 예외 발생 등 시스템적인 이유)
                    Log.d("YMC", "onFailure 에러: " + t.message.toString());
                }
            })
    }
}