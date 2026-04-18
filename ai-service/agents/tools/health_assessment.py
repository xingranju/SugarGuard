"""
健康评估工具
"""
from typing import Dict, List
import logging

logger = logging.getLogger(__name__)


class HealthAssessmentTool:
    """健康评估工具类"""
    
    def calculate_bmi(self, weight: float, height: float) -> Dict:
        """
        计算BMI指数
        
        Args:
            weight: 体重(kg)
            height: 身高(cm)
            
        Returns:
            BMI评估结果
        """
        try:
            # 计算BMI
            height_m = height / 100
            bmi = weight / (height_m ** 2)
            
            # 判断状态
            if bmi < 18.5:
                status = "偏瘦"
                category = "underweight"
            elif 18.5 <= bmi < 24:
                status = "正常"
                category = "normal"
            elif 24 <= bmi < 28:
                status = "超重"
                category = "overweight"
            else:
                status = "肥胖"
                category = "obese"
            
            return {
                "bmi": round(bmi, 2),
                "status": status,
                "category": category
            }
        except Exception as e:
            logger.error(f"BMI计算失败: {e}")
            return {"error": str(e)}
    
    def assess_sugar_intake(self, daily_sugar_list: List[float], sugar_limit: float = 50.0) -> Dict:
        """
        评估糖分摄入
        
        Args:
            daily_sugar_list: 每日糖分摄入列表
            sugar_limit: 糖分限制(g/day)
            
        Returns:
            糖分评估结果
        """
        try:
            if not daily_sugar_list:
                return {
                    "average_daily_sugar": 0,
                    "risk_level": "未知",
                    "recommendation": "暂无数据"
                }
            
            # 计算平均值
            avg_sugar = sum(daily_sugar_list) / len(daily_sugar_list)
            
            # 判断风险等级
            if avg_sugar <= sugar_limit:
                risk_level = "低风险"
                color = "green"
            elif sugar_limit < avg_sugar <= sugar_limit * 1.5:
                risk_level = "中等风险"
                color = "orange"
            else:
                risk_level = "高风险"
                color = "red"
            
            # 计算超标百分比
            exceed_percentage = ((avg_sugar - sugar_limit) / sugar_limit * 100) if avg_sugar > sugar_limit else 0
            
            return {
                "average_daily_sugar": round(avg_sugar, 2),
                "sugar_limit": sugar_limit,
                "risk_level": risk_level,
                "color": color,
                "exceed_percentage": round(exceed_percentage, 1),
                "days_analyzed": len(daily_sugar_list),
                "max_sugar": round(max(daily_sugar_list), 2) if daily_sugar_list else 0,
                "min_sugar": round(min(daily_sugar_list), 2) if daily_sugar_list else 0
            }
        except Exception as e:
            logger.error(f"糖分评估失败: {e}")
            return {"error": str(e)}
    
    def calculate_daily_needs(self, age: int, gender: str, weight: float, height: float, activity_level: str = "moderate") -> Dict:
        """
        计算每日营养需求
        
        Args:
            age: 年龄
            gender: 性别 (male/female)
            weight: 体重(kg)
            height: 身高(cm)
            activity_level: 活动水平 (sedentary/light/moderate/active)
            
        Returns:
            每日营养需求
        """
        try:
            # 基础代谢率 (Harris-Benedict公式)
            if gender.lower() == "male":
                bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
            else:
                bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
            
            # 活动系数
            activity_multipliers = {
                "sedentary": 1.2,
                "light": 1.375,
                "moderate": 1.55,
                "active": 1.725,
                "very_active": 1.9
            }
            
            multiplier = activity_multipliers.get(activity_level.lower(), 1.55)
            daily_calories = bmr * multiplier
            
            # WHO建议：糖分摄入不超过总热量的10%
            sugar_limit = (daily_calories * 0.1) / 4  # 1g糖=4卡路里
            
            # 蛋白质需求：1.2g/kg体重（青少年）
            protein_need = weight * 1.2
            
            # 脂肪限制：30%热量来自脂肪，1g脂肪=9卡路里
            fat_limit = (daily_calories * 0.3) / 9
            
            return {
                "daily_calories": round(daily_calories),
                "sugar_limit": round(sugar_limit),
                "protein_need": round(protein_need),
                "fat_limit": round(fat_limit),
                "bmr": round(bmr)
            }
        except Exception as e:
            logger.error(f"营养需求计算失败: {e}")
            return {"error": str(e)}
    
    def assess_drink_health_impact(self, sugar_content: float, calories: float, caffeine: float = 0) -> Dict:
        """
        评估单个饮品的健康影响
        
        Args:
            sugar_content: 糖分(g)
            calories: 热量(kcal)
            caffeine: 咖啡因(mg)
            
        Returns:
            健康影响评估
        """
        try:
            # 糖分评估
            if sugar_content > 25:
                sugar_level = "high"
            elif sugar_content > 12:
                sugar_level = "medium"
            else:
                sugar_level = "low"
            
            # 热量评估
            if calories > 200:
                calorie_level = "high"
            elif calories > 100:
                calorie_level = "medium"
            else:
                calorie_level = "low"
            
            # 咖啡因评估
            if caffeine > 100:
                caffeine_level = "high"
            elif caffeine > 50:
                caffeine_level = "medium"
            else:
                caffeine_level = "low"
            
            return {
                "sugar_level": sugar_level,
                "calorie_level": calorie_level,
                "caffeine_level": caffeine_level if caffeine > 0 else "none",
                "sugar_content": sugar_content,
                "calories": calories,
                "caffeine": caffeine
            }
        except Exception as e:
            logger.error(f"健康影响评估失败: {e}")
            return {"error": str(e)}
    
    def detect_risk_behavior(self, text: str) -> Dict:
        """
        检测高风险行为
        
        Args:
            text: 用户输入文本
            
        Returns:
            风险检测结果
        """
        try:
            risk_keywords = {
                "高糖摄入": ["每天都喝奶茶", "一天好几杯", "不喝饮料难受", "离不开奶茶"],
                "节食": ["不吃饭", "绝食", "只喝水不吃东西", "饿肚子"],
                "暴饮暴食": ["吃很多甜食", "控制不住", "报复性进食", "狂吃"]
            }
            
            detected_risks = []
            for risk_type, keywords in risk_keywords.items():
                if any(keyword in text for keyword in keywords):
                    detected_risks.append(risk_type)
            
            if detected_risks:
                return {
                    "has_risk": True,
                    "risk_types": detected_risks,
                    "warning": "⚠️ 检测到潜在健康风险行为，建议调整饮食习惯，必要时咨询专业人士"
                }
            else:
                return {
                    "has_risk": False,
                    "risk_types": [],
                    "warning": None
                }
        except Exception as e:
            logger.error(f"风险行为检测失败: {e}")
            return {"error": str(e)}

