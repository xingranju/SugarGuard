package com.example.usermanagement.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户通知偏好设置
 * 控制各类定时通知是否生成 + 提醒时段
 */
@Entity
@Table(name = "user_notification_settings")
public class UserNotificationSettings {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "sugar_alert", nullable = false)
    private Boolean sugarAlert = true;

    @Column(name = "record_reminder", nullable = false)
    private Boolean recordReminder = true;

    @Column(name = "meal_reminder", nullable = false)
    private Boolean mealReminder = true;

    @Column(name = "water_reminder", nullable = false)
    private Boolean waterReminder = true;

    @Column(name = "weekly_report", nullable = false)
    private Boolean weeklyReport = false;

    @Column(name = "quiet_start", length = 5)
    private String quietStart = "22:00";

    @Column(name = "quiet_end", length = 5)
    private String quietEnd = "08:00";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserNotificationSettings() {}

    public UserNotificationSettings(Long userId) {
        this.userId = userId;
    }

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

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
