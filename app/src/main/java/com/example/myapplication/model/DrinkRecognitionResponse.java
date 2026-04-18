package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 饮品识别响应模型
 */
public class DrinkRecognitionResponse {
    private boolean success;
    private Recognition recognition;
    private Nutrition nutrition;
    
    @SerializedName("health_assessment")
    private HealthAssessment healthAssessment;
    
    private String recommendation;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public Recognition getRecognition() {
        return recognition;
    }
    
    public void setRecognition(Recognition recognition) {
        this.recognition = recognition;
    }
    
    public Nutrition getNutrition() {
        return nutrition;
    }
    
    public void setNutrition(Nutrition nutrition) {
        this.nutrition = nutrition;
    }
    
    public HealthAssessment getHealthAssessment() {
        return healthAssessment;
    }
    
    public void setHealthAssessment(HealthAssessment healthAssessment) {
        this.healthAssessment = healthAssessment;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    /**
     * 识别结果
     */
    public static class Recognition {
        @SerializedName("drink_name")
        private String drinkName;
        
        private float confidence;
        
        @SerializedName("all_results")
        private List<Result> allResults;
        
        public String getDrinkName() {
            return drinkName;
        }
        
        public void setDrinkName(String drinkName) {
            this.drinkName = drinkName;
        }
        
        public float getConfidence() {
            return confidence;
        }
        
        public void setConfidence(float confidence) {
            this.confidence = confidence;
        }
        
        public List<Result> getAllResults() {
            return allResults;
        }
        
        public void setAllResults(List<Result> allResults) {
            this.allResults = allResults;
        }
        
        public static class Result {
            private String label;
            private float confidence;
            
            public String getLabel() {
                return label;
            }
            
            public void setLabel(String label) {
                this.label = label;
            }
            
            public float getConfidence() {
                return confidence;
            }
            
            public void setConfidence(float confidence) {
                this.confidence = confidence;
            }
        }
    }
    
    /**
     * 营养信息
     */
    public static class Nutrition {
        @SerializedName("sugar_content")
        private float sugarContent;
        
        private float calories;
        
        @SerializedName("health_score")
        private int healthScore;
        
        public float getSugarContent() {
            return sugarContent;
        }
        
        public void setSugarContent(float sugarContent) {
            this.sugarContent = sugarContent;
        }
        
        public float getCalories() {
            return calories;
        }
        
        public void setCalories(float calories) {
            this.calories = calories;
        }
        
        public int getHealthScore() {
            return healthScore;
        }
        
        public void setHealthScore(int healthScore) {
            this.healthScore = healthScore;
        }
    }
    
    /**
     * 健康评估
     */
    public static class HealthAssessment {
        @SerializedName("health_level")
        private String healthLevel;
        
        @SerializedName("health_advice")
        private String healthAdvice;
        
        @SerializedName("sugar_level")
        private String sugarLevel;
        
        @SerializedName("sugar_warning")
        private String sugarWarning;
        
        @SerializedName("calorie_level")
        private String calorieLevel;
        
        // 保留旧字段兼容性
        private String sugarImpact;
        private String calorieImpact;
        
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
        
        public String getSugarImpact() {
            return sugarImpact;
        }
        
        public void setSugarImpact(String sugarImpact) {
            this.sugarImpact = sugarImpact;
        }
        
        public String getCalorieImpact() {
            return calorieImpact;
        }
        
        public void setCalorieImpact(String calorieImpact) {
            this.calorieImpact = calorieImpact;
        }
        
        public String getSugarLevel() {
            return sugarLevel;
        }
        
        public void setSugarLevel(String sugarLevel) {
            this.sugarLevel = sugarLevel;
        }
        
        public String getSugarWarning() {
            return sugarWarning;
        }
        
        public void setSugarWarning(String sugarWarning) {
            this.sugarWarning = sugarWarning;
        }
        
        public String getCalorieLevel() {
            return calorieLevel;
        }
        
        public void setCalorieLevel(String calorieLevel) {
            this.calorieLevel = calorieLevel;
        }
    }
}

