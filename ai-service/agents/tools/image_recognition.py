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


FOOD_EN_TO_CN = {
    "espresso": "浓缩咖啡", "cup": "杯装饮品", "coffee mug": "咖啡杯",
    "teapot": "茶壶", "beer glass": "啤酒杯", "wine bottle": "红酒",
    "water bottle": "矿泉水", "pop bottle": "汽水", "beer bottle": "啤酒瓶",
    "red wine": "红酒", "eggnog": "蛋酒", "ice cream": "冰淇淋",
    "chocolate sauce": "巧克力酱", "pizza": "披萨", "cheeseburger": "芝士汉堡",
    "hotdog": "热狗", "french loaf": "法棍面包", "pretzel": "椒盐卷饼",
    "bagel": "贝果", "banana": "香蕉", "strawberry": "草莓",
    "orange": "橙子", "lemon": "柠檬", "pineapple": "菠萝",
    "fig": "无花果", "pomegranate": "石榴", "apple": "苹果",
    "granny smith": "青苹果", "custard apple": "番荔枝", "jackfruit": "菠萝蜜",
    "mushroom": "蘑菇", "broccoli": "西兰花", "cauliflower": "花菜",
    "bell pepper": "甜椒", "cucumber": "黄瓜", "head cabbage": "卷心菜",
    "artichoke": "洋蓟", "zucchini": "西葫芦", "acorn squash": "橡果南瓜",
    "butternut squash": "冬南瓜", "spaghetti squash": "金丝瓜",
    "corn": "玉米", "meat loaf": "肉饼", "potpie": "馅饼",
    "burrito": "墨西哥卷", "carbonara": "卡博纳拉意面",
    "guacamole": "牛油果酱", "trifle": "英式蛋糕",
    "ice lolly": "冰棒", "consomme": "清汤",
    "grocery store": "杂货店", "bakery": "面包店",
    "plate": "盘子", "tray": "托盘", "bowl": "碗",
    "dining table": "餐桌", "restaurant": "餐厅",
}

CONFIDENCE_THRESHOLD = 0.15


class ImageRecognitionTool:
    """图像识别工具类"""
    
    def __init__(self):
        """初始化模型"""
        self.device = 0 if torch.cuda.is_available() else -1
        use_fp16 = torch.cuda.is_available()
        logger.info(f"使用设备: {'GPU (FP16)' if use_fp16 else 'CPU'}")
        
        try:
            logger.info(f"从本地加载模型: {settings.HF_IMAGE_MODEL}")
            
            dtype = torch.float16 if use_fp16 else torch.float32
            model = AutoModelForImageClassification.from_pretrained(
                settings.HF_IMAGE_MODEL,
                local_files_only=True,
                torch_dtype=dtype,
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
            logger.info("模型加载成功 (FP16=%s)", use_fp16)
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
            
            formatted_results = []
            for result in results:
                score = float(result['score'])
                if score < CONFIDENCE_THRESHOLD:
                    continue
                en_label = result['label']
                cn_label = FOOD_EN_TO_CN.get(en_label.lower().replace("_", " "), "")
                formatted_results.append({
                    "label": en_label,
                    "label_cn": cn_label,
                    "confidence": score,
                    "confidence_percent": f"{score * 100:.2f}%"
                })
            
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
            'milk', 'water', 'cola', 'soda', 'bottle', 'cup', 'glass',
            'pizza', 'burger', 'hotdog', 'bread', 'cake', 'ice cream',
            'banana', 'apple', 'orange', 'strawberry', 'lemon', 'fruit',
            'broccoli', 'mushroom', 'corn', 'pepper', 'cucumber',
            'meat', 'egg', 'cheese', 'soup', 'salad', 'rice', 'noodle',
            'chocolate', 'cookie', 'pretzel', 'bagel', 'burrito',
            'espresso', 'carbonara', 'guacamole', 'consomme',
            'plate', 'bowl', 'tray', 'restaurant', 'bakery', 'grocery',
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

