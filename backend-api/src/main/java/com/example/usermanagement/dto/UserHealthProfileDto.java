package com.example.usermanagement.dto;

/**
 * 用户健康档案DTO
 */
public class UserHealthProfileDto {
    
    private Long profileId;
    private Long userId;
    private Integer age;
    private String gender;
    private Float height;
    private Float weight;
    private String healthConditions;
    private String allergies;
    private String medications;
    private String activityLevel;
    private Float sugarLimit;
    private Float calorieLimit;
    private Float waterGoal;
    private String createdAt;
    private String updatedAt;

    // 默认构造函数
    public UserHealthProfileDto() {
    }

    // Getter 和 Setter 方法
    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public String getHealthConditions() {
        return healthConditions;
    }

    public void setHealthConditions(String healthConditions) {
        this.healthConditions = healthConditions;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public Float getSugarLimit() {
        return sugarLimit;
    }

    public void setSugarLimit(Float sugarLimit) {
        this.sugarLimit = sugarLimit;
    }

    public Float getCalorieLimit() {
        return calorieLimit;
    }

    public void setCalorieLimit(Float calorieLimit) {
        this.calorieLimit = calorieLimit;
    }

    public Float getWaterGoal() {
        return waterGoal;
    }

    public void setWaterGoal(Float waterGoal) {
        this.waterGoal = waterGoal;
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


