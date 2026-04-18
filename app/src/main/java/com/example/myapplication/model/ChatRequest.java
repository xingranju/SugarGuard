package com.example.myapplication.model;

/**
 * 智能对话请求模型
 */
public class ChatRequest {
    private int userId;
    private String message;
    private Boolean saveHistory;
    
    public ChatRequest(int userId, String message) {
        this.userId = userId;
        this.message = message;
        this.saveHistory = true;
    }
    
    public ChatRequest(int userId, String message, boolean saveHistory) {
        this.userId = userId;
        this.message = message;
        this.saveHistory = saveHistory;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Boolean getSaveHistory() {
        return saveHistory;
    }
    
    public void setSaveHistory(Boolean saveHistory) {
        this.saveHistory = saveHistory;
    }
}

