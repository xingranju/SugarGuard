package com.example.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

public class CheckInDto {
    private Long id;
    private Long userId;
    private String checkInDate;
    private Float sugarIntake;
    private Boolean withinLimit;
    private Integer streak;
    private String notes;

    public CheckInDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }
    public Float getSugarIntake() { return sugarIntake; }
    public void setSugarIntake(Float sugarIntake) { this.sugarIntake = sugarIntake; }
    public Boolean getWithinLimit() { return withinLimit; }
    public void setWithinLimit(Boolean withinLimit) { this.withinLimit = withinLimit; }
    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static class CheckInRequest {
        @JsonProperty("userId")
        private Long userId;
        @JsonProperty("sugarIntake")
        private Float sugarIntake;
        private String notes;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Float getSugarIntake() { return sugarIntake; }
        public void setSugarIntake(Float sugarIntake) { this.sugarIntake = sugarIntake; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class CheckInResponse {
        private Long id;
        private Integer streak;
        private Boolean withinLimit;
        private List<BadgeDto> newBadges = new ArrayList<>();

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getStreak() { return streak; }
        public void setStreak(Integer streak) { this.streak = streak; }
        public Boolean getWithinLimit() { return withinLimit; }
        public void setWithinLimit(Boolean withinLimit) { this.withinLimit = withinLimit; }
        public List<BadgeDto> getNewBadges() { return newBadges; }
        public void setNewBadges(List<BadgeDto> newBadges) { this.newBadges = newBadges; }
    }
}
