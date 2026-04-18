package com.example.usermanagement.service;

import com.example.usermanagement.entity.HealthReport;
import com.example.usermanagement.entity.MealRecord;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.entity.UserHealthProfile;
import com.example.usermanagement.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Transactional
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private HealthReportRepository reportRepository;

    @Autowired
    private MealRecordRepository mealRecordRepository;

    @Autowired
    private UserHealthProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyReports() {
        logger.info("========== 凌晨定时任务：开始生成健康报告 ==========");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                generateAllReportsForUser(user.getId());
            } catch (Exception e) {
                logger.error("为用户 {} 生成报告失败: {}", user.getId(), e.getMessage());
            }
        }
        logger.info("========== 健康报告生成完毕 ==========");
    }

    public void generateAllReportsForUser(Long userId) {
        float sugarLimit = profileRepository.findByUserId(userId)
                .map(UserHealthProfile::getSugarLimit).orElse(25f);

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        generateDayReport(userId, yesterday, sugarLimit);

        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            LocalDate weekStart = yesterday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd = weekStart.plusDays(6);
            generatePeriodReport(userId, "weekly", weekStart, weekEnd, sugarLimit);
        }

        if (today.getDayOfMonth() == 1) {
            LocalDate monthStart = yesterday.withDayOfMonth(1);
            LocalDate monthEnd = yesterday.with(TemporalAdjusters.lastDayOfMonth());
            generatePeriodReport(userId, "monthly", monthStart, monthEnd, sugarLimit);
        }

        if ((today.getMonthValue() == 1 || today.getMonthValue() == 7) && today.getDayOfMonth() == 1) {
            LocalDate halfStart = yesterday.minusMonths(5).withDayOfMonth(1);
            LocalDate halfEnd = yesterday.with(TemporalAdjusters.lastDayOfMonth());
            generatePeriodReport(userId, "half_year", halfStart, halfEnd, sugarLimit);
        }
    }

    public void generateReportsOnDemand(Long userId) {
        float sugarLimit = profileRepository.findByUserId(userId)
                .map(UserHealthProfile::getSugarLimit).orElse(25f);

        LocalDate today = LocalDate.now();

        for (int i = 0; i < 14; i++) {
            LocalDate date = today.minusDays(i);
            generateDayReport(userId, date, sugarLimit);
        }

        for (int w = 0; w < 8; w++) {
            LocalDate weekEnd = today.minusWeeks(w);
            LocalDate weekStart = weekEnd.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(today)) weekEnd = today;
            generatePeriodReport(userId, "weekly", weekStart, weekEnd, sugarLimit);
        }

        for (int m = 0; m < 6; m++) {
            LocalDate monthDate = today.minusMonths(m);
            LocalDate monthStart = monthDate.withDayOfMonth(1);
            LocalDate monthEnd = monthDate.with(TemporalAdjusters.lastDayOfMonth());
            if (monthEnd.isAfter(today)) monthEnd = today;
            generatePeriodReport(userId, "monthly", monthStart, monthEnd, sugarLimit);
        }

        LocalDate halfStart = today.minusDays(179);
        generatePeriodReport(userId, "half_year", halfStart, today, sugarLimit);
    }

    private void generateDayReport(Long userId, LocalDate date, float sugarLimit) {
        if (reportRepository.findByUserIdAndPeriodTypeAndStartDate(userId, "daily", date).isPresent()) {
            return;
        }

        List<MealRecord> meals = mealRecordRepository.findByUserIdAndMealDateOrderByMealTimeDesc(userId, date);
        if (meals.isEmpty()) return;

        float totalSugar = 0f, totalCalories = 0f;
        for (MealRecord m : meals) {
            totalSugar += (m.getSugarContent() != null ? m.getSugarContent() : 0f);
            totalCalories += (m.getCalories() != null ? m.getCalories() : 0f);
        }

        int overDays = totalSugar > sugarLimit ? 1 : 0;
        int score = computeScore(overDays, 1);

        HealthReport report = new HealthReport();
        report.setUserId(userId);
        report.setPeriodType("daily");
        report.setStartDate(date);
        report.setEndDate(date);
        report.setAvgSugar(totalSugar);
        report.setAvgCalories(totalCalories);
        report.setTotalSugar(totalSugar);
        report.setTotalCalories(totalCalories);
        report.setOverDays(overDays);
        report.setTotalDays(1);
        report.setRecordDays(1);
        report.setSugarLimit(sugarLimit);
        report.setScore(score);
        report.setSummary(generateSummary(overDays, 1, totalSugar, sugarLimit));

        reportRepository.save(report);
    }

    private void generatePeriodReport(Long userId, String periodType,
                                       LocalDate startDate, LocalDate endDate, float sugarLimit) {
        if (reportRepository.findByUserIdAndPeriodTypeAndStartDate(userId, periodType, startDate).isPresent()) {
            return;
        }

        List<MealRecord> meals = mealRecordRepository
                .findByUserIdAndMealDateBetweenOrderByMealTimeDesc(userId, startDate, endDate);
        if (meals.isEmpty()) return;

        Map<LocalDate, Float> dailySugar = new HashMap<>();
        Map<LocalDate, Float> dailyCalories = new HashMap<>();
        for (MealRecord m : meals) {
            LocalDate d = m.getMealDate();
            dailySugar.merge(d, m.getSugarContent() != null ? m.getSugarContent() : 0f, Float::sum);
            dailyCalories.merge(d, m.getCalories() != null ? m.getCalories() : 0f, Float::sum);
        }

        int recordDays = dailySugar.size();
        float totalSugar = 0f, totalCalories = 0f;
        int overDays = 0;
        for (Map.Entry<LocalDate, Float> e : dailySugar.entrySet()) {
            float daySugar = e.getValue();
            totalSugar += daySugar;
            totalCalories += dailyCalories.getOrDefault(e.getKey(), 0f);
            if (daySugar > sugarLimit) overDays++;
        }

        int totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        float avgSugar = recordDays > 0 ? totalSugar / recordDays : 0f;
        float avgCalories = recordDays > 0 ? totalCalories / recordDays : 0f;
        int score = computeScore(overDays, totalDays);

        HealthReport report = new HealthReport();
        report.setUserId(userId);
        report.setPeriodType(periodType);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setAvgSugar(avgSugar);
        report.setAvgCalories(avgCalories);
        report.setTotalSugar(totalSugar);
        report.setTotalCalories(totalCalories);
        report.setOverDays(overDays);
        report.setTotalDays(totalDays);
        report.setRecordDays(recordDays);
        report.setSugarLimit(sugarLimit);
        report.setScore(score);
        report.setSummary(generateSummary(overDays, totalDays, avgSugar, sugarLimit));

        reportRepository.save(report);
    }

    private int computeScore(int overDays, int totalDays) {
        return (int) ((1f - (float) overDays / Math.max(totalDays, 1)) * 100);
    }

    private String generateSummary(int overDays, int totalDays, float avgSugar, float sugarLimit) {
        if (overDays == 0) return "表现完美！全部达标";
        if (overDays <= totalDays / 4) return "整体不错，偶尔超标";
        if (avgSugar > sugarLimit * 1.5f) return "糖分摄入偏高，需要注意控制";
        return "需要注意控糖，加油！";
    }

    public List<HealthReport> getReports(Long userId, String periodType, LocalDate from, LocalDate to) {
        if (periodType != null && from != null && to != null) {
            return reportRepository.findByUserIdAndPeriodTypeAndDateRange(userId, periodType, from, to);
        } else if (periodType != null) {
            return reportRepository.findByUserIdAndPeriodType(userId, periodType);
        } else if (from != null && to != null) {
            return reportRepository.findByUserIdAndDateRange(userId, from, to);
        }
        return reportRepository.findAllByUserId(userId);
    }
}
