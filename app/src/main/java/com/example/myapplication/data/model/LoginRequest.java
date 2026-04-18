package com.example.myapplication.data.model;

/**
 * 登录请求类
 * dengLu_qingQiu
 */
public class LoginRequest {
    private String username;
    private String password;
    private String deviceInfo;
    private String ipAddress;

    // 默认构造函数
    public LoginRequest() {
    }

    // 带参数构造函数
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // 全参数构造函数
    public LoginRequest(String username, String password, String deviceInfo, String ipAddress) {
        this.username = username;
        this.password = password;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
    }

    // Getter 和 Setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
