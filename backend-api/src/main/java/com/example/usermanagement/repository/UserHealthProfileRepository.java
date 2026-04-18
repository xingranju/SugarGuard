package com.example.usermanagement.repository;

import com.example.usermanagement.entity.UserHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 用户健康档案数据访问接口
 */
@Repository
public interface UserHealthProfileRepository extends JpaRepository<UserHealthProfile, Long> {
    
    /**
     * 根据用户ID查询健康档案
     */
    Optional<UserHealthProfile> findByUserId(Long userId);
    
    /**
     * 检查用户是否已有健康档案
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 根据用户ID删除健康档案
     */
    void deleteByUserId(Long userId);
}

