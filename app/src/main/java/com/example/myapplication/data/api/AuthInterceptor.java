package com.example.myapplication.data.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 认证拦截器，用于添加token到请求头
 * renZheng_lanJieQi
 */
public class AuthInterceptor implements Interceptor {
    private static final String PREF_NAME = "auth_prefs";
    private static final String TOKEN_KEY = "access_token";
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // 获取存储的token
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);

        // 构建请求
        Request.Builder builder = chain.request().newBuilder();

        // 添加公共请求头
        builder.addHeader("Content-Type", "application/json");
        builder.addHeader("Accept", "application/json");
        builder.addHeader("User-Agent", "Android-App");

        // 如果有token，添加到Authorization头
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}
