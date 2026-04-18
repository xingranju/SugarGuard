package com.example.usermanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 对话历史实体类
 */
@Entity
@Table(name = "conversation_history")
public class ConversationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Integer conversationId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "response", columnDefinition = "TEXT", nullable = false)
    private String response;
    
    @Column(name = "intent", length = 50)
    private String intent;
    
    @Column(name = "context_data", columnDefinition = "JSON")
    private String contextData;
    
    @Column(name = "feedback")
    private Integer feedback;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public ConversationHistory() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    public String getContextData() {
        return contextData;
    }
    
    public void setContextData(String contextData) {
        this.contextData = contextData;
    }
    
    public Integer getFeedback() {
        return feedback;
    }
    
    public void setFeedback(Integer feedback) {
        this.feedback = feedback;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}






