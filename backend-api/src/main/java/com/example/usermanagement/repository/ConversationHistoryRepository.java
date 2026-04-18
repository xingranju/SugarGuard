package com.example.usermanagement.repository;

import com.example.usermanagement.entity.ConversationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对话历史Repository
 */
@Repository
public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Integer> {
    
    /**
     * 根据用户ID查找对话历史，按时间倒序
     */
    List<ConversationHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据用户ID和意图查找对话历史
     */
    List<ConversationHistory> findByUserIdAndIntent(Long userId, String intent);
    
    /**
     * 根据用户ID和意图查找对话历史，按时间倒序
     */
    List<ConversationHistory> findByUserIdAndIntentOrderByCreatedAtDesc(Long userId, String intent);
}






