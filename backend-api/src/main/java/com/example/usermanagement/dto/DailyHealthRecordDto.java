package com.example.usermanagement.dto;

/**
 * 每日健康记录DTO
 */
public class DailyHealthRecordDto {
    
    private Long recordId;
    private Long userId;
    private String recordDate; // yyyy-MM-dd格式
    private Float totalSugarIntake;
    private Float totalCalories;
    private Float waterIntake;
    private Float exerciseMinutes;
    private Float sleepHours;
    private Float systolicBp;
    private Float diastolicBp;
    private Float bloodGlucose;
    private Float weight;
    private String mood;
    private String notes;
    private String createdAt;

    // 默认构造函数
    public DailyHealthRecordDto() {
    }

    // Getter 和 Setter 方法
    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(String recordDate) {
        this.recordDate = recordDate;
    }

    public Float getTotalSugarIntake() {
        return totalSugarIntake;
    }

    public void setTotalSugarIntake(Float totalSugarIntake) {
        this.totalSugarIntake = totalSugarIntake;
    }

    public Float getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(Float totalCalories) {
        this.totalCalories = totalCalories;
    }

    public Float getWaterIntake() {
        return waterIntake;
    }

    public void setWaterIntake(Float waterIntake) {
        this.waterIntake = waterIntake;
    }

    public Float getExerciseMinutes() {
        return exerciseMinutes;
    }

    public void setExerciseMinutes(Float exerciseMinutes) {
        this.exerciseMinutes = exerciseMinutes;
    }

    public Float getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(Float sleepHours) {
        this.sleepHours = sleepHours;
    }

    public Float getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(Float systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Float getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(Float diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Float getBloodGlucose() {
        return bloodGlucose;
    }

    public void setBloodGlucose(Float bloodGlucose) {
        this.bloodGlucose = bloodGlucose;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}


