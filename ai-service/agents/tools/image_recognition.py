"""
图像识别工具 - 基于Hugging Face ViT模型
"""
from transformers import pipeline, AutoImageProcessor, AutoModelForImageClassification
from PIL import Image
import torch
from typing import Dict, List
import logging
import os
from config.settings import settings

logger = logging.getLogger(__name__)

# 设置环境变量,禁用自动下载
os.environ['TRANSFORMERS_OFFLINE'] = '1'
os.environ['HF_HUB_OFFLINE'] = '1'


class ImageRecognitionTool:
    """图像识别工具类"""
    
    def __init__(self):
        """初始化模型"""
        self.device = 0 if torch.cuda.is_available() else -1
        logger.info(f"使用设备: {'GPU' if self.device == 0 else 'CPU'}")
        
        try:
            # 加载图像分类模型（从本地路径）
            logger.info(f"从本地加载模型: {settings.HF_IMAGE_MODEL}")
            
            # 使用AutoModel方式加载,确保使用本地文件
            model = AutoModelForImageClassification.from_pretrained(
                settings.HF_IMAGE_MODEL,
                local_files_only=True
            )
            processor = AutoImageProcessor.from_pretrained(
                settings.HF_IMAGE_MODEL,
                local_files_only=True
            )
            
            self.classifier = pipeline(
                "image-classification",
                model=model,
                feature_extractor=processor,
                device=self.device
            )
            logger.info(f"成功加载本地模型")
        except Exception as e:
            logger.error(f"模型加载失败: {e}")
            raise
    
    def preprocess_image(self, image: Image.Image) -> Image.Image:
        """
        图像预处理
        
        Args:
            image: PIL图像对象
            
        Returns:
            处理后的图像
        """
        try:
            # 转换为RGB模式
            if image.mode != 'RGB':
                image = image.convert('RGB')
            
            # 调整大小
            image = image.resize((224, 224))
            
            return image
        except Exception as e:
            logger.error(f"图像预处理失败: {e}")
            raise
    
    def recognize(self, image: Image.Image, top_k: int = 3) -> List[Dict]:
        """
        识别图像中的物体
        
        Args:
            image: PIL图像对象
            top_k: 返回前k个结果
            
        Returns:
            识别结果列表，每个结果包含label和score
        """
        try:
            # 预处理图像
            processed_image = self.preprocess_image(image)
            
            # 执行识别
            results = self.classifier(processed_image, top_k=top_k)
            
            # 格式化结果
            formatted_results = [
                {
                    "label": result['label'],
                    "confidence": float(result['score']),
                    "confidence_percent": f"{result['score'] * 100:.2f}%"
                }
                for result in results
            ]
            
            logger.info(f"识别成功，Top-1: {formatted_results[0]['label']} ({formatted_results[0]['confidence_percent']})")
            
            return formatted_results
            
        except Exception as e:
            logger.error(f"图像识别失败: {e}")
            raise
    
    def is_food_item(self, label: str) -> bool:
        """
        判断识别结果是否为食物/饮品
        
        Args:
            label: 识别标签
            
        Returns:
            是否为食物/饮品
        """
        food_keywords = [
            'food', 'drink', 'beverage', 'tea', 'coffee', 'juice', 
            'milk', 'water', 'cola', 'soda', 'bottle', 'cup', 'glass'
        ]
        
        label_lower = label.lower()
        return any(keyword in label_lower for keyword in food_keywords)


# 全局工具实例
_image_recognition_tool = None


def get_image_recognition_tool() -> ImageRecognitionTool:
    """获取图像识别工具单例"""
    global _image_recognition_tool
    if _image_recognition_tool is None:
        _image_recognition_tool = ImageRecognitionTool()
    return _image_recognition_tool

