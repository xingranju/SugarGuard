package com.example.myapplication.data.api;

import android.content.Context;

import com.example.myapplication.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API客户端配置类
 * API_keHu_peiZhi
 */
public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/"; // Android模拟器默认IP
    // 如果是真机，请替换为实际服务器IP，如：http://192.168.1.100:8080/

    private static Retrofit retrofit = null;

    /**
     * 获取Retrofit实例
     * huoQu_Retrofit_shiLi
     */
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            // 创建OkHttp客户端
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            // 添加请求头拦截器
            httpClient.addInterceptor(new AuthInterceptor(context));

            // 在调试模式下添加日志拦截器
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClient.addInterceptor(logging);
            }

            // 构建OkHttp客户端
            OkHttpClient client = httpClient.build();

            // 构建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    /**
     * 获取认证API服务
     * huoQu_renZheng_API_fuWu
     */
    public static AuthApiService getAuthApiService(Context context) {
        return getClient(context).create(AuthApiService.class);
    }
    
    /**
     * 获取食物识别API服务
     * huoQu_shiWu_shibie_API_fuWu
     */
    public static FoodRecognitionApiService getFoodRecognitionApiService(Context context) {
        return getClient(context).create(FoodRecognitionApiService.class);
    }

    /**
     * 重新创建Retrofit实例（用于更新token后）
     * chongXin_chuangJian_Retrofit_shiLi
     */
    public static void resetClient() {
        retrofit = null;
    }
}
