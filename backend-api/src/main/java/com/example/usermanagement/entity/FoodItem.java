package com.example.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 食物项实体
 * shiWu_xiang_shiTi
 */
@Entity
@Table(name = "food_items")
public class FoodItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @JsonProperty("nameCN")
    private String nameCN;           // 食物名称（中文）
    
    @Column(nullable = false)
    @JsonProperty("nameEN")
    private String nameEN;           // 食物名称（英文）
    
    @Column(nullable = false)
    private String category;         // 食物类别
    
    @Column(nullable = false)
    private Float sugar;             // 糖分含量（克）
    
    @Column(nullable = false)
    private Float calories;          // 卡路里（千卡）
    
    @Column(nullable = false)
    private Float protein;           // 蛋白质（克）
    
    @Column(nullable = false)
    private Float fat;               // 脂肪（克）
    
    @Column(nullable = false)
    private Float carbohydrate;      // 碳水化合物（克）
    
    @Column(nullable = false)
    @JsonProperty("servingSize")
    private Float servingSize;       // 标准份量（克）
    
    @Column(nullable = false)
    @JsonProperty("healthLevel")
    private String healthLevel;      // 健康等级
    
    @Column(columnDefinition = "TEXT")
    @JsonProperty("healthAdvice")
    private String healthAdvice;     // 健康建议
    
    @Column(nullable = false)
    private Float confidence;        // 识别置信度
    
    @Column
    @JsonProperty("imageUrl")
    private String imageUrl;         // 图片URL
    
    @Column(nullable = false)
    private Long userId;             // 用户ID
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // 创建时间
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public FoodItem() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNameCN() {
        return nameCN;
    }
    
    public void setNameCN(String nameCN) {
        this.nameCN = nameCN;
    }
    
    public String getNameEN() {
        return nameEN;
    }
    
    public void setNameEN(String nameEN) {
        this.nameEN = nameEN;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Float getSugar() {
        return sugar;
    }
    
    public void setSugar(Float sugar) {
        this.sugar = sugar;
    }
    
    public Float getCalories() {
        return calories;
    }
    
    public void setCalories(Float calories) {
        this.calories = calories;
    }
    
    public Float getProtein() {
        return protein;
    }
    
    public void setProtein(Float protein) {
        this.protein = protein;
    }
    
    public Float getFat() {
        return fat;
    }
    
    public void setFat(Float fat) {
        this.fat = fat;
    }
    
    public Float getCarbohydrate() {
        return carbohydrate;
    }
    
    public void setCarbohydrate(Float carbohydrate) {
        this.carbohydrate = carbohydrate;
    }
    
    public Float getServingSize() {
        return servingSize;
    }
    
    public void setServingSize(Float servingSize) {
        this.servingSize = servingSize;
    }
    
    public String getHealthLevel() {
        return healthLevel;
    }
    
    public void setHealthLevel(String healthLevel) {
        this.healthLevel = healthLevel;
    }
    
    public String getHealthAdvice() {
        return healthAdvice;
    }
    
    public void setHealthAdvice(String healthAdvice) {
        this.healthAdvice = healthAdvice;
    }
    
    public Float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

