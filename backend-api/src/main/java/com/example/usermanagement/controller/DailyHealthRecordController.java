package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.DailyHealthRecordDto;
import com.example.usermanagement.service.DailyHealthRecordService;
import com.example.usermanagement.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 每日健康记录控制器
 */
@RestController
@RequestMapping("/api/health-records")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DailyHealthRecordController {

    private static final Logger logger = LoggerFactory.getLogger(DailyHealthRecordController.class);

    @Autowired
    private DailyHealthRecordService recordService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取指定日期的健康记录
     * GET /api/health-records/{date}
     */
    @GetMapping("/{date}")
    public ResponseEntity<ApiResponse<DailyHealthRecordDto>> getRecordByDate(
            @PathVariable String date,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 获取 {} 的健康记录", userId, date);
            
            ApiResponse<DailyHealthRecordDto> response = recordService.getRecordByDate(userId, date);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }
        } catch (Exception e) {
            logger.error("获取健康记录失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取健康记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取最近N天的健康记录
     * GET /api/health-records/recent/{days}
     */
    @GetMapping("/recent/{days}")
    public ResponseEntity<ApiResponse<List<DailyHealthRecordDto>>> getRecentRecords(
            @PathVariable int days,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 获取最近 {} 天的健康记录", userId, days);
            
            ApiResponse<List<DailyHealthRecordDto>> response = recordService.getRecentRecords(userId, days);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取健康记录失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取健康记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取指定日期范围的健康记录
     * GET /api/health-records/range?startDate=xxx&endDate=xxx
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<DailyHealthRecordDto>>> getRecordsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 获取 {} 到 {} 的健康记录", userId, startDate, endDate);
            
            ApiResponse<List<DailyHealthRecordDto>> response = 
                    recordService.getRecordsByDateRange(userId, startDate, endDate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取健康记录失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取健康记录失败: " + e.getMessage()));
        }
    }

    /**
     * 创建或更新健康记录
     * POST /api/health-records
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DailyHealthRecordDto>> createOrUpdateRecord(
            @RequestBody DailyHealthRecordDto recordDto,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 保存健康记录", userId);
            
            ApiResponse<DailyHealthRecordDto> response = 
                    recordService.createOrUpdateRecord(userId, recordDto);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }
        } catch (Exception e) {
            logger.error("保存健康记录失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("保存健康记录失败: " + e.getMessage()));
        }
    }

    /**
     * 删除健康记录
     * DELETE /api/health-records/{date}
     */
    @DeleteMapping("/{date}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @PathVariable String date,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 删除 {} 的健康记录", userId, date);
            
            ApiResponse<Void> response = recordService.deleteRecord(userId, date);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }
        } catch (Exception e) {
            logger.error("删除健康记录失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("删除健康记录失败: " + e.getMessage()));
        }
    }

    /**
     * 获取记录统计
     * GET /api/health-records/stats/count
     */
    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponse<Long>> getRecordCount(
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 获取记录统计", userId);
            
            ApiResponse<Long> response = recordService.getRecordCount(userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取记录统计失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取记录统计失败: " + e.getMessage()));
        }
    }

    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("未授权访问");
        }
        
        Object principal = authentication.getPrincipal();
        
        // JWT过滤器将userId作为字符串存储在principal中
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                logger.error("无法解析用户ID: {}", principal);
                throw new RuntimeException("用户ID格式错误");
            }
        } else if (principal instanceof Long) {
            return (Long) principal;
        }
        
        logger.error("未知的principal类型: {}", principal.getClass().getName());
        throw new RuntimeException("无法获取用户ID");
    }

    /**
     * 从请求头中提取用户ID（备用方法）
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("未提供认证令牌");
    }

    /**
     * 从请求中提取JWT令牌
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


