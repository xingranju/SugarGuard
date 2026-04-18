package com.example.myapplication.data.model;

/**
 * 登录响应类
 * dengLu_xiangYing
 */
public class LoginResponse {
    private String token;
    private String refreshToken;
    private long expiresIn;
    private User user;
    private String sessionId;

    // 默认构造函数
    public LoginResponse() {
    }

    // 基本构造函数
    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    // 全参数构造函数
    public LoginResponse(String token, String refreshToken, long expiresIn, User user, String sessionId) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
        this.sessionId = sessionId;
    }

    // Getter 和 Setter 方法
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    // 检查token是否有效
    public boolean isTokenValid() {
        return token != null && !token.isEmpty() && expiresIn > System.currentTimeMillis() / 1000;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + (token != null ? "[PROTECTED]" : "null") + '\'' +
                ", refreshToken='" + (refreshToken != null ? "[PROTECTED]" : "null") + '\'' +
                ", expiresIn=" + expiresIn +
                ", user=" + user +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
