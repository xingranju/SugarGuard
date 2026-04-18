"""
饮品推荐系统
实现基于内容的推荐和协同过滤推荐算法
"""
from typing import List, Dict, Optional, Tuple
import numpy as np
from collections import defaultdict
import logging

logger = logging.getLogger(__name__)


class DrinkRecommendationSystem:
    """饮品推荐系统"""
    
    def __init__(self, db_tool):
        """
        初始化推荐系统
        
        Args:
            db_tool: 数据库查询工具
        """
        self.db_tool = db_tool
        logger.info("饮品推荐系统初始化完成")
    
    
    def recommend_healthy_drinks(
        self, 
        user_id: int, 
        limit: int = 5,
        max_sugar: Optional[float] = None
    ) -> List[Dict]:
        """
        基于健康的饮品推荐
        
        Args:
            user_id: 用户ID
            limit: 推荐数量
            max_sugar: 最大糖分限制
            
        Returns:
            推荐饮品列表
        """
        try:
            # 获取用户档案
            profile = self.db_tool.get_user_profile(user_id)
            
            if not profile:
                # 如果没有档案,推荐最健康的饮品
                return self._recommend_top_healthy_drinks(limit)
            
            # 获取用户今日摄入
            today_record = self.db_tool.get_today_health_record(user_id)
            today_sugar = today_record.get("total_sugar_intake", 0) if today_record else 0
            
            # 计算剩余糖分额度
            sugar_limit = profile.get("sugar_limit", 50)
            remaining_sugar = max(0, sugar_limit - today_sugar)
            
            if max_sugar is None:
                max_sugar = remaining_sugar
            else:
                max_sugar = min(max_sugar, remaining_sugar)
            
            # 获取所有饮品
            all_drinks = self.db_tool.get_all_drinks()
            
            # 筛选和评分
            scored_drinks = []
            for drink in all_drinks:
                score = self._calculate_health_score(
                    drink=drink,
                    user_profile=profile,
                    max_sugar=max_sugar
                )
                
                if score > 0:  # 只保留符合条件的饮品
                    scored_drinks.append({
                        **drink,
                        "recommendation_score": score,
                        "reason": self._generate_recommendation_reason(drink, profile, "健康")
                    })
            
            # 按评分排序
            scored_drinks.sort(key=lambda x: x["recommendation_score"], reverse=True)
            
            logger.info(f"为用户 {user_id} 生成 {len(scored_drinks[:limit])} 个健康推荐")
            return scored_drinks[:limit]
            
        except Exception as e:
            logger.error(f"健康推荐失败: {e}")
            return []
    
    
    def recommend_collaborative(
        self,
        user_id: int,
        limit: int = 5
    ) -> List[Dict]:
        """
        基于协同过滤的推荐
        
        Args:
            user_id: 用户ID
            limit: 推荐数量
            
        Returns:
            推荐饮品列表
        """
        try:
            # 获取用户偏好
            user_preferences = self.db_tool.get_user_preferences(user_id)
            
            if not user_preferences:
                logger.info(f"用户 {user_id} 无偏好数据,回退到健康推荐")
                return self.recommend_healthy_drinks(user_id, limit)
            
            # 获取所有用户偏好(用于协同过滤)
            all_preferences = self.db_tool.get_all_user_preferences()
            
            # 找到相似用户
            similar_users = self._find_similar_users(
                user_id=user_id,
                user_prefs=user_preferences,
                all_prefs=all_preferences,
                top_k=10
            )
            
            # 获取相似用户喜欢但当前用户未尝试的饮品
            recommendations = self._get_collaborative_recommendations(
                user_id=user_id,
                similar_users=similar_users,
                user_prefs=user_preferences,
                limit=limit
            )
            
            logger.info(f"为用户 {user_id} 生成 {len(recommendations)} 个协同过滤推荐")
            return recommendations
            
        except Exception as e:
            logger.error(f"协同过滤推荐失败: {e}")
            return []
    
    
    def recommend_mixed(
        self,
        user_id: int,
        limit: int = 50
    ) -> List[Dict]:
        """
        混合推荐策略 - 优先考虑用户饮品偏好，结合健康档案和健康记录
        
        Args:
            user_id: 用户ID
            limit: 推荐数量（默认50）
            
        Returns:
            推荐饮品列表（按综合评分降序排序）
        """
        try:
            # 获取用户数据
            user_profile = self.db_tool.get_user_profile(user_id)
            user_preferences = self.db_tool.get_user_preferences(user_id)
            today_record = self.db_tool.get_today_health_record(user_id)
            recent_records = self.db_tool.get_daily_health_records(user_id, days=7)
            
            # 获取所有饮品
            all_drinks = self.db_tool.get_all_drinks()
            
            # 构建用户偏好字典（方便快速查找）
            preference_dict = {
                pref["drink_id"]: {
                    "preference_score": pref.get("preference_score", 3),
                    "times_consumed": pref.get("times_consumed", 0)
                }
                for pref in user_preferences
            }
            
            # 计算每个饮品的综合评分
            scored_drinks = []
            for drink in all_drinks:
                comprehensive_score = self._calculate_comprehensive_score(
                    drink=drink,
                    user_profile=user_profile,
                    preference_dict=preference_dict,
                    today_record=today_record,
                    recent_records=recent_records
                )
                
                # 只保留评分大于0的饮品
                if comprehensive_score > 0:
                    scored_drinks.append({
                        **drink,
                        "recommendation_score": comprehensive_score,
                        "reason": self._generate_comprehensive_reason(
                            drink, user_profile, preference_dict
                        )
                    })
            
            # 按综合评分降序排序
            scored_drinks.sort(key=lambda x: x["recommendation_score"], reverse=True)
            
            # 返回前limit个推荐
            result = scored_drinks[:limit]
            logger.info(f"为用户 {user_id} 生成 {len(result)} 个综合推荐")
            return result
            
        except Exception as e:
            logger.error(f"混合推荐失败: {e}")
            return []
    
    
    def _calculate_comprehensive_score(
        self,
        drink: Dict,
        user_profile: Optional[Dict],
        preference_dict: Dict,
        today_record: Optional[Dict],
        recent_records: List[Dict]
    ) -> float:
        """
        计算综合评分 - 优先考虑饮品偏好，然后考虑健康档案和记录
        
        评分权重：
        - 用户偏好：40%（如果有偏好数据）
        - 健康匹配度：30%
        - 今日摄入情况：20%
        - 近期饮用习惯：10%
        
        Args:
            drink: 饮品信息
            user_profile: 用户档案
            preference_dict: 用户偏好字典
            today_record: 今日健康记录
            recent_records: 近期健康记录
            
        Returns:
            综合评分（0-100）
        """
        score = 0.0
        drink_id = drink["drink_id"]
        
        # 1. 用户偏好评分（40%权重）
        if drink_id in preference_dict:
            pref = preference_dict[drink_id]
            # 偏好评分(1-5) -> 转换为0-100
            preference_score = (pref["preference_score"] / 5.0) * 100
            # 饮用次数加成（最多10次有效）
            consumption_bonus = min(pref["times_consumed"], 10) * 2
            score += (preference_score + consumption_bonus) * 0.4
        else:
            # 无偏好数据，使用基础分50分
            score += 50 * 0.4
        
        # 2. 健康匹配度评分（30%权重）
        if user_profile:
            health_match_score = self._calculate_health_match_score(drink, user_profile)
            score += health_match_score * 0.3
        else:
            # 无档案时使用饮品本身的健康评分
            score += drink.get("health_score", 50) * 0.3
        
        # 3. 今日摄入情况评分（20%权重）
        if user_profile and today_record:
            today_score = self._calculate_today_intake_score(
                drink, user_profile, today_record
            )
            score += today_score * 0.2
        else:
            # 无今日记录，使用中等分数
            score += 60 * 0.2
        
        # 4. 近期饮用习惯评分（10%权重）
        if recent_records:
            habit_score = self._calculate_habit_score(drink, recent_records)
            score += habit_score * 0.1
        else:
            score += 50 * 0.1
        
        return min(100, max(0, score))
    
    
    def _calculate_health_match_score(self, drink: Dict, user_profile: Dict) -> float:
        """计算健康匹配度评分"""
        score = 0.0
        
        # 基础健康评分
        score += drink.get("health_score", 50) * 0.5
        
        # 糖分匹配
        sugar_limit = user_profile.get("sugar_limit", 50)
        if drink["sugar_content"] <= sugar_limit * 0.2:  # 单次建议不超过20%每日限额
            score += 30
        elif drink["sugar_content"] <= sugar_limit * 0.3:
            score += 20
        elif drink["sugar_content"] <= sugar_limit * 0.5:
            score += 10
        else:
            return 0  # 糖分过高，不推荐
        
        # 热量匹配
        calorie_limit = user_profile.get("calorie_limit", 2000)
        if drink["calories"] <= calorie_limit * 0.05:  # 不超过5%每日热量
            score += 20
        elif drink["calories"] <= calorie_limit * 0.1:
            score += 10
        
        return min(100, score)
    
    
    def _calculate_today_intake_score(
        self, drink: Dict, user_profile: Dict, today_record: Dict
    ) -> float:
        """计算今日摄入情况评分"""
        score = 100.0
        
        # 检查今日糖分摄入
        sugar_limit = user_profile.get("sugar_limit", 50)
        today_sugar = today_record.get("total_sugar_intake", 0)
        remaining_sugar = sugar_limit - today_sugar
        
        if drink["sugar_content"] > remaining_sugar:
            return 0  # 超出今日限额
        elif remaining_sugar < sugar_limit * 0.3:
            # 剩余额度不足30%，优先低糖饮品
            if drink["sugar_content"] < 5:
                score = 100
            elif drink["sugar_content"] < 10:
                score = 70
            else:
                score = 40
        
        # 检查今日热量摄入
        calorie_limit = user_profile.get("calorie_limit", 2000)
        today_calories = today_record.get("total_calories", 0)
        remaining_calories = calorie_limit - today_calories
        
        if drink["calories"] > remaining_calories * 0.1:
            score *= 0.7  # 热量较高，降低评分
        
        return score
    
    
    def _calculate_habit_score(self, drink: Dict, recent_records: List[Dict]) -> float:
        """计算近期饮用习惯评分"""
        # 分析近期糖分和热量摄入趋势
        if not recent_records:
            return 50
        
        avg_sugar = sum(r.get("total_sugar_intake", 0) for r in recent_records) / len(recent_records)
        avg_calories = sum(r.get("total_calories", 0) for r in recent_records) / len(recent_records)
        
        score = 100.0
        
        # 如果近期摄入偏高，优先推荐低糖低卡饮品
        if avg_sugar > 40:  # 近期糖分偏高
            if drink["sugar_content"] < 10:
                score = 100
            else:
                score = 60
        
        if avg_calories > 2200:  # 近期热量偏高
            if drink["calories"] < 50:
                score = min(score, 100)
            else:
                score *= 0.8
        
        return score
    
    
    def _generate_comprehensive_reason(
        self, drink: Dict, user_profile: Optional[Dict], preference_dict: Dict
    ) -> str:
        """生成综合推荐理由"""
        reasons = []
        drink_id = drink["drink_id"]
        
        # 优先说明用户偏好
        if drink_id in preference_dict:
            pref = preference_dict[drink_id]
            times = pref["times_consumed"]
            if times > 5:
                reasons.append(f"您已饮用{times}次，喜爱饮品")
            elif times > 0:
                reasons.append("您曾饮用过")
            
            pref_score = pref["preference_score"]
            if pref_score >= 4:
                reasons.append("高偏好评分")
        
        # 健康特点
        if drink["sugar_content"] == 0:
            reasons.append("无糖健康")
        elif drink["sugar_content"] < 10:
            reasons.append("低糖")
        
        if drink["calories"] < 50:
            reasons.append("低热量")
        
        if drink.get("health_score", 50) >= 80:
            reasons.append("健康评分优秀")
        
        # 特殊健康状况
        if user_profile:
            if user_profile.get("has_diabetes") and drink["sugar_content"] == 0:
                reasons.append("适合控糖人群")
        
        return "、".join(reasons) if reasons else "为您推荐"
    
    
    def _calculate_health_score(
        self,
        drink: Dict,
        user_profile: Dict,
        max_sugar: float
    ) -> float:
        """
        计算饮品健康评分
        
        Args:
            drink: 饮品信息
            user_profile: 用户档案
            max_sugar: 最大糖分
            
        Returns:
            评分(0-100)
        """
        score = 0.0
        
        # 糖分限制(硬性条件)
        if drink["sugar_content"] > max_sugar:
            return 0.0
        
        # 基础健康评分(40分)
        score += drink.get("health_score", 50) * 0.4
        
        # 糖分评分(30分) - 越低越好
        sugar_score = max(0, 100 - drink["sugar_content"] * 2)
        score += sugar_score * 0.3
        
        # 热量评分(20分) - 越低越好
        calorie_score = max(0, 100 - drink["calories"] / 5)
        score += calorie_score * 0.2
        
        # 特殊健康状况加分(10分)
        if user_profile.get("has_diabetes") and drink["sugar_content"] == 0:
            score += 10  # 糖尿病患者优先推荐无糖饮品
        
        if user_profile.get("health_goal") == "weight_loss" and drink["calories"] < 100:
            score += 5  # 减重目标优先推荐低热量
        
        return min(100, score)
    
    
    def _find_similar_users(
        self,
        user_id: int,
        user_prefs: List[Dict],
        all_prefs: List[Dict],
        top_k: int = 10
    ) -> List[Tuple[int, float]]:
        """
        找到相似用户
        
        Args:
            user_id: 目标用户ID
            user_prefs: 用户偏好
            all_prefs: 所有用户偏好
            top_k: 返回前k个相似用户
            
        Returns:
            [(用户ID, 相似度), ...]
        """
        # 构建用户-饮品评分矩阵
        user_drink_matrix = defaultdict(dict)
        
        # 目标用户的偏好
        for pref in user_prefs:
            drink_id = pref["drink_id"]
            # 综合评分: 偏好分数*0.7 + 饮用次数*0.3
            score = (pref.get("preference_score", 3) * 0.7 + 
                    min(pref.get("times_consumed", 0), 10) * 0.03)
            user_drink_matrix[user_id][drink_id] = score
        
        # 其他用户的偏好
        for pref in all_prefs:
            if pref["user_id"] != user_id:
                drink_id = pref["drink_id"]
                score = (pref.get("preference_score", 3) * 0.7 + 
                        min(pref.get("times_consumed", 0), 10) * 0.03)
                user_drink_matrix[pref["user_id"]][drink_id] = score
        
        # 计算余弦相似度
        similarities = []
        target_vector = user_drink_matrix[user_id]
        
        for other_user_id, other_vector in user_drink_matrix.items():
            if other_user_id != user_id:
                similarity = self._cosine_similarity(target_vector, other_vector)
                if similarity > 0:
                    similarities.append((other_user_id, similarity))
        
        # 排序并返回top k
        similarities.sort(key=lambda x: x[1], reverse=True)
        return similarities[:top_k]
    
    
    def _cosine_similarity(self, vec1: Dict, vec2: Dict) -> float:
        """计算余弦相似度"""
        # 找到共同的饮品
        common_drinks = set(vec1.keys()) & set(vec2.keys())
        
        if not common_drinks:
            return 0.0
        
        # 计算点积和模
        dot_product = sum(vec1[d] * vec2[d] for d in common_drinks)
        norm1 = np.sqrt(sum(v**2 for v in vec1.values()))
        norm2 = np.sqrt(sum(v**2 for v in vec2.values()))
        
        if norm1 == 0 or norm2 == 0:
            return 0.0
        
        return dot_product / (norm1 * norm2)
    
    
    def _get_collaborative_recommendations(
        self,
        user_id: int,
        similar_users: List[Tuple[int, float]],
        user_prefs: List[Dict],
        limit: int
    ) -> List[Dict]:
        """获取协同过滤推荐"""
        # 用户已尝试过的饮品
        tried_drinks = {pref["drink_id"] for pref in user_prefs}
        
        # 候选饮品及加权评分
        candidate_scores = defaultdict(float)
        candidate_count = defaultdict(int)
        
        # 遍历相似用户的偏好
        for similar_user_id, similarity in similar_users:
            similar_prefs = self.db_tool.get_user_preferences(similar_user_id)
            
            for pref in similar_prefs:
                drink_id = pref["drink_id"]
                
                # 跳过用户已尝试的
                if drink_id in tried_drinks:
                    continue
                
                # 加权评分
                score = (pref.get("preference_score", 3) * similarity * 0.7 + 
                        min(pref.get("times_consumed", 0), 5) * similarity * 0.3)
                candidate_scores[drink_id] += score
                candidate_count[drink_id] += 1
        
        # 计算平均评分
        avg_scores = {
            drink_id: candidate_scores[drink_id] / candidate_count[drink_id]
            for drink_id in candidate_scores
        }
        
        # 排序
        sorted_drinks = sorted(avg_scores.items(), key=lambda x: x[1], reverse=True)
        
        # 获取饮品详情
        recommendations = []
        for drink_id, score in sorted_drinks[:limit]:
            drink_info = self.db_tool.get_drink_by_id(drink_id)
            if drink_info:
                profile = self.db_tool.get_user_profile(user_id)
                recommendations.append({
                    **drink_info,
                    "recommendation_score": score,
                    "reason": self._generate_recommendation_reason(drink_info, profile, "协同过滤")
                })
        
        return recommendations
    
    
    def _recommend_top_healthy_drinks(self, limit: int) -> List[Dict]:
        """推荐最健康的饮品"""
        all_drinks = self.db_tool.get_all_drinks()
        
        # 按健康评分和糖分排序
        all_drinks.sort(key=lambda x: (-x.get("health_score", 50), x["sugar_content"]))
        
        result = []
        for drink in all_drinks[:limit]:
            result.append({
                **drink,
                "recommendation_score": drink.get("health_score", 50),
                "reason": f"健康评分{drink.get('health_score', 50)}分，糖分仅{drink['sugar_content']}g"
            })
        
        return result
    
    
    def _generate_recommendation_reason(
        self,
        drink: Dict,
        user_profile: Optional[Dict],
        rec_type: str
    ) -> str:
        """生成推荐理由"""
        reasons = []
        
        if rec_type == "健康":
            if drink["sugar_content"] == 0:
                reasons.append("无糖配方")
            elif drink["sugar_content"] < 10:
                reasons.append("低糖健康")
            
            if drink["calories"] < 50:
                reasons.append("低热量")
            
            if drink.get("health_score", 50) >= 80:
                reasons.append("健康评分优秀")
        
        elif rec_type == "协同过滤":
            reasons.append("相似用户喜欢")
            if drink.get("health_score", 50) >= 70:
                reasons.append("同时也很健康")
        
        if user_profile and user_profile.get("has_diabetes") and drink["sugar_content"] == 0:
            reasons.append("适合糖尿病人群")
        
        return "、".join(reasons) if reasons else "推荐给你"


def get_recommendation_system(db_tool) -> DrinkRecommendationSystem:
    """获取推荐系统实例"""
    return DrinkRecommendationSystem(db_tool)

