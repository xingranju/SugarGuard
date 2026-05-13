package com.example.usermanagement.dto;

public class RankingDto {
    private Integer rank;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Integer streak;
    private Integer totalCheckIns;
    private Integer badgeCount;

    public RankingDto() {}

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }
    public Integer getTotalCheckIns() { return totalCheckIns; }
    public void setTotalCheckIns(Integer totalCheckIns) { this.totalCheckIns = totalCheckIns; }
    public Integer getBadgeCount() { return badgeCount; }
    public void setBadgeCount(Integer badgeCount) { this.badgeCount = badgeCount; }
}
