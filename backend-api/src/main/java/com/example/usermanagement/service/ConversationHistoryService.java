package com.example.usermanagement.service;

import com.example.usermanagement.entity.ConversationHistory;
import com.example.usermanagement.repository.ConversationHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 对话历史服务类
 */
@Service
@Transactional
public class ConversationHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationHistoryService.class);
    
    @Autowired
    private ConversationHistoryRepository conversationHistoryRepository;
    
    /**
     * 获取用户的对话历史（仅AI问答对话）
     */
    public List<ConversationHistory> getUserConversations(Long userId, int limit) {
        logger.info("获取用户{}的AI问答对话历史，限制{}条", userId, limit);
        
        List<ConversationHistory> conversations = conversationHistoryRepository
                .findByUserIdAndIntentOrderByCreatedAtDesc(userId, "ai_chat");
        
        if (conversations.size() > limit) {
            conversations = conversations.subList(0, limit);
        }
        
        return conversations;
    }
    
    /**
     * 获取所有对话历史（仅AI问答对话）
     */
    public List<ConversationHistory> getAllUserConversations(Long userId) {
        logger.info("获取用户{}的所有AI问答对话历史", userId);
        return conversationHistoryRepository.findByUserIdAndIntentOrderByCreatedAtDesc(userId, "ai_chat");
    }
    
    /**
     * 更新用户反馈
     */
    public boolean updateFeedback(Integer conversationId, Long userId, Integer feedback) {
        try {
            ConversationHistory conversation = conversationHistoryRepository.findById(conversationId).orElse(null);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                logger.warn("对话记录不存在或不属于该用户：conversationId={}, userId={}", conversationId, userId);
                return false;
            }
            
            conversation.setFeedback(feedback);
            conversationHistoryRepository.save(conversation);
            logger.info("更新对话反馈成功：conversationId={}, feedback={}", conversationId, feedback);
            return true;
        } catch (Exception e) {
            logger.error("更新对话反馈失败", e);
            return false;
        }
    }
    
    /**
     * 删除对话记录
     */
    public boolean deleteConversation(Integer conversationId, Long userId) {
        try {
            ConversationHistory conversation = conversationHistoryRepository.findById(conversationId).orElse(null);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                logger.warn("对话记录不存在或不属于该用户：conversationId={}, userId={}", conversationId, userId);
                return false;
            }
            
            conversationHistoryRepository.delete(conversation);
            logger.info("删除对话记录成功：conversationId={}", conversationId);
            return true;
        } catch (Exception e) {
            logger.error("删除对话记录失败", e);
            return false;
        }
    }
}






