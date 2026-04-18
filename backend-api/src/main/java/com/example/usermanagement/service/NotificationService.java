package com.example.usermanagement.service;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.NotificationDto;
import com.example.usermanagement.dto.NotificationSettingsDto;
import com.example.usermanagement.entity.DailyHealthRecord;
import com.example.usermanagement.entity.MealRecord;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.entity.UserHealthProfile;
import com.example.usermanagement.entity.UserNotification;
import com.example.usermanagement.entity.UserNotificationSettings;
import com.example.usermanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    // ISO 8601 with offset (e.g. 2026-04-17T14:23:45+08:00) - 客户端可精确解析时区
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final ZoneId ZONE = ZoneId.systemDefault();

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MealRecordRepository mealRecordRepository;

    @Autowired
    private UserHealthProfileRepository profileRepository;

    @Autowired
    private DailyHealthRecordRepository healthRecordRepository;

    @Autowired
    private UserNotificationSettingsRepository settingsRepository;

    // ============ REST API 业务方法 ============

    public ApiResponse<List<NotificationDto>> getUserNotifications(Long userId) {
        List<UserNotification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<NotificationDto> dtos = new ArrayList<>();
        for (UserNotification n : list) {
            dtos.add(toDto(n));
        }
        return ApiResponse.success("获取通知成功", dtos);
    }

    public ApiResponse<Long> getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsRead(userId, false);
        return ApiResponse.success("获取未读数成功", count);
    }

    public ApiResponse<NotificationDto> markAsRead(Long notificationId, Long userId) {
        Optional<UserNotification> opt = notificationRepository.findById(notificationId);
        if (opt.isEmpty()) {
            return ApiResponse.error(404, "通知不存在");
        }
        UserNotification n = opt.get();
        if (!n.getUserId().equals(userId)) {
            return ApiResponse.error(403, "无权操作");
        }
        n.setIsRead(true);
        n.setReadAt(LocalDateTime.now());
        notificationRepository.save(n);
        return ApiResponse.success("标记已读成功", toDto(n));
    }

    public ApiResponse<Void> markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        return ApiResponse.success("全部标记已读成功", null);
    }

    // ============ 用户通知偏好设置 ============

    public ApiResponse<NotificationSettingsDto> getSettings(Long userId) {
        UserNotificationSettings s = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        return ApiResponse.success("获取通知设置成功", toSettingsDto(s));
    }

    public ApiResponse<NotificationSettingsDto> updateSettings(Long userId, NotificationSettingsDto dto) {
        UserNotificationSettings s = settingsRepository.findByUserId(userId)
                .orElseGet(() -> new UserNotificationSettings(userId));
        if (dto.getSugarAlert() != null) s.setSugarAlert(dto.getSugarAlert());
        if (dto.getRecordReminder() != null) s.setRecordReminder(dto.getRecordReminder());
        if (dto.getMealReminder() != null) s.setMealReminder(dto.getMealReminder());
        if (dto.getWaterReminder() != null) s.setWaterReminder(dto.getWaterReminder());
        if (dto.getWeeklyReport() != null) s.setWeeklyReport(dto.getWeeklyReport());
        if (dto.getQuietStart() != null) s.setQuietStart(dto.getQuietStart());
        if (dto.getQuietEnd() != null) s.setQuietEnd(dto.getQuietEnd());
        s.setUpdatedAt(LocalDateTime.now());
        settingsRepository.save(s);
        return ApiResponse.success("更新通知设置成功", toSettingsDto(s));
    }

    private UserNotificationSettings createDefaultSettings(Long userId) {
        UserNotificationSettings s = new UserNotificationSettings(userId);
        s.setUpdatedAt(LocalDateTime.now());
        return settingsRepository.save(s);
    }

    private NotificationSettingsDto toSettingsDto(UserNotificationSettings s) {
        NotificationSettingsDto dto = new NotificationSettingsDto();
        dto.setUserId(s.getUserId());
        dto.setSugarAlert(s.getSugarAlert());
        dto.setRecordReminder(s.getRecordReminder());
        dto.setMealReminder(s.getMealReminder());
        dto.setWaterReminder(s.getWaterReminder());
        dto.setWeeklyReport(s.getWeeklyReport());
        dto.setQuietStart(s.getQuietStart());
        dto.setQuietEnd(s.getQuietEnd());
        return dto;
    }

    // ============ 定时任务：每 15 分钟检查一次 ============
    // 原来的每小时频率过低，用户从吃完饭到超标可能 30 分钟内都得不到反馈

    @Scheduled(fixedRate = 900000, initialDelay = 30000) // 15 分钟
    public void generateScheduledNotifications() {
        logger.info("========== 开始生成定时通知 ==========");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                generateNotificationsForUser(user.getId());
            } catch (Exception e) {
                logger.error("为用户 {} 生成通知失败: {}", user.getId(), e.getMessage());
            }
        }
        logger.info("========== 定时通知生成完毕 ==========");
    }

    private void generateNotificationsForUser(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalTime nowTime = LocalTime.now();

        UserNotificationSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        // 免打扰时段（如 22:00-08:00 不发），过时段不发提醒类，但糖分超标仍发
        boolean inQuietHours = isInQuietHours(nowTime, settings.getQuietStart(), settings.getQuietEnd());

        Optional<UserHealthProfile> profileOpt = profileRepository.findByUserId(userId);
        float sugarLimit = profileOpt.map(UserHealthProfile::getSugarLimit).orElse(25f);

        if (Boolean.TRUE.equals(settings.getSugarAlert())) {
            checkSugarOverLimit(userId, today, sugarLimit, todayStart);
        }
        if (!inQuietHours && Boolean.TRUE.equals(settings.getRecordReminder())) {
            checkRecordReminder(userId, today, todayStart);
        }
        if (!inQuietHours && Boolean.TRUE.equals(settings.getMealReminder())) {
            checkMealReminder(userId, today, todayStart, nowTime);
        }
        if (!inQuietHours && Boolean.TRUE.equals(settings.getWaterReminder())) {
            checkWaterReminder(userId, today, todayStart);
        }
        if (Boolean.TRUE.equals(settings.getWeeklyReport())) {
            checkWeeklyReport(userId, today, todayStart);
        }
    }

    private boolean isInQuietHours(LocalTime now, String quietStart, String quietEnd) {
        try {
            LocalTime start = LocalTime.parse(quietStart);
            LocalTime end = LocalTime.parse(quietEnd);
            if (start.equals(end)) return false;
            if (start.isBefore(end)) {
                return !now.isBefore(start) && now.isBefore(end);
            } else {
                // 跨午夜：22:00 - 08:00
                return !now.isBefore(start) || now.isBefore(end);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void checkSugarOverLimit(Long userId, LocalDate today, float sugarLimit, LocalDateTime since) {
        if (!notificationRepository.findRecentByType(userId, "sugar_alert", since).isEmpty()) return;

        List<MealRecord> meals = mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(userId, today);
        double totalSugar = meals.stream()
                .filter(m -> m.getSugarContent() != null)
                .mapToDouble(MealRecord::getSugarContent)
                .sum();

        if (totalSugar > sugarLimit) {
            int overPct = (int) ((totalSugar / sugarLimit - 1) * 100);
            createNotification(userId, "sugar_alert",
                    "今日糖分超标提醒",
                    String.format("今日已摄入%.0fg糖分，超出目标%.0fg的%d%%，建议控制后续饮食。", totalSugar, sugarLimit, overPct),
                    "analysis");
        }
    }

    private void checkRecordReminder(Long userId, LocalDate today, LocalDateTime since) {
        if (!notificationRepository.findRecentByType(userId, "record_reminder", since).isEmpty()) return;

        List<MealRecord> todayMeals = mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(userId, today);
        if (todayMeals.isEmpty()) {
            long daysWithout = 0;
            for (int i = 1; i <= 7; i++) {
                LocalDate d = today.minusDays(i);
                List<MealRecord> m = mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(userId, d);
                if (!m.isEmpty()) { daysWithout = i; break; }
            }
            if (daysWithout == 0) daysWithout = 7;

            if (daysWithout >= 1) {
                createNotification(userId, "record_reminder",
                        "饮食记录提醒",
                        String.format("你已经%d天没有记录饮食了，坚持记录有助于更好地控制糖分摄入哦！", daysWithout),
                        "diary");
            }
        }
    }

    /**
     * 早/午/晚餐记录提醒：定时窗口内若当餐未记录就提醒
     * 早餐窗口：07:30 - 09:30；午餐窗口：11:30 - 13:30；晚餐窗口：18:00 - 20:30
     */
    private void checkMealReminder(Long userId, LocalDate today, LocalDateTime since, LocalTime now) {
        String mealType = null;
        String label = null;
        if (now.isAfter(LocalTime.of(7, 30)) && now.isBefore(LocalTime.of(9, 30))) {
            mealType = "breakfast"; label = "早餐";
        } else if (now.isAfter(LocalTime.of(11, 30)) && now.isBefore(LocalTime.of(13, 30))) {
            mealType = "lunch"; label = "午餐";
        } else if (now.isAfter(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(20, 30))) {
            mealType = "dinner"; label = "晚餐";
        }
        if (mealType == null) return;

        // 每个窗口内每种 meal 最多发一次（按 type + 餐次）
        String notifType = "meal_reminder_" + mealType;
        if (!notificationRepository.findRecentByType(userId, notifType, since).isEmpty()) return;

        List<MealRecord> todayMeals = mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(userId, today);
        final String mt = mealType;
        boolean hasMeal = todayMeals.stream()
                .anyMatch(m -> m.getMealType() != null && mt.equalsIgnoreCase(m.getMealType().name()));
        if (!hasMeal) {
            createNotification(userId, notifType,
                    label + "记录提醒",
                    String.format("该记录%s了，花 30 秒记录一下吧～坚持记录有助于更好地控制糖分。", label),
                    "diary");
        }
    }

    private void checkWaterReminder(Long userId, LocalDate today, LocalDateTime since) {
        if (!notificationRepository.findRecentByType(userId, "water_reminder", since).isEmpty()) return;

        Optional<DailyHealthRecord> recordOpt = healthRecordRepository.findByUserIdAndRecordDate(userId, today);
        float waterIntake = recordOpt.map(DailyHealthRecord::getWaterIntake).orElse(0f);
        if (waterIntake < 1000) {
            createNotification(userId, "water_reminder",
                    "饮水提醒",
                    "今日饮水量不足1000ml，记得多喝水保持身体健康！",
                    "health_record");
        }
    }

    private void checkWeeklyReport(Long userId, LocalDate today, LocalDateTime since) {
        if (today.getDayOfWeek().getValue() != 1) return;
        LocalDateTime weekStart = today.minusDays(7).atStartOfDay();
        if (!notificationRepository.findRecentByType(userId, "weekly_report", weekStart).isEmpty()) return;

        createNotification(userId, "weekly_report",
                "周报已生成",
                "你的上周控糖周报已准备就绪，快来看看本周表现吧！",
                "analysis");
    }

    private void createNotification(Long userId, String type, String title, String content, String targetPage) {
        UserNotification n = new UserNotification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setTargetPage(targetPage);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
        logger.info("为用户 {} 创建通知: [{}] {}", userId, type, title);
    }

    private NotificationDto toDto(UserNotification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setUserId(n.getUserId());
        dto.setTitle(n.getTitle());
        dto.setContent(n.getContent());
        dto.setType(n.getType());
        dto.setIsRead(n.getIsRead());
        dto.setTargetPage(n.getTargetPage());
        // 统一输出为 ISO 8601 + 时区偏移（如 2026-04-17T14:23:45+08:00），避免客户端时区误解
        if (n.getCreatedAt() != null) {
            dto.setCreatedAt(n.getCreatedAt().atZone(ZONE).toOffsetDateTime().format(ISO_FMT));
        }
        if (n.getReadAt() != null) {
            dto.setReadAt(n.getReadAt().atZone(ZONE).toOffsetDateTime().format(ISO_FMT));
        }
        return dto;
    }
}
