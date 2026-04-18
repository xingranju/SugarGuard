"""
健康评估工具测试
"""
import pytest
import sys
import os

# 添加项目路径
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from agents.tools.health_assessment import HealthAssessmentTool


class TestHealthAssessmentTool:
    """健康评估工具测试类"""
    
    @pytest.fixture
    def health_tool(self):
        """创建健康评估工具实例"""
        return HealthAssessmentTool()
    
    def test_calculate_bmi_normal(self, health_tool):
        """测试BMI计算 - 正常体重"""
        result = health_tool.calculate_bmi(weight=65, height=170)
        
        assert result["bmi"] == pytest.approx(22.49, rel=0.01)
        assert result["category"] == "正常"
        assert result["health_status"] == "healthy"
    
    def test_calculate_bmi_underweight(self, health_tool):
        """测试BMI计算 - 偏瘦"""
        result = health_tool.calculate_bmi(weight=50, height=170)
        
        assert result["bmi"] == pytest.approx(17.30, rel=0.01)
        assert result["category"] == "偏瘦"
        assert result["health_status"] == "underweight"
    
    def test_calculate_bmi_overweight(self, health_tool):
        """测试BMI计算 - 超重"""
        result = health_tool.calculate_bmi(weight=75, height=170)
        
        assert result["bmi"] == pytest.approx(25.95, rel=0.01)
        assert result["category"] == "超重"
        assert result["health_status"] == "overweight"
    
    def test_calculate_bmi_obese(self, health_tool):
        """测试BMI计算 - 肥胖"""
        result = health_tool.calculate_bmi(weight=90, height=170)
        
        assert result["bmi"] == pytest.approx(31.14, rel=0.01)
        assert result["category"] == "肥胖"
        assert result["health_status"] == "obese"
    
    def test_assess_sugar_intake_normal(self, health_tool):
        """测试糖分摄入评估 - 正常"""
        daily_intake = [30, 35, 40, 32, 38]
        result = health_tool.assess_sugar_intake(daily_intake, limit=50)
        
        assert result["average_intake"] == 35.0
        assert result["limit"] == 50
        assert result["assessment"] == "良好"
    
    def test_assess_sugar_intake_exceeded(self, health_tool):
        """测试糖分摄入评估 - 超标"""
        daily_intake = [60, 65, 70, 55, 68]
        result = health_tool.assess_sugar_intake(daily_intake, limit=50)
        
        assert result["average_intake"] == 63.6
        assert result["limit"] == 50
        assert result["assessment"] == "超标"
    
    def test_calculate_daily_needs_male(self, health_tool):
        """测试每日营养需求计算 - 男性"""
        result = health_tool.calculate_daily_needs(
            age=18,
            gender="male",
            weight=70,
            height=175
        )
        
        assert "calories" in result
        assert "protein" in result
        assert "carbs" in result
        assert "fat" in result
        assert "sugar_limit" in result
        assert result["calories"] > 2000
    
    def test_calculate_daily_needs_female(self, health_tool):
        """测试每日营养需求计算 - 女性"""
        result = health_tool.calculate_daily_needs(
            age=18,
            gender="female",
            weight=55,
            height=165
        )
        
        assert "calories" in result
        assert result["calories"] > 1500
        assert result["sugar_limit"] == 50
    
    def test_assess_drink_health_impact_low_sugar(self, health_tool):
        """测试饮品健康影响评估 - 低糖"""
        result = health_tool.assess_drink_health_impact(
            sugar_content=5,
            calories=30,
            caffeine=25
        )
        
        assert result["health_level"] == "健康"
        assert "低糖" in result["health_advice"]
    
    def test_assess_drink_health_impact_high_sugar(self, health_tool):
        """测试饮品健康影响评估 - 高糖"""
        result = health_tool.assess_drink_health_impact(
            sugar_content=50,
            calories=400,
            caffeine=30
        )
        
        assert result["health_level"] == "不健康"
        assert "高糖" in result["health_advice"] or "超标" in result["health_advice"]
    
    def test_detect_risk_behavior_no_risk(self, health_tool):
        """测试风险行为检测 - 无风险"""
        result = health_tool.detect_risk_behavior("我想喝点水")
        
        assert result["has_risk"] == False
    
    def test_detect_risk_behavior_with_risk(self, health_tool):
        """测试风险行为检测 - 有风险"""
        result = health_tool.detect_risk_behavior("我今天喝了5杯奶茶")
        
        # 可能检测到风险
        assert "has_risk" in result
        assert "warning" in result


if __name__ == "__main__":
    pytest.main([__file__, "-v"])

