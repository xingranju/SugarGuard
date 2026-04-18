package com.example.usermanagement.controller;

import com.example.usermanagement.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * jianKang_jianCha_kongZhi_qi
 */
@RestController
public class HealthController {

    /**
     * 健康检查端点
     * jianKang_jianCha_duanDian
     */
    @GetMapping({"/health", "/api/health"})
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "User Management API");
        healthInfo.put("version", "1.0.0");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("message", "服务运行正常");

        return ApiResponse.success("健康检查通过", healthInfo);
    }
}
