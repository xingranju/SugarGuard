package com.example.usermanagement.repository;

import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户数据访问接口
 * yongHu_shuJu_fangWen_jieKou
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * genJu_yongHuMing_chaZhao_yongHu
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * genJu_youXiang_chaZhao_yongHu
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户
     * genJu_yongHuMing_huo_youXiang_chaZhao_yongHu
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * 检查用户名是否存在
     * jianCha_yongHuMing_shiFou_cunZai
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * jianCha_youXiang_shiFou_cunZai
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     * jianCha_shouJiHao_shiFou_cunZai
     */
    boolean existsByPhone(String phone);

    /**
     * 更新用户最后登录时间
     * gengXin_yongHu_zuiHou_dengLu_shiJian
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginTime = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * 根据用户状态查找用户
     * genJu_yongHu_zhuangTai_chaZhao_yongHu
     */
    @Query("SELECT u FROM User u WHERE u.status = :status")
    Optional<User> findByStatus(@Param("status") User.UserStatus status);
}
