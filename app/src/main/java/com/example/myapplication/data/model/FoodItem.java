package com.example.myapplication.data.model;

import java.io.Serializable;

/**
 * 食物项数据模型
 * shiWu_xiang_shuJu_moXing
 */
public class FoodItem implements Serializable {
    private String name_CN;           // 食物名称（中文）
    private String name_EN;           // 食物名称（英文）
    private String category;          // 食物类别
    private float sugar;              // 糖分含量（克）
    private float calories;           // 卡路里（千卡）
    private float protein;            // 蛋白质（克）
    private float fat;                // 脂肪（克）
    private float carbohydrate;       // 碳水化合物（克）
    private float servingSize;        // 标准份量（克）
    private String healthLevel;       // 健康等级：healthy/moderate/unhealthy/high_sugar
    private float confidence;         // 识别置信度 (0-1)
    private String imageUrl;          // 图片URL
    private long timestamp;           // 记录时间戳

    // 构造函数
    public FoodItem() {
        this.timestamp = System.currentTimeMillis();
    }

    public FoodItem(String name_CN, String name_EN, String category) {
        this.name_CN = name_CN;
        this.name_EN = name_EN;
        this.category = category;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getName_CN() {
        return name_CN;
    }

    public void setName_CN(String name_CN) {
        this.name_CN = name_CN;
    }

    public String getName_EN() {
        return name_EN;
    }

    public void setName_EN(String name_EN) {
        this.name_EN = name_EN;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getSugar() {
        return sugar;
    }

    public void setSugar(float sugar) {
        this.sugar = sugar;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public float getProtein() {
        return protein;
    }

    public void setProtein(float protein) {
        this.protein = protein;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public float getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(float carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public float getServingSize() {
        return servingSize;
    }

    public void setServingSize(float servingSize) {
        this.servingSize = servingSize;
    }

    public String getHealthLevel() {
        return healthLevel;
    }

    public void setHealthLevel(String healthLevel) {
        this.healthLevel = healthLevel;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取健康等级的中文显示
     * huoQu_jiankang_dengJi_zhongWen_xianShi
     */
    public String getHealthLevelCN() {
        switch (healthLevel) {
            case "healthy":
                return "健康";
            case "moderate":
                return "适量";
            case "unhealthy":
                return "不健康";
            case "high_sugar":
                return "高糖";
            default:
                return "未知";
        }
    }

    /**
     * 获取健康等级对应的颜色
     * huoQu_jiankang_dengJi_duiYing_de_yanse
     */
    public String getHealthLevelColor() {
        switch (healthLevel) {
            case "healthy":
                return "#4CAF50";  // 绿色
            case "moderate":
                return "#FF9800";  // 橙色
            case "unhealthy":
                return "#F44336";  // 红色
            case "high_sugar":
                return "#E91E63";  // 粉红色
            default:
                return "#9E9E9E";  // 灰色
        }
    }

    @Override
    public String toString() {
        return "FoodItem{" +
                "name_CN='" + name_CN + '\'' +
                ", name_EN='" + name_EN + '\'' +
                ", category='" + category + '\'' +
                ", sugar=" + sugar +
                ", calories=" + calories +
                ", healthLevel='" + healthLevel + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}

