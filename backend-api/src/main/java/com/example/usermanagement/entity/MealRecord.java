package com.example.usermanagement.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 餐食记录实体
 */
@Entity
@Table(name = "meal_records")
public class MealRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Integer mealId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;
    
    @Column(name = "meal_time", nullable = false)
    private LocalDateTime mealTime;
    
    @Column(name = "meal_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MealType mealType;
    
    @Column(name = "drink_id")
    private Integer drinkId;
    
    @Column(name = "food_name", length = 200)
    private String foodName;
    
    @Column(name = "portion_size")
    private Float portionSize;
    
    @Column(name = "calories")
    private Float calories;
    
    @Column(name = "sugar_content")
    private Float sugarContent;
    
    @Column(name = "image_path", length = 500)
    private String imagePath;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public MealRecord() {}
    
    public MealRecord(Long userId, LocalDate mealDate, LocalDateTime mealTime, 
                     MealType mealType, String foodName, Float calories, Float sugarContent) {
        this.userId = userId;
        this.mealDate = mealDate;
        this.mealTime = mealTime;
        this.mealType = mealType;
        this.foodName = foodName;
        this.calories = calories;
        this.sugarContent = sugarContent;
    }
    
    // Getters and Setters
    public Integer getMealId() {
        return mealId;
    }
    
    public void setMealId(Integer mealId) {
        this.mealId = mealId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDate getMealDate() {
        return mealDate;
    }
    
    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }
    
    public LocalDateTime getMealTime() {
        return mealTime;
    }
    
    public void setMealTime(LocalDateTime mealTime) {
        this.mealTime = mealTime;
    }
    
    public MealType getMealType() {
        return mealType;
    }
    
    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }
    
    public Integer getDrinkId() {
        return drinkId;
    }
    
    public void setDrinkId(Integer drinkId) {
        this.drinkId = drinkId;
    }
    
    public String getFoodName() {
        return foodName;
    }
    
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
    
    public Float getPortionSize() {
        return portionSize;
    }
    
    public void setPortionSize(Float portionSize) {
        this.portionSize = portionSize;
    }
    
    public Float getCalories() {
        return calories;
    }
    
    public void setCalories(Float calories) {
        this.calories = calories;
    }
    
    public Float getSugarContent() {
        return sugarContent;
    }
    
    public void setSugarContent(Float sugarContent) {
        this.sugarContent = sugarContent;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * 餐次类型枚举
     */
    public enum MealType {
        breakfast, lunch, dinner, snack
    }
}

