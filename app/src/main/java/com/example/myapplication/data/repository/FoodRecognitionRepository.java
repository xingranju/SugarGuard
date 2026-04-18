package com.example.myapplication.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.api.ApiClient;
import com.example.myapplication.data.api.FoodRecognitionApiService;
import com.example.myapplication.data.model.ApiResponse;
import com.example.myapplication.data.model.FoodItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 食物识别仓库
 * shiWu_shibie_cangKu
 */
public class FoodRecognitionRepository {
    private static final String TAG = "FoodRecognitionRepo";
    
    private FoodRecognitionApiService apiService;
    private Context context;
    
    private MutableLiveData<FoodItem> recognitionResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    
    public FoodRecognitionRepository(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = ApiClient.getFoodRecognitionApiService(this.context);
    }
    
    /**
     * 识别食物
     * shibie_shiWu
     */
    public void recognizeFood(Uri imageUri) {
        isLoading.postValue(true);
        
        try {
            // 将URI转换为文件
            File imageFile = uriToFile(imageUri);
            if (imageFile == null) {
                errorMessage.postValue("无法读取图片文件");
                isLoading.postValue(false);
                return;
            }
            
            // 创建RequestBody
            RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                imageFile
            );
            
            // 创建MultipartBody.Part
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.getName(),
                requestFile
            );
            
            // 调用API
            Call<ApiResponse<FoodItem>> call = apiService.recognizeFood(imagePart);
            call.enqueue(new Callback<ApiResponse<FoodItem>>() {
                @Override
                public void onResponse(Call<ApiResponse<FoodItem>> call, 
                                     Response<ApiResponse<FoodItem>> response) {
                    isLoading.postValue(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<FoodItem> apiResponse = response.body();
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            recognitionResult.postValue(apiResponse.getData());
                            Log.d(TAG, "食物识别成功: " + apiResponse.getData().getName_CN());
                        } else {
                            errorMessage.postValue(apiResponse.getMessage());
                        }
                    } else {
                        errorMessage.postValue("识别失败: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<FoodItem>> call, Throwable t) {
                    isLoading.postValue(false);
                    errorMessage.postValue("网络错误: " + t.getMessage());
                    Log.e(TAG, "识别失败", t);
                }
            });
            
        } catch (Exception e) {
            isLoading.postValue(false);
            errorMessage.postValue("处理图片失败: " + e.getMessage());
            Log.e(TAG, "处理图片失败", e);
        }
    }
    
    /**
     * 将URI转换为文件
     * jiang_URI_zhuanHuan_wei_wenJian
     */
    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            
            // 创建临时文件
            File tempFile = new File(context.getCacheDir(), "temp_food_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            
            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "URI转文件失败", e);
            return null;
        }
    }
    
    // Getters for LiveData
    public LiveData<FoodItem> getRecognitionResult() {
        return recognitionResult;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}

