package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 饮品推荐响应模型
 */
public class RecommendationResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("recommendations")
    private List<DrinkRecommendation> recommendations;
    
    @SerializedName("user_sugar_limit")
    private float userSugarLimit;
    
    @SerializedName("recommendation_strategy")
    private String recommendationStrategy;
    
    @SerializedName("recommendation_count")
    private int recommendationCount;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public List<DrinkRecommendation> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<DrinkRecommendation> recommendations) {
        this.recommendations = recommendations;
    }
    
    public float getUserSugarLimit() {
        return userSugarLimit;
    }
    
    public void setUserSugarLimit(float userSugarLimit) {
        this.userSugarLimit = userSugarLimit;
    }
    
    public String getRecommendationStrategy() {
        return recommendationStrategy;
    }
    
    public void setRecommendationStrategy(String recommendationStrategy) {
        this.recommendationStrategy = recommendationStrategy;
    }
    
    public int getRecommendationCount() {
        return recommendationCount;
    }
    
    public void setRecommendationCount(int recommendationCount) {
        this.recommendationCount = recommendationCount;
    }
    
    /**
     * 饮品推荐子模型
     */
    public static class DrinkRecommendation {
        @SerializedName("drink_id")
        private int drinkId;
        
        @SerializedName("drink_name")
        private String drinkName;
        
        @SerializedName("brand")
        private String brand;
        
        @SerializedName("category")
        private String category;
        
        @SerializedName("sugar_content")
        private float sugarContent;
        
        @SerializedName("calories")
        private float calories;
        
        @SerializedName("volume")
        private float volume;
        
        @SerializedName("caffeine")
        private float caffeine;
        
        @SerializedName("health_score")
        private int healthScore;
        
        @SerializedName("image_url")
        private String imageUrl;
        
        @SerializedName("reason")
        private String reason;
        
        @SerializedName("ai_recommendation_reason")
        private String aiRecommendationReason;
        
        @SerializedName("ai_health_advice")
        private String aiHealthAdvice;
        
        @SerializedName("recommendation_score")
        private float recommendationScore;
        
        // Getters and Setters
        public int getDrinkId() {
            return drinkId;
        }
        
        public void setDrinkId(int drinkId) {
            this.drinkId = drinkId;
        }
        
        public String getDrinkName() {
            return drinkName;
        }
        
        public void setDrinkName(String drinkName) {
            this.drinkName = drinkName;
        }
        
        public String getBrand() {
            return brand;
        }
        
        public void setBrand(String brand) {
            this.brand = brand;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public float getSugarContent() {
            return sugarContent;
        }
        
        public void setSugarContent(float sugarContent) {
            this.sugarContent = sugarContent;
        }
        
        public float getCalories() {
            return calories;
        }
        
        public void setCalories(float calories) {
            this.calories = calories;
        }
        
        public float getCaffeine() {
            return caffeine;
        }
        
        public void setCaffeine(float caffeine) {
            this.caffeine = caffeine;
        }
        
        public float getVolume() {
            return volume;
        }
        
        public void setVolume(float volume) {
            this.volume = volume;
        }
        
        public int getHealthScore() {
            return healthScore;
        }
        
        public void setHealthScore(int healthScore) {
            this.healthScore = healthScore;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
        
        public String getAiRecommendationReason() {
            return aiRecommendationReason;
        }
        
        public void setAiRecommendationReason(String aiRecommendationReason) {
            this.aiRecommendationReason = aiRecommendationReason;
        }
        
        public String getAiHealthAdvice() {
            return aiHealthAdvice;
        }
        
        public void setAiHealthAdvice(String aiHealthAdvice) {
            this.aiHealthAdvice = aiHealthAdvice;
        }
        
        public float getRecommendationScore() {
            return recommendationScore;
        }
        
        public void setRecommendationScore(float recommendationScore) {
            this.recommendationScore = recommendationScore;
        }
    }
}

