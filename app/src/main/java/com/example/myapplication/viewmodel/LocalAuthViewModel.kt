package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocalAuthViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "LocalAuthViewModel"
    private val authApi = RetrofitClient.getAuthApiService()
    private val prefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _loginResult = MutableLiveData<UserDto?>()
    val loginResult: LiveData<UserDto?> = _loginResult

    private val _registerResult = MutableLiveData<UserDto?>()
    val registerResult: LiveData<UserDto?> = _registerResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(username: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = ""

        val request = LoginRequest(username, password)
        authApi.login(request).enqueue(object : Callback<ApiResponse<LoginResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<LoginResponse>>,
                response: Response<ApiResponse<LoginResponse>>
            ) {
                _isLoading.postValue(false)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.isSuccess && apiResponse.data != null) {
                        val loginResp = apiResponse.data
                        saveLoginState(loginResp)
                        _loginResult.postValue(loginResp.user)
                        Log.d(TAG, "登录成功: ${loginResp.user?.username}")
                    } else {
                        _errorMessage.postValue(apiResponse.message ?: "登录失败")
                        Log.e(TAG, "登录失败: ${apiResponse.message}")
                    }
                } else {
                    _errorMessage.postValue("用户名或密码错误")
                    Log.e(TAG, "登录失败: HTTP ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginResponse>>, t: Throwable) {
                _isLoading.postValue(false)
                _errorMessage.postValue("网络连接失败: ${t.message}")
                Log.e(TAG, "登录网络错误", t)
            }
        })
    }

    fun register(
        username: String, email: String, password: String, confirmPassword: String,
        phone: String?, gender: String?, birthday: String?
    ) {
        if (password != confirmPassword) {
            _errorMessage.value = "两次输入的密码不一致"
            return
        }
        _isLoading.value = true
        _errorMessage.value = ""

        val request = RegisterRequest(
            username = username,
            email = email.ifBlank { "$username@sugarguard.app" },
            password = password,
            confirmPassword = confirmPassword,
            phone = phone,
            gender = gender,
            birthday = birthday
        )
        authApi.register(request).enqueue(object : Callback<ApiResponse<UserDto>> {
            override fun onResponse(
                call: Call<ApiResponse<UserDto>>,
                response: Response<ApiResponse<UserDto>>
            ) {
                _isLoading.postValue(false)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.isSuccess && apiResponse.data != null) {
                        val user = apiResponse.data
                        autoLoginAfterRegister(username, password, user)
                    } else {
                        _errorMessage.postValue(apiResponse.message ?: "注册失败")
                        Log.e(TAG, "注册失败: ${apiResponse.message}")
                    }
                } else {
                    _errorMessage.postValue("注册失败，用户名可能已存在")
                    Log.e(TAG, "注册失败: HTTP ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {
                _isLoading.postValue(false)
                _errorMessage.postValue("网络连接失败: ${t.message}")
                Log.e(TAG, "注册网络错误", t)
            }
        })
    }

    private fun autoLoginAfterRegister(username: String, password: String, registeredUser: UserDto) {
        val loginReq = LoginRequest(username, password)
        authApi.login(loginReq).enqueue(object : Callback<ApiResponse<LoginResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<LoginResponse>>,
                response: Response<ApiResponse<LoginResponse>>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val loginResp = response.body()!!.data
                    if (loginResp != null) {
                        saveLoginState(loginResp)
                        _registerResult.postValue(loginResp.user ?: registeredUser)
                        Log.d(TAG, "注册后自动登录成功")
                    }
                } else {
                    prefs.edit()
                        .putLong("user_id", registeredUser.id)
                        .putString("username", registeredUser.username)
                        .apply()
                    _registerResult.postValue(registeredUser)
                    Log.w(TAG, "注册后自动登录失败，仅保存用户信息")
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginResponse>>, t: Throwable) {
                prefs.edit()
                    .putLong("user_id", registeredUser.id)
                    .putString("username", registeredUser.username)
                    .apply()
                _registerResult.postValue(registeredUser)
            }
        })
    }

    fun logout() {
        authApi.logout().enqueue(object : Callback<ApiResponse<Void>> {
            override fun onResponse(call: Call<ApiResponse<Void>>, response: Response<ApiResponse<Void>>) {}
            override fun onFailure(call: Call<ApiResponse<Void>>, t: Throwable) {}
        })
        prefs.edit().clear().apply()
        _loginResult.value = null
    }

    fun quickSetup() {
        login("demo", "123456")
    }

    fun isLoggedIn(): Boolean {
        return prefs.getLong("user_id", -1) > 0 &&
               !prefs.getString("access_token", null).isNullOrEmpty()
    }

    fun getCurrentUserId(): Long {
        return prefs.getLong("user_id", 1)
    }

    fun getCurrentUsername(): String {
        return prefs.getString("username", "糖知用户") ?: "糖知用户"
    }

    private fun saveLoginState(loginResp: LoginResponse) {
        prefs.edit()
            .putLong("user_id", loginResp.user?.id ?: 1)
            .putString("username", loginResp.user?.username ?: "")
            .putString("access_token", loginResp.token ?: "")
            .putString("refresh_token", loginResp.refreshToken ?: "")
            .apply()
    }
}
