package com.example.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FamilyDto {

    public static class FamilyGroupDto {
        private Long id;
        private String name;
        private String inviteCode;
        private Integer memberCount;
        private String avatarUrl;
        private String description;
        private Long creatorId;
        private String createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getInviteCode() { return inviteCode; }
        public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
        public Integer getMemberCount() { return memberCount; }
        public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getCreatorId() { return creatorId; }
        public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    public static class FamilyMemberDto {
        private Long id;
        private Long userId;
        private String username;
        private String avatarUrl;
        private String role;
        private String nickname;
        private Float todaySugar;
        private Float sugarLimit;
        private Integer streak;
        private String lastCheckIn;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public Float getTodaySugar() { return todaySugar; }
        public void setTodaySugar(Float todaySugar) { this.todaySugar = todaySugar; }
        public Float getSugarLimit() { return sugarLimit; }
        public void setSugarLimit(Float sugarLimit) { this.sugarLimit = sugarLimit; }
        public Integer getStreak() { return streak; }
        public void setStreak(Integer streak) { this.streak = streak; }
        public String getLastCheckIn() { return lastCheckIn; }
        public void setLastCheckIn(String lastCheckIn) { this.lastCheckIn = lastCheckIn; }
    }

    public static class CreateFamilyRequest {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class JoinFamilyRequest {
        @JsonProperty("inviteCode")
        private String inviteCode;
        public String getInviteCode() { return inviteCode; }
        public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    }

    public static class UpdateFamilyRequest {
        private String name;
        private String avatarUrl;
        private String description;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class HealthAlertDto {
        private Long id;
        private Long userId;
        private String username;
        private String alertType;
        private String message;
        private String severity;
        private Boolean isRead;
        private String createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
}
