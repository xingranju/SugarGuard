package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import com.example.usermanagement.entity.ConversationHistory;
import com.example.usermanagement.service.ConversationHistoryService;
import com.example.usermanagement.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 对话历史控制器
 */
@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConversationHistoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationHistoryController.class);
    
    @Autowired
    private ConversationHistoryService conversationHistoryService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取用户的对话历史
     * GET /api/conversations?limit=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationHistory>>> getUserConversations(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 获取对话历史，限制 {} 条", userId, limit);
            
            List<ConversationHistory> conversations = conversationHistoryService.getUserConversations(userId, limit);
            
            return ResponseEntity.ok(ApiResponse.success("获取对话历史成功", conversations));
        } catch (Exception e) {
            logger.error("获取对话历史失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取对话历史失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有对话历史
     * GET /api/conversations/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ConversationHistory>>> getAllConversations(
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 获取所有对话历史", userId);
            
            List<ConversationHistory> conversations = conversationHistoryService.getAllUserConversations(userId);
            
            return ResponseEntity.ok(ApiResponse.success("获取对话历史成功", conversations));
        } catch (Exception e) {
            logger.error("获取对话历史失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取对话历史失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新对话反馈
     * PUT /api/conversations/{conversationId}/feedback
     */
    @PutMapping("/{conversationId}/feedback")
    public ResponseEntity<ApiResponse<Void>> updateFeedback(
            @PathVariable Integer conversationId,
            @RequestBody Map<String, Integer> request,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Integer feedback = request.get("feedback");
            
            if (feedback == null || feedback < 1 || feedback > 5) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("反馈评分必须在1-5之间"));
            }
            
            logger.info("用户 {} 更新对话 {} 的反馈为 {}", userId, conversationId, feedback);
            
            boolean success = conversationHistoryService.updateFeedback(conversationId, userId, feedback);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("更新反馈成功", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("对话记录不存在或无权操作"));
            }
        } catch (Exception e) {
            logger.error("更新对话反馈失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("更新反馈失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除对话记录
     * DELETE /api/conversations/{conversationId}
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable Integer conversationId,
            Authentication authentication) {
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            logger.info("用户 {} 删除对话 {}", userId, conversationId);
            
            boolean success = conversationHistoryService.deleteConversation(conversationId, userId);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("删除对话成功", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("对话记录不存在或无权操作"));
            }
        } catch (Exception e) {
            logger.error("删除对话记录失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("删除对话失败: " + e.getMessage()));
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
}






