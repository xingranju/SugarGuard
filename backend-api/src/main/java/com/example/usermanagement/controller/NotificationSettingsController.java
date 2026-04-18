package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.NotificationSettingsDto;
import com.example.usermanagement.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户通知偏好设置接口
 * - GET /api/notification-settings?userId=xx
 * - PUT /api/notification-settings?userId=xx  body: NotificationSettingsDto
 */
@RestController
@RequestMapping("/api/notification-settings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSettingsController.class);

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationSettingsDto>> getSettings(
            Authentication authentication,
            @RequestParam(required = false) Long userId) {
        try {
            Long uid = resolveUserId(authentication, userId);
            return ResponseEntity.ok(notificationService.getSettings(uid));
        } catch (Exception e) {
            logger.error("获取通知设置失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取通知设置失败: " + e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse<NotificationSettingsDto>> updateSettings(
            Authentication authentication,
            @RequestParam(required = false) Long userId,
            @RequestBody NotificationSettingsDto dto) {
        try {
            Long uid = resolveUserId(authentication, userId);
            logger.info("更新用户 {} 的通知设置", uid);
            return ResponseEntity.ok(notificationService.updateSettings(uid, dto));
        } catch (Exception e) {
            logger.error("更新通知设置失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("更新通知设置失败: " + e.getMessage()));
        }
    }

    private Long resolveUserId(Authentication authentication, Long queryUserId) {
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            try {
                if (principal instanceof String) {
                    return Long.parseLong((String) principal);
                } else if (principal instanceof Long) {
                    return (Long) principal;
                }
            } catch (NumberFormatException ignored) {}
        }
        if (queryUserId != null) {
            return queryUserId;
        }
        throw new RuntimeException("未授权访问且未提供userId参数");
    }
}
