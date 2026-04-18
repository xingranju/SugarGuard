package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.dto.ChatRequest;
import com.example.usermanagement.service.AIServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * AI功能控制器
 * 提供饮品识别、健康分析、智能对话、饮品推荐等AI功能
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private AIServiceProxy aiServiceProxy;

    /**
     * 饮品识别接口
     * POST /api/ai/recognize-drink
     *
     * @param file           上传的图片文件
     * @param authentication 认证信息
     * @return 识别结果
     */
    @PostMapping("/recognize-drink")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recognizeDrink(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            // 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("请上传图片文件"));
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("只支持图片文件"));
            }

            // 验证文件大小（10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("图片文件不能超过10MB"));
            }

            // 获取用户ID
            Long userId = getUserIdFromAuthentication(authentication);

            logger.info("用户 {} 请求识别饮品，文件名: {}, 大小: {} bytes",
                    userId, file.getOriginalFilename(), file.getSize());

            // 调用AI服务
            Map<String, Object> result = aiServiceProxy.recognizeDrink(file, userId);

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            logger.error("饮品识别失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("饮品识别失败: " + e.getMessage()));
        }
    }

    /**
     * 健康数据分析接口
     * GET /api/ai/health-analysis
     *
     * @param days           分析天数（默认7天）
     * @param authentication 认证信息
     * @return 健康分析结果
     */
    @GetMapping("/health-analysis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthAnalysis(
            @RequestParam(defaultValue = "7") int days,
            Authentication authentication) {

        try {
            Long userId = getUserIdFromAuthentication(authentication);

            logger.info("用户 {} 请求健康分析，天数: {}", userId, days);

            // 调用AI服务
            Map<String, Object> result = aiServiceProxy.analyzeHealth(userId, days);

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            logger.error("健康分析失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("健康分析失败: " + e.getMessage()));
        }
    }

    /**
     * 智能对话接口
     * POST /api/ai/chat
     *
     * @param chatRequest    对话请求
     * @param authentication 认证信息
     * @return 对话回复
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, Object>>> chat(
            @RequestBody ChatRequest chatRequest,
            Authentication authentication) {

        try {
            if (chatRequest.getMessage() == null || chatRequest.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("消息不能为空"));
            }

            Long userId = getUserIdFromAuthentication(authentication);

            boolean saveHistory = chatRequest.getSaveHistory() == null || chatRequest.getSaveHistory();
            logger.info("用户 {} 发送消息: {}, saveHistory={}", userId, chatRequest.getMessage(), saveHistory);

            Map<String, Object> result = aiServiceProxy.chat(userId, chatRequest.getMessage(), saveHistory);

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            logger.error("智能对话失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("智能对话失败: " + e.getMessage()));
        }
    }

    /**
     * 饮品推荐接口
     * POST /api/ai/recommend-drinks
     *
     * @param request 推荐请求（可选参数）
     * @param authentication 认证信息
     * @return 推荐饮品列表
     */
    @PostMapping("/recommend-drinks")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recommendDrinks(
            @RequestBody(required = false) Map<String, Object> request,
            Authentication authentication) {

        try {
            Long userId = getUserIdFromAuthentication(authentication);

            logger.info("用户 {} 请求饮品推荐", userId);

            // 调用AI服务
            Map<String, Object> result = aiServiceProxy.recommendDrinks(userId);

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            logger.error("饮品推荐失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("饮品推荐失败: " + e.getMessage()));
        }
    }

    /**
     * AI服务健康检查
     * GET /api/ai/health-check
     *
     * @return AI服务状态
     */
    @GetMapping("/health-check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> aiHealthCheck() {
        try {
            Map<String, Object> health = aiServiceProxy.checkAIServiceHealth();
            return ResponseEntity.ok(ApiResponse.success(health));
        } catch (Exception e) {
            logger.error("AI服务健康检查失败", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("AI服务不可用: " + e.getMessage()));
        }
    }

    /**
     * 从认证信息中获取用户ID
     *
     * @param authentication 认证信息
     * @return 用户ID
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return 1L;
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            return 1L;
        }
    }
}

