package com.example.usermanagement.dto;

/**
 * 饮品信息DTO
 */
public class DrinkDto {
    
    private Integer drinkId;
    private String drinkName;
    private String brand;
    private String category;
    private Float sugarContent;
    private Float calories;
    private Float volume;
    private Float caffeine;
    private Float fat;
    private Float protein;
    private Float sodium;
    private Integer healthScore;
    private String ingredients;
    private String allergens;
    private String imageUrl;
    private String sourceUrl;

    // 默认构造函数
    public DrinkDto() {
    }

    // Getter 和 Setter 方法
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
}


