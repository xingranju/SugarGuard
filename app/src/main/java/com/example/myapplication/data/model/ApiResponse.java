package com.example.myapplication.data.model;

/**
 * API响应基础类
 * API_xiangYing_jiChu
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int code;
    private long timestamp;

    // 默认构造函数
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    // 成功响应构造函数
    public ApiResponse(T data) {
        this.success = true;
        this.data = data;
        this.code = 200;
        this.message = "操作成功";
        this.timestamp = System.currentTimeMillis();
    }

    // 失败响应构造函数
    public ApiResponse(String message) {
        this.success = false;
        this.message = message;
        this.code = 400;
        this.timestamp = System.currentTimeMillis();
    }

    // 全参数构造函数
    public ApiResponse(boolean success, String message, T data, int code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
        this.timestamp = System.currentTimeMillis();
    }

    // Getter 和 Setter 方法
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", code=" + code +
                ", timestamp=" + timestamp +
                '}';
    }
}
