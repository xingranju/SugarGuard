package com.example.myapplication.util

import android.content.Context
import java.io.File

/**
 * 饮品图片工具类
 * 用于从本地images目录匹配饮品图片
 */
object DrinkImageUtil {
    // 本地图片目录路径
    private const val IMAGES_DIR = "E:\\code\\Anroid\\MyApplication\\images"
    
    /**
     * 根据饮品名称查找匹配的本地图片路径
     * @param drinkName 饮品名称
     * @return 完整的图片文件路径，如果未找到则返回null
     */
    fun getLocalImagePath(drinkName: String?): String? {
        if (drinkName.isNullOrBlank()) return null
        
        val imagesDir = File(IMAGES_DIR)
        if (!imagesDir.exists() || !imagesDir.isDirectory) {
            return null
        }
        
        // 清理饮品名称（去除特殊字符和空格）
        val cleanName = drinkName.trim()
        
        // 遍历images目录下的所有文件
        val imageFiles = imagesDir.listFiles { file ->
            file.isFile && file.extension.lowercase() in listOf("webp", "jpg", "jpeg", "png")
        } ?: return null
        
        // 1. 首先尝试精确匹配（不包含扩展名）
        val exactMatch = imageFiles.find { file ->
            file.nameWithoutExtension.equals(cleanName, ignoreCase = true)
        }
        if (exactMatch != null) {
            return exactMatch.absolutePath
        }
        
        // 2. 尝试部分匹配（饮品名称包含在文件名中）
        val partialMatch = imageFiles.find { file ->
            file.nameWithoutExtension.contains(cleanName, ignoreCase = true)
        }
        if (partialMatch != null) {
            return partialMatch.absolutePath
        }
        
        // 3. 尝试反向匹配（文件名包含在饮品名称中）
        val reverseMatch = imageFiles.find { file ->
            cleanName.contains(file.nameWithoutExtension, ignoreCase = true)
        }
        if (reverseMatch != null) {
            return reverseMatch.absolutePath
        }
        
        // 4. 如果都没有匹配，返回null
        return null
    }
    
    /**
     * 获取图片URI，用于Coil加载
     * @param drinkName 饮品名称
     * @return 图片URI字符串
     */
    fun getImageUri(drinkName: String?): String? {
        val localPath = getLocalImagePath(drinkName) ?: return null
        // 将Windows路径转换为file:// URI
        return "file:///$localPath"
    }
    
    /**
     * 检查本地图片是否存在
     * @param drinkName 饮品名称
     * @return true如果图片存在，否则false
     */
    fun hasLocalImage(drinkName: String?): Boolean {
        return getLocalImagePath(drinkName) != null
    }
    
    /**
     * 获取所有可用的饮品图片名称（不包含扩展名）
     * @return 图片名称列表
     */
    fun getAllAvailableImageNames(): List<String> {
        val imagesDir = File(IMAGES_DIR)
        if (!imagesDir.exists() || !imagesDir.isDirectory) {
            return emptyList()
        }
        
        return imagesDir.listFiles { file ->
            file.isFile && file.extension.lowercase() in listOf("webp", "jpg", "jpeg", "png")
        }?.map { it.nameWithoutExtension } ?: emptyList()
    }
}

