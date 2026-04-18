package com.example.myapplication.data.api;

import com.example.myapplication.data.model.ApiResponse;
import com.example.myapplication.data.model.FoodItem;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * 食物识别API服务接口
 * shiWu_shibie_API_fuWu_jieKou
 */
public interface FoodRecognitionApiService {

    /**
     * 上传图片识别食物
     * shangChuan_tuPian_shibie_shiWu
     * 
     * @param image 食物图片文件
     * @return 食物识别结果
     */
    @Multipart
    @POST("food/recognize")
    Call<ApiResponse<FoodItem>> recognizeFood(
            @Part MultipartBody.Part image
    );

    /**
     * 上传图片并附带额外信息
     * shangChuan_tuPian_bing_fuDai_eWai_xinXi
     * 
     * @param image 食物图片文件
     * @param description 用户描述（可选）
     * @return 食物识别结果
     */
    @Multipart
    @POST("food/recognize")
    Call<ApiResponse<FoodItem>> recognizeFoodWithDescription(
            @Part MultipartBody.Part image,
            @Part("description") RequestBody description
    );
}

