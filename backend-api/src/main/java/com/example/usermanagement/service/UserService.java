package com.example.usermanagement.service;

import com.example.usermanagement.dto.*;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 用户服务类
 * yongHu_fuWu_lei
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     * yongHu_dengLu
     */
    public ApiResponse<LoginResponse> login(LoginRequest loginRequest) {
        try {
            logger.info("用户登录尝试: {}", loginRequest.getUsername());

            // 查找用户（支持用户名或邮箱登录）
            User user = userRepository.findByUsernameOrEmail(loginRequest.getUsername())
                    .orElse(null);

            if (user == null) {
                logger.warn("用户不存在: {}", loginRequest.getUsername());
                return ApiResponse.error("用户名或密码错误");
            }

            // 验证密码
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                logger.warn("密码错误: {}", loginRequest.getUsername());
                return ApiResponse.error("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                logger.warn("用户状态异常: {} - {}", loginRequest.getUsername(), user.getStatus());
                return ApiResponse.error("账户已被禁用，请联系管理员");
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);

            // 生成JWT令牌
            String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
            
            // 获取令牌过期时间
            long expiresIn = jwtUtil.getTokenRemainingTime(accessToken);

            // 生成会话ID
            String sessionId = UUID.randomUUID().toString();

            // 构建响应
            UserDto userDto = UserDto.fromEntity(user);
            LoginResponse loginResponse = new LoginResponse(
                    accessToken,
                    refreshToken,
                    expiresIn,
                    userDto,
                    sessionId
            );

            logger.info("用户登录成功: {}", user.getUsername());
            return ApiResponse.success("登录成功", loginResponse);

        } catch (Exception e) {
            logger.error("登录过程中发生错误", e);
            return ApiResponse.error("登录失败，请稍后重试");
        }
    }

    /**
     * 用户注册
     * yongHu_zhuCe
     */
    public ApiResponse<UserDto> register(RegisterRequest registerRequest) {
        try {
            logger.info("用户注册尝试: {}", registerRequest.getUsername());

            // 验证密码匹配
            if (!registerRequest.isPasswordMatch()) {
                return ApiResponse.error("两次输入的密码不一致");
            }

            // 检查用户名是否已存在
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                logger.warn("用户名已存在: {}", registerRequest.getUsername());
                return ApiResponse.error("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                logger.warn("邮箱已存在: {}", registerRequest.getEmail());
                return ApiResponse.error("邮箱已被注册");
            }

            // 检查手机号是否已存在（如果提供了手机号）
            if (registerRequest.getPhone() != null && !registerRequest.getPhone().isEmpty()) {
                if (userRepository.existsByPhone(registerRequest.getPhone())) {
                    logger.warn("手机号已存在: {}", registerRequest.getPhone());
                    return ApiResponse.error("手机号已被注册");
                }
            }

            // 创建新用户
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            user.setPhone(registerRequest.getPhone());
            
            // 设置性别
            if (registerRequest.getGender() != null && !registerRequest.getGender().isEmpty()) {
                user.setGender(User.Gender.fromValue(registerRequest.getGender()));
            }

            // 设置生日
            if (registerRequest.getBirthday() != null && !registerRequest.getBirthday().isEmpty()) {
                try {
                    LocalDate birthday = LocalDate.parse(registerRequest.getBirthday(), 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    user.setBirthday(birthday);
                } catch (Exception e) {
                    logger.warn("生日格式错误: {}", registerRequest.getBirthday());
                    // 生日格式错误不影响注册，只是不设置生日
                }
            }

            user.setStatus(User.UserStatus.ACTIVE);
            user.setEmailVerified(false);
            user.setPhoneVerified(false);

            // 保存用户
            User savedUser = userRepository.save(user);

            logger.info("用户注册成功: {}", savedUser.getUsername());
            return ApiResponse.success("注册成功", UserDto.fromEntity(savedUser));

        } catch (Exception e) {
            logger.error("注册过程中发生错误", e);
            return ApiResponse.error("注册失败，请稍后重试");
        }
    }

    /**
     * 根据用户名获取用户信息
     * genJu_yongHuMing_huoQu_yongHu_xinXi
     */
    public ApiResponse<UserDto> getUserByUsername(String username) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            return ApiResponse.success(UserDto.fromEntity(user));
        } catch (Exception e) {
            logger.error("获取用户信息时发生错误", e);
            return ApiResponse.error("获取用户信息失败");
        }
    }

    /**
     * 根据用户ID获取用户信息
     * genJu_yongHu_ID_huoQu_yongHu_xinXi
     */
    public ApiResponse<UserDto> getUserById(Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            return ApiResponse.success(UserDto.fromEntity(user));
        } catch (Exception e) {
            logger.error("获取用户信息时发生错误", e);
            return ApiResponse.error("获取用户信息失败");
        }
    }

    /**
     * 验证JWT令牌并获取用户信息
     * yanZheng_JWT_lingPai_bing_huoQu_yongHu_xinXi
     */
    public ApiResponse<UserDto> getCurrentUser(String token) {
        try {
            if (!jwtUtil.isValidToken(token)) {
                return ApiResponse.error(401, "令牌无效");
            }

            String username = jwtUtil.getUsernameFromToken(token);
            return getUserByUsername(username);
        } catch (Exception e) {
            logger.error("获取当前用户信息时发生错误", e);
            return ApiResponse.error("获取用户信息失败");
        }
    }
}
