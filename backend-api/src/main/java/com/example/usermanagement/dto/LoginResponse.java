package com.example.usermanagement.dto;

/**
 * 登录响应DTO
 * dengLu_xiangYing_DTO
 */
public class LoginResponse {

    private String token;
    private String refreshToken;
    private long expiresIn;
    private UserDto user;
    private String sessionId;

    public LoginResponse() {
    }

    public LoginResponse(String token, String refreshToken, long expiresIn, UserDto user, String sessionId) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
        this.sessionId = sessionId;
    }

    // Getter 和 Setter
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

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='[PROTECTED]'" +
                ", refreshToken='[PROTECTED]'" +
                ", expiresIn=" + expiresIn +
                ", user=" + user +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
