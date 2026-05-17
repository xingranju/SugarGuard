package com.example.usermanagement.service;

import com.example.usermanagement.dto.FamilyDto.*;
import com.example.usermanagement.entity.*;
import com.example.usermanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FamilyService {

    @Autowired
    private FamilyGroupRepository groupRepository;
    @Autowired
    private FamilyMemberRepository memberRepository;
    @Autowired
    private HealthAlertRepository alertRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CheckInRepository checkInRepository;
    @Autowired
    private UserHealthProfileRepository profileRepository;
    @Autowired
    private MealRecordRepository mealRecordRepository;

    public FamilyGroupDto createFamily(Long userId, String name) {
        String inviteCode = generateInviteCode();
        FamilyGroup group = new FamilyGroup();
        group.setName(name);
        group.setInviteCode(inviteCode);
        group.setCreatorId(userId);
        groupRepository.save(group);

        FamilyMember owner = new FamilyMember();
        owner.setGroupId(group.getId());
        owner.setUserId(userId);
        owner.setRole("owner");
        try {
            var user = userRepository.findById(userId);
            user.ifPresent(u -> owner.setNickname(u.getUsername()));
        } catch (Exception ignored) {}
        memberRepository.save(owner);

        return toGroupDto(group, 1);
    }

    public FamilyGroupDto joinFamily(Long userId, String inviteCode) {
        Optional<FamilyGroup> opt = groupRepository.findByInviteCode(inviteCode);
        if (opt.isEmpty()) throw new RuntimeException("邀请码无效");

        FamilyGroup group = opt.get();
        if (memberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new RuntimeException("已经是该家庭成员");
        }

        FamilyMember member = new FamilyMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole("member");
        try {
            var user = userRepository.findById(userId);
            user.ifPresent(u -> member.setNickname(u.getUsername()));
        } catch (Exception ignored) {}
        memberRepository.save(member);

        int count = memberRepository.findByGroupId(group.getId()).size();
        return toGroupDto(group, count);
    }

    public List<FamilyGroupDto> getMyFamilies(Long userId) {
        List<FamilyMember> memberships = memberRepository.findByUserId(userId);
        List<FamilyGroupDto> result = new ArrayList<>();
        for (FamilyMember fm : memberships) {
            Optional<FamilyGroup> opt = groupRepository.findById(fm.getGroupId());
            if (opt.isPresent()) {
                int count = memberRepository.findByGroupId(fm.getGroupId()).size();
                result.add(toGroupDto(opt.get(), count));
            }
        }
        return result;
    }

    public List<FamilyMemberDto> getFamilyMembers(Long groupId) {
        List<FamilyMember> members = memberRepository.findByGroupId(groupId);
        LocalDate today = LocalDate.now();
        return members.stream().map(fm -> {
            FamilyMemberDto dto = new FamilyMemberDto();
            dto.setId(fm.getId());
            dto.setUserId(fm.getUserId());
            dto.setRole(fm.getRole());
            dto.setNickname(fm.getNickname());

            try {
                var user = userRepository.findById(fm.getUserId());
                if (user.isPresent()) {
                    dto.setUsername(user.get().getUsername());
                    dto.setAvatarUrl(user.get().getAvatarUrl());
                }
            } catch (Exception ignored) {
                dto.setUsername("用户" + fm.getUserId());
            }

            try {
                var profile = profileRepository.findByUserId(fm.getUserId());
                profile.ifPresent(p -> dto.setSugarLimit(
                    p.getSugarLimit() != null ? p.getSugarLimit() : 25f));
            } catch (Exception ignored) {
                dto.setSugarLimit(25f);
            }

            try {
                var todayMeals = mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(
                    fm.getUserId(), today);
                float totalSugar = 0f;
                if (todayMeals != null) {
                    for (var meal : todayMeals) {
                        totalSugar += meal.getSugarContent() != null ?
                            meal.getSugarContent() : 0f;
                    }
                }
                dto.setTodaySugar(totalSugar);
            } catch (Exception ignored) {
                dto.setTodaySugar(0f);
            }

            try {
                var latestCheckIn = checkInRepository.findTopByUserIdOrderByCheckInDateDesc(fm.getUserId());
                if (latestCheckIn.isPresent()) {
                    CheckIn ci = latestCheckIn.get();
                    dto.setLastCheckIn(ci.getCheckInDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    if (ci.getCheckInDate().equals(today) || ci.getCheckInDate().equals(today.minusDays(1))) {
                        dto.setStreak(ci.getStreak());
                    } else {
                        dto.setStreak(0);
                    }
                } else {
                    dto.setStreak(0);
                }
            } catch (Exception ignored) {
                dto.setStreak(0);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteFamily(Long groupId, Long userId) {
        Optional<FamilyGroup> opt = groupRepository.findById(groupId);
        if (opt.isEmpty()) throw new RuntimeException("家庭不存在");
        FamilyGroup group = opt.get();
        if (!group.getCreatorId().equals(userId)) {
            throw new RuntimeException("只有创建者可以删除家庭");
        }
        alertRepository.deleteByGroupId(groupId);
        memberRepository.deleteAll(memberRepository.findByGroupId(groupId));
        groupRepository.delete(group);
    }

    public void leaveFamily(Long groupId, Long userId) {
        Optional<FamilyMember> opt = memberRepository.findByGroupIdAndUserId(groupId, userId);
        if (opt.isEmpty()) throw new RuntimeException("你不是该家庭成员");
        FamilyMember fm = opt.get();
        if ("owner".equals(fm.getRole())) {
            throw new RuntimeException("创建者不能退出家庭，请先删除家庭");
        }
        memberRepository.delete(fm);
    }

    public void removeMember(Long groupId, Long targetUserId, Long operatorId) {
        Optional<FamilyMember> operator = memberRepository.findByGroupIdAndUserId(groupId, operatorId);
        if (operator.isEmpty() || !"owner".equals(operator.get().getRole())) {
            throw new RuntimeException("无权限移除成员");
        }
        if (operatorId.equals(targetUserId)) {
            throw new RuntimeException("不能移除自己");
        }
        Optional<FamilyMember> target = memberRepository.findByGroupIdAndUserId(groupId, targetUserId);
        target.ifPresent(memberRepository::delete);
    }

    public List<HealthAlertDto> checkHealthAlerts(Long groupId) {
        List<FamilyMember> members = memberRepository.findByGroupId(groupId);
        List<HealthAlertDto> newAlerts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (FamilyMember fm : members) {
            String username = fm.getNickname() != null ? fm.getNickname() : "用户" + fm.getUserId();
            try {
                var user = userRepository.findById(fm.getUserId());
                if (user.isPresent()) username = user.get().getUsername();
            } catch (Exception ignored) {}

            float sugarLimit = 25f;
            try {
                var profile = profileRepository.findByUserId(fm.getUserId());
                if (profile.isPresent() && profile.get().getSugarLimit() != null) {
                    sugarLimit = profile.get().getSugarLimit();
                }
            } catch (Exception ignored) {}

            float todaySugar = 0f;
            try {
                var todayMeals = mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(
                    fm.getUserId(), today);
                if (todayMeals != null) {
                    for (var meal : todayMeals) {
                        todaySugar += meal.getSugarContent() != null ?
                            meal.getSugarContent() : 0f;
                    }
                }
            } catch (Exception ignored) {}

            if (todaySugar > sugarLimit * 1.5) {
                newAlerts.add(createAlert(fm.getUserId(), groupId, username,
                    "sugar_high", username + "今日糖摄入已超标50%以上", "danger"));
            } else if (todaySugar > sugarLimit) {
                newAlerts.add(createAlert(fm.getUserId(), groupId, username,
                    "sugar_high", username + "今日糖摄入已超标", "warning"));
            }

            var latestCheckIn = checkInRepository.findTopByUserIdOrderByCheckInDateDesc(fm.getUserId());
            if (latestCheckIn.isPresent()) {
                CheckIn ci = latestCheckIn.get();
                long daysSince = today.toEpochDay() - ci.getCheckInDate().toEpochDay();
                if (daysSince >= 2) {
                    newAlerts.add(createAlert(fm.getUserId(), groupId, username,
                        "no_record", username + "已" + daysSince + "天未打卡记录", "info"));
                }
            }
        }
        return newAlerts;
    }

    private HealthAlertDto createAlert(Long userId, Long groupId, String username,
                                        String alertType, String message, String severity) {
        HealthAlert alert = new HealthAlert();
        alert.setUserId(userId);
        alert.setGroupId(groupId);
        alert.setAlertType(alertType);
        alert.setMessage(message);
        alert.setSeverity(severity);
        alertRepository.save(alert);

        HealthAlertDto dto = new HealthAlertDto();
        dto.setId(alert.getId());
        dto.setUserId(userId);
        dto.setUsername(username);
        dto.setAlertType(alertType);
        dto.setMessage(message);
        dto.setSeverity(severity);
        dto.setIsRead(false);
        dto.setCreatedAt(alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null);
        return dto;
    }

    public List<HealthAlertDto> getAlerts(Long groupId) {
        return alertRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
            .map(a -> {
                HealthAlertDto dto = new HealthAlertDto();
                dto.setId(a.getId());
                dto.setUserId(a.getUserId());
                dto.setAlertType(a.getAlertType());
                dto.setMessage(a.getMessage());
                dto.setSeverity(a.getSeverity());
                dto.setIsRead(a.getIsRead());
                dto.setCreatedAt(a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
                try {
                    var user = userRepository.findById(a.getUserId());
                    user.ifPresent(u -> dto.setUsername(u.getUsername()));
                } catch (Exception ignored) {}
                return dto;
            }).collect(Collectors.toList());
    }

    public void markAlertRead(Long alertId) {
        alertRepository.findById(alertId).ifPresent(a -> {
            a.setIsRead(true);
            alertRepository.save(a);
        });
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();
        if (groupRepository.findByInviteCode(code).isPresent()) {
            return generateInviteCode();
        }
        return code;
    }

    public FamilyGroupDto updateFamily(Long groupId, Long userId, String name, String avatarUrl, String description) {
        Optional<FamilyGroup> opt = groupRepository.findById(groupId);
        if (opt.isEmpty()) throw new RuntimeException("家庭不存在");
        FamilyGroup group = opt.get();

        Optional<FamilyMember> member = memberRepository.findByGroupIdAndUserId(groupId, userId);
        if (member.isEmpty() || !"owner".equals(member.get().getRole())) {
            throw new RuntimeException("只有管理员可以修改家庭信息");
        }

        if (name != null && !name.isBlank()) group.setName(name);
        if (avatarUrl != null) group.setAvatarUrl(avatarUrl);
        if (description != null) group.setDescription(description);
        groupRepository.save(group);

        int count = memberRepository.findByGroupId(groupId).size();
        return toGroupDto(group, count);
    }

    private FamilyGroupDto toGroupDto(FamilyGroup group, int memberCount) {
        FamilyGroupDto dto = new FamilyGroupDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setInviteCode(group.getInviteCode());
        dto.setMemberCount(memberCount);
        dto.setAvatarUrl(group.getAvatarUrl());
        dto.setDescription(group.getDescription());
        dto.setCreatorId(group.getCreatorId());
        dto.setCreatedAt(group.getCreatedAt() != null ? group.getCreatedAt().toString() : null);
        return dto;
    }
}
