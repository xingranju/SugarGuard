package com.example.usermanagement.dto;

public class BadgeDto {
    private Long id;
    private String name;
    private String description;
    private String iconName;
    private String category;
    private String conditionDesc;
    private Integer threshold;
    private boolean earned;
    private String earnedAt;
    private Integer progress;

    public BadgeDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getConditionDesc() { return conditionDesc; }
    public void setConditionDesc(String conditionDesc) { this.conditionDesc = conditionDesc; }
    public Integer getThreshold() { return threshold; }
    public void setThreshold(Integer threshold) { this.threshold = threshold; }
    public boolean isEarned() { return earned; }
    public void setEarned(boolean earned) { this.earned = earned; }
    public String getEarnedAt() { return earnedAt; }
    public void setEarnedAt(String earnedAt) { this.earnedAt = earnedAt; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
}
