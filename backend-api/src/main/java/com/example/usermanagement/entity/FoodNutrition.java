package com.example.usermanagement.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 食品营养成分实体类
 */
@Entity
@Table(name = "food_nutrition")
public class FoodNutrition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "food_name", nullable = false)
    private String foodName;
    
    @Column(name = "caloric_value")
    private BigDecimal caloricValue;
    
    @Column(name = "fat")
    private BigDecimal fat;
    
    @Column(name = "saturated_fats")
    private BigDecimal saturatedFats;
    
    @Column(name = "monounsaturated_fats")
    private BigDecimal monounsaturatedFats;
    
    @Column(name = "polyunsaturated_fats")
    private BigDecimal polyunsaturatedFats;
    
    @Column(name = "carbohydrates")
    private BigDecimal carbohydrates;
    
    @Column(name = "sugars")
    private BigDecimal sugars;
    
    @Column(name = "protein")
    private BigDecimal protein;
    
    @Column(name = "dietary_fiber")
    private BigDecimal dietaryFiber;
    
    @Column(name = "cholesterol")
    private BigDecimal cholesterol;
    
    @Column(name = "sodium")
    private BigDecimal sodium;
    
    @Column(name = "water")
    private BigDecimal water;
    
    @Column(name = "vitamin_a")
    private BigDecimal vitaminA;
    
    @Column(name = "vitamin_b1")
    private BigDecimal vitaminB1;
    
    @Column(name = "vitamin_b11")
    private BigDecimal vitaminB11;
    
    @Column(name = "vitamin_b12")
    private BigDecimal vitaminB12;
    
    @Column(name = "vitamin_b2")
    private BigDecimal vitaminB2;
    
    @Column(name = "vitamin_b3")
    private BigDecimal vitaminB3;
    
    @Column(name = "vitamin_b5")
    private BigDecimal vitaminB5;
    
    @Column(name = "vitamin_b6")
    private BigDecimal vitaminB6;
    
    @Column(name = "vitamin_c")
    private BigDecimal vitaminC;
    
    @Column(name = "vitamin_d")
    private BigDecimal vitaminD;
    
    @Column(name = "vitamin_e")
    private BigDecimal vitaminE;
    
    @Column(name = "vitamin_k")
    private BigDecimal vitaminK;
    
    @Column(name = "calcium")
    private BigDecimal calcium;
    
    @Column(name = "copper")
    private BigDecimal copper;
    
    @Column(name = "iron")
    private BigDecimal iron;
    
    @Column(name = "magnesium")
    private BigDecimal magnesium;
    
    @Column(name = "manganese")
    private BigDecimal manganese;
    
    @Column(name = "phosphorus")
    private BigDecimal phosphorus;
    
    @Column(name = "potassium")
    private BigDecimal potassium;
    
    @Column(name = "selenium")
    private BigDecimal selenium;
    
    @Column(name = "zinc")
    private BigDecimal zinc;
    
    @Column(name = "nutrition_density")
    private BigDecimal nutritionDensity;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFoodName() {
        return foodName;
    }
    
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
    
    public BigDecimal getCaloricValue() {
        return caloricValue;
    }
    
    public void setCaloricValue(BigDecimal caloricValue) {
        this.caloricValue = caloricValue;
    }
    
    public BigDecimal getFat() {
        return fat;
    }
    
    public void setFat(BigDecimal fat) {
        this.fat = fat;
    }
    
    public BigDecimal getSaturatedFats() {
        return saturatedFats;
    }
    
    public void setSaturatedFats(BigDecimal saturatedFats) {
        this.saturatedFats = saturatedFats;
    }
    
    public BigDecimal getMonounsaturatedFats() {
        return monounsaturatedFats;
    }
    
    public void setMonounsaturatedFats(BigDecimal monounsaturatedFats) {
        this.monounsaturatedFats = monounsaturatedFats;
    }
    
    public BigDecimal getPolyunsaturatedFats() {
        return polyunsaturatedFats;
    }
    
    public void setPolyunsaturatedFats(BigDecimal polyunsaturatedFats) {
        this.polyunsaturatedFats = polyunsaturatedFats;
    }
    
    public BigDecimal getCarbohydrates() {
        return carbohydrates;
    }
    
    public void setCarbohydrates(BigDecimal carbohydrates) {
        this.carbohydrates = carbohydrates;
    }
    
    public BigDecimal getSugars() {
        return sugars;
    }
    
    public void setSugars(BigDecimal sugars) {
        this.sugars = sugars;
    }
    
    public BigDecimal getProtein() {
        return protein;
    }
    
    public void setProtein(BigDecimal protein) {
        this.protein = protein;
    }
    
    public BigDecimal getDietaryFiber() {
        return dietaryFiber;
    }
    
    public void setDietaryFiber(BigDecimal dietaryFiber) {
        this.dietaryFiber = dietaryFiber;
    }
    
    public BigDecimal getCholesterol() {
        return cholesterol;
    }
    
    public void setCholesterol(BigDecimal cholesterol) {
        this.cholesterol = cholesterol;
    }
    
    public BigDecimal getSodium() {
        return sodium;
    }
    
    public void setSodium(BigDecimal sodium) {
        this.sodium = sodium;
    }
    
    public BigDecimal getWater() {
        return water;
    }
    
    public void setWater(BigDecimal water) {
        this.water = water;
    }
    
    public BigDecimal getVitaminA() {
        return vitaminA;
    }
    
    public void setVitaminA(BigDecimal vitaminA) {
        this.vitaminA = vitaminA;
    }
    
    public BigDecimal getVitaminB1() {
        return vitaminB1;
    }
    
    public void setVitaminB1(BigDecimal vitaminB1) {
        this.vitaminB1 = vitaminB1;
    }
    
    public BigDecimal getVitaminB11() {
        return vitaminB11;
    }
    
    public void setVitaminB11(BigDecimal vitaminB11) {
        this.vitaminB11 = vitaminB11;
    }
    
    public BigDecimal getVitaminB12() {
        return vitaminB12;
    }
    
    public void setVitaminB12(BigDecimal vitaminB12) {
        this.vitaminB12 = vitaminB12;
    }
    
    public BigDecimal getVitaminB2() {
        return vitaminB2;
    }
    
    public void setVitaminB2(BigDecimal vitaminB2) {
        this.vitaminB2 = vitaminB2;
    }
    
    public BigDecimal getVitaminB3() {
        return vitaminB3;
    }
    
    public void setVitaminB3(BigDecimal vitaminB3) {
        this.vitaminB3 = vitaminB3;
    }
    
    public BigDecimal getVitaminB5() {
        return vitaminB5;
    }
    
    public void setVitaminB5(BigDecimal vitaminB5) {
        this.vitaminB5 = vitaminB5;
    }
    
    public BigDecimal getVitaminB6() {
        return vitaminB6;
    }
    
    public void setVitaminB6(BigDecimal vitaminB6) {
        this.vitaminB6 = vitaminB6;
    }
    
    public BigDecimal getVitaminC() {
        return vitaminC;
    }
    
    public void setVitaminC(BigDecimal vitaminC) {
        this.vitaminC = vitaminC;
    }
    
    public BigDecimal getVitaminD() {
        return vitaminD;
    }
    
    public void setVitaminD(BigDecimal vitaminD) {
        this.vitaminD = vitaminD;
    }
    
    public BigDecimal getVitaminE() {
        return vitaminE;
    }
    
    public void setVitaminE(BigDecimal vitaminE) {
        this.vitaminE = vitaminE;
    }
    
    public BigDecimal getVitaminK() {
        return vitaminK;
    }
    
    public void setVitaminK(BigDecimal vitaminK) {
        this.vitaminK = vitaminK;
    }
    
    public BigDecimal getCalcium() {
        return calcium;
    }
    
    public void setCalcium(BigDecimal calcium) {
        this.calcium = calcium;
    }
    
    public BigDecimal getCopper() {
        return copper;
    }
    
    public void setCopper(BigDecimal copper) {
        this.copper = copper;
    }
    
    public BigDecimal getIron() {
        return iron;
    }
    
    public void setIron(BigDecimal iron) {
        this.iron = iron;
    }
    
    public BigDecimal getMagnesium() {
        return magnesium;
    }
    
    public void setMagnesium(BigDecimal magnesium) {
        this.magnesium = magnesium;
    }
    
    public BigDecimal getManganese() {
        return manganese;
    }
    
    public void setManganese(BigDecimal manganese) {
        this.manganese = manganese;
    }
    
    public BigDecimal getPhosphorus() {
        return phosphorus;
    }
    
    public void setPhosphorus(BigDecimal phosphorus) {
        this.phosphorus = phosphorus;
    }
    
    public BigDecimal getPotassium() {
        return potassium;
    }
    
    public void setPotassium(BigDecimal potassium) {
        this.potassium = potassium;
    }
    
    public BigDecimal getSelenium() {
        return selenium;
    }
    
    public void setSelenium(BigDecimal selenium) {
        this.selenium = selenium;
    }
    
    public BigDecimal getZinc() {
        return zinc;
    }
    
    public void setZinc(BigDecimal zinc) {
        this.zinc = zinc;
    }
    
    public BigDecimal getNutritionDensity() {
        return nutritionDensity;
    }
    
    public void setNutritionDensity(BigDecimal nutritionDensity) {
        this.nutritionDensity = nutritionDensity;
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

