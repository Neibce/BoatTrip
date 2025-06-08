package com.boattrip.boattrip

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.boattrip.boattrip.llm.LLMRouteRequest
import com.boattrip.boattrip.llm.LLMService
import com.boattrip.boattrip.llm.Message
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

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃: 30초
            .readTimeout(60, TimeUnit.SECONDS) // 읽기 타임아웃: 30초
            .writeTimeout(60, TimeUnit.SECONDS) // 쓰기 타임아웃: 30초
            .build()

        val retrofit = Retrofit.Builder().baseUrl("https://api.upstage.ai/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()).build();
        val service = retrofit.create(LLMService::class.java);

        val request = LLMRouteRequest(
            model = "solar-pro2-preview",
            messages = listOf(
                Message(
                    role = "system", content = "당신은 한국 여행사의 매우 우수한 관광 가이드입니다.\n" +
                            "\n" +
                            "고객을 위한 후쿠오카 3박 4일 일정을 짜주세요.\n" +
                            "빈 시간이 없도록 알차게 준비해야 합니다.\n" +
                            "도보, 대중교통 동선도 생각해야 합니다.\n"+
                            "대략적인 시간도 포함되어야 합니다.\n" +
                            "식당을 적을 때에는 절대로 없는 상호명을 지어내지 않아야 합니다.\n" +
                            "숙박은 적지 않아야 합니다.\n" +
                            "시간은 반드시 ISO 8601에 따른 파싱 가능한 형태여야 합니다.\n" +
                            "\n" +
                            "반드시 json 형식으로 반환하세요.\n" +
                            "해당 장소의 정확한 위치 좌표(lat, lng)도 각 장소마다 필히 반드시 반환하세요.\n" +
                            "반드시 정확한 위치를 반환하도록 하세요.\n" +
                            "\n" +
                            "당신은 단순 REST API처럼 동작하여야 하며, json 응답이외에 어떤 것도 말해서는 안됩니다.\n" +
                            "\n" +
                            "모든 내용은 한국어로 작성해야 합니다.\n" +
                            "\n" +
                            "\n" +
                            "JSON 응답 형식은 다음의 예시를 무조건 준수해야 합니다.\n" +
                            "\n" +
                            "{\n" +
                            "  \"itinerary\": [\n" +
                            "    {\n" +
                            "      \"day\": 1,\n" +
                            "      \"date\": \"2025-06-08\",\n" +
                            "      \"schedule\": [\n" +
                            "        {\n" +
                            "          \"time\": \"2025-06-08T09:00:00+09:00\",\n" +
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
                Message(role = "user", content = "짜주세요..")
            ),
            stream = false
        )

        service.getStructuredAnswer("Bearer up_VljCSWmilJloaBCJ0RHUUnWdcusC2", request)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        // 정상적으로 통신이 성고된 경우
                        var result: String? = response.body()?.string()
                        var a = JsonParser.parseString(result).asJsonObject["choices"].asJsonArray.get(0)
                            .asJsonObject["message"].asJsonObject["content"].asString
                        var b = JsonParser.parseString(a.replace("```json", "").replace("```", "")).asJsonObject
                        Log.d("YMC", "onResponse 성공: " + result?.toString() + b.toString());

                        //load RouteActivity and pass the data
                        val intent = Intent(this@RouteFetchActivity, RouteViewActivity::class.java)
                        intent.putExtra("routeData", b.toString())
                        startActivity(intent)
                    } else {
                        // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                        Log.d("YMC",
                            "onResponse 실패 응답 코드: " + response.code()
                                .toString() + ", 메시지: " + response.message()
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