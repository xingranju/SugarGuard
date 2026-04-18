package com.example.usermanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户饮品偏好实体类
 */
@Entity
@Table(name = "user_drink_preferences")
public class UserDrinkPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Integer preferenceId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "drink_id", nullable = false)
    private Integer drinkId;
    
    @Column(name = "consumption_count")
    private Integer consumptionCount = 0;
    
    @Column(name = "last_consumed_at")
    private LocalDateTime lastConsumedAt;
    
    @Column(name = "preference_score")
    private Integer preferenceScore = 3;  // 1-5评分，默认3（中等偏好）
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    
    public Integer getPreferenceId() {
        return preferenceId;
    }
    
    public void setPreferenceId(Integer preferenceId) {
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
    
    public Integer getConsumptionCount() {
        return consumptionCount;
    }
    
    public void setConsumptionCount(Integer consumptionCount) {
        this.consumptionCount = consumptionCount;
    }
    
    public LocalDateTime getLastConsumedAt() {
        return lastConsumedAt;
    }
    
    public void setLastConsumedAt(LocalDateTime lastConsumedAt) {
        this.lastConsumedAt = lastConsumedAt;
    }
    
    public Integer getPreferenceScore() {
        return preferenceScore;
    }
    
    public void setPreferenceScore(Integer preferenceScore) {
        this.preferenceScore = preferenceScore;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
