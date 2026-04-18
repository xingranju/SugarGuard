package com.example.usermanagement.controller;

import com.example.usermanagement.dto.*;
import com.example.usermanagement.service.UserService;
import com.example.usermanagement.util.JwtUtil;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * renZheng_kongZhi_qi
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     * yongHu_dengLu
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        logger.info("收到登录请求: {}", loginRequest.getUsername());
        
        // 设置设备信息和IP地址
        loginRequest.setIpAddress(getClientIpAddress(request));
        loginRequest.setDeviceInfo(request.getHeader("User-Agent"));

        ApiResponse<LoginResponse> response = userService.login(loginRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户注册
     * yongHu_zhuCe
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        
        logger.info("收到注册请求 - username: {}, email: {}", 
                    registerRequest.getUsername(), registerRequest.getEmail());
        logger.info("confirmPassword: {}, password: {}", 
                    (registerRequest.getConfirmPassword() != null ? "已设置" : "null"),
                    (registerRequest.getPassword() != null ? "已设置" : "null"));
        logger.info("RegisterRequest详细信息: {}", registerRequest);

        ApiResponse<UserDto> response = userService.register(registerRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前用户信息
     * huoQu_dangQian_yongHu_xinXi
     */
    @PostMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            HttpServletRequest request) {
        
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(401, "未提供认证令牌"));
        }

        ApiResponse<UserDto> response = userService.getCurrentUser(token);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * 刷新令牌
     * shuaXin_lingPai
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestBody String refreshToken) {
        
        logger.info("收到刷新令牌请求");

        try {
            if (!jwtUtil.isValidToken(refreshToken)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("刷新令牌无效"));
            }

            String username = jwtUtil.getUsernameFromToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);

            // 生成新的访问令牌
            String newAccessToken = jwtUtil.generateToken(username, userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(username, userId);
            long expiresIn = jwtUtil.getTokenRemainingTime(newAccessToken);

            // 获取用户信息
            ApiResponse<UserDto> userResponse = userService.getUserByUsername(username);
            if (!userResponse.isSuccess()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在"));
            }

            LoginResponse loginResponse = new LoginResponse(
                    newAccessToken,
                    newRefreshToken,
                    expiresIn,
                    userResponse.getData(),
                    java.util.UUID.randomUUID().toString()
            );

            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", loginResponse));

        } catch (Exception e) {
            logger.error("刷新令牌时发生错误", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("令牌刷新失败"));
        }
    }

    /**
     * 用户登出
     * yongHu_dengChu
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        
        logger.info("收到登出请求");

        // 这里可以实现令牌黑名单机制
        // 目前简单返回成功响应
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    /**
     * 从请求中提取JWT令牌
     * cong_qingQiu_zhong_tiQu_JWT_lingPai
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     * huoQu_keHuDuan_IP_diZhi
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
