package com.example.usermanagement.dto;

/**
 * 用户饮品偏好DTO
 */
public class UserDrinkPreferenceDto {
    
    private Long preferenceId;
    private Long userId;
    private Integer drinkId;
    private String drinkName;
    private String brand;
    private String category;
    private String imageUrl;
    private Float sugarContent;
    private Float calories;
    private Integer healthScore;
    private Integer preferenceScore;
    private String lastConsumed;
    private Integer timesConsumed;
    private String createdAt;
    private String updatedAt;

    // 默认构造函数
    public UserDrinkPreferenceDto() {
    }

    // Getter 和 Setter 方法
    public Long getPreferenceId() {
        return preferenceId;
    }

    public void setPreferenceId(Long preferenceId) {
        this.preferenceId = preferenceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getDrinkId() {
        return drinkId;
    }

    public void setDrinkId(Integer drinkId) {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Float getSugarContent() {
        return sugarContent;
    }

    public void setSugarContent(Float sugarContent) {
        this.sugarContent = sugarContent;
    }

    public Float getCalories() {
        return calories;
    }

    public void setCalories(Float calories) {
        this.calories = calories;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public Integer getPreferenceScore() {
        return preferenceScore;
    }

    public void setPreferenceScore(Integer preferenceScore) {
        this.preferenceScore = preferenceScore;
    }

    public String getLastConsumed() {
        return lastConsumed;
    }

    public void setLastConsumed(String lastConsumed) {
        this.lastConsumed = lastConsumed;
    }

    public Integer getTimesConsumed() {
        return timesConsumed;
    }

    public void setTimesConsumed(Integer timesConsumed) {
        this.timesConsumed = timesConsumed;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}


