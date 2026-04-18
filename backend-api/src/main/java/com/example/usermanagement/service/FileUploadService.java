package com.example.usermanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Service
public class FileUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${file.upload.avatar-dir:uploads/avatars}")
    private String avatarDir;
    
    @Value("${file.upload.max-size:10485760}") // 默认10MB
    private Long maxFileSize;
    
    /**
     * 上传头像文件
     */
    public String uploadAvatar(MultipartFile file) throws IOException {
        logger.info("开始上传头像, 文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        
        // 验证文件
        validateFile(file);
        
        // 确保上传目录存在
        Path uploadPath = Paths.get(avatarDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("创建头像上传目录: {}", avatarDir);
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // 保存文件
        Path targetPath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回相对路径（用于数据库存储和访问）
        String relativePath = "/uploads/avatars/" + newFilename;
        logger.info("头像上传成功, 路径: {}", relativePath);
        
        return relativePath;
    }
    
    /**
     * 上传通用文件
     */
    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        logger.info("开始上传文件, 文件名: {}, 子目录: {}", file.getOriginalFilename(), subDir);
        
        // 验证文件
        validateFile(file);
        
        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir, subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("创建上传目录: {}", uploadPath);
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        // 保存文件
        Path targetPath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 返回相对路径
        String relativePath = "/uploads/" + subDir + "/" + newFilename;
        logger.info("文件上传成功, 路径: {}", relativePath);
        
        return relativePath;
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型，只支持图片格式（jpg, jpeg, png, gif）");
        }
    }
    
    /**
     * 验证图片类型
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif");
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()) {
                // 移除开头的 "/"
                String cleanPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
                Path path = Paths.get(cleanPath);
                
                if (Files.exists(path)) {
                    Files.delete(path);
                    logger.info("文件删除成功: {}", filePath);
                }
            }
        } catch (IOException e) {
            logger.error("文件删除失败: {}", filePath, e);
        }
    }
}

