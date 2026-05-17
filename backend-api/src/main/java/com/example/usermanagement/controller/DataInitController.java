package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.entity.*;
import com.example.usermanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class DataInitController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private BadgeRepository badgeRepository;
    @Autowired private UserBadgeRepository userBadgeRepository;

    @PostMapping("/init-test-data")
    public ResponseEntity<?> initTestData() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> log = new ArrayList<>();

        String encodedPwd = passwordEncoder.encode("123456");

        try {
            var adminOpt = userRepository.findByUsername("admin");
            if (adminOpt.isPresent()) {
                User admin = adminOpt.get();
                admin.setAvatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop&crop=face");
                userRepository.save(admin);
                log.add("admin头像已设置");

                ensureBadgesForUser(admin.getId(), 2);
                log.add("admin徽章数设为2");
            }
        } catch (Exception e) {
            log.add("admin处理异常: " + e.getMessage());
        }

        String[][] users = {
            {"xiaoming", "xiaoming@test.com", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop&crop=face"},
            {"xiaohong", "xiaohong@test.com", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop&crop=face"},
            {"zhangwei", "zhangwei@test.com", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&h=200&fit=crop&crop=face"},
            {"lili", "lili@test.com", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop&crop=face"},
            {"wangfang", "wangfang@test.com", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&h=200&fit=crop&crop=face"},
            {"chenjie", "chenjie@test.com", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&h=200&fit=crop&crop=face"},
            {"liuyan", "liuyan@test.com", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop&crop=face"},
            {"huangpeng", "huangpeng@test.com", "https://randomuser.me/api/portraits/men/42.jpg"}
        };

        int[] streakDays = {15, 10, 7, 5, 3, 8, 12, 4};

        for (int i = 0; i < users.length; i++) {
            try {
                String username = users[i][0];
                String email = users[i][1];
                String avatar = users[i][2];

                if (userRepository.findByUsername(username).isPresent()) {
                    User existing = userRepository.findByUsername(username).get();
                    existing.setAvatarUrl(avatar);
                    userRepository.save(existing);
                    createCheckInsForUser(existing.getId(), streakDays[i]);
                    ensureBadgesForUser(existing.getId(), Math.min(streakDays[i] / 3, 4));
                    log.add(username + " 已存在,已更新头像和打卡记录");
                    continue;
                }

                User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPasswordHash(encodedPwd);
                user.setAvatarUrl(avatar);
                user.setGender(i % 2 == 0 ? User.Gender.MALE : User.Gender.FEMALE);
                user.setStatus(User.UserStatus.ACTIVE);
                user.setEmailVerified(true);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);

                createCheckInsForUser(user.getId(), streakDays[i]);
                ensureBadgesForUser(user.getId(), Math.min(streakDays[i] / 3, 4));

                log.add(username + " 创建成功 (id=" + user.getId() + ", streak=" + streakDays[i] + ")");
            } catch (Exception e) {
                log.add(users[i][0] + " 创建失败: " + e.getMessage());
            }
        }

        result.put("log", log);
        result.put("total_users", userRepository.count());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private void createCheckInsForUser(Long userId, int days) {
        LocalDate today = LocalDate.now();
        Random rand = new Random(userId);
        for (int d = days - 1; d >= 0; d--) {
            LocalDate date = today.minusDays(d);
            if (checkInRepository.findByUserIdAndCheckInDate(userId, date).isPresent()) continue;

            CheckIn ci = new CheckIn();
            ci.setUserId(userId);
            ci.setCheckInDate(date);
            ci.setSugarIntake(10f + rand.nextFloat() * 20f);
            ci.setWithinLimit(rand.nextFloat() > 0.3f);
            ci.setStreak(days - d);
            ci.setNotes("测试打卡数据");
            checkInRepository.save(ci);
        }
    }

    private void ensureBadgesForUser(Long userId, int targetCount) {
        List<Badge> allBadges = badgeRepository.findAllByOrderBySortOrderAsc();
        long currentCount = userBadgeRepository.countByUserId(userId);

        for (Badge badge : allBadges) {
            if (currentCount >= targetCount) break;
            if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) continue;

            UserBadge ub = new UserBadge();
            ub.setUserId(userId);
            ub.setBadgeId(badge.getId());
            ub.setProgress(100);
            userBadgeRepository.save(ub);
            currentCount++;
        }
    }
}
