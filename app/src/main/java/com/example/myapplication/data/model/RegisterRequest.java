package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * 注册请求类
 * zhuCe_qingQiu
 */
public class RegisterRequest {
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("confirmPassword")
    private String confirmPassword;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("gender")
    private String gender;
    
    @SerializedName("birthday")
    private String birthday;

    // 默认构造函数
    public RegisterRequest() {
    }

    // 基本构造函数
    public RegisterRequest(String username, String email, String password, String confirmPassword) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // 全参数构造函数
    public RegisterRequest(String username, String email, String password, String confirmPassword,
                          String phone, String gender, String birthday) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.phone = phone;
        this.gender = gender;
        this.birthday = birthday;
    }

    // Getter 和 Setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    // 验证密码是否匹配
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }

    // 验证邮箱格式（简单验证）
    public boolean isValidEmail() {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    // 验证手机号格式（中国手机号）
    public boolean isValidPhone() {
        if (phone == null || phone.isEmpty()) {
            return true; // 手机号可选
        }
        String phoneRegex = "^1[3-9]\\d{9}$";
        return phone.matches(phoneRegex);
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }
}
