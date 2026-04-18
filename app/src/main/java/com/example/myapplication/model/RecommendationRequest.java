package com.example.myapplication.model;

/**
 * 饮品推荐请求模型
 */
public class RecommendationRequest {
    private int userId;
    private String strategy; // "healthy", "collaborative", "mixed"
    private int limit;
    
    public RecommendationRequest(int userId, String strategy, int limit) {
        this.userId = userId;
        this.strategy = strategy;
        this.limit = limit;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getStrategy() {
        return strategy;
    }
    
    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
}

