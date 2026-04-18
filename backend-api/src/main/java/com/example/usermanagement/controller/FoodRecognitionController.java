package com.example.usermanagement.controller;

import com.example.usermanagement.entity.FoodItem;
import com.example.usermanagement.service.HuggingFaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 食物识别控制器
 * shiWu_shibie_kongZhiQi
 */
@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "*")
public class FoodRecognitionController {

    private final HuggingFaceService huggingFaceService;

    @Autowired
    public FoodRecognitionController(HuggingFaceService huggingFaceService) {
        this.huggingFaceService = huggingFaceService;
    }

    /**
     * 上传图片进行食物识别
     * shangChuan_tuPian_jinXing_shiWu_shibie
     * @param image 上传的图片文件
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public ResponseEntity<FoodItem> recognizeFood(@RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        // 调用HuggingFaceService模拟识别
        FoodItem recognizedFood = huggingFaceService.recognizeFood(image);
        return ResponseEntity.ok(recognizedFood);
    }
}

