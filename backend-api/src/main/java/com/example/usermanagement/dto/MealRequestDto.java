package com.example.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MealRequestDto {
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("meal_date")
    private String mealDate;
    @JsonProperty("meal_time")
    private String mealTime;
    @JsonProperty("food_name")
    private String foodName;
    @JsonProperty("sugar_content")
    private Float sugarContent;
    @JsonProperty("calories")
    private Float calories;
    @JsonProperty("protein")
    private Float protein;
    @JsonProperty("fat")
    private Float fat;
    @JsonProperty("carbohydrate")
    private Float carbohydrate;
    @JsonProperty("portion_size")
    private String portionSize;
    @JsonProperty("notes")
    private String notes;
    @JsonProperty("meal_type")
    private String mealType;
    @JsonProperty("image_path")
    private String imagePath;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMealDate() { return mealDate; }
    public void setMealDate(String mealDate) { this.mealDate = mealDate; }
    public String getMealTime() { return mealTime; }
    public void setMealTime(String mealTime) { this.mealTime = mealTime; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public Float getSugarContent() { return sugarContent; }
    public void setSugarContent(Float sugarContent) { this.sugarContent = sugarContent; }
    public Float getCalories() { return calories; }
    public void setCalories(Float calories) { this.calories = calories; }
    public Float getProtein() { return protein; }
    public void setProtein(Float protein) { this.protein = protein; }
    public Float getFat() { return fat; }
    public void setFat(Float fat) { this.fat = fat; }
    public Float getCarbohydrate() { return carbohydrate; }
    public void setCarbohydrate(Float carbohydrate) { this.carbohydrate = carbohydrate; }
    public String getPortionSize() { return portionSize; }
    public void setPortionSize(String portionSize) { this.portionSize = portionSize; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Float getPortionSizeAsFloat() {
        if (portionSize == null || portionSize.isEmpty()) return null;
        try {
            String numeric = portionSize.replaceAll("[^0-9.]", "");
            if (numeric.isEmpty()) return null;
            return Float.parseFloat(numeric);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
