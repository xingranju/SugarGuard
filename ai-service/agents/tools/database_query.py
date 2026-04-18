"""
数据库查询工具
"""
from sqlalchemy.orm import Session
from database.models import (
    UserHealthProfile, DailyHealthRecord, MealRecord,
    Drink, UserDrinkPreference, ConversationHistory
)
from datetime import datetime, timedelta, date
from typing import Dict, List, Optional
import logging

logger = logging.getLogger(__name__)


class DatabaseQueryTool:
    """数据库查询工具类"""
    
    def __init__(self, db: Session):
        """
        初始化
        
        Args:
            db: 数据库会话
        """
        self.db = db
    
    def get_user_profile(self, user_id: int) -> Optional[Dict]:
        """
        获取用户健康档案
        
        Args:
            user_id: 用户ID
            
        Returns:
            用户档案字典
        """
        try:
            logger.info(f"正在查询用户档案: user_id={user_id}, type={type(user_id)}")
            profile = self.db.query(UserHealthProfile).filter(
                UserHealthProfile.user_id == user_id
            ).first()
            
            if not profile:
                logger.warning(f"用户档案不存在: user_id={user_id}")
                # 尝试列出所有档案
                all_profiles = self.db.query(UserHealthProfile).all()
                logger.info(f"数据库中的所有用户ID: {[p.user_id for p in all_profiles]}")
                return None
            
            return {
                "user_id": profile.user_id,
                "age": profile.age,
                "gender": profile.gender,
                "height": profile.height,
                "weight": profile.weight,
                "health_conditions": profile.health_conditions,
                "allergies": profile.allergies,
                "activity_level": profile.activity_level,
                "sugar_limit": profile.sugar_limit,
                "calorie_limit": profile.calorie_limit,
                "water_goal": profile.water_goal
            }
        except Exception as e:
            logger.error(f"获取用户档案失败: {e}")
            import traceback
            traceback.print_exc()
            return None
    
    def get_daily_health_records(self, user_id: int, days: int = 7) -> List[Dict]:
        """
        获取用户近期健康记录
        
        Args:
            user_id: 用户ID
            days: 天数
            
        Returns:
            健康记录列表
        """
        try:
            start_date = date.today() - timedelta(days=days)
            
            records = self.db.query(DailyHealthRecord).filter(
                DailyHealthRecord.user_id == user_id,
                DailyHealthRecord.record_date >= start_date
            ).order_by(DailyHealthRecord.record_date.desc()).all()
            
            return [
                {
                    "record_date": str(record.record_date),
                    "total_sugar_intake": record.total_sugar_intake,
                    "total_calories": record.total_calories,
                    "water_intake": record.water_intake,
                    "exercise_minutes": record.exercise_minutes,
                    "weight": record.weight,
                    "blood_sugar": record.blood_glucose  # 修复:使用blood_glucose字段,但保持API输出名称为blood_sugar
                }
                for record in records
            ]
        except Exception as e:
            logger.error(f"获取健康记录失败: {e}")
            return []
    
    def get_meal_records(self, user_id: int, days: int = 7) -> List[Dict]:
        """
        获取用户近期饮食记录
        
        Args:
            user_id: 用户ID
            days: 天数
            
        Returns:
            饮食记录列表
        """
        try:
            start_date = date.today() - timedelta(days=days)
            
            records = self.db.query(MealRecord).filter(
                MealRecord.user_id == user_id,
                MealRecord.meal_date >= start_date
            ).order_by(MealRecord.meal_date.desc(), MealRecord.meal_time.desc()).all()
            
            return [
                {
                    "food_name": record.food_name,
                    "meal_date": str(record.meal_date),
                    "meal_time": str(record.meal_time),
                    "meal_type": record.meal_type,
                    "sugar_content": record.sugar_content,
                    "calories": record.calories
                }
                for record in records
            ]
        except Exception as e:
            logger.error(f"获取饮食记录失败: {e}")
            return []
    
    def search_drink_by_name(self, drink_name: str) -> Optional[Dict]:
        """
        根据名称搜索饮品
        
        Args:
            drink_name: 饮品名称
            
        Returns:
            饮品信息字典
        """
        try:
            drink = self.db.query(Drink).filter(
                Drink.drink_name.like(f"%{drink_name}%")
            ).first()
            
            if not drink:
                return None
            
            return {
                "drink_id": drink.drink_id,
                "drink_name": drink.drink_name,
                "brand": drink.brand,
                "category": drink.category,
                "sugar_content": drink.sugar_content,
                "calories": drink.calories,
                "volume": drink.volume,
                "caffeine": drink.caffeine,
                "health_score": drink.health_score,
                "image_url": drink.image_url
            }
        except Exception as e:
            logger.error(f"搜索饮品失败: {e}")
            return None
    
    def get_healthy_drinks(self, max_sugar: float = 50.0, min_health_score: int = 70, limit: int = 20) -> List[Dict]:
        """
        获取健康饮品列表
        
        Args:
            max_sugar: 最大糖分
            min_health_score: 最小健康评分
            limit: 返回数量
            
        Returns:
            饮品列表
        """
        try:
            drinks = self.db.query(Drink).filter(
                Drink.sugar_content <= max_sugar,
                Drink.health_score >= min_health_score
            ).order_by(Drink.health_score.desc()).limit(limit).all()
            
            return [
                {
                    "drink_id": drink.drink_id,
                    "drink_name": drink.drink_name,
                    "brand": drink.brand,
                    "sugar_content": drink.sugar_content,
                    "calories": drink.calories,
                    "volume": drink.volume,
                    "health_score": drink.health_score,
                    "image_url": drink.image_url
                }
                for drink in drinks
            ]
        except Exception as e:
            logger.error(f"获取健康饮品失败: {e}")
            return []
    
    def save_meal_record(self, user_id: int, drink_name: str, sugar_content: float, calories: float, image_path: Optional[str] = None) -> bool:
        """
        保存饮食记录
        
        Args:
            user_id: 用户ID
            drink_name: 饮品名称
            sugar_content: 糖分
            calories: 热量
            image_path: 图片路径
            
        Returns:
            是否成功
        """
        try:
            now = datetime.now()
            meal_record = MealRecord(
                user_id=user_id,
                meal_date=now.date(),
                meal_time=now,
                meal_type="snack",
                food_name=drink_name,
                sugar_content=sugar_content,
                calories=calories,
                image_path=image_path
            )
            
            self.db.add(meal_record)
            self.db.commit()
            
            logger.info(f"保存饮食记录成功: user_id={user_id}, drink={drink_name}")
            return True
        except Exception as e:
            logger.error(f"保存饮食记录失败: {e}")
            self.db.rollback()
            return False
    
    def save_conversation(self, user_id: int, user_message: str, bot_response: str, intent: str, context_data: dict = None) -> bool:
        """
        保存对话记录
        
        Args:
            user_id: 用户ID
            user_message: 用户消息
            bot_response: 机器人回复
            intent: 意图
            context_data: 上下文数据
            
        Returns:
            是否成功
        """
        try:
            conversation = ConversationHistory(
                user_id=user_id,
                message=user_message,
                response=bot_response,
                intent=intent,
                context_data=context_data
            )
            
            self.db.add(conversation)
            self.db.commit()
            
            logger.info(f"保存对话记录成功: user_id={user_id}")
            return True
        except Exception as e:
            logger.error(f"保存对话记录失败: {e}")
            self.db.rollback()
            return False
    
    def get_today_health_record(self, user_id: int) -> Optional[Dict]:
        """
        获取用户今日健康记录
        
        Args:
            user_id: 用户ID
            
        Returns:
            今日健康记录
        """
        try:
            today = datetime.now().date()
            record = self.db.query(DailyHealthRecord).filter(
                DailyHealthRecord.user_id == user_id,
                DailyHealthRecord.record_date == today
            ).first()
            
            if record:
                return {
                    "record_id": record.record_id,
                    "user_id": record.user_id,
                    "record_date": record.record_date.isoformat(),
                    "total_sugar_intake": record.total_sugar_intake,
                    "total_calories": record.total_calories,
                    "water_intake": record.water_intake,
                    "exercise_minutes": record.exercise_minutes,
                    "weight": record.weight,
                    "blood_sugar": record.blood_glucose  # 使用blood_glucose字段
                }
            return None
        except Exception as e:
            logger.error(f"获取今日健康记录失败: {e}")
            return None
    
    def get_recent_conversations(self, user_id: int, limit: int = 5) -> List[Dict]:
        """
        获取用户最近对话记录
        
        Args:
            user_id: 用户ID
            limit: 限制条数
            
        Returns:
            对话记录列表
        """
        try:
            conversations = self.db.query(ConversationHistory).filter(
                ConversationHistory.user_id == user_id
            ).order_by(ConversationHistory.created_at.desc()).limit(limit).all()
            
            result = []
            for conv in reversed(conversations):  # 反转以保持时间顺序
                result.append({
                    "conversation_id": conv.conversation_id,
                    "user_message": conv.message,
                    "bot_response": conv.response,
                    "intent": conv.intent,
                    "feedback": conv.feedback,
                    "timestamp": conv.created_at.isoformat()
                })
            
            return result
        except Exception as e:
            logger.error(f"获取对话历史失败: {e}")
            return []
    
    def get_all_drinks(self) -> List[Dict]:
        """
        获取所有饮品
        
        Returns:
            饮品列表
        """
        try:
            drinks = self.db.query(Drink).all()
            result = []
            for drink in drinks:
                result.append({
                    "drink_id": drink.drink_id,
                    "drink_name": drink.drink_name,
                    "brand": drink.brand,
                    "category": drink.category,
                    "sugar_content": drink.sugar_content,
                    "calories": drink.calories,
                    "volume": drink.volume,
                    "caffeine": drink.caffeine,
                    "health_score": drink.health_score,
                    "image_url": drink.image_url
                })
            return result
        except Exception as e:
            logger.error(f"获取所有饮品失败: {e}")
            return []
    
    def get_drink_by_id(self, drink_id: int) -> Optional[Dict]:
        """
        根据ID获取饮品信息
        
        Args:
            drink_id: 饮品ID
            
        Returns:
            饮品信息
        """
        try:
            drink = self.db.query(Drink).filter(Drink.drink_id == drink_id).first()
            if drink:
                return {
                    "drink_id": drink.drink_id,
                    "drink_name": drink.drink_name,
                    "brand": drink.brand,
                    "category": drink.category,
                    "sugar_content": drink.sugar_content,
                    "calories": drink.calories,
                    "volume": drink.volume,
                    "caffeine": drink.caffeine,
                    "health_score": drink.health_score,
                    "image_url": drink.image_url
                }
            return None
        except Exception as e:
            logger.error(f"获取饮品失败: {e}")
            return None
    
    def get_user_preferences(self, user_id: int) -> List[Dict]:
        """
        获取用户饮品偏好
        
        Args:
            user_id: 用户ID
            
        Returns:
            偏好列表
        """
        try:
            preferences = self.db.query(UserDrinkPreference).filter(
                UserDrinkPreference.user_id == user_id
            ).all()
            
            result = []
            for pref in preferences:
                result.append({
                    "preference_id": pref.preference_id,
                    "user_id": pref.user_id,
                    "drink_id": pref.drink_id,
                    "preference_score": pref.preference_score,
                    "times_consumed": pref.times_consumed,
                    "last_consumed": pref.last_consumed.isoformat() if pref.last_consumed else None
                })
            return result
        except Exception as e:
            logger.error(f"获取用户偏好失败: {e}")
            return []
    
    def get_all_user_preferences(self) -> List[Dict]:
        """
        获取所有用户偏好(用于协同过滤)
        
        Returns:
            所有偏好列表
        """
        try:
            preferences = self.db.query(UserDrinkPreference).all()
            
            result = []
            for pref in preferences:
                result.append({
                    "user_id": pref.user_id,
                    "drink_id": pref.drink_id,
                    "preference_score": pref.preference_score,
                    "times_consumed": pref.times_consumed
                })
            return result
        except Exception as e:
            logger.error(f"获取所有用户偏好失败: {e}")
            return []

