package com.example.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 用户管理系统主应用类
 * yongHu_guanLi_xiTong_zhu_yingYong
 */
@SpringBootApplication
@EnableScheduling
public class UserManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
        System.out.println("=================================");
        System.out.println("用户管理系统API服务器启动成功！");
        System.out.println("API地址: http://localhost:8080");
        System.out.println("=================================");
    }
}
