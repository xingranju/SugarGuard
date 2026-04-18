package com.example.myapplication.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.api.AIApiService;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.ChatRequest;
import com.example.myapplication.model.ChatResponse;
import com.example.myapplication.model.DrinkRecognitionResponse;
import com.example.myapplication.model.HealthAnalysisResponse;
import com.example.myapplication.model.RecommendationRequest;
import com.example.myapplication.model.RecommendationResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AI服务ViewModel
 * 负责管理AI相关的业务逻辑和数据
 */
public class AIServiceViewModel extends AndroidViewModel {
    
    private static final String TAG = "AIServiceViewModel";
    
    private final AIApiService aiApiService;
    
    // LiveData for UI观察
    private final MutableLiveData<DrinkRecognitionResponse> drinkRecognitionResult = new MutableLiveData<>();
    private final MutableLiveData<ChatResponse> chatResponse = new MutableLiveData<>();
    private final MutableLiveData<RecommendationResponse> recommendationResult = new MutableLiveData<>();
    private final MutableLiveData<HealthAnalysisResponse> healthAnalysisResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public AIServiceViewModel(@NonNull Application application) {
        super(application);
        aiApiService = RetrofitClient.getAIApiService();
    }
    
    // Getters for LiveData
    public LiveData<DrinkRecognitionResponse> getDrinkRecognitionResult() {
        return drinkRecognitionResult;
    }
    
    public LiveData<ChatResponse> getChatResponse() {
        return chatResponse;
    }
    
    public LiveData<RecommendationResponse> getRecommendationResult() {
        return recommendationResult;
    }
    
