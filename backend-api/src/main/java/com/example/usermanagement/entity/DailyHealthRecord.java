package com.example.usermanagement.entity;

import javax.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日健康记录实体类
 */
@Entity
@Table(name = "daily_health_records")
@EntityListeners(AuditingEntityListener.class)
public class DailyHealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "total_sugar_intake")
    private Float totalSugarIntake; // 当日总糖分摄入(g)

    @Column(name = "total_calories")
    private Float totalCalories; // 当日总热量摄入(kcal)

    @Column(name = "water_intake")
    private Float waterIntake; // 当日饮水量(ml)

    @Column(name = "exercise_minutes")
    private Float exerciseMinutes; // 当日运动时长(分钟)

    @Column(name = "sleep_hours")
    private Float sleepHours; // 睡眠时长(小时)

    @Column(name = "systolic_bp")
    private Float systolicBp; // 收缩压(mmHg)

    @Column(name = "diastolic_bp")
    private Float diastolicBp; // 舒张压(mmHg)

    @Column(name = "blood_glucose")
    private Float bloodGlucose; // 血糖(mmol/L)

    @Column
    private Float weight; // 体重(kg)

    @Column(length = 20)
    private String mood; // excellent, good, normal, bad, terrible

    @Column(columnDefinition = "TEXT")
    private String notes; // 备注

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 默认构造函数
    public DailyHealthRecord() {
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

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "DailyHealthRecord{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", recordDate=" + recordDate +
                ", totalSugarIntake=" + totalSugarIntake +
                ", totalCalories=" + totalCalories +
                ", waterIntake=" + waterIntake +
                ", exerciseMinutes=" + exerciseMinutes +
                ", mood='" + mood + '\'' +
                '}';
    }
}


