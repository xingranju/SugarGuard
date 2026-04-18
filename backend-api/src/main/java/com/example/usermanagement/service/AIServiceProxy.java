package com.example.usermanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * AI服务代理类
 * 负责调用Python AI服务（端口8000）
 */
@Service
public class AIServiceProxy {

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    public AIServiceProxy() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 调用饮品识别API
     *
     * @param file   图片文件
     * @param userId 用户ID
     * @return 识别结果
     */
    public Map<String, Object> recognizeDrink(MultipartFile file, Long userId) {
        try {
            String url = aiServiceUrl + "/api/recognize-drink?user_id=" + userId;

            // 构建multipart请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("AI服务调用失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("调用AI识别服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用健康分析API
     *
     * @param userId 用户ID
     * @param days   分析天数
     * @return 分析结果
     */
    public Map<String, Object> analyzeHealth(Long userId, int days) {
        try {
            String url = aiServiceUrl + "/api/health-analysis/" + userId + "?days=" + days;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("AI服务调用失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("调用AI健康分析服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用智能对话API
     *
     * @param userId  用户ID
     * @param message 用户消息
     * @return 对话回复
     */
    public Map<String, Object> chat(Long userId, String message) {
        return chat(userId, message, true);
    }

    public Map<String, Object> chat(Long userId, String message, boolean saveHistory) {
        try {
            String url = aiServiceUrl + "/api/chat";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("message", message);
            requestBody.put("save_history", saveHistory);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("AI服务调用失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("调用AI对话服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用饮品推荐API
     *
     * @param userId 用户ID
     * @return 推荐结果
     */
    public Map<String, Object> recommendDrinks(Long userId) {
        try {
            String url = aiServiceUrl + "/api/recommend-drinks";
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);
            requestBody.put("request_type", "mixed");  // 使用混合推荐策略
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("AI服务调用失败: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("调用AI推荐服务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查AI服务健康状态
     *
     * @return 健康状态
     */
    public Map<String, Object> checkAIServiceHealth() {
        try {
            String url = aiServiceUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "unhealthy");
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }
}

