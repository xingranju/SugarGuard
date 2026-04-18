package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.UserHealthProfileDto;
import com.example.usermanagement.service.UserHealthProfileService;
import com.example.usermanagement.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户档案控制器
 */
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserHealthProfileService healthProfileService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前用户的健康档案
     * GET /api/profile/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<UserHealthProfileDto>> getHealthProfile(
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 获取健康档案", userId);
            
            ApiResponse<UserHealthProfileDto> response = healthProfileService.getUserHealthProfile(userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }
        } catch (Exception e) {
            logger.error("获取健康档案失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取健康档案失败: " + e.getMessage()));
        }
    }

    /**
     * 创建或更新健康档案
     * POST /api/profile/health
     */
    @PostMapping("/health")
    public ResponseEntity<ApiResponse<UserHealthProfileDto>> createOrUpdateHealthProfile(
            @RequestBody UserHealthProfileDto profileDto,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 保存健康档案", userId);
            
            ApiResponse<UserHealthProfileDto> response = 
                    healthProfileService.createOrUpdateHealthProfile(userId, profileDto);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }
        } catch (Exception e) {
            logger.error("保存健康档案失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("保存健康档案失败: " + e.getMessage()));
        }
    }

    /**
     * 删除健康档案
     * DELETE /api/profile/health
     */
    @DeleteMapping("/health")
    public ResponseEntity<ApiResponse<Void>> deleteHealthProfile(
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 删除健康档案", userId);
            
            ApiResponse<Void> response = healthProfileService.deleteHealthProfile(userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }
        } catch (Exception e) {
            logger.error("删除健康档案失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("删除健康档案失败: " + e.getMessage()));
        }
    }

    /**
     * 计算BMI
     * GET /api/profile/health/bmi
     */
    @GetMapping("/health/bmi")
    public ResponseEntity<ApiResponse<Double>> calculateBMI(
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 计算BMI", userId);
            
            ApiResponse<Double> response = healthProfileService.calculateBMI(userId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }
        } catch (Exception e) {
            logger.error("计算BMI失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("计算BMI失败: " + e.getMessage()));
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

