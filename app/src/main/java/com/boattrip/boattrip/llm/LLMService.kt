package com.boattrip.boattrip.llm;

import okhttp3.ResponseBody
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST

interface LLMService {
    @Headers("Content-Type: application/json")
    @POST("v1/responses")
    fun getStructuredAnswer(@Header("Authorization") authorization:String, @Body request: LLMRouteRequest): Call<ResponseBody>
}
