package com.example.usermanagement.service;

import com.example.usermanagement.dto.UpdateUserInfoRequest;
import com.example.usermanagement.dto.UserInfoDto;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户信息服务类
 */
@Service
public class UserInfoService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 根据用户ID获取用户信息
     */
    public UserInfoDto getUserInfoById(Long userId) {
        logger.info("获取用户信息, userId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return convertToDto(user);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public UserInfoDto updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        logger.info("更新用户信息, userId: {}, request: {}", userId, request);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新字段（只更新非null的字段）
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            // 检查用户名是否已被其他用户使用
            userRepository.findByUsername(request.getUsername()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new RuntimeException("用户名已被使用");
                }
            });
            user.setUsername(request.getUsername());
        }
        
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // 检查邮箱是否已被其他用户使用
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(userId)) {
                    throw new RuntimeException("邮箱已被使用");
                }
            });
            user.setEmail(request.getEmail());
        }
        
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        if (request.getGender() != null) {
            user.setGender(User.Gender.fromValue(request.getGender()));
        }
        
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        
        User savedUser = userRepository.save(user);
        logger.info("用户信息更新成功, userId: {}", userId);
        
        return convertToDto(savedUser);
    }
    
    /**
     * 更新用户头像
     */
    @Transactional
    public UserInfoDto updateAvatar(Long userId, String avatarUrl) {
        logger.info("更新用户头像, userId: {}, avatarUrl: {}", userId, avatarUrl);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setAvatarUrl(avatarUrl);
        User savedUser = userRepository.save(user);
        
        logger.info("用户头像更新成功, userId: {}", userId);
        return convertToDto(savedUser);
    }
    
    /**
     * 将User实体转换为UserInfoDto
     */
    private UserInfoDto convertToDto(User user) {
        UserInfoDto dto = new UserInfoDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setGender(user.getGender() != null ? user.getGender().getValue() : null);
        dto.setBirthday(user.getBirthday());
        dto.setStatus(user.getStatus() != null ? user.getStatus().getValue() : null);
        dto.setEmailVerified(user.getEmailVerified());
        dto.setPhoneVerified(user.getPhoneVerified());
        dto.setLastLoginTime(user.getLastLoginTime());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}

