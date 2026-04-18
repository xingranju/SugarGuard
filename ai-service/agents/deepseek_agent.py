"""
DeepSeek API集成 - LangChain智能对话代理
实现基于大模型的健康问答和建议生成
"""
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from typing import List, Dict, Optional
import logging

from config.settings import settings

logger = logging.getLogger(__name__)


class DeepSeekAgent:
    """DeepSeek对话智能体"""
    
    def __init__(self, tools: Optional[List] = None):
        """
        初始化DeepSeek智能体
        
        Args:
            tools: 可用工具列表(预留参数,当前版本未使用)
        """
        self.tools = tools or []
        
        # 初始化DeepSeek LLM（通过OpenAI兼容接口）
        self.llm = ChatOpenAI(
            model=settings.DEEPSEEK_MODEL,
            openai_api_key=settings.DEEPSEEK_API_KEY,
            openai_api_base=settings.DEEPSEEK_BASE_URL,
            temperature=0.7,
            max_tokens=2000,
        )
        
        # 系统提示词
        self.system_prompt = """你是SugarGuard - 一位专业的青少年健康助手，专注于帮助青少年控制糖分摄入、养成健康饮食习惯。

你的核心职责：
1. **健康教育**：用通俗易懂的语言解释糖分摄入对健康的影响
2. **个性化建议**：根据用户的健康档案（年龄、身高、体重、血糖等）提供定制化建议
3. **风险警示**：当发现潜在健康风险时，温和但明确地提醒用户
4. **饮品推荐**：推荐低糖、健康的替代饮品，并解释推荐理由
5. **习惯养成**：鼓励和引导青少年养成健康的生活方式

交流风格：
- 友好、温暖，像朋友一样交流
- 使用青少年容易理解的语言
- 避免说教，多用鼓励和正向引导
- 提供具体、可操作的建议
- 适时使用emoji让对话更生动 😊

重要原则：
- 如果用户询问医疗诊断问题，建议咨询专业医生
- 对于严重的健康问题（如血糖异常高），强烈建议就医
- 始终以用户的健康和安全为第一优先级
"""
        
        logger.info("DeepSeek智能体初始化成功")
    
    
    async def chat(
        self, 
        user_message: str, 
        user_context: Optional[Dict] = None,
        chat_history: Optional[List] = None
    ) -> Dict:
        """
        进行对话
        
        Args:
            user_message: 用户消息
            user_context: 用户上下文信息（健康档案等）
            chat_history: 历史对话记录
            
        Returns:
            对话响应
        """
        try:
            # 构建增强的用户消息（包含上下文）
            enhanced_message = user_message
            if user_context:
                context_str = self._format_user_context(user_context)
                enhanced_message = f"{context_str}\n\n用户问题：{user_message}"
            
            # 构建消息列表
            messages = [
                SystemMessage(content=self.system_prompt)
            ]
            
            # 添加对话历史
            if chat_history:
                for item in chat_history[-3:]:  # 只保留最近3轮
                    if "user_message" in item:
                        messages.append(HumanMessage(content=item["user_message"]))
                    if "bot_response" in item:
                        messages.append(AIMessage(content=item["bot_response"]))
            
            # 添加当前用户消息
            messages.append(HumanMessage(content=enhanced_message))
            
            # 执行对话
            response = await self.llm.ainvoke(messages)
            
            return {
                "success": True,
                "response": response.content if hasattr(response, 'content') else str(response),
                "intermediate_steps": []
            }
            
        except Exception as e:
            logger.error(f"对话失败: {e}")
            return {
                "success": False,
                "response": "抱歉，我遇到了一些技术问题。请稍后再试。😅",
                "error": str(e)
            }
    
    
    def _format_user_context(self, context: Dict) -> str:
        """格式化用户上下文信息"""
        lines = ["【用户健康档案】"]
        
        if "age" in context and context["age"]:
            lines.append(f"年龄：{context['age']}岁")
        if "gender" in context and context["gender"]:
            gender_str = "男" if context["gender"] == "male" else "女"
            lines.append(f"性别：{gender_str}")
        if context.get("height") and context.get("weight"):
            bmi = context["weight"] / ((context["height"] / 100) ** 2)
            lines.append(f"身高/体重：{context['height']}cm / {context['weight']}kg (BMI: {bmi:.1f})")
        if context.get("health_conditions"):
            lines.append(f"健康状况：{context['health_conditions']}")
        if context.get("allergies"):
            lines.append(f"过敏信息：{context['allergies']}")
        if context.get("activity_level"):
            lines.append(f"活动水平：{context['activity_level']}")
        if "sugar_limit" in context:
            lines.append(f"每日糖分限额：{context['sugar_limit']}g")
        if context.get("calorie_limit"):
            lines.append(f"每日热量限额：{context['calorie_limit']}kcal")
        
        lines.append("")
        lines.append("【今日摄入情况】")
        if "today_sugar_intake" in context:
            lines.append(f"今日已摄入糖分：{context['today_sugar_intake']}g")
        if "today_calories" in context:
            lines.append(f"今日已摄入热量：{context['today_calories']}kcal")
        if "today_water" in context:
            lines.append(f"今日饮水量：{context['today_water']}ml")
        
        if context.get("week_avg_sugar") or context.get("week_avg_calories"):
            lines.append("")
            lines.append("【近7天健康数据】")
            if context.get("week_avg_sugar"):
                lines.append(f"近7天日均糖分：{context['week_avg_sugar']}g")
            if context.get("week_avg_calories"):
                lines.append(f"近7天日均热量：{context['week_avg_calories']}kcal")
        
        if context.get("recent_meals"):
            lines.append("")
            lines.append("【近期饮食记录】")
            lines.append(context["recent_meals"])
        
        if context.get("favorite_drinks"):
            lines.append("")
            lines.append("【用户偏好饮品】")
            lines.append(context["favorite_drinks"])
        
        return "\n".join(lines)
    
    
    def _format_chat_history(self, chat_history: List[Dict]) -> List:
        """格式化对话历史"""
        messages = []
        for item in chat_history[-5:]:  # 只保留最近5轮对话
            if "user_message" in item:
                messages.append(HumanMessage(content=item["user_message"]))
            if "bot_response" in item:
                messages.append(AIMessage(content=item["bot_response"]))
        return messages
    
    
    async def generate_recommendation_reason(
        self, 
        drink_info: Dict,
        user_context: Dict
    ) -> str:
        """
        为饮品推荐生成理由
        
        Args:
            drink_info: 饮品信息
            user_context: 用户上下文
            
        Returns:
            推荐理由
        """
        try:
            prompt = f"""基于以下信息，为用户生成一个简短、友好的饮品推荐理由（50字以内）：

用户信息：
- 年龄：{user_context.get('age', '未知')}岁
- 健康目标：{user_context.get('health_goal', '控糖')}
- 今日已摄入糖分：{user_context.get('today_sugar_intake', 0)}g
- 每日限额：{user_context.get('sugar_limit', 50)}g

推荐饮品：
- 名称：{drink_info.get('drink_name')}
- 品牌：{drink_info.get('brand')}
- 糖分含量：{drink_info.get('sugar_content')}g
- 热量：{drink_info.get('calories')}kcal
- 健康评分：{drink_info.get('health_score')}/100
- 描述：{drink_info.get('description')}

请用青少年喜欢的语气，简洁地说明为什么推荐这款饮品。"""

            response = await self.llm.ainvoke([HumanMessage(content=prompt)])
            return response.content.strip()
            
        except Exception as e:
            logger.error(f"生成推荐理由失败: {e}")
            return f"这款{drink_info.get('drink_name')}健康评分高，适合你！"
    
    
    async def analyze_drink_risk(
        self,
        drink_info: Dict,
        user_context: Dict
    ) -> Dict:
        """
        分析饮品风险
        
        Args:
            drink_info: 饮品信息
            user_context: 用户上下文
            
        Returns:
            风险分析结果
        """
        try:
            sugar_content = drink_info.get('sugar_content', 0)
            today_intake = user_context.get('today_sugar_intake', 0)
            sugar_limit = user_context.get('sugar_limit', 50)
            total_intake = today_intake + sugar_content
            
            # 计算风险等级
            if total_intake >= sugar_limit * 1.5:
                risk_level = "high"
                risk_emoji = "🔴"
            elif total_intake >= sugar_limit:
                risk_level = "medium"
                risk_emoji = "🟡"
            else:
                risk_level = "low"
                risk_emoji = "🟢"
            
            # 生成风险提示
            prompt = f"""用户即将饮用一款饮品，请分析风险并给出建议（100字以内）：

用户信息：
- 年龄：{user_context.get('age')}岁
- 是否有糖尿病：{'是' if user_context.get('has_diabetes') else '否'}
- 每日糖分限额：{sugar_limit}g
- 今日已摄入：{today_intake}g

饮品信息：
- 名称：{drink_info.get('drink_name')}
- 糖分：{sugar_content}g
- 饮用后总摄入：{total_intake}g

风险等级：{risk_level}

请给出：
1. 简短的风险评估
2. 是否建议饮用
3. 替代建议（如果风险高）"""

            response = await self.llm.ainvoke([HumanMessage(content=prompt)])
            
            return {
                "risk_level": risk_level,
                "risk_emoji": risk_emoji,
                "total_sugar_after": total_intake,
                "exceeds_limit": total_intake > sugar_limit,
                "advice": response.content.strip()
            }
            
        except Exception as e:
            logger.error(f"风险分析失败: {e}")
            return {
                "risk_level": "unknown",
                "risk_emoji": "⚠️",
                "advice": "建议适量饮用，注意糖分摄入。"
            }
    
    
    async def generate_health_advice(
        self,
        drink_info: Dict,
        user_context: Dict,
        rank: int = 1
    ) -> str:
        """
        为推荐饮品生成AI健康建议
        
        Args:
            drink_info: 饮品信息
            user_context: 用户上下文（包含健康档案、偏好、今日摄入等）
            rank: 推荐排名
            
        Returns:
            AI健康建议文本
        """
        try:
            # 构建用户健康状况描述
            age = user_context.get('age', '未知')
            gender = "男" if user_context.get('gender') == 'male' else "女"
            sugar_limit = user_context.get('sugar_limit', 50)
            today_sugar = user_context.get('today_sugar', 0)
            recent_avg_sugar = user_context.get('recent_avg_sugar', 0)
            has_prefs = user_context.get('has_preferences', False)
            
            # 计算剩余糖分额度
            remaining_sugar = max(0, sugar_limit - today_sugar)
            sugar_usage_rate = (today_sugar / sugar_limit * 100) if sugar_limit > 0 else 0
            
            # 构建提示词
            prompt = f"""作为专业的青少年健康顾问，为用户生成一条个性化的健康建议（80-120字）。

【用户健康档案】
- 年龄/性别：{age}岁 / {gender}
- 每日糖分限额：{sugar_limit}g
- 今日已摄入糖分：{today_sugar}g（剩余额度：{remaining_sugar}g，已使用{sugar_usage_rate:.0f}%）
- 近7日平均糖分摄入：{recent_avg_sugar:.1f}g
- 是否有饮品偏好记录：{'是' if has_prefs else '否'}

【推荐饮品（第{rank}名）】
- 名称：{drink_info.get('drink_name')}
- 品牌：{drink_info.get('brand')}
- 糖分含量：{drink_info.get('sugar_content')}g
- 热量：{drink_info.get('calories')}kcal
- 健康评分：{drink_info.get('health_score', 50)}/100
- 推荐评分：{drink_info.get('recommendation_score', 0):.1f}/100
- 推荐理由：{drink_info.get('reason', '为您推荐')}

【要求】
1. 分析为什么这款饮品适合该用户（结合排名、健康状况、今日摄入情况）
2. 给出具体的饮用建议（如最佳饮用时间、饮用量建议）
3. 如果今日糖分已超标或接近超标，提醒用户并给出调整建议
4. 语气友好、专业，适合青少年阅读
5. 不要使用emoji"""

            response = await self.llm.ainvoke([HumanMessage(content=prompt)])
            advice = response.content.strip()
            
            logger.info(f"成功生成健康建议 (第{rank}名): {drink_info.get('drink_name')}")
            return advice
            
        except Exception as e:
            logger.error(f"生成健康建议失败 (第{rank}名): {e}")
            # 返回默认建议
            sugar = drink_info.get('sugar_content', 0)
            if sugar == 0:
                return f"这款{drink_info.get('drink_name')}无糖配方，适合您当前的健康状况。建议您可以随时饮用，但要注意控制总摄入量。"
            elif sugar < 10:
                return f"这款{drink_info.get('drink_name')}属于低糖饮品，含糖{sugar}g。您今日已摄入{user_context.get('today_sugar', 0)}g糖分，建议适量饮用。"
            else:
                remaining = max(0, user_context.get('sugar_limit', 50) - user_context.get('today_sugar', 0))
                if sugar <= remaining:
                    return f"这款{drink_info.get('drink_name')}含糖{sugar}g，在您的剩余额度内。建议上午或运动后饮用效果更佳。"
                else:
                    return f"这款{drink_info.get('drink_name')}含糖{sugar}g，已超出您今日剩余额度。建议明天再饮用，或选择低糖替代品。"
    
    
    async def generate_health_analysis_advice(
        self,
        user_profile: Dict,
        bmi_analysis: Dict,
        sugar_assessment: Dict,
        health_records: List[Dict]
    ) -> Dict:
        """
        为健康分析生成AI建议
        
        Args:
            user_profile: 用户健康档案
            bmi_analysis: BMI分析结果
            sugar_assessment: 糖分评估结果
            health_records: 健康记录列表
            
        Returns:
            包含AI建议的字典
        """
        try:
            age = user_profile.get('age', '未知')
            gender = "男" if user_profile.get('gender') == 'male' else "女"
            weight = user_profile.get('weight', 0)
            height = user_profile.get('height', 0)
            
            # BMI信息
            bmi_value = bmi_analysis.get('bmi', 0)
            bmi_status = bmi_analysis.get('status', '未知')
            
            # 糖分摄入信息
            avg_sugar = sugar_assessment.get('average_daily_sugar', 0)
            sugar_limit = sugar_assessment.get('sugar_limit', 50)
            risk_level = sugar_assessment.get('risk_level', '未知')
            exceed_pct = sugar_assessment.get('exceed_percentage', 0)
            days_analyzed = sugar_assessment.get('days_analyzed', 0)
            
            # 构建提示词
            prompt = f"""作为专业的青少年健康顾问，基于用户的健康数据生成全面的健康建议（分为BMI建议和糖分管理建议，每个80-120字）。

【用户基本信息】
- 年龄/性别：{age}岁 / {gender}
- 身高/体重：{height}cm / {weight}kg
- 分析周期：近{days_analyzed}天

【BMI分析】
- BMI值：{bmi_value}
- 状态：{bmi_status}

【糖分摄入评估】
- 日均糖分摄入：{avg_sugar}g
- 每日建议限额：{sugar_limit}g
- 风险等级：{risk_level}
{'- 超标程度：' + str(exceed_pct) + '%' if exceed_pct > 0 else '- 控制良好'}

【要求】
请分别生成两段建议（JSON格式）：
1. bmi_advice: 针对BMI状态的具体建议（包括饮食、运动、生活习惯调整）
2. sugar_advice: 针对糖分摄入的管理建议（包括如何降低糖分、饮品选择、替代方案）

要求：
- 语气友好专业，适合青少年
- 给出具体可执行的行动建议
- 如果数据正常，给予鼓励并提醒保持
- 如果数据异常，要强调重要性但不要过于严厉
- 返回格式：{{"bmi_advice": "...", "sugar_advice": "..."}}"""

            response = await self.llm.ainvoke([HumanMessage(content=prompt)])
            advice_text = response.content.strip()
            
            # 尝试解析JSON响应
            import json
            import re
            
            # 提取JSON部分（如果LLM返回了额外文本）
            json_match = re.search(r'\{.*\}', advice_text, re.DOTALL)
            if json_match:
                advice_dict = json.loads(json_match.group())
                bmi_advice = advice_dict.get('bmi_advice', '')
                sugar_advice = advice_dict.get('sugar_advice', '')
            else:
                # 如果无法解析JSON，尝试分段处理
                parts = advice_text.split('\n\n')
                bmi_advice = parts[0] if len(parts) > 0 else advice_text
                sugar_advice = parts[1] if len(parts) > 1 else advice_text
            
            logger.info(f"成功生成健康分析建议 (用户{user_profile.get('user_id', '未知')})")
            return {
                "bmi_advice": bmi_advice,
                "sugar_advice": sugar_advice
            }
            
        except Exception as e:
            logger.error(f"生成健康分析建议失败: {e}")
            # 返回默认建议
            bmi_status = bmi_analysis.get('status', '正常')
            risk_level = sugar_assessment.get('risk_level', '低风险')
            
            if bmi_status == "正常":
                bmi_advice = f"您的BMI值{bmi_analysis.get('bmi', 0)}处于正常范围，这很棒！建议继续保持均衡饮食和适量运动，每周至少进行3次30分钟以上的有氧运动。"
            elif bmi_status == "偏瘦":
                bmi_advice = f"您的BMI值{bmi_analysis.get('bmi', 0)}偏低。建议适当增加营养摄入，多吃富含蛋白质的食物（如鸡蛋、瘦肉、豆制品），保证三餐规律，必要时咨询营养师。"
            else:
                bmi_advice = f"您的BMI值{bmi_analysis.get('bmi', 0)}提示体重偏高。建议控制热量摄入，增加运动量，每天至少30分钟中等强度运动，少吃高糖高脂食物，多吃蔬菜水果。"
            
            if risk_level == "低风险":
                sugar_advice = f"您近期日均糖分摄入{sugar_assessment.get('average_daily_sugar', 0)}g，控制得很好！请继续保持，优先选择无糖或低糖饮品，多喝白开水。"
            elif risk_level == "中等风险":
                sugar_advice = f"您近期日均糖分摄入{sugar_assessment.get('average_daily_sugar', 0)}g，略高于建议量。建议减少含糖饮料，用茶水、苏打水等替代，逐步培养清淡口味。"
            else:
                sugar_advice = f"您近期日均糖分摄入{sugar_assessment.get('average_daily_sugar', 0)}g，已严重超标！请立即调整，停止饮用含糖饮料，多喝水，增加运动。长期高糖摄入会影响健康，请重视。"
            
            return {
                "bmi_advice": bmi_advice,
                "sugar_advice": sugar_advice
            }
    
    
    async def generate_drink_recognition_advice(
        self,
        food_name: str,
        nutrition_info: Dict,
        health_assessment: Dict,
        user_profile: Optional[Dict] = None
    ) -> str:
        """
        为食物识别生成AI健康建议
        
        Args:
            food_name: 食物名称
            nutrition_info: 营养信息
            health_assessment: 健康评估
            user_profile: 用户档案（可选）
            
        Returns:
            AI健康建议文本
        """
        try:
            sugar = nutrition_info.get('sugar_content', 0)
            calories = nutrition_info.get('calories', 0)
            health_score = nutrition_info.get('health_score', 50)
            sugar_level = health_assessment.get('sugar_level', 'unknown')
            calorie_level = health_assessment.get('calorie_level', 'unknown')
            
            # 构建用户上下文
            user_context_str = ""
            if user_profile:
                age = user_profile.get('age', '未知')
                sugar_limit = user_profile.get('sugar_limit', 50)
                user_context_str = f"\n\n用户信息：\n- 年龄：{age}岁\n- 每日糖分限额：{sugar_limit}g"
            
            # 构建提示词
            prompt = f"""作为专业的营养师，为用户分析这款食物并给出健康建议（80-120字）。

【识别食物】
- 名称：{food_name}
- 糖分含量：{sugar}g（{sugar_level}）
- 热量：{calories}kcal（{calorie_level}）
- 健康评分：{health_score}/100{user_context_str}

【要求】
1. 客观评价这款食物的营养特点
2. 根据糖分和热量水平给出具体建议
3. 提供食用频率建议（如每日、每周几次等）
4. 如果有替代建议，简要说明
5. 语气专业但友好，适合青少年
6. 不要使用emoji符号"""

            response = await self.llm.ainvoke([HumanMessage(content=prompt)])
            advice = response.content.strip()
            
            logger.info(f"成功生成食物识别建议: {food_name}")
            return advice
            
        except Exception as e:
            logger.error(f"生成食物识别建议失败: {e}")
            # 返回默认建议
            if sugar > 25:
                return f"这款{food_name}含糖量较高（{sugar}g），建议偶尔食用，每周不超过1-2次。可以适量减少份量，平时多选择低糖食物。"
            elif sugar > 12:
                return f"这款{food_name}含有中等糖分（{sugar}g），建议适量食用，每周2-3次为宜。搭配运动后食用效果更佳，注意控制总糖分摄入。"
            else:
                return f"这款{food_name}属于低糖食物（{sugar}g），相对健康。可以适量食用，但仍需注意整体饮食平衡，保持多样化的食物选择。"


# 全局实例
_deepseek_agent = None


def get_deepseek_agent(tools: Optional[List] = None) -> DeepSeekAgent:
    """获取DeepSeek智能体单例"""
    global _deepseek_agent
    if _deepseek_agent is None:
        _deepseek_agent = DeepSeekAgent(tools=tools)
    elif tools:
        _deepseek_agent.tools = tools
    return _deepseek_agent

