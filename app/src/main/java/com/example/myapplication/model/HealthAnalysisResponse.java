package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * 健康分析响应模型
 */
public class HealthAnalysisResponse {
    private boolean success;
    
    @SerializedName("user_profile")
    private Map<String, Object> userProfile;
    
    @SerializedName("bmi_analysis")
    private BmiAnalysis bmiAnalysis;
    
    @SerializedName("sugar_assessment")
    private SugarAssessment sugarAssessment;
    
    @SerializedName("daily_needs")
    private Map<String, Object> dailyNeeds;
    
    @SerializedName("health_records")
    private List<Map<String, Object>> healthRecords;
    
    @SerializedName("analysis_period")
    private String analysisPeriod;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public Map<String, Object> getUserProfile() {
        return userProfile;
    }
    
    public void setUserProfile(Map<String, Object> userProfile) {
        this.userProfile = userProfile;
    }
    
    public BmiAnalysis getBmiAnalysis() {
        return bmiAnalysis;
    }
    
    public void setBmiAnalysis(BmiAnalysis bmiAnalysis) {
        this.bmiAnalysis = bmiAnalysis;
    }
    
    public SugarAssessment getSugarAssessment() {
        return sugarAssessment;
    }
    
    public void setSugarAssessment(SugarAssessment sugarAssessment) {
        this.sugarAssessment = sugarAssessment;
    }
    
    public Map<String, Object> getDailyNeeds() {
        return dailyNeeds;
    }
    
    public void setDailyNeeds(Map<String, Object> dailyNeeds) {
        this.dailyNeeds = dailyNeeds;
    }
    
    public List<Map<String, Object>> getHealthRecords() {
        return healthRecords;
    }
    
    public void setHealthRecords(List<Map<String, Object>> healthRecords) {
        this.healthRecords = healthRecords;
    }
    
    public String getAnalysisPeriod() {
        return analysisPeriod;
    }
    
    public void setAnalysisPeriod(String analysisPeriod) {
        this.analysisPeriod = analysisPeriod;
    }
    
    /**
     * BMI分析
     */
    public static class BmiAnalysis {
        private float bmi;
        
        @SerializedName("status")
        private String status;
        
        @SerializedName("recommendation")
        private String recommendation;
        
        @SerializedName("ai_advice")
        private String aiAdvice;
        
        // 兼容旧字段名
        private String category;
        private String healthStatus;
        private String advice;
        
        public float getBmi() {
            return bmi;
        }
        
        public void setBmi(float bmi) {
            this.bmi = bmi;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getRecommendation() {
            return recommendation;
        }
        
        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
        
        // 兼容旧方法
        public String getCategory() {
            return status != null ? status : category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getHealthStatus() {
            return status != null ? status : healthStatus;
        }
        
        public void setHealthStatus(String healthStatus) {
            this.healthStatus = healthStatus;
        }
        
        public String getAdvice() {
            return recommendation != null ? recommendation : advice;
        }
        
        public void setAdvice(String advice) {
            this.advice = advice;
        }
        
        public String getAiAdvice() {
            return aiAdvice;
        }
        
        public void setAiAdvice(String aiAdvice) {
            this.aiAdvice = aiAdvice;
        }
    }
    
    /**
     * 糖分评估
     */
    public static class SugarAssessment {
        @SerializedName("average_daily_sugar")
        private float averageDailySugar;
        
        @SerializedName("sugar_limit")
        private float sugarLimit;
        
        @SerializedName("risk_level")
        private String riskLevel;
        
        @SerializedName("recommendation")
        private String recommendation;
        
        @SerializedName("ai_advice")
        private String aiAdvice;
        
        @SerializedName("days_analyzed")
        private int daysAnalyzed;
        
        private String color;
        
        // 兼容旧字段名
        private float averageIntake;
        private float limit;
        private String assessment;
        private String advice;
        
        public float getAverageDailySugar() {
            return averageDailySugar;
        }
        
        public void setAverageDailySugar(float averageDailySugar) {
            this.averageDailySugar = averageDailySugar;
        }
        
        public float getSugarLimit() {
            return sugarLimit;
        }
        
        public void setSugarLimit(float sugarLimit) {
            this.sugarLimit = sugarLimit;
        }
        
        public String getRiskLevel() {
            return riskLevel;
        }
        
        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }
        
        public String getRecommendation() {
            return recommendation;
        }
        
        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
        
        public int getDaysAnalyzed() {
            return daysAnalyzed;
        }
        
        public void setDaysAnalyzed(int daysAnalyzed) {
            this.daysAnalyzed = daysAnalyzed;
        }
        
        public String getColor() {
            return color;
        }
        
        public void setColor(String color) {
            this.color = color;
        }
        
        // 兼容旧方法
        public float getAverageIntake() {
            return averageDailySugar > 0 ? averageDailySugar : averageIntake;
        }
        
        public void setAverageIntake(float averageIntake) {
            this.averageIntake = averageIntake;
        }
        
        public float getLimit() {
            return sugarLimit > 0 ? sugarLimit : limit;
        }
        
        public void setLimit(float limit) {
            this.limit = limit;
        }
        
        public String getAssessment() {
            return riskLevel != null ? riskLevel : assessment;
        }
        
        public void setAssessment(String assessment) {
            this.assessment = assessment;
        }
        
        public String getAdvice() {
            return recommendation != null ? recommendation : advice;
        }
        
        public void setAdvice(String advice) {
            this.advice = advice;
        }
        
        public String getAiAdvice() {
            return aiAdvice;
        }
        
        public void setAiAdvice(String aiAdvice) {
            this.aiAdvice = aiAdvice;
        }
    }
}

