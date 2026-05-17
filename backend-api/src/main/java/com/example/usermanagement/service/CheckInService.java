package com.example.usermanagement.service;

import com.example.usermanagement.dto.*;
import com.example.usermanagement.entity.*;
import com.example.usermanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    @Autowired
    private CheckInRepository checkInRepository;
    @Autowired
    private BadgeRepository badgeRepository;
    @Autowired
    private UserBadgeRepository userBadgeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserHealthProfileRepository profileRepository;

    @PostConstruct
    public void initBadges() {
        if (badgeRepository.count() < 15) {
            userBadgeRepository.deleteAll();
            badgeRepository.deleteAll();
            List<Badge> badges = Arrays.asList(
                new Badge("初心者", "完成首次打卡，迈出控糖第一步", "🌟", "streak", "首次打卡", 1, 1),
                new Badge("三日坚持", "连续打卡3天，好的开始", "🔥", "streak", "连续打卡3天", 3, 2),
                new Badge("周达人", "连续打卡7天，一周不落", "⭐", "streak", "连续打卡7天", 7, 3),
                new Badge("半月勇士", "连续打卡14天，毅力可嘉", "🛡️", "streak", "连续打卡14天", 14, 4),
                new Badge("月冠军", "连续打卡30天，自律典范", "🏆", "streak", "连续打卡30天", 30, 5),
                new Badge("百日传说", "连续打卡100天，传奇成就", "🐉", "streak", "连续打卡100天", 100, 6),
                new Badge("控糖新星", "累计打卡10次", "💫", "total", "累计打卡10次", 10, 7),
                new Badge("控糖达人", "累计打卡30次，坚持有回报", "🎯", "total", "累计打卡30次", 30, 8),
                new Badge("控糖精英", "累计打卡50次，越来越棒", "🎖️", "total", "累计打卡50次", 50, 9),
                new Badge("控糖大师", "累计打卡100次，实力非凡", "👑", "total", "累计打卡100次", 100, 10),
                new Badge("控糖之王", "累计打卡200次，无人能及", "👸", "total", "累计打卡200次", 200, 11),
                new Badge("低糖先锋", "连续3天糖摄入达标", "🍃", "special", "连续3天达标", 3, 12),
                new Badge("完美控糖", "连续7天糖摄入达标", "💎", "special", "连续7天达标", 7, 13),
                new Badge("健康守护者", "连续14天糖摄入达标", "💪", "special", "连续14天达标", 14, 14),
                new Badge("至尊控糖", "连续30天糖摄入达标，真正的自律王", "✨", "special", "连续30天达标", 30, 15)
            );
            badgeRepository.saveAll(badges);
        }
        retroAwardBadges();
    }

    private void retroAwardBadges() {
        List<Long> userIds = checkInRepository.findAllDistinctUserIds();
        for (Long uid : userIds) {
            int streak = getStreak(uid);
            Integer maxStreak = checkInRepository.findMaxStreakByUserId(uid);
            if (maxStreak != null && maxStreak > streak) {
                streak = maxStreak;
            }
            checkAndAwardBadges(uid, streak);
        }
    }

    public CheckInDto.CheckInResponse checkIn(Long userId, Float sugarIntake, String notes) {
        LocalDate today = LocalDate.now();
        Optional<CheckIn> existing = checkInRepository.findByUserIdAndCheckInDate(userId, today);
        if (existing.isPresent()) {
            CheckIn ci = existing.get();
            CheckInDto.CheckInResponse resp = new CheckInDto.CheckInResponse();
            resp.setId(ci.getId());
            resp.setStreak(ci.getStreak());
            resp.setWithinLimit(ci.getWithinLimit());
            resp.setNewBadges(Collections.emptyList());
            return resp;
        }

        float sugarLimit = 25f;
        try {
            var profile = profileRepository.findByUserId(userId);
            if (profile.isPresent()) {
                sugarLimit = profile.get().getSugarLimit() != null ?
                    profile.get().getSugarLimit() : 25f;
            }
        } catch (Exception ignored) {}

        boolean withinLimit = sugarIntake != null && sugarIntake <= sugarLimit;

        int streak = 1;
        Optional<CheckIn> yesterday = checkInRepository.findByUserIdAndCheckInDate(userId, today.minusDays(1));
        if (yesterday.isPresent()) {
            streak = yesterday.get().getStreak() + 1;
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckInDate(today);
        checkIn.setSugarIntake(sugarIntake);
        checkIn.setWithinLimit(withinLimit);
        checkIn.setStreak(streak);
        checkIn.setNotes(notes);
        checkInRepository.save(checkIn);

        List<BadgeDto> newBadges = checkAndAwardBadges(userId, streak);

        CheckInDto.CheckInResponse resp = new CheckInDto.CheckInResponse();
        resp.setId(checkIn.getId());
        resp.setStreak(streak);
        resp.setWithinLimit(withinLimit);
        resp.setNewBadges(newBadges);
        return resp;
    }

    private List<BadgeDto> checkAndAwardBadges(Long userId, int currentStreak) {
        List<BadgeDto> newBadges = new ArrayList<>();
        List<Badge> allBadges = badgeRepository.findAllByOrderBySortOrderAsc();
        long totalCheckIns = checkInRepository.countByUserId(userId);
        long withinLimitCount = checkInRepository.countByUserIdAndWithinLimitTrue(userId);

        for (Badge badge : allBadges) {
            if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) continue;

            boolean earned = false;
            switch (badge.getCategory()) {
                case "streak":
                    earned = currentStreak >= badge.getThreshold();
                    break;
                case "total":
                    earned = totalCheckIns >= badge.getThreshold();
                    break;
                case "special":
                    earned = countConsecutiveWithinLimit(userId) >= badge.getThreshold();
                    break;
            }

            if (earned) {
                UserBadge ub = new UserBadge();
                ub.setUserId(userId);
                ub.setBadgeId(badge.getId());
                ub.setProgress(100);
                userBadgeRepository.save(ub);

                BadgeDto dto = toBadgeDto(badge, true, ub);
                newBadges.add(dto);
            }
        }
        return newBadges;
    }

    private int countConsecutiveWithinLimit(Long userId) {
        List<CheckIn> history = checkInRepository.findByUserIdOrderByCheckInDateDesc(userId);
        int count = 0;
        for (CheckIn ci : history) {
            if (Boolean.TRUE.equals(ci.getWithinLimit())) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public List<CheckInDto> getCheckInHistory(Long userId) {
        return checkInRepository.findByUserIdOrderByCheckInDateDesc(userId)
            .stream().map(this::toCheckInDto).collect(Collectors.toList());
    }

    public int getStreak(Long userId) {
        Optional<CheckIn> latest = checkInRepository.findTopByUserIdOrderByCheckInDateDesc(userId);
        if (latest.isEmpty()) return 0;
        CheckIn ci = latest.get();
        LocalDate today = LocalDate.now();
        if (ci.getCheckInDate().equals(today) || ci.getCheckInDate().equals(today.minusDays(1))) {
            return ci.getStreak();
        }
        return 0;
    }

    public List<BadgeDto> getBadges(Long userId) {
        List<Badge> allBadges = badgeRepository.findAllByOrderBySortOrderAsc();
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);
        Map<Long, UserBadge> ubMap = userBadges.stream()
            .collect(Collectors.toMap(UserBadge::getBadgeId, ub -> ub));

        long totalCheckIns = checkInRepository.countByUserId(userId);
        int currentStreak = getStreak(userId);
        int withinLimitStreak = countConsecutiveWithinLimit(userId);

        return allBadges.stream().map(badge -> {
            UserBadge ub = ubMap.get(badge.getId());
            boolean earned = ub != null;
            BadgeDto dto = toBadgeDto(badge, earned, ub);
            if (!earned) {
                int progress = 0;
                switch (badge.getCategory()) {
                    case "streak":
                        progress = Math.min(100, (int)(currentStreak * 100.0 / badge.getThreshold()));
                        break;
                    case "total":
                        progress = Math.min(100, (int)(totalCheckIns * 100.0 / badge.getThreshold()));
                        break;
                    case "special":
                        progress = Math.min(100, (int)(withinLimitStreak * 100.0 / badge.getThreshold()));
                        break;
                }
                dto.setProgress(progress);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<RankingDto> getRanking(int top) {
        List<Long> userIds = checkInRepository.findAllDistinctUserIds();
        List<RankingDto> rankings = new ArrayList<>();

        for (Long uid : userIds) {
            int streak = getStreak(uid);
            long totalCheckIns = checkInRepository.countByUserId(uid);
            long badgeCount = userBadgeRepository.countByUserId(uid);

            String username = "用户" + uid;
            String avatarUrl = null;
            try {
                var user = userRepository.findById(uid);
                if (user.isPresent()) {
                    username = user.get().getUsername();
                    avatarUrl = user.get().getAvatarUrl();
                }
            } catch (Exception ignored) {}

            RankingDto dto = new RankingDto();
            dto.setUserId(uid);
            dto.setUsername(username);
            dto.setAvatarUrl(avatarUrl);
            dto.setStreak(streak);
            dto.setTotalCheckIns((int) totalCheckIns);
            dto.setBadgeCount((int) badgeCount);
            rankings.add(dto);
        }

        rankings.sort((a, b) -> {
            int cmp = Integer.compare(b.getTotalCheckIns(), a.getTotalCheckIns());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(b.getBadgeCount(), a.getBadgeCount());
            if (cmp != 0) return cmp;
            return Integer.compare(b.getStreak(), a.getStreak());
        });

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings.stream().limit(top).collect(Collectors.toList());
    }

    private CheckInDto toCheckInDto(CheckIn ci) {
        CheckInDto dto = new CheckInDto();
        dto.setId(ci.getId());
        dto.setUserId(ci.getUserId());
        dto.setCheckInDate(ci.getCheckInDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        dto.setSugarIntake(ci.getSugarIntake());
        dto.setWithinLimit(ci.getWithinLimit());
        dto.setStreak(ci.getStreak());
        dto.setNotes(ci.getNotes());
        return dto;
    }

    private BadgeDto toBadgeDto(Badge badge, boolean earned, UserBadge ub) {
        BadgeDto dto = new BadgeDto();
        dto.setId(badge.getId());
        dto.setName(badge.getName());
        dto.setDescription(badge.getDescription());
        dto.setIconName(badge.getIconName());
        dto.setCategory(badge.getCategory());
        dto.setConditionDesc(badge.getConditionDesc());
        dto.setThreshold(badge.getThreshold());
        dto.setEarned(earned);
        if (ub != null) {
            dto.setEarnedAt(ub.getEarnedAt() != null ? ub.getEarnedAt().toString() : null);
            dto.setProgress(100);
        } else {
            dto.setProgress(0);
        }
        return dto;
    }
}
