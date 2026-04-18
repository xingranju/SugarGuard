package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.NotificationDto;
import com.example.usermanagement.service.NotificationService;
import com.example.usermanagement.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            Authentication authentication,
            @RequestParam(required = false) Long userId) {
        try {
            Long uid = resolveUserId(authentication, userId);
            logger.info("用户 {} 获取通知列表", uid);
            return ResponseEntity.ok(notificationService.getUserNotifications(uid));
        } catch (Exception e) {
            logger.error("获取通知列表失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取通知列表失败: " + e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            Authentication authentication,
            @RequestParam(required = false) Long userId) {
        try {
            Long uid = resolveUserId(authentication, userId);
            return ResponseEntity.ok(notificationService.getUnreadCount(uid));
        } catch (Exception e) {
            logger.error("获取未读数失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取未读数失败: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(required = false) Long userId) {
        try {
            Long uid = resolveUserId(authentication, userId);
            logger.info("用户 {} 标记通知 {} 已读", uid, id);
            ApiResponse<NotificationDto> resp = notificationService.markAsRead(id, uid);
            return resp.isSuccess() ? ResponseEntity.ok(resp) : ResponseEntity.status(resp.getCode()).body(resp);
        } catch (Exception e) {
            logger.error("标记已读失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("标记已读失败: " + e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            Authentication authentication,
            @RequestParam(required = false) Long userId) {
        try {
            Long uid = resolveUserId(authentication, userId);
            logger.info("用户 {} 全部标记已读", uid);
            return ResponseEntity.ok(notificationService.markAllAsRead(uid));
        } catch (Exception e) {
            logger.error("全部标记已读失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("全部标记已读失败: " + e.getMessage()));
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
