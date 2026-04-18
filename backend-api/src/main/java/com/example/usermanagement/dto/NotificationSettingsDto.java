package com.example.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationSettingsDto {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("sugar_alert")
    private Boolean sugarAlert;

    @JsonProperty("record_reminder")
    private Boolean recordReminder;

    @JsonProperty("meal_reminder")
    private Boolean mealReminder;

    @JsonProperty("water_reminder")
    private Boolean waterReminder;

    @JsonProperty("weekly_report")
    private Boolean weeklyReport;

    @JsonProperty("quiet_start")
    private String quietStart;

    @JsonProperty("quiet_end")
    private String quietEnd;

    public NotificationSettingsDto() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Boolean getSugarAlert() { return sugarAlert; }
    public void setSugarAlert(Boolean sugarAlert) { this.sugarAlert = sugarAlert; }

    public Boolean getRecordReminder() { return recordReminder; }
    public void setRecordReminder(Boolean recordReminder) { this.recordReminder = recordReminder; }

    public Boolean getMealReminder() { return mealReminder; }
    public void setMealReminder(Boolean mealReminder) { this.mealReminder = mealReminder; }

    public Boolean getWaterReminder() { return waterReminder; }
    public void setWaterReminder(Boolean waterReminder) { this.waterReminder = waterReminder; }

    public Boolean getWeeklyReport() { return weeklyReport; }
    public void setWeeklyReport(Boolean weeklyReport) { this.weeklyReport = weeklyReport; }

    public String getQuietStart() { return quietStart; }
    public void setQuietStart(String quietStart) { this.quietStart = quietStart; }

    public String getQuietEnd() { return quietEnd; }
    public void setQuietEnd(String quietEnd) { this.quietEnd = quietEnd; }
}
