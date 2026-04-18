package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.UpdateUserInfoRequest;
import com.example.usermanagement.dto.UserInfoDto;
import com.example.usermanagement.service.FileUploadService;
import com.example.usermanagement.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 用户信息管理控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserInfoController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserInfoController.class);
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoDto>> getUserInfo() {
        try {
            Long userId = getUserIdFromAuthentication();
            logger.info("获取用户信息请求, userId: {}", userId);
            
            UserInfoDto userInfo = userInfoService.getUserInfoById(userId);
            
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", userInfo));
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取用户信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoDto>> updateUserInfo(
            @Valid @RequestBody UpdateUserInfoRequest request) {
        try {
            Long userId = getUserIdFromAuthentication();
            logger.info("更新用户信息请求, userId: {}, request: {}", userId, request);
            
            UserInfoDto userInfo = userInfoService.updateUserInfo(userId, request);
            
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", userInfo));
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新用户信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        try {
            Long userId = getUserIdFromAuthentication();
            logger.info("上传头像请求, userId: {}, 文件名: {}", userId, file.getOriginalFilename());
            
            // 上传文件
            String avatarUrl = fileUploadService.uploadAvatar(file);
            
            // 更新用户头像URL
            UserInfoDto userInfo = userInfoService.updateAvatar(userId, avatarUrl);
            
            return ResponseEntity.ok(ApiResponse.success("头像上传成功", userInfo.getAvatarUrl()));
        } catch (IllegalArgumentException e) {
            logger.error("头像上传失败: 参数错误", e);
            return ResponseEntity.ok(ApiResponse.error("头像上传失败: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("头像上传失败", e);
            return ResponseEntity.ok(ApiResponse.error("头像上传失败: " + e.getMessage()));
        }
    }
    
    /**
     * 从Authentication中获取用户ID
     */
    private Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        
        try {
            // JWT过滤器将userId作为String存储在principal中
            String userIdStr = authentication.getPrincipal().toString();
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            logger.error("无法获取用户ID", e);
            throw new RuntimeException("无法获取用户ID");
        }
    }
}


