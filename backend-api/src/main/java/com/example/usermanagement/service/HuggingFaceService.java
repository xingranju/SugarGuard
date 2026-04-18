package com.example.usermanagement.service;

import com.example.usermanagement.entity.FoodItem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Hugging Face AI模型服务
 * HuggingFace_AI_moXing_fuWu
 */
@Service
public class HuggingFaceService {

    private final List<FoodItem> mockFoodItems = Arrays.asList(
            // 高糖饮品
            createFoodItem(1L, "奶茶", "Bubble Tea", "drink", 45f, 350f, 5f, 15f, 50f, 300f, "unhealthy", 0.92f),
            createFoodItem(2L, "可乐", "Cola", "drink", 39f, 150f, 0f, 0f, 39f, 330f, "unhealthy", 0.95f),
            // 健康食物
            createFoodItem(3L, "苹果", "Apple", "fruit", 10f, 95f, 0.5f, 0.3f, 25f, 182f, "healthy", 0.98f),
            createFoodItem(4L, "香蕉", "Banana", "fruit", 14f, 105f, 1.3f, 0.4f, 27f, 118f, "healthy", 0.97f),
            createFoodItem(5L, "沙拉", "Salad", "dish", 5f, 200f, 10f, 10f, 15f, 250f, "healthy", 0.96f),
            createFoodItem(6L, "鸡胸肉", "Chicken Breast", "meat", 0f, 165f, 31f, 3.6f, 0f, 172f, "healthy", 0.99f),
            // 适量食物
            createFoodItem(7L, "米饭", "Rice", "staple", 0f, 130f, 2.7f, 0.3f, 28f, 100f, "moderate", 0.90f),
            createFoodItem(8L, "饺子", "Dumplings", "staple", 2f, 250f, 8f, 10f, 30f, 150f, "moderate", 0.88f),
            createFoodItem(9L, "面条", "Noodles", "staple", 1f, 160f, 5f, 2f, 30f, 150f, "moderate", 0.89f),
            // 不健康食物
            createFoodItem(10L, "汉堡", "Hamburger", "fast_food", 10f, 300f, 15f, 15f, 30f, 200f, "unhealthy", 0.85f),
            createFoodItem(11L, "披萨", "Pizza", "fast_food", 8f, 266f, 11f, 10f, 33f, 100f, "unhealthy", 0.87f),
            createFoodItem(12L, "炸薯条", "French Fries", "fast_food", 0f, 312f, 3.4f, 15f, 41f, 100f, "unhealthy", 0.86f)
    );

    private final Random random = new Random();

    private static final Map<String, List<String>> KEYWORD_MAP = new HashMap<>();
    static {
        KEYWORD_MAP.put("奶茶", Arrays.asList("bubble", "milk_tea", "boba", "奶茶", "milktea", "naicha"));
        KEYWORD_MAP.put("可乐", Arrays.asList("cola", "coke", "pepsi", "可乐", "kele", "soda"));
        KEYWORD_MAP.put("苹果", Arrays.asList("apple", "苹果", "pingguo", "green_apple", "red_apple", "fruit"));
        KEYWORD_MAP.put("香蕉", Arrays.asList("banana", "香蕉", "xiangjiao"));
        KEYWORD_MAP.put("沙拉", Arrays.asList("salad", "沙拉", "shala", "vegetable"));
        KEYWORD_MAP.put("鸡胸肉", Arrays.asList("chicken", "鸡", "ji", "breast", "meat", "poultry"));
        KEYWORD_MAP.put("米饭", Arrays.asList("rice", "米饭", "mifan", "fan"));
        KEYWORD_MAP.put("饺子", Arrays.asList("dumpling", "饺", "jiaozi", "gyoza"));
        KEYWORD_MAP.put("面条", Arrays.asList("noodle", "面", "mian", "pasta", "ramen", "udon"));
        KEYWORD_MAP.put("汉堡", Arrays.asList("hamburger", "burger", "汉堡", "hanbao"));
        KEYWORD_MAP.put("披萨", Arrays.asList("pizza", "披萨", "pisa"));
        KEYWORD_MAP.put("炸薯条", Arrays.asList("fries", "french_fries", "薯条", "shutiao", "potato"));
    }

    /**
     * 模拟食物识别（基于文件名关键词匹配优先，无匹配时随机）
     * @param imageFile 上传的图片文件
     * @return 模拟的食物识别结果
     */
    public FoodItem recognizeFood(MultipartFile imageFile) {
        try {
            TimeUnit.MILLISECONDS.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String filename = imageFile.getOriginalFilename();
        if (filename != null) {
            String lowerName = filename.toLowerCase().replaceAll("[_\\-\\s.]+", " ");
            for (FoodItem item : mockFoodItems) {
                String cnName = item.getNameCN();
                List<String> keywords = KEYWORD_MAP.getOrDefault(cnName, Collections.emptyList());
                for (String kw : keywords) {
                    if (lowerName.contains(kw.toLowerCase())) {
                        return item;
                    }
                }
            }
        }

        int index = random.nextInt(mockFoodItems.size());
        return mockFoodItems.get(index);
    }

    private FoodItem createFoodItem(Long id, String name_CN, String name_EN, String category,
                                    Float sugar, Float calories, Float protein, Float fat,
                                    Float carbohydrate, Float servingSize, String healthLevel, Float confidence) {
        FoodItem item = new FoodItem();
        item.setId(id);
        item.setNameCN(name_CN);
        item.setNameEN(name_EN);
        item.setCategory(category);
        item.setSugar(sugar);
        item.setCalories(calories);
        item.setProtein(protein);
        item.setFat(fat);
        item.setCarbohydrate(carbohydrate);
        item.setServingSize(servingSize);
        item.setHealthLevel(healthLevel);
        item.setConfidence(confidence);
        return item;
    }
}

