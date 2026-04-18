package com.example.usermanagement.repository;

import com.example.usermanagement.entity.UserDrinkPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户饮品偏好数据仓库
 */
@Repository
public interface UserDrinkPreferenceRepository extends JpaRepository<UserDrinkPreference, Integer> {
    
    /**
     * 根据用户ID和饮品ID查找偏好记录
     */
    Optional<UserDrinkPreference> findByUserIdAndDrinkId(Long userId, Integer drinkId);
}
