package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.example.myapplication.data.model.User;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * 用户管理类 - 用于管理当前登录用户的信息
 */
public class UserManager {
    private static final String TAG = "UserManager";
    private static final String PREF_NAME = "auth_prefs";
    private static final String TOKEN_KEY = "access_token";
    private static final String USER_KEY = "user_info";
    private static final String USER_ID_KEY = "user_id";
    
    private static UserManager instance;
    private SharedPreferences prefs;
    private Gson gson;
    
    private UserManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }
    
    /**
     * 保存用户信息
     */
    public void saveUser(User user) {
        if (user == null) {
            Log.w(TAG, "尝试保存空的用户对象");
            return;
        }
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_KEY, gson.toJson(user));
        if (user.getId() != null) {
            editor.putLong(USER_ID_KEY, user.getId());
            Log.d(TAG, "用户信息已保存: userId=" + user.getId() + ", username=" + user.getUsername());
        } else {
            Log.w(TAG, "用户对象没有ID: username=" + user.getUsername());
        }
        editor.apply();
    }
    
    /**
     * 获取当前用户信息
     */
    public User getCurrentUser() {
        String userJson = prefs.getString(USER_KEY, null);
        if (userJson != null) {
            try {
                return gson.fromJson(userJson, User.class);
            } catch (Exception e) {
                Log.e(TAG, "解析用户信息失败", e);
            }
        }
        return null;
    }
    
    /**
     * 获取当前用户ID
     * 优先从SharedPreferences获取,如果没有则从JWT token中解析
     */
    public Long getCurrentUserId() {
        // 1. 先尝试从SharedPreferences获取
        long savedUserId = prefs.getLong(USER_ID_KEY, -1);
        if (savedUserId != -1) {
            Log.d(TAG, "从SharedPreferences获取用户ID: " + savedUserId);
            return savedUserId;
        }
        
        // 2. 尝试从保存的User对象获取
        User user = getCurrentUser();
        if (user != null && user.getId() != null) {
            // 保存到SharedPreferences以便下次快速访问
            prefs.edit().putLong(USER_ID_KEY, user.getId()).apply();
            Log.d(TAG, "从User对象获取用户ID: " + user.getId());
            return user.getId();
        }
        
        // 3. 从JWT token中解析
        String token = prefs.getString(TOKEN_KEY, null);
        if (token != null) {
            try {
                Long userId = parseUserIdFromToken(token);
                if (userId != null && userId > 0) {
                    // 保存到SharedPreferences
                    prefs.edit().putLong(USER_ID_KEY, userId).apply();
                    Log.d(TAG, "从JWT token解析到用户ID: " + userId);
                    return userId;
                } else {
                    Log.w(TAG, "JWT token中的用户ID无效: " + userId);
                }
            } catch (Exception e) {
                Log.e(TAG, "从token解析userId失败", e);
            }
        } else {
            Log.w(TAG, "没有找到JWT token");
        }
        
        // 4. 如果都失败了，抛出异常而不是返回默认值
        Log.e(TAG, "无法获取用户ID - 所有方法都失败了");
        Log.e(TAG, "Debug信息 - savedUserId: " + savedUserId + ", user: " + (user != null) + ", token: " + (token != null));
        throw new IllegalStateException("无法获取当前用户ID，请重新登录");
    }
    
    /**
     * 从JWT token中解析用户ID
     */
    private Long parseUserIdFromToken(String token) {
        try {
            // JWT格式: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            
            // 解码payload部分
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP));
            JSONObject jsonObject = new JSONObject(payload);
            
            // 尝试不同的字段名
            if (jsonObject.has("userId")) {
                return jsonObject.getLong("userId");
            } else if (jsonObject.has("user_id")) {
                return jsonObject.getLong("user_id");
            } else if (jsonObject.has("id")) {
                return jsonObject.getLong("id");
            } else if (jsonObject.has("sub")) {
                // 有些JWT使用sub作为用户标识
                String sub = jsonObject.getString("sub");
                try {
                    return Long.parseLong(sub);
                } catch (NumberFormatException e) {
                    // sub可能是username,不是userId
                    Log.w(TAG, "JWT sub不是数字: " + sub);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "解析JWT token失败", e);
        }
        return null;
    }
    
    /**
     * 清除用户信息
     */
    public void clearUser() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(USER_KEY);
        editor.remove(USER_ID_KEY);
        editor.apply();
        Log.d(TAG, "用户信息已清除");
    }
    
    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        String token = prefs.getString(TOKEN_KEY, null);
        return token != null && !token.isEmpty();
    }
}

