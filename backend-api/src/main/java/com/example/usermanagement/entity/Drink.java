package com.example.usermanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 饮品实体类
 */
@Entity
@Table(name = "drinks")
public class Drink {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drink_id")
    private Integer drinkId;
    
    @Column(name = "drink_name", nullable = false, length = 100)
    private String drinkName;
    
    @Column(name = "brand", length = 100)
    private String brand;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "sugar_content", nullable = false)
    private Float sugarContent;
    
    @Column(name = "calories", nullable = false)
    private Float calories;
    
    @Column(name = "volume")
    private Float volume = 500.0f;
    
    @Column(name = "caffeine")
    private Float caffeine = 0.0f;
    
    @Column(name = "fat")
    private Float fat = 0.0f;
    
    @Column(name = "protein")
    private Float protein = 0.0f;
    
    @Column(name = "sodium")
    private Float sodium = 0.0f;
    
    @Column(name = "health_score")
    private Integer healthScore = 50;
    
    @Column(name = "ingredients", columnDefinition = "TEXT")
    private String ingredients;
    
    @Column(name = "allergens", length = 200)
    private String allergens;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "source_url", length = 500)
    private String sourceUrl;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    
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
    
    public Float getVolume() {
        return volume;
    }
    
    public void setVolume(Float volume) {
        this.volume = volume;
    }
    
    public Float getCaffeine() {
        return caffeine;
    }
    
    public void setCaffeine(Float caffeine) {
        this.caffeine = caffeine;
    }
    
    public Float getFat() {
        return fat;
    }
    
    public void setFat(Float fat) {
        this.fat = fat;
    }
    
    public Float getProtein() {
        return protein;
    }
    
    public void setProtein(Float protein) {
        this.protein = protein;
    }
    
    public Float getSodium() {
        return sodium;
    }
    
    public void setSodium(Float sodium) {
        this.sodium = sodium;
    }
    
    public Integer getHealthScore() {
        return healthScore;
    }
    
    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }
    
    public String getIngredients() {
        return ingredients;
    }
    
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
    
    public String getAllergens() {
        return allergens;
    }
    
    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getSourceUrl() {
        return sourceUrl;
    }
    
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
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
