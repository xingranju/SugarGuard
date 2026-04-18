"""
数据库模型定义
"""
from sqlalchemy import Column, Integer, BigInteger, String, Float, DateTime, Boolean, Text, JSON, Date, Time, ForeignKey, Enum as SQLEnum
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
from datetime import datetime
import enum

Base = declarative_base()


class Gender(enum.Enum):
    """性别枚举"""
    MALE = "male"
    FEMALE = "female"
    OTHER = "other"


class DrinkCategory(enum.Enum):
    """饮品类别枚举"""
    MILK_TEA = "奶茶"
    COFFEE = "咖啡"
    JUICE = "果汁"
    TEA = "茶饮"
    OTHER = "其他"


# ==================== 用户相关表 ====================

class UserHealthProfile(Base):
    """用户健康档案表"""
    __tablename__ = 'user_health_profile'
    
    profile_id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, unique=True, nullable=False)  # 关联users表的id (BIGINT)
    age = Column(Integer, nullable=False)
    gender = Column(String(10), nullable=False)  # 'male', 'female', 'other'
    height = Column(Float, nullable=False)  # cm
    weight = Column(Float, nullable=False)  # kg
    health_conditions = Column(Text)  # JSON格式的健康状况
    allergies = Column(String(500))  # 过敏史
    medications = Column(String(500))  # 当前用药
    activity_level = Column(String(20), default='moderate')  # 活动水平
    sugar_limit = Column(Float, default=50.0)  # g/day
    calorie_limit = Column(Float, default=2000.0)  # kcal/day
    water_goal = Column(Float, default=2000.0)  # ml/day
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关系
    daily_records = relationship("DailyHealthRecord", back_populates="user")
    meal_records = relationship("MealRecord", back_populates="user")
    conversations = relationship("ConversationHistory", back_populates="user")
    recommendations = relationship("RecommendationHistory", back_populates="user")
    preferences = relationship("UserDrinkPreference", back_populates="user")


class DailyHealthRecord(Base):
    """每日健康记录表"""
    __tablename__ = 'daily_health_records'
    
    record_id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, ForeignKey('user_health_profile.user_id'), nullable=False)
    record_date = Column(Date, nullable=False)
    total_sugar_intake = Column(Float, default=0.0)  # g
    total_calories = Column(Float, default=0.0)  # kcal
    water_intake = Column(Float, default=0.0)  # ml
    exercise_minutes = Column(Integer, default=0)
    weight = Column(Float)  # kg
    blood_glucose = Column(Float)  # mmol/L (修复:与数据库字段名一致)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # 关系
    user = relationship("UserHealthProfile", back_populates="daily_records")


class MealRecord(Base):
    """饮食记录表"""
    __tablename__ = 'meal_records'
    
    meal_id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, ForeignKey('user_health_profile.user_id'), nullable=False)
    meal_date = Column(Date, nullable=False)
    meal_time = Column(DateTime, nullable=False)
    meal_type = Column(String(20), nullable=False)
    drink_id = Column(Integer)
    food_name = Column(String(200))
    portion_size = Column(Float)
    sugar_content = Column(Float, default=0.0)  # g
    calories = Column(Float, default=0.0)  # kcal
    image_path = Column(String(500))
    notes = Column(Text)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # 关系
    user = relationship("UserHealthProfile", back_populates="meal_records")


# ==================== 饮品相关表 ====================

class Drink(Base):
    """饮品信息表"""
    __tablename__ = 'drinks'
    
    drink_id = Column(Integer, primary_key=True, autoincrement=True)
    drink_name = Column(String(100), nullable=False)
    brand = Column(String(100))
    category = Column(String(50))
    sugar_content = Column(Float, default=0.0, nullable=False)  # g per 100ml
    calories = Column(Float, default=0.0, nullable=False)  # kcal per 100ml
    volume = Column(Float, default=500.0)  # ml
    caffeine = Column(Float, default=0.0)  # mg per 100ml
    fat = Column(Float, default=0.0)  # g per 100ml
    protein = Column(Float, default=0.0)  # g per 100ml
    sodium = Column(Float, default=0.0)  # mg per 100ml
    health_score = Column(Integer, default=50)  # 0-100
    ingredients = Column(Text)
    allergens = Column(String(200))
    image_url = Column(String(500))
    source_url = Column(String(500))
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关系
    preferences = relationship("UserDrinkPreference", back_populates="drink")
    recommendations = relationship("RecommendationHistory", back_populates="drink")


class UserDrinkPreference(Base):
    """用户饮品偏好表"""
    __tablename__ = 'user_drink_preferences'
    
    preference_id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, ForeignKey('user_health_profile.user_id'), nullable=False)
    drink_id = Column(Integer, ForeignKey('drinks.drink_id'), nullable=False)
    preference_score = Column(Integer, default=3)  # 1-5
    last_consumed = Column(DateTime)
    times_consumed = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关系
    user = relationship("UserHealthProfile", back_populates="preferences")
    drink = relationship("Drink", back_populates="preferences")


# ==================== 对话相关表 ====================

class ConversationHistory(Base):
    """对话历史记录表"""
    __tablename__ = 'conversation_history'
    
    conversation_id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, ForeignKey('user_health_profile.user_id'), nullable=False)
    message = Column(Text, nullable=False, comment='用户消息')
    response = Column(Text, nullable=False, comment='AI回复')
    intent = Column(String(50), comment='意图识别')
    context_data = Column(JSON, comment='上下文数据')
    feedback = Column(Integer, comment='用户反馈(1-5)')
    created_at = Column(DateTime, default=datetime.utcnow, comment='创建时间')
    
    # 关系
    user = relationship("UserHealthProfile", back_populates="conversations")


# ==================== 推荐相关表 ====================

class RecommendationHistory(Base):
    """推荐记录表"""
    __tablename__ = 'recommendation_history'
    
    rec_id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, ForeignKey('user_health_profile.user_id'), nullable=False)
    drink_id = Column(Integer, ForeignKey('drinks.drink_id'), nullable=False)
    recommendation_reason = Column(Text)
    was_clicked = Column(Boolean, default=False)
    was_ordered = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    # 关系
    user = relationship("UserHealthProfile", back_populates="recommendations")
    drink = relationship("Drink", back_populates="recommendations")


# ==================== 知识库相关表 ====================

class HealthKnowledge(Base):
    """健康知识库表"""
    __tablename__ = 'health_knowledge'
    
    knowledge_id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(200), nullable=False)
    content = Column(Text, nullable=False)
    category = Column(String(50))
    tags = Column(JSON)
    source_url = Column(String(255))
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

