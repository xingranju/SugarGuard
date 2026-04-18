package com.example.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 智能对话请求DTO
 */
public class ChatRequest {
    private String message;
    @JsonProperty("saveHistory")
    private Boolean saveHistory;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
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

