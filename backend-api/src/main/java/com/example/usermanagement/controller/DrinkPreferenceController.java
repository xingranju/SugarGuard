package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.DrinkDto;
import com.example.usermanagement.dto.UserDrinkPreferenceDto;
import com.example.usermanagement.service.DrinkPreferenceService;
import com.example.usermanagement.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 饮品偏好控制器
 */
@RestController
@RequestMapping("/api/drink-preferences")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DrinkPreferenceController {

    private static final Logger logger = LoggerFactory.getLogger(DrinkPreferenceController.class);

    @Autowired
    private DrinkPreferenceService drinkPreferenceService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户所有饮品偏好
     * GET /api/drinks/preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<List<UserDrinkPreferenceDto>>> getUserPreferences(
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 获取饮品偏好", userId);
            
            ApiResponse<List<UserDrinkPreferenceDto>> response = 
                    drinkPreferenceService.getUserDrinkPreferences(userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取饮品偏好失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取饮品偏好失败: " + e.getMessage()));
        }
    }

    /**
     * 添加或更新饮品偏好
     * POST /api/drinks/preferences
     */
    @PostMapping("/preferences")
    public ResponseEntity<ApiResponse<UserDrinkPreferenceDto>> addOrUpdatePreference(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Integer drinkId = (Integer) request.get("drinkId");
            Integer preferenceScore = (Integer) request.get("preferenceScore");
            
            if (drinkId == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("饮品ID不能为空"));
            }
            
            logger.info("用户 {} 保存饮品 {} 的偏好", userId, drinkId);
            
            ApiResponse<UserDrinkPreferenceDto> response = 
                    drinkPreferenceService.addOrUpdatePreference(userId, drinkId, preferenceScore);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }
        } catch (Exception e) {
            logger.error("保存饮品偏好失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("保存饮品偏好失败: " + e.getMessage()));
        }
    }

    /**
     * 记录饮用
     * POST /api/drinks/preferences/{drinkId}/consume
     */
    @PostMapping("/preferences/{drinkId}/consume")
    public ResponseEntity<ApiResponse<UserDrinkPreferenceDto>> recordConsumption(
            @PathVariable Integer drinkId,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 记录饮用饮品 {}", userId, drinkId);
            
            ApiResponse<UserDrinkPreferenceDto> response = 
                    drinkPreferenceService.recordConsumption(userId, drinkId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("记录饮用失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("记录饮用失败: " + e.getMessage()));
        }
    }

    /**
     * 删除饮品偏好
     * DELETE /api/drinks/preferences/{drinkId}
     */
    @DeleteMapping("/preferences/{drinkId}")
    public ResponseEntity<ApiResponse<Void>> deletePreference(
            @PathVariable Integer drinkId,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 删除饮品 {} 的偏好", userId, drinkId);
            
            ApiResponse<Void> response = drinkPreferenceService.deletePreference(userId, drinkId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }
        } catch (Exception e) {
            logger.error("删除饮品偏好失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("删除饮品偏好失败: " + e.getMessage()));
        }
    }

    /**
     * 更新偏好评分
     * PUT /api/drink-preferences/preferences/{drinkId}
     */
    @PutMapping("/preferences/{drinkId}")
    public ResponseEntity<ApiResponse<UserDrinkPreferenceDto>> updatePreferenceScore(
            @PathVariable Integer drinkId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Integer preferenceScore = (Integer) request.get("preferenceScore");
            
            if (preferenceScore == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("偏好评分不能为空"));
            }
            
            logger.info("用户 {} 更新饮品 {} 的偏好评分为 {}", userId, drinkId, preferenceScore);
            
            ApiResponse<UserDrinkPreferenceDto> response = 
                    drinkPreferenceService.addOrUpdatePreference(userId, drinkId, preferenceScore);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新偏好评分失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("更新偏好评分失败: " + e.getMessage()));
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

