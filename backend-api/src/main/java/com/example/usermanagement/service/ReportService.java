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
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private AIServiceProxy aiServiceProxy;

    @Autowired
    private DailyHealthRecordRepository healthRecordRepository;

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

    /**
     * 为指定用户生成 AI 营养教练周报或月报
     * @param periodType "weekly" 或 "monthly"
     */
    public HealthReport generateAiNutritionReport(Long userId, String periodType) {
        float sugarLimit = profileRepository.findByUserId(userId)
                .map(UserHealthProfile::getSugarLimit).orElse(25f);

        LocalDate today = LocalDate.now();
        LocalDate startDate, endDate;
        if ("monthly".equals(periodType)) {
            startDate = today.withDayOfMonth(1);
            endDate = today;
        } else {
            startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            endDate = today;
        }

        List<MealRecord> meals = mealRecordRepository
                .findByUserIdAndMealDateBetweenOrderByMealTimeDesc(userId, startDate, endDate);

        Map<LocalDate, Float> dailySugar = new LinkedHashMap<>();
        Map<LocalDate, Float> dailyCalories = new LinkedHashMap<>();
        Map<LocalDate, List<String>> dailyFoods = new LinkedHashMap<>();
        for (MealRecord m : meals) {
            LocalDate d = m.getMealDate();
            dailySugar.merge(d, m.getSugarContent() != null ? m.getSugarContent() : 0f, Float::sum);
            dailyCalories.merge(d, m.getCalories() != null ? m.getCalories() : 0f, Float::sum);
            dailyFoods.computeIfAbsent(d, k -> new ArrayList<>());
            if (m.getFoodName() != null) dailyFoods.get(d).add(m.getFoodName());
        }

        int recordDays = dailySugar.size();
        float totalSugar = 0f, totalCalories = 0f;
        int overDays = 0;
        float maxSugarDay = 0f;
        LocalDate worstDay = today;
        for (Map.Entry<LocalDate, Float> e : dailySugar.entrySet()) {
            float daySugar = e.getValue();
            totalSugar += daySugar;
            totalCalories += dailyCalories.getOrDefault(e.getKey(), 0f);
            if (daySugar > sugarLimit) overDays++;
            if (daySugar > maxSugarDay) { maxSugarDay = daySugar; worstDay = e.getKey(); }
        }

        int totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        float avgSugar = recordDays > 0 ? totalSugar / recordDays : 0f;
        float avgCalories = recordDays > 0 ? totalCalories / recordDays : 0f;
        int score = computeScore(overDays, totalDays);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M月d日");
        StringBuilder dailyDetail = new StringBuilder();
        for (Map.Entry<LocalDate, Float> e : dailySugar.entrySet()) {
            LocalDate d = e.getKey();
            float sugar = e.getValue();
            float cal = dailyCalories.getOrDefault(d, 0f);
            List<String> foods = dailyFoods.getOrDefault(d, Collections.emptyList());
            String topFoods = foods.size() > 5 ? String.join("、", foods.subList(0, 5)) + "等" : String.join("、", foods);
            dailyDetail.append(String.format("%s: 糖%.1fg/热量%.0fkcal%s, 食物: %s\n",
                    d.format(fmt), sugar, cal, sugar > sugarLimit ? "(超标)" : "(达标)", topFoods));
        }

        String periodLabel = "monthly".equals(periodType) ? "本月" : "本周";
        String prompt = String.format(
            "你是「糖知」APP的AI营养教练。请为用户生成一份专业的%s健康报告。\n\n" +
            "用户数据：\n" +
            "- 期间：%s 至 %s\n" +
            "- 每日糖分目标：%.0fg\n" +
            "- 日均糖分：%.1fg，日均热量：%.0fkcal\n" +
            "- 记录天数：%d天，超标天数：%d天\n" +
            "- 最高糖分日：%s（%.1fg）\n" +
            "- 总糖分：%.1fg，总热量：%.0fkcal\n" +
            "- 控糖评分：%d/100\n\n" +
            "每日明细：\n%s\n" +
            "请按以下格式生成报告（纯文本，不要markdown）：\n\n" +
            "【%s营养报告】\n\n" +
            "一、总体评价\n用2-3句话评价整体表现。\n\n" +
            "二、数据亮点\n列出3个关键数据发现。\n\n" +
            "三、饮食分析\n分析饮食结构和糖分来源。\n\n" +
            "四、改善建议\n给出4条具体可执行的控糖建议。\n\n" +
            "五、下%s目标\n设定2个量化目标。",
            periodLabel,
            startDate.format(fmt), endDate.format(fmt),
            sugarLimit, avgSugar, avgCalories,
            recordDays, overDays,
            worstDay.format(fmt), maxSugarDay,
            totalSugar, totalCalories, score,
            dailyDetail.toString(),
            periodLabel,
            "monthly".equals(periodType) ? "月" : "周"
        );

        String aiReport;
        try {
            Map<String, Object> aiResult = aiServiceProxy.chat(userId, prompt, false);
            Object response = aiResult.get("response");
            if (response != null) {
                aiReport = response.toString();
            } else {
                Object data = aiResult.get("data");
                if (data instanceof Map) {
                    aiReport = (String) ((Map<?, ?>) data).get("response");
                } else {
                    aiReport = data != null ? data.toString() : null;
                }
            }
            if (aiReport == null || aiReport.trim().isEmpty()) {
                aiReport = generateFallbackReport(periodLabel, avgSugar, sugarLimit, overDays, totalDays, score);
            }
        } catch (Exception e) {
            logger.error("AI报告生成失败: {}", e.getMessage());
            aiReport = generateFallbackReport(periodLabel, avgSugar, sugarLimit, overDays, totalDays, score);
        }

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
        report.setAiReport(aiReport);

        Optional<HealthReport> existing = reportRepository
                .findByUserIdAndPeriodTypeAndStartDate(userId, periodType, startDate);
        if (existing.isPresent()) {
            HealthReport old = existing.get();
            old.setAvgSugar(avgSugar);
            old.setAvgCalories(avgCalories);
            old.setTotalSugar(totalSugar);
            old.setTotalCalories(totalCalories);
            old.setOverDays(overDays);
            old.setRecordDays(recordDays);
            old.setScore(score);
            old.setSummary(report.getSummary());
            old.setAiReport(aiReport);
            return reportRepository.save(old);
        }
        return reportRepository.save(report);
    }

    private String generateFallbackReport(String period, float avgSugar, float limit,
                                           int overDays, int totalDays, int score) {
        return String.format(
            "【%s营养报告】\n\n" +
            "一、总体评价\n日均糖分%.1fg，目标%.0fg，控糖评分%d分。%s\n\n" +
            "二、数据亮点\n1. 记录了%d天的饮食数据\n2. 超标%d天\n3. 评分%d/100\n\n" +
            "三、改善建议\n1. 选择低糖饮品替代含糖饮料\n2. 多吃蔬菜和全谷物\n3. 控制甜品摄入频率\n4. 养成看营养标签的习惯\n\n" +
            "（AI详细分析暂时不可用，以上为基础统计报告）",
            period, avgSugar, limit, score,
            overDays == 0 ? "表现优秀！" : overDays <= totalDays / 4 ? "整体不错，继续保持。" : "需要加强控糖意识。",
            totalDays, overDays, score
        );
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
