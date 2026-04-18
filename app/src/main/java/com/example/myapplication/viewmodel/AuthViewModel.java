package com.example.myapplication.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapplication.data.model.LoginResponse;
import com.example.myapplication.data.model.User;
import com.example.myapplication.data.repository.AuthRepository;

/**
 * 认证ViewModel
 * renZheng_ViewModel
 */
public class AuthViewModel extends AndroidViewModel {
    private AuthRepository authRepository;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    /**
     * 登录
     * dengLu
     */
    public void login(String username, String password) {
        authRepository.login(username, password);
    }

    /**
     * 注册
     * zhuCe
     */
    public void register(String username, String email, String password, String confirmPassword,
                        String phone, String gender, String birthday) {
        authRepository.register(username, email, password, confirmPassword, phone, gender, birthday);
    }

    /**
     * 登出
     * dengChu
     */
    public void logout() {
        authRepository.logout();
    }

    /**
     * 获取当前用户信息
     * huoQu_dangQian_yongHu_xinXi
     */
    public void getCurrentUser() {
        authRepository.getCurrentUser();
    }

    /**
     * 检查是否已登录
     * jianCha_shiFou_yi_dengLu
     */
    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    /**
     * 获取保存的token
     * huoQu_baoCun_de_token
     */
    public String getSavedToken() {
        return authRepository.getSavedToken();
    }

    // LiveData观察者
    public LiveData<LoginResponse> getLoginResult() {
        return authRepository.getLoginResult();
    }

    public LiveData<User> getRegisterResult() {
        return authRepository.getRegisterResult();
    }

    public LiveData<Boolean> getLogoutResult() {
        return authRepository.getLogoutResult();
    }

    public LiveData<String> getErrorMessage() {
        return authRepository.getErrorMessage();
    }
}
