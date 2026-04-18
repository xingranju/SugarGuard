package com.example.usermanagement.dto;

import com.example.usermanagement.entity.HealthReport;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthReportDto {

    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("period_type")
    private String periodType;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("avg_sugar")
    private Float avgSugar;

    @JsonProperty("avg_calories")
    private Float avgCalories;

    @JsonProperty("total_sugar")
    private Float totalSugar;

    @JsonProperty("total_calories")
    private Float totalCalories;

    @JsonProperty("over_days")
    private Integer overDays;

    @JsonProperty("total_days")
    private Integer totalDays;

    @JsonProperty("record_days")
    private Integer recordDays;

    @JsonProperty("sugar_limit")
    private Float sugarLimit;

    private Integer score;

    private String summary;

    @JsonProperty("created_at")
    private String createdAt;

    public HealthReportDto() {}

    public static HealthReportDto fromEntity(HealthReport r) {
        HealthReportDto dto = new HealthReportDto();
        dto.id = r.getId();
        dto.userId = r.getUserId();
        dto.periodType = r.getPeriodType();
        dto.startDate = r.getStartDate().toString();
        dto.endDate = r.getEndDate().toString();
        dto.avgSugar = r.getAvgSugar();
        dto.avgCalories = r.getAvgCalories();
        dto.totalSugar = r.getTotalSugar();
        dto.totalCalories = r.getTotalCalories();
        dto.overDays = r.getOverDays();
        dto.totalDays = r.getTotalDays();
        dto.recordDays = r.getRecordDays();
        dto.sugarLimit = r.getSugarLimit();
        dto.score = r.getScore();
        dto.summary = r.getSummary();
        dto.createdAt = r.getCreatedAt() != null ? r.getCreatedAt().toString() : null;
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
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
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
