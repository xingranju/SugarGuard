"""
推荐系统测试
"""
import pytest
import sys
import os
from unittest.mock import Mock, MagicMock

# 添加项目路径
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from agents.recommendation import DrinkRecommendationSystem


class TestRecommendationSystem:
    """推荐系统测试类"""
    
    @pytest.fixture
    def mock_db_tool(self):
        """创建模拟的数据库工具"""
        db_tool = Mock()
        
        # 模拟用户档案
        db_tool.get_user_profile.return_value = {
            "user_id": 1,
            "age": 18,
            "gender": "male",
            "height": 175,
            "weight": 70,
            "sugar_limit": 50,
            "has_diabetes": False
        }
        
        # 模拟今日健康记录
        db_tool.get_today_health_record.return_value = {
            "total_sugar_intake": 20
        }
        
        # 模拟饮品列表
        db_tool.get_all_drinks.return_value = [
            {
                "drink_id": 1,
                "drink_name": "纯净水",
                "brand": "农夫山泉",
                "category": "其他",
                "sugar_content": 0,
                "calories": 0,
                "caffeine": 0,
                "health_score": 100
            },
            {
                "drink_id": 2,
                "drink_name": "绿茶",
                "brand": "立顿",
                "category": "茶饮",
                "sugar_content": 0,
                "calories": 2,
                "caffeine": 25,
                "health_score": 95
            },
            {
                "drink_id": 3,
                "drink_name": "珍珠奶茶",
                "brand": "喜茶",
                "category": "奶茶",
                "sugar_content": 35,
                "calories": 350,
                "caffeine": 30,
                "health_score": 30
            }
        ]
        
        # 模拟用户偏好
        db_tool.get_user_preferences.return_value = [
            {"drink_id": 2, "rating": 5, "click_count": 10, "order_count": 5}
        ]
        
        # 模拟所有用户偏好
        db_tool.get_all_user_preferences.return_value = [
            {"user_id": 1, "drink_id": 2, "rating": 5, "click_count": 10, "order_count": 5},
            {"user_id": 2, "drink_id": 1, "rating": 4, "click_count": 8, "order_count": 3},
            {"user_id": 2, "drink_id": 2, "rating": 5, "click_count": 12, "order_count": 6}
        ]
        
        # 模拟根据ID获取饮品
        def mock_get_drink_by_id(drink_id):
            drinks = {
                1: {
                    "drink_id": 1,
                    "drink_name": "纯净水",
                    "brand": "农夫山泉",
                    "category": "其他",
                    "sugar_content": 0,
                    "calories": 0,
                    "caffeine": 0,
                    "health_score": 100
                },
                2: {
                    "drink_id": 2,
                    "drink_name": "绿茶",
                    "brand": "立顿",
                    "category": "茶饮",
                    "sugar_content": 0,
                    "calories": 2,
                    "caffeine": 25,
                    "health_score": 95
                }
            }
            return drinks.get(drink_id)
        
        db_tool.get_drink_by_id.side_effect = mock_get_drink_by_id
        
        return db_tool
    
    @pytest.fixture
    def rec_system(self, mock_db_tool):
        """创建推荐系统实例"""
        return DrinkRecommendationSystem(mock_db_tool)
    
    def test_recommend_healthy_drinks(self, rec_system):
        """测试健康推荐"""
        recommendations = rec_system.recommend_healthy_drinks(user_id=1, limit=5)
        
        assert len(recommendations) > 0
        # 确保推荐的饮品糖分不超过剩余额度
        for drink in recommendations:
            assert drink["sugar_content"] <= 30  # 剩余额度 = 50 - 20 = 30
    
    def test_recommend_collaborative(self, rec_system):
        """测试协同过滤推荐"""
        recommendations = rec_system.recommend_collaborative(user_id=1, limit=5)
        
        # 应该返回推荐结果(可能是健康推荐作为回退)
        assert isinstance(recommendations, list)
    
    def test_recommend_mixed(self, rec_system):
        """测试混合推荐"""
        recommendations = rec_system.recommend_mixed(user_id=1, limit=5)
        
        assert len(recommendations) > 0
        # 检查推荐类型标记
        for drink in recommendations:
            assert "recommendation_type" in drink or "reason" in drink
    
    def test_calculate_health_score(self, rec_system, mock_db_tool):
        """测试健康评分计算"""
        drink = {
            "drink_id": 1,
            "drink_name": "纯净水",
            "sugar_content": 0,
            "calories": 0,
            "health_score": 100
        }
        
        user_profile = mock_db_tool.get_user_profile(1)
        score = rec_system._calculate_health_score(drink, user_profile, max_sugar=30)
        
        # 纯净水应该得到高分
        assert score > 80
    
    def test_calculate_health_score_exceeds_sugar(self, rec_system, mock_db_tool):
        """测试健康评分计算 - 糖分超标"""
        drink = {
            "drink_id": 3,
            "drink_name": "珍珠奶茶",
            "sugar_content": 35,
            "calories": 350,
            "health_score": 30
        }
        
        user_profile = mock_db_tool.get_user_profile(1)
        score = rec_system._calculate_health_score(drink, user_profile, max_sugar=10)
        
        # 糖分超标应该得0分
        assert score == 0


if __name__ == "__main__":
    pytest.main([__file__, "-v"])

