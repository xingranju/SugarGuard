package com.example.myapplication.api

import com.example.myapplication.model.FoodItem
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FoodRecognitionApiService {
    @Multipart
    @POST("api/food/recognize")
    fun recognizeFood(@Part file: MultipartBody.Part): Call<FoodItem>
}
