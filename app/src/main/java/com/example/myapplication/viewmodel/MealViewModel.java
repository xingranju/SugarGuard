package com.example.myapplication.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.api.MealApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.model.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * 饮食日记ViewModel - 已集成MySQL数据库
 */
public class MealViewModel extends ViewModel {
    private static final String TAG = "MealViewModel";
    
    private final MealApiService mealApiService;
    
    // LiveData
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");
    private final MutableLiveData<List<Map<String, Object>>> dailyMeals = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now());
    
    public MealViewModel() {
        mealApiService = RetrofitClient.getMealApiService();
    }
    
    // Getters for LiveData
    public LiveData<Boolean> isLoading() { return isLoading; }
    public LiveData<String> errorMessage() { return errorMessage; }
    public LiveData<List<Map<String, Object>>> dailyMeals() { return dailyMeals; }
    public LiveData<LocalDate> selectedDate() { return selectedDate; }
    
    /**
     * 添加饮食记录(带图片)
     * 匹配后端 POST /api/meals/with-image
     */
    public void addMealWithImage(int userId, String foodName, float sugarContent, float calories,
                                Float protein, Float fat, Float carbohydrate, Float portionSize,
                                String notes, MealType mealType, File imageFile) {
        isLoading.setValue(true);
        errorMessage.setValue("");
        
        try {
            // 创建所有RequestBody参数
            RequestBody userIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
            RequestBody foodNamePart = RequestBody.create(MediaType.parse("text/plain"), foodName);
            RequestBody sugarContentPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(sugarContent));
            RequestBody caloriesPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(calories));
            RequestBody mealTypePart = RequestBody.create(MediaType.parse("text/plain"), mealType.name());
            RequestBody notesPart = RequestBody.create(MediaType.parse("text/plain"), notes != null ? notes : "");
            
            RequestBody proteinPart = protein != null ? RequestBody.create(MediaType.parse("text/plain"), String.valueOf(protein)) : null;
            RequestBody fatPart = fat != null ? RequestBody.create(MediaType.parse("text/plain"), String.valueOf(fat)) : null;
            RequestBody carbPart = carbohydrate != null ? RequestBody.create(MediaType.parse("text/plain"), String.valueOf(carbohydrate)) : null;
            RequestBody portionPart = portionSize != null ? RequestBody.create(MediaType.parse("text/plain"), String.valueOf(portionSize)) : null;
            
            // 创建图片的MultipartBody.Part
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);
            
            Call<ApiResponse<Map<String, Object>>> call = mealApiService.addMealWithImage(
                userIdPart, foodNamePart, sugarContentPart, caloriesPart,
                proteinPart, fatPart, carbPart, portionPart,
                notesPart, mealTypePart, imagePart
            );
            
            call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, Object>>> call, 
                                     Response<ApiResponse<Map<String, Object>>> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Map<String, Object>> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            errorMessage.postValue("添加成功!");
                            Log.d(TAG, "添加饮食记录成功: " + apiResponse.getMessage());
                            // 刷新当天的记录
                            getDailyMeals(userId, selectedDate.getValue());
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "添加失败 (API返回错误)";
                            errorMessage.postValue(errorMsg);
                            Log.e(TAG, "添加饮食记录失败: " + errorMsg);
                        }
                    } else {
                        errorMessage.postValue("添加饮食记录失败: " + response.code());
                        Log.e(TAG, "添加饮食记录失败: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                    isLoading.postValue(false);
                    errorMessage.postValue("网络错误: " + t.getMessage());
                    Log.e(TAG, "添加饮食记录网络错误", t);
                }
            });
        } catch (Exception e) {
            isLoading.postValue(false);
            errorMessage.postValue("创建请求失败: " + e.getMessage());
            Log.e(TAG, "创建请求失败", e);
        }
    }
    
    /**
     * 获取指定日期的饮食记录
     * 匹配后端 GET /api/meals/daily
     */
    public void getDailyMeals(int userId, LocalDate date) {
        isLoading.setValue(true);
        errorMessage.setValue("");
        
        String dateStr = date.toString(); // yyyy-MM-dd 格式
        
        Call<ApiResponse<Map<String, Object>>> call = mealApiService.getDailyMeals(userId, dateStr);
        call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, 
                                 Response<ApiResponse<Map<String, Object>>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        
                        // 提取meals列表
                        Object mealsObj = data.get("meals");
                        if (mealsObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> mealsList = (List<Map<String, Object>>) mealsObj;
                            dailyMeals.postValue(mealsList);
                            Log.d(TAG, "加载饮食记录成功: " + date + ", 记录数: " + mealsList.size());
                        } else {
                            dailyMeals.postValue(new ArrayList<>());
                        }
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "加载失败";
                        errorMessage.postValue(errorMsg);
                        dailyMeals.postValue(new ArrayList<>());
                        Log.e(TAG, "加载饮食记录失败: " + errorMsg);
                    }
                } else {
                    errorMessage.postValue("加载失败: " + response.code());
                    dailyMeals.postValue(new ArrayList<>());
                    Log.e(TAG, "加载饮食记录失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("网络错误: " + t.getMessage());
                dailyMeals.postValue(new ArrayList<>());
                Log.e(TAG, "加载饮食记录网络错误", t);
            }
        });
    }
    
    /**
     * 删除饮食记录
     * 匹配后端 DELETE /api/meals/{meal_id}?user_id=xxx
     */
    public void deleteMeal(long userId, int mealId) {
        isLoading.setValue(true);
        errorMessage.setValue("");
        
        Call<ApiResponse<String>> call = mealApiService.deleteMeal(mealId, (int)userId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "删除饮食记录成功: " + mealId);
                        errorMessage.postValue("删除成功");
                        // 刷新当天的记录
                        getDailyMeals((int)userId, selectedDate.getValue());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "删除失败";
                        errorMessage.postValue(errorMsg);
                        Log.e(TAG, "删除饮食记录失败: " + errorMsg);
                    }
                } else {
                    errorMessage.postValue("删除失败: " + response.code());
                    Log.e(TAG, "删除饮食记录失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("网络错误: " + t.getMessage());
                Log.e(TAG, "删除饮食记录网络错误", t);
            }
        });
    }
    
    /**
     * 选择日期
     * @param date 选择的日期
     * @param userId 当前用户ID (由调用方传入)
     */
    public void selectDate(LocalDate date, int userId) {
        selectedDate.setValue(date);
        // 自动加载该日期的记录
        getDailyMeals(userId, date);
    }
}
