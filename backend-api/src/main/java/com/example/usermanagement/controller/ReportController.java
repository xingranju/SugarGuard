package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.HealthReportDto;
import com.example.usermanagement.entity.HealthReport;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HealthReportDto>>> getReports(
            Authentication authentication,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String periodType,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        Long uid = resolveUserId(authentication, userId);
        if (uid == null) return ResponseEntity.ok(ApiResponse.error("请登录或传入 userId"));

        LocalDate fromDate = from != null ? LocalDate.parse(from) : null;
        LocalDate toDate = to != null ? LocalDate.parse(to) : null;

        List<HealthReport> reports = reportService.getReports(uid, periodType, fromDate, toDate);
        List<HealthReportDto> dtos = reports.stream()
                .map(HealthReportDto::fromEntity).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<String>> generateReports(
            Authentication authentication,
            @RequestParam(required = false) Long userId) {
        Long uid = resolveUserId(authentication, userId);
        if (uid == null) return ResponseEntity.ok(ApiResponse.error("请登录或传入 userId"));

        reportService.generateReportsOnDemand(uid);
        return ResponseEntity.ok(ApiResponse.success("报告生成完成"));
    }

    /** JWT 优先；无 Security 上下文时使用查询参数 userId（与 /api/meals 等 permitAll 用法一致） */
    private Long resolveUserId(Authentication authentication, Long queryUserId) {
        Long fromAuth = getUserId(authentication);
        if (fromAuth != null) return fromAuth;
        return queryUserId;
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null) return null;
        String username = authentication.getName();
        return userRepository.findByUsername(username).map(User::getId).orElse(null);
    }
}
