package com.example.usermanagement.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "period_type", "start_date"})
})
public class HealthReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "avg_sugar")
    private Float avgSugar;

    @Column(name = "avg_calories")
    private Float avgCalories;

    @Column(name = "total_sugar")
    private Float totalSugar;

    @Column(name = "total_calories")
    private Float totalCalories;

    @Column(name = "over_days")
    private Integer overDays;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column(name = "record_days")
    private Integer recordDays;

    @Column(name = "sugar_limit")
    private Float sugarLimit;

    @Column(name = "score")
    private Integer score;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public HealthReport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Float getAvgSugar() { return avgSugar; }
    public void setAvgSugar(Float avgSugar) { this.avgSugar = avgSugar; }

    public Float getAvgCalories() { return avgCalories; }
    public void setAvgCalories(Float avgCalories) { this.avgCalories = avgCalories; }

    public Float getTotalSugar() { return totalSugar; }
    public void setTotalSugar(Float totalSugar) { this.totalSugar = totalSugar; }

    public Float getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Float totalCalories) { this.totalCalories = totalCalories; }

    public Integer getOverDays() { return overDays; }
    public void setOverDays(Integer overDays) { this.overDays = overDays; }

    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }

    public Integer getRecordDays() { return recordDays; }
    public void setRecordDays(Integer recordDays) { this.recordDays = recordDays; }

    public Float getSugarLimit() { return sugarLimit; }
    public void setSugarLimit(Float sugarLimit) { this.sugarLimit = sugarLimit; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
