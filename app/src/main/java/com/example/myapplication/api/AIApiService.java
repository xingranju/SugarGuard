package com.example.myapplication.api;

import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.ChatRequest;
import com.example.myapplication.model.ChatResponse;
import com.example.myapplication.model.DrinkRecognitionResponse;
import com.example.myapplication.model.HealthAnalysisResponse;
import com.example.myapplication.model.RecommendationRequest;
import com.example.myapplication.model.RecommendationResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * AI服务API接口定义
 * 通过Spring Boot后端代理访问Python AI服务
 */
public interface AIApiService {
    
    /**
     * 饮品识别API
     * @param file 上传的图片文件
     * @return 识别结果(包装在ApiResponse中)
     * 注意: userId从JWT token中自动获取,不需要传递
     */
    @Multipart
    @POST("api/ai/recognize-drink")
    Call<ApiResponse<DrinkRecognitionResponse>> recognizeDrink(
            @Part MultipartBody.Part file
    );
    
    /**
     * 健康数据分析API
     * @param days 分析天数
     * @return 健康分析报告(包装在ApiResponse中)
     * 注意: userId从JWT token中自动获取,不需要传递
     */
    @GET("api/ai/health-analysis")
    Call<ApiResponse<HealthAnalysisResponse>> getHealthAnalysis(
            @Query("days") int days
    );
    
    /**
     * 智能对话API
     * @param request 对话请求
     * @return 对话响应(包装在ApiResponse中)
     */
    @POST("api/ai/chat")
    Call<ApiResponse<ChatResponse>> chat(@Body ChatRequest request);
    
    /**
     * 饮品推荐API
     * @param request 推荐请求
     * @return 推荐结果(包装在ApiResponse中)
     */
    @POST("api/ai/recommend-drinks")
    Call<ApiResponse<RecommendationResponse>> getRecommendations(@Body RecommendationRequest request);
}