    public LiveData<HealthAnalysisResponse> getHealthAnalysisResult() {
        return healthAnalysisResult;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 识别饮品 (使用File)
     */
    public void recognizeDrink(int userId, File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            errorMessage.setValue("图片文件不存在");
            Log.e(TAG, "图片文件无效");
            return;
        }
        
        isLoading.setValue(true);
        errorMessage.setValue(""); // 使用空字符串而不是null
        
        Log.d(TAG, "开始识别饮品 - 用户ID: " + userId + ", 文件: " + imageFile.getAbsolutePath() + ", 大小: " + imageFile.length() + " bytes");
        
        try {
            // 创建MultipartBody.Part
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
            
            // 调用API (userId从JWT token自动获取,不需要传递参数)
            Call<ApiResponse<DrinkRecognitionResponse>> call = aiApiService.recognizeDrink(body);
            
            Log.d(TAG, "API调用已发送: /api/ai/recognize-drink (userId从JWT token自动获取)");
            
            call.enqueue(new Callback<ApiResponse<DrinkRecognitionResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<DrinkRecognitionResponse>> call, Response<ApiResponse<DrinkRecognitionResponse>> response) {
                    isLoading.postValue(false);
                    Log.d(TAG, "收到API响应 - 状态码: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<DrinkRecognitionResponse> apiResponse = response.body();
                        
                        // 从ApiResponse中提取data字段
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            DrinkRecognitionResponse result = apiResponse.getData();
                            drinkRecognitionResult.postValue(result);
                            
                            // 安全地访问嵌套对象,避免NullPointerException
                            if (result.getRecognition() != null && result.getRecognition().getDrinkName() != null) {
                                Log.d(TAG, "饮品识别成功: " + result.getRecognition().getDrinkName());
                            } else {
                                Log.d(TAG, "饮品识别成功,但识别结果为空");
                            }
                        } else {
                            String errorMsg = "识别失败: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "未知错误");
                            errorMessage.postValue(errorMsg);
                            Log.e(TAG, errorMsg);
                        }
                    } else {
                        String errorMsg = "识别失败 (HTTP " + response.code() + ")";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                errorMsg += ": " + errorBody;
                                Log.e(TAG, "错误详情: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "读取错误信息失败", e);
                        }
                        errorMessage.postValue(errorMsg);
                        Log.e(TAG, "饮品识别失败: " + errorMsg);
                    }
                    
                    // 清理临时文件
                    if (imageFile.exists()) {
                        boolean deleted = imageFile.delete();
                        Log.d(TAG, "临时文件删除: " + deleted);
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<DrinkRecognitionResponse>> call, Throwable t) {
                    isLoading.postValue(false);
                    String errorMsg = "网络错误: " + t.getMessage();
                    errorMessage.postValue(errorMsg);
                    Log.e(TAG, "饮品识别网络错误: " + t.getMessage(), t);
                    
                    // 打印堆栈跟踪
                    t.printStackTrace();
                    
                    // 清理临时文件
                    if (imageFile.exists()) {
                        boolean deleted = imageFile.delete();
                        Log.d(TAG, "临时文件删除: " + deleted);
                    }
                }
            });
            
        } catch (Exception e) {
            isLoading.setValue(false);
            errorMessage.setValue("请求构建失败: " + e.getMessage());
            Log.e(TAG, "请求构建失败", e);
        }
    }
    
    /**
     * 识别饮品 (使用Bitmap)
     */
    public void recognizeDrink(Bitmap imageBitmap, int userId) {
        isLoading.setValue(true);
        errorMessage.setValue(""); // 使用空字符串而不是null
        
        try {
            // 将Bitmap转换为文件
            File imageFile = bitmapToFile(imageBitmap);
            
            // 创建MultipartBody.Part
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
            
            // 调用API (userId从JWT token自动获取,不需要传递参数)
            Call<ApiResponse<DrinkRecognitionResponse>> call = aiApiService.recognizeDrink(body);
            call.enqueue(new Callback<ApiResponse<DrinkRecognitionResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<DrinkRecognitionResponse>> call, Response<ApiResponse<DrinkRecognitionResponse>> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<DrinkRecognitionResponse> apiResponse = response.body();
                        
                        // 从ApiResponse中提取data字段
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            DrinkRecognitionResponse result = apiResponse.getData();
                            drinkRecognitionResult.postValue(result);
                            
                            // 安全地访问嵌套对象,避免NullPointerException
                            if (result.getRecognition() != null && result.getRecognition().getDrinkName() != null) {
                                Log.d(TAG, "饮品识别成功: " + result.getRecognition().getDrinkName());
                            } else {
                                Log.d(TAG, "饮品识别成功,但识别结果为空");
                            }
                        } else {
                            String errorMsg = "识别失败: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "未知错误");
                            errorMessage.postValue(errorMsg);
                            Log.e(TAG, errorMsg);
                        }
                    } else {
                        errorMessage.postValue("识别失败: " + response.code());
                        Log.e(TAG, "饮品识别失败: " + response.code());
                    }
                    
                    // 清理临时文件
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<DrinkRecognitionResponse>> call, Throwable t) {
                    isLoading.postValue(false);
                    errorMessage.postValue("网络错误: " + t.getMessage());
                    Log.e(TAG, "饮品识别网络错误", t);
                    
                    // 清理临时文件
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
            });
            
        } catch (IOException e) {
            isLoading.postValue(false);
            errorMessage.postValue("图片处理失败: " + e.getMessage());
            Log.e(TAG, "图片处理失败", e);
        }
    }
    
    /**
     * 健康问答
     */
    public void chat(int userId, String message) {
        isLoading.setValue(true);
        errorMessage.setValue(null); // 清空错误消息
        
        ChatRequest request = new ChatRequest(userId, message);
        
        Call<ApiResponse<ChatResponse>> call = aiApiService.chat(request);
        call.enqueue(new Callback<ApiResponse<ChatResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChatResponse>> call, Response<ApiResponse<ChatResponse>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ChatResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ChatResponse chatData = apiResponse.getData();
                        chatResponse.postValue(chatData);
                        errorMessage.postValue(null); // 清除错误消息
                        Log.d(TAG, "聊天成功: " + (chatData.getResponse() != null ? chatData.getResponse().substring(0, Math.min(50, chatData.getResponse().length())) : "null"));
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "聊天失败 (API返回错误)";
                        errorMessage.postValue(errorMsg);
                        Log.e(TAG, "聊天失败: " + errorMsg);
                    }
                } else {
                    errorMessage.postValue("聊天失败: " + response.code());
                    Log.e(TAG, "聊天失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ChatResponse>> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("网络错误: " + t.getMessage());
                Log.e(TAG, "聊天网络错误", t);
            }
        });
    }
    
    /**
     * 获取饮品推荐
     */
    public void getRecommendations(int userId, String strategy, int limit) {
        isLoading.setValue(true);
        errorMessage.setValue(null); // 清空错误消息
        
        RecommendationRequest request = new RecommendationRequest(userId, strategy, limit);
        
        Call<ApiResponse<RecommendationResponse>> call = aiApiService.getRecommendations(request);
        call.enqueue(new Callback<ApiResponse<RecommendationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<RecommendationResponse>> call, Response<ApiResponse<RecommendationResponse>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<RecommendationResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        RecommendationResponse recData = apiResponse.getData();
                        recommendationResult.postValue(recData);
                        errorMessage.postValue(null); // 清除错误消息
                        Log.d(TAG, "推荐成功: " + recData.getRecommendationCount() + "条");
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "推荐失败 (API返回错误)";
                        errorMessage.postValue(errorMsg);
                        Log.e(TAG, "推荐失败: " + errorMsg);
                    }
                } else {
                    errorMessage.postValue("推荐失败: " + response.code());
                    Log.e(TAG, "推荐失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<RecommendationResponse>> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("网络错误: " + t.getMessage());
                Log.e(TAG, "推荐网络错误", t);
            }
        });
    }
    
    /**
     * 获取健康分析
     * 注意: userId从JWT token自动获取,不需要传递参数
     */
    public void getHealthAnalysis(int userId, int days) {
        isLoading.setValue(true);
        errorMessage.setValue(""); // 使用空字符串而不是null
        
        // userId从JWT token自动获取,不需要传递参数
        Call<ApiResponse<HealthAnalysisResponse>> call = aiApiService.getHealthAnalysis(days);
        call.enqueue(new Callback<ApiResponse<HealthAnalysisResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<HealthAnalysisResponse>> call, Response<ApiResponse<HealthAnalysisResponse>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<HealthAnalysisResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        HealthAnalysisResponse healthData = apiResponse.getData();
                        healthAnalysisResult.postValue(healthData);
                        Log.d(TAG, "健康分析成功");
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "健康分析失败 (API返回错误)";
                        errorMessage.postValue(errorMsg);
                        Log.e(TAG, "健康分析失败: " + errorMsg);
                    }
                } else {
                    errorMessage.postValue("健康分析失败: " + response.code());
                    Log.e(TAG, "健康分析失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<HealthAnalysisResponse>> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("网络错误: " + t.getMessage());
                Log.e(TAG, "健康分析网络错误", t);
            }
        });
    }
    
    /**
     * 将Bitmap转换为File
     */
    private File bitmapToFile(Bitmap bitmap) throws IOException {
        File file = new File(getApplication().getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
        byte[] bitmapData = bos.toByteArray();
        
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapData);
        fos.flush();
        fos.close();
        
        return file;
    }
    
    /**
     * 清除错误消息
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}

