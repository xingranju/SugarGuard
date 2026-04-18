package com.example.myapplication.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.api.ApiClient;
import com.example.myapplication.data.api.AuthApiService;
import com.example.myapplication.data.model.ApiResponse;
import com.example.myapplication.data.model.LoginRequest;
import com.example.myapplication.data.model.LoginResponse;
import com.example.myapplication.data.model.RegisterRequest;
import com.example.myapplication.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 认证数据仓库
 * renZheng_shuJu_cangKu
 */
public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private static final String PREF_NAME = "auth_prefs";
    private static final String TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String USER_KEY = "user_data";

    private final AuthApiService apiService;
    private final SharedPreferences prefs;
    private final Context context;

    // LiveData用于观察登录状态
    private MutableLiveData<LoginResponse> loginResult = new MutableLiveData<>();
    private MutableLiveData<User> registerResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> logoutResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthRepository(Context context) {
        this.context = context;
        this.apiService = ApiClient.getAuthApiService(context);
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 用户登录
     * yongHu_dengLu
     */
    public void login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        // 添加设备信息
        loginRequest.setDeviceInfo(getDeviceInfo());

        apiService.login(loginRequest).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        // 保存token和用户信息
                        saveAuthData(apiResponse.getData());
                        loginResult.setValue(apiResponse.getData());
                        Log.d(TAG, "登录成功: " + username);
                    } else {
                        errorMessage.setValue(apiResponse.getMessage());
                        Log.e(TAG, "登录失败: " + apiResponse.getMessage());
                    }
                } else {
                    errorMessage.setValue("网络请求失败，请检查网络连接");
                    Log.e(TAG, "网络请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                errorMessage.setValue("网络连接失败: " + t.getMessage());
                Log.e(TAG, "网络连接失败", t);
            }
        });
    }

    /**
     * 用户注册
     * yongHu_zhuCe
     */
    public void register(String username, String email, String password, String confirmPassword,
                        String phone, String gender, String birthday) {
        RegisterRequest registerRequest = new RegisterRequest(username, email, password, confirmPassword,
                phone, gender, birthday);
        
        // 调试日志：打印请求对象
        Log.d(TAG, "创建注册请求 - username: " + username + ", email: " + email);
        Log.d(TAG, "password: " + (password != null ? "已设置" : "null") + ", confirmPassword: " + (confirmPassword != null ? "已设置" : "null"));
        Log.d(TAG, "RegisterRequest toString: " + registerRequest.toString());

        apiService.register(registerRequest).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        registerResult.setValue(apiResponse.getData());
                        Log.d(TAG, "注册成功: " + username);
                    } else {
                        errorMessage.setValue(apiResponse.getMessage());
                        Log.e(TAG, "注册失败: " + apiResponse.getMessage());
                    }
                } else {
                    errorMessage.setValue("网络请求失败，请检查网络连接");
                    Log.e(TAG, "网络请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                errorMessage.setValue("网络连接失败: " + t.getMessage());
                Log.e(TAG, "网络连接失败", t);
            }
        });
    }

    /**
     * 用户登出
     * yongHu_dengChu
     */
    public void logout() {
        apiService.logout().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                // 无论服务器响应如何，都清除本地数据
                clearAuthData();
                logoutResult.setValue(true);
                Log.d(TAG, "登出成功");
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // 即使网络失败，也清除本地数据
                clearAuthData();
                logoutResult.setValue(true);
                Log.e(TAG, "登出网络请求失败，但已清除本地数据", t);
            }
        });
    }

    /**
     * 获取当前用户信息
     * huoQu_dangQian_yongHu_xinXi
     */
    public void getCurrentUser() {
        apiService.getCurrentUser().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        // 更新本地存储的用户信息
                        saveUserData(apiResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e(TAG, "获取用户信息失败", t);
            }
        });
    }

    /**
     * 检查用户是否已登录
     * jianCha_yongHu_shiFou_yi_dengLu
     */
    public boolean isLoggedIn() {
        String token = prefs.getString(TOKEN_KEY, null);
        return token != null && !token.isEmpty();
    }

    /**
     * 获取保存的token
     * huoQu_baoCun_de_token
     */
    public String getSavedToken() {
        return prefs.getString(TOKEN_KEY, null);
    }

    /**
     * 获取保存的用户信息
     * huoQu_baoCun_de_yongHu_xinXi
     */
    public User getSavedUser() {
        String userJson = prefs.getString(USER_KEY, null);
        if (userJson != null) {
            // 这里应该使用Gson解析，但为了简化暂时返回null
            // 实际项目中需要添加Gson依赖并解析
            return null;
        }
        return null;
    }

    /**
     * 保存认证数据
     * baoCun_renZheng_shuJu
     */
    private void saveAuthData(LoginResponse loginResponse) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_KEY, loginResponse.getToken());
        if (loginResponse.getRefreshToken() != null) {
            editor.putString(REFRESH_TOKEN_KEY, loginResponse.getRefreshToken());
        }
        editor.apply();
        
        // 保存用户信息
        if (loginResponse.getUser() != null) {
            com.example.myapplication.util.UserManager.getInstance(context).saveUser(loginResponse.getUser());
        }
    }

    /**
     * 保存用户信息
     * baoCun_yongHu_xinXi
     */
    private void saveUserData(User user) {
        if (user != null) {
            com.example.myapplication.util.UserManager.getInstance(context).saveUser(user);
        }
    }

    /**
     * 清除认证数据
     * qingChu_renZheng_shuJu
     */
    private void clearAuthData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TOKEN_KEY);
        editor.remove(REFRESH_TOKEN_KEY);
        editor.remove(USER_KEY);
        editor.apply();

        // 重置API客户端
        ApiClient.resetClient();
    }

    /**
     * 获取设备信息
     * huoQu_sheBei_xinXi
     */
    private String getDeviceInfo() {
        return Build.MANUFACTURER + " " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")";
    }

    // LiveData getters
    public LiveData<LoginResponse> getLoginResult() {
        return loginResult;
    }

    public LiveData<User> getRegisterResult() {
        return registerResult;
    }

    public LiveData<Boolean> getLogoutResult() {
        return logoutResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
