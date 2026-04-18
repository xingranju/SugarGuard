"""
AI智能体服务主应用
"""
from fastapi import FastAPI, File, UploadFile, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import Optional, List
from PIL import Image
import io
import logging
import requests
from datetime import datetime

from config.settings import settings
from database.database import get_db, init_db
from agents.tools.image_recognition import get_image_recognition_tool
from agents.tools.database_query import DatabaseQueryTool
from agents.tools.health_assessment import HealthAssessmentTool
from agents.deepseek_agent import get_deepseek_agent
from agents.rag_knowledge import get_rag_system
from agents.tools.knowledge_tool import create_knowledge_search_tool, create_drink_info_tool, create_health_calculation_tool
from agents.recommendation import get_recommendation_system

# 配置日志
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL),
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 创建FastAPI应用
app = FastAPI(
    title="SugarGuard AI Service",
    description="青少年智能控糖助手AI服务",
    version="1.0.0"
)

# CORS配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应该限制具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==================== 请求/响应模型 ====================

class HealthProfileRequest(BaseModel):
    """健康档案请求"""
    user_id: int
    age: int
    gender: str
    height: float
    weight: float
    blood_sugar_level: Optional[float] = None
    has_diabetes: bool = False
    family_history: bool = False


class ChatRequest(BaseModel):
    """对话请求"""
    user_id: int
    message: str
    save_history: bool = True


class RecommendationRequest(BaseModel):
    """推荐请求"""
    user_id: int
    request_type: str = "healthy"  # healthy, collaborative, mixed


# ==================== 启动和关闭事件 ====================

@app.on_event("startup")
async def startup_event():
    """应用启动时执行"""
    logger.info("AI服务启动中...")
    try:
        # 初始化数据库
        init_db()
        logger.info("数据库初始化完成")
        
        # 预加载图像识别模型
        get_image_recognition_tool()
        logger.info("图像识别模型加载完成")
        
        # 初始化RAG知识库系统
        get_rag_system()
        logger.info("RAG知识库初始化完成")
        
        logger.info(f"AI服务已启动，监听端口: {settings.SERVICE_PORT}")
    except Exception as e:
        logger.error(f"启动失败: {e}")
        raise


@app.on_event("shutdown")
async def shutdown_event():
    """应用关闭时执行"""
    logger.info("AI服务关闭中...")


# ==================== 辅助函数 ====================

def query_food_nutrition_from_backend(food_name: str) -> dict:
    """
    从后端食品营养数据库查询食品营养信息
    使用多重搜索策略确保能找到匹配数据
    
    Args:
        food_name: 食品名称
        
    Returns:
        包含营养信息的字典，如果查询失败返回None
    """
    backend_url = "http://localhost:8080/api/food-nutrition/search"
    
    # 策略1: 直接搜索原始名称
    try:
        response = requests.get(backend_url, params={"name": food_name}, timeout=5)
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                nutrition_data = data.get("data", {})
                logger.info(f"✅ 精确匹配成功: {nutrition_data.get('food_name')}")
                return nutrition_data
    except Exception as e:
        logger.warning(f"策略1失败: {e}")
    
    # 策略2: 提取关键词搜索
    keywords = extract_food_keywords(food_name)
    for keyword in keywords:
        if len(keyword) < 2:  # 跳过太短的关键词
            continue
        try:
            response = requests.get(backend_url, params={"name": keyword}, timeout=5)
            if response.status_code == 200:
                data = response.json()
                if data.get("success"):
                    nutrition_data = data.get("data", {})
                    logger.info(f"✅ 关键词匹配成功: {keyword} -> {nutrition_data.get('food_name')}")
                    return nutrition_data
        except Exception as e:
            continue
    
    # 策略3: 使用搜索-多结果接口，取第一个结果
    try:
        multi_url = "http://localhost:8080/api/food-nutrition/search-multiple"
        response = requests.get(multi_url, params={"keyword": food_name, "limit": 3}, timeout=5)
        
        if response.status_code == 200:
            data = response.json()
            if data.get("success"):
                results = data.get("data", [])
                if results:
                    first_result = results[0]
                    nutrition_data = {
                        "food_name": first_result.get("foodName"),
                        "calories": float(first_result.get("caloricValue", 0)),
                        "sugars": float(first_result.get("sugars", 0)),
                        "protein": float(first_result.get("protein", 0)),
                        "fat": float(first_result.get("fat", 0)),
                        "carbohydrates": float(first_result.get("carbohydrates", 0)),
                        "dietary_fiber": float(first_result.get("dietaryFiber", 0)),
                    }
                    logger.info(f"✅ 模糊匹配成功: {food_name} -> {nutrition_data.get('food_name')}")
                    return nutrition_data
    except Exception as e:
        logger.warning(f"策略3失败: {e}")
    
    # 策略4: 从常见食物映射获取估算值
    estimated_nutrition = get_estimated_nutrition(food_name)
    if estimated_nutrition:
        logger.info(f"📊 使用估算营养数据: {food_name}")
        return estimated_nutrition
    
    logger.warning(f"❌ 所有搜索策略均失败: {food_name}")
    return None


def extract_food_keywords(food_name: str) -> list:
    """
    从食品名称中提取关键词
    """
    # 移除常见修饰词
    stop_words = ["fresh", "raw", "cooked", "fried", "boiled", "steamed", "organic",
                  "新鲜", "生", "熟", "炸", "煮", "蒸", "有机"]
    
    cleaned = food_name.lower()
    for word in stop_words:
        cleaned = cleaned.replace(word.lower(), " ")
    
    # 分割成关键词
    keywords = [kw.strip() for kw in cleaned.split() if kw.strip()]
    
    # 也尝试原始名称
    if food_name.lower() not in [kw.lower() for kw in keywords]:
        keywords.insert(0, food_name)
    
    return keywords


def calculate_health_score(nutrition_info: dict) -> int:
    """
    智能计算食品健康评分 (0-100分)
    
    评分标准：
    - 高分: 低糖、低热量、高蛋白、高纤维
    - 低分: 高糖、高热量、高脂肪、低营养
    
    Args:
        nutrition_info: 营养信息字典
        
    Returns:
        健康评分 (0-100)
    """
    score = 50  # 基础分
    
    # 提取营养数据
    sugars = float(nutrition_info.get('sugars', 0))
    calories = float(nutrition_info.get('calories', 0))
    protein = float(nutrition_info.get('protein', 0))
    fat = float(nutrition_info.get('fat', 0))
    carbs = float(nutrition_info.get('carbohydrates', 0))
    fiber = float(nutrition_info.get('dietary_fiber', 0))
    
    # 1. 糖分评分 (权重: 30%)
    # 每100g含糖量
    if sugars <= 5:
        score += 30  # 低糖，优秀
    elif sugars <= 10:
        score += 20  # 中等
    elif sugars <= 20:
        score += 10  # 偏高
    elif sugars <= 30:
        score -= 5   # 高糖
    else:
        score -= 20  # 超高糖
    
    # 2. 热量评分 (权重: 20%)
    # 每100g热量
    if calories <= 50:
        score += 20  # 低热量
    elif calories <= 100:
        score += 15  # 较低
    elif calories <= 200:
        score += 5   # 中等
    elif calories <= 300:
        score -= 5   # 偏高
    else:
        score -= 15  # 高热量
    
    # 3. 蛋白质评分 (权重: 15%)
    # 每100g蛋白质含量
    if protein >= 10:
        score += 15  # 高蛋白，优秀
    elif protein >= 5:
        score += 10  # 中等蛋白
    elif protein >= 2:
        score += 5   # 少量蛋白
    # else: 0分，无加分
    
    # 4. 脂肪评分 (权重: 15%)
    # 每100g脂肪含量
    if fat <= 3:
        score += 15  # 低脂，优秀
    elif fat <= 10:
        score += 8   # 中等脂肪
    elif fat <= 20:
        score += 0   # 偏高，不加分
    else:
        score -= 10  # 高脂
    
    # 5. 膳食纤维评分 (权重: 10%)
    # 每100g膳食纤维
    if fiber >= 5:
        score += 10  # 高纤维，优秀
    elif fiber >= 2:
        score += 5   # 中等纤维
    # else: 0分
    
    # 6. 营养密度评分 (权重: 10%)
    # 蛋白质/热量比值
    if calories > 0:
        protein_density = (protein * 4) / calories  # 蛋白质提供的热量占比
        if protein_density >= 0.3:
            score += 10  # 高营养密度
        elif protein_density >= 0.15:
            score += 5   # 中等
    
    # 7. 碳水质量评分
    # 碳水化合物中纤维的占比
    if carbs > 0 and fiber > 0:
        fiber_ratio = fiber / carbs
        if fiber_ratio >= 0.15:
            score += 5  # 优质碳水
    
    # 限制分数范围在 0-100
    score = max(0, min(100, score))
    
    return int(score)


def get_health_category(score: int) -> str:
    """
    根据健康评分获取健康类别
    """
    if score >= 80:
        return "非常健康"
    elif score >= 60:
        return "比较健康"
    elif score >= 40:
        return "一般"
    elif score >= 20:
        return "不太健康"
    else:
        return "不健康"


def get_estimated_nutrition(food_name: str) -> dict:
    """
    基于食物类型返回估算的营养数据
    """
    name_lower = food_name.lower()
    
    # 水果类
    if any(fruit in name_lower for fruit in ["apple", "orange", "banana", "grape", "berry", 
                                               "苹果", "橙", "香蕉", "葡萄", "莓"]):
        return {
            "food_name": f"{food_name} (估算)",
            "calories": 52.0,
            "sugars": 10.0,
            "protein": 0.3,
            "fat": 0.2,
            "carbohydrates": 14.0,
            "dietary_fiber": 2.4
        }
    
    # 咖啡/茶类
    if any(drink in name_lower for drink in ["coffee", "espresso", "latte", "cappuccino",
                                               "tea", "咖啡", "茶"]):
        return {
            "food_name": f"{food_name} (估算)",
            "calories": 5.0,
            "sugars": 0.0,
            "protein": 0.3,
            "fat": 0.1,
            "carbohydrates": 0.0,
            "dietary_fiber": 0.0
        }
    
    # 乳制品类
    if any(dairy in name_lower for dairy in ["milk", "cheese", "yogurt", "cream",
                                              "牛奶", "奶酪", "酸奶", "奶油"]):
        return {
            "food_name": f"{food_name} (估算)",
            "calories": 61.0,
            "sugars": 5.0,
            "protein": 3.2,
            "fat": 3.3,
            "carbohydrates": 4.8,
            "dietary_fiber": 0.0
        }
    
    # 默认保守估算
    return {
        "food_name": f"{food_name} (保守估算)",
        "calories": 100.0,
        "sugars": 10.0,
        "protein": 2.0,
        "fat": 3.0,
        "carbohydrates": 15.0,
        "dietary_fiber": 1.0
    }


# ==================== API端点 ====================

@app.get("/")
async def root():
    """根路径"""
    return {
        "service": "SugarGuard AI Service",
        "version": "1.0.0",
        "status": "running"
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat()
    }


@app.post("/api/recognize-drink")
async def recognize_drink(
    file: UploadFile = File(...),
    user_id: int = 1,
    db: Session = Depends(get_db)
):
    """
    饮品识别接口
    
    Args:
        file: 上传的图片文件
        user_id: 用户ID
        db: 数据库会话
        
    Returns:
        识别结果和健康建议
    """
    try:
        # 读取图像
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))
        
        # 图像识别
        image_tool = get_image_recognition_tool()
        recognition_results = image_tool.recognize(image, top_k=3)
        
        # 获取最可能的结果
        top_result = recognition_results[0]
        drink_name = top_result['label']
        confidence = top_result['confidence']
        
        # 初始化数据库工具
        db_tool = DatabaseQueryTool(db)
        
        # 优先从食品营养数据库查询
        nutrition_data = query_food_nutrition_from_backend(drink_name)
        data_source = "estimated"  # 默认数据来源
        
        if nutrition_data:
            # 使用营养数据库的真实数据
            drink_info = {
                "drink_name": nutrition_data.get('food_name', drink_name),
                "sugar_content": float(nutrition_data.get('sugars', 0)),
                "calories": float(nutrition_data.get('calories', 0)),
                "protein": float(nutrition_data.get('protein', 0)),
                "fat": float(nutrition_data.get('fat', 0)),
                "carbohydrates": float(nutrition_data.get('carbohydrates', 0)),
                "dietary_fiber": float(nutrition_data.get('dietary_fiber', 0))
            }
            # 智能计算健康评分
            drink_info["health_score"] = calculate_health_score(nutrition_data)
            data_source = "nutrition_database"
            health_category = get_health_category(drink_info["health_score"])
            logger.info(f"使用营养数据库数据: {drink_name} - 糖分:{drink_info['sugar_content']}g, 热量:{drink_info['calories']}kcal, 健康评分:{drink_info['health_score']}分({health_category})")
        else:
            # 尝试从饮品数据库查询
            drink_info = db_tool.search_drink_by_name(drink_name)
            
            # 如果都找不到，使用保守的估算值
            if not drink_info:
                drink_info = {
                    "drink_name": drink_name,
                    "sugar_content": 15.0,  # 保守估算
                    "calories": 100.0,
                    "protein": 0.0,
                    "fat": 0.0,
                    "carbohydrates": 15.0,
                    "dietary_fiber": 0.0
                }
                # 智能计算健康评分
                drink_info["health_score"] = calculate_health_score(drink_info)
                data_source = "estimated"
                logger.warning(f"未找到食品: {drink_name}, 使用保守估算值, 健康评分:{drink_info['health_score']}分")
            else:
                # 如果从饮品数据库找到了，也计算健康评分
                if "health_score" not in drink_info or drink_info.get("health_score") == 0:
                    drink_info["health_score"] = calculate_health_score(drink_info)
                data_source = "drink_database"
        
        # 健康评估
        health_tool = HealthAssessmentTool()
        health_impact = health_tool.assess_drink_health_impact(
            sugar_content=drink_info['sugar_content'],
            calories=drink_info['calories'],
            caffeine=drink_info.get('caffeine', 0)
        )
        
        # 获取健康类别
        health_score = drink_info.get('health_score', 50)
        health_category = get_health_category(health_score)
        
        # 获取用户档案
        user_profile = db_tool.get_user_profile(user_id)
        
        # 使用DeepSeek生成AI健康建议
        deepseek_agent = get_deepseek_agent()
        logger.info(f"为识别食物生成AI健康建议: {drink_name}")
        
        try:
            nutrition_info = {
                "sugar_content": drink_info['sugar_content'],
                "calories": drink_info['calories'],
                "health_score": health_score
            }
            
            ai_advice = await deepseek_agent.generate_drink_recognition_advice(
                food_name=drink_name,
                nutrition_info=nutrition_info,
                health_assessment=health_impact,
                user_profile=user_profile
            )
            health_advice = ai_advice
            logger.info(f"成功生成AI建议")
        except Exception as e:
            logger.error(f"生成AI建议失败: {e}")
            # 使用简化的默认建议
            health_advice = f"该食物含糖{drink_info['sugar_content']}g，热量{drink_info['calories']}kcal，健康评分{health_score}分。请根据个人健康状况适量食用。"
        
        # 保存饮食记录
        db_tool.save_meal_record(
            user_id=user_id,
            drink_name=drink_name,
            sugar_content=drink_info['sugar_content'],
            calories=drink_info['calories']
        )
        
        return {
            "success": True,
            "recognition": {
                "drink_name": drink_name,
                "confidence": confidence,
                "all_results": recognition_results
            },
            "nutrition": {
                "sugar_content": drink_info['sugar_content'],
                "calories": drink_info['calories'],
                "protein": drink_info.get('protein', 0),
                "fat": drink_info.get('fat', 0),
                "carbohydrates": drink_info.get('carbohydrates', 0),
                "dietary_fiber": drink_info.get('dietary_fiber', 0),
                "health_score": health_score,
                "health_category": health_category,
                "data_source": data_source
            },
            "health_assessment": {
                **health_impact,
                "health_advice": health_advice
            }
        }
        
    except Exception as e:
        logger.error(f"饮品识别失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/health-analysis/{user_id}")
async def health_analysis(
    user_id: int,
    days: int = 7,
    db: Session = Depends(get_db)
):
    """
    健康数据分析接口
    
    Args:
        user_id: 用户ID
        days: 分析天数
        db: 数据库会话
        
    Returns:
        健康分析报告
    """
    try:
        db_tool = DatabaseQueryTool(db)
        health_tool = HealthAssessmentTool()
        
        # 获取用户档案
        profile = db_tool.get_user_profile(user_id)
        if not profile:
            raise HTTPException(status_code=404, detail="用户档案不存在")
        
        # 获取健康记录
        health_records = db_tool.get_daily_health_records(user_id, days)
        
        # 计算BMI
        bmi_result = health_tool.calculate_bmi(profile['weight'], profile['height'])
        
        # 评估糖分摄入
        daily_sugar = [record['total_sugar_intake'] for record in health_records if record['total_sugar_intake']]
        sugar_assessment = health_tool.assess_sugar_intake(daily_sugar, profile['sugar_limit'])
        
        # 计算每日营养需求
        daily_needs = health_tool.calculate_daily_needs(
            age=profile['age'],
            gender=profile['gender'],
            weight=profile['weight'],
            height=profile['height']
        )
        
        # 使用DeepSeek生成AI健康建议
        deepseek_agent = get_deepseek_agent()
        logger.info(f"为用户 {user_id} 生成AI健康分析建议...")
        
        try:
            ai_advice = await deepseek_agent.generate_health_analysis_advice(
                user_profile=profile,
                bmi_analysis=bmi_result,
                sugar_assessment=sugar_assessment,
                health_records=health_records
            )
            bmi_result["ai_advice"] = ai_advice.get("bmi_advice", "")
            sugar_assessment["ai_advice"] = ai_advice.get("sugar_advice", "")
            logger.info(f"成功生成AI健康建议")
        except Exception as e:
            logger.error(f"生成AI健康建议失败: {e}")
            # 如果AI生成失败，不影响其他数据返回
            bmi_result["ai_advice"] = ""
            sugar_assessment["ai_advice"] = ""
        
        return {
            "success": True,
            "user_profile": profile,
            "bmi_analysis": bmi_result,
            "sugar_assessment": sugar_assessment,
            "daily_needs": daily_needs,
            "health_records": health_records,
            "analysis_period": f"最近{days}天"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"健康分析失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/chat")
async def chat(
    request: ChatRequest,
    db: Session = Depends(get_db)
):
    """
    智能对话接口 - 使用DeepSeek大模型
    
    Args:
        request: 对话请求
        db: 数据库会话
        
    Returns:
        对话回复
    """
    try:
        db_tool = DatabaseQueryTool(db)
        health_tool = HealthAssessmentTool()
        
        # 检测风险行为
        risk_check = health_tool.detect_risk_behavior(request.message)
        
        # 获取用户档案和上下文
        user_profile = db_tool.get_user_profile(request.user_id)
        user_context = {}
        
        if user_profile:
            user_context = {
                "age": user_profile.get("age"),
                "gender": user_profile.get("gender"),
                "height": user_profile.get("height"),
                "weight": user_profile.get("weight"),
                "health_conditions": user_profile.get("health_conditions"),
                "allergies": user_profile.get("allergies"),
                "activity_level": user_profile.get("activity_level"),
                "sugar_limit": user_profile.get("sugar_limit", 50),
                "calorie_limit": user_profile.get("calorie_limit"),
            }
            
            today_record = db_tool.get_today_health_record(request.user_id)
            if today_record:
                user_context["today_sugar_intake"] = today_record.get("total_sugar_intake", 0)
                user_context["today_calories"] = today_record.get("total_calories", 0)
                user_context["today_water"] = today_record.get("water_intake", 0)

            recent_health = db_tool.get_daily_health_records(request.user_id, days=7)
            if recent_health:
                avg_sugar = sum(r.get("total_sugar_intake", 0) or 0 for r in recent_health) / len(recent_health)
                avg_cal = sum(r.get("total_calories", 0) or 0 for r in recent_health) / len(recent_health)
                user_context["week_avg_sugar"] = round(avg_sugar, 1)
                user_context["week_avg_calories"] = round(avg_cal, 1)

            recent_meals = db_tool.get_meal_records(request.user_id, days=3)
            if recent_meals:
                meal_summary = []
                for m in recent_meals[:10]:
                    name = m.get("food_name", "")
                    sugar = m.get("sugar_content", 0) or 0
                    meal_summary.append(f"{name}({sugar}g糖)")
                user_context["recent_meals"] = "、".join(meal_summary)

            user_prefs = db_tool.get_user_preferences(request.user_id)
            if user_prefs:
                pref_drink_ids = [p["drink_id"] for p in user_prefs if p.get("preference_score", 0) >= 3]
                pref_names = []
                for did in pref_drink_ids[:5]:
                    drink = db_tool.get_drink_by_id(did)
                    if drink:
                        pref_names.append(drink["drink_name"])
                if pref_names:
                    user_context["favorite_drinks"] = "、".join(pref_names)
        
        chat_history = db_tool.get_recent_conversations(request.user_id, limit=5)
        
        # 创建LangChain工具
        rag_system = get_rag_system()
        tools = [
            create_knowledge_search_tool(rag_system),
            create_drink_info_tool(db_tool),
            create_health_calculation_tool()
        ]
        
        # 使用DeepSeek智能体进行对话
        deepseek_agent = get_deepseek_agent(tools=tools)
        chat_result = await deepseek_agent.chat(
            user_message=request.message,
            user_context=user_context if user_context else None,
            chat_history=chat_history
        )
        
        response = chat_result.get("response", "抱歉,我暂时无法回答这个问题。")
        intent = "ai_chat"
        
        # 添加风险提示
        if risk_check['has_risk']:
            response = risk_check['warning'] + "\n\n" + response
        
        if request.save_history:
            context = {
                "has_risk": risk_check['has_risk'],
                "intent": intent
            }
            db_tool.save_conversation(
                user_id=request.user_id,
                user_message=request.message,
                bot_response=response,
                intent=intent,
                context_data=context
            )
        
        return {
            "success": True,
            "response": response,
            "intent": intent,
            "risk_detected": risk_check['has_risk']
        }
        
    except Exception as e:
        logger.error(f"对话处理失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/recommend-drinks")
async def recommend_drinks(
    request: RecommendationRequest,
    db: Session = Depends(get_db)
):
    """
    饮品推荐接口 - 支持健康推荐、协同过滤、混合推荐
    
    Args:
        request: 推荐请求
        db: 数据库会话
        
    Returns:
        推荐饮品列表
    """
    try:
        db_tool = DatabaseQueryTool(db)
        rec_system = get_recommendation_system(db_tool)
        
        # 获取用户档案
        profile = db_tool.get_user_profile(request.user_id)
        if not profile:
            raise HTTPException(status_code=404, detail="用户档案不存在")
        
        # 使用优化后的混合推荐策略
        # 优先考虑用户饮品偏好，结合健康档案和健康记录生成50个推荐
        recommended_drinks = rec_system.recommend_mixed(
            user_id=request.user_id,
            limit=50
        )
        
        # 如果推荐结果为空，则获取所有饮品并基于健康评分排序
        if not recommended_drinks:
            logger.warning(f"混合推荐返回空结果，回退到全部饮品")
            all_drinks = db_tool.get_all_drinks()
            # 计算每个饮品的综合评分
            for drink in all_drinks:
                # 基于健康评分、糖分、热量计算综合分数
                health = drink.get("health_score", 50)
                sugar = drink.get("sugar_content", 0)
                calories = drink.get("calories", 0)
                
                # 健康评分占60%，低糖占20%，低卡占20%
                sugar_score = max(0, 100 - sugar * 2)  # 糖分越低分数越高
                calorie_score = max(0, 100 - calories / 2)  # 热量越低分数越高
                
                drink["recommendation_score"] = health * 0.6 + sugar_score * 0.2 + calorie_score * 0.2
            
            # 按综合评分降序排序
            recommended_drinks = sorted(all_drinks, key=lambda x: x.get("recommendation_score", 0), reverse=True)[:50]
        
        # 使用DeepSeek为前8个推荐生成AI健康建议
        deepseek_agent = get_deepseek_agent()
        
        # 获取用户的健康数据和偏好用于生成AI建议
        today_record = db_tool.get_today_health_record(request.user_id)
        recent_records = db_tool.get_daily_health_records(request.user_id, days=7)
        user_preferences = db_tool.get_user_preferences(request.user_id)
        
        user_context = {
            **profile,
            "today_sugar": today_record.get("total_sugar_intake", 0) if today_record else 0,
            "today_calories": today_record.get("total_calories", 0) if today_record else 0,
            "recent_avg_sugar": sum(r.get("total_sugar_intake", 0) for r in recent_records) / len(recent_records) if recent_records else 0,
            "recent_avg_calories": sum(r.get("total_calories", 0) for r in recent_records) / len(recent_records) if recent_records else 0,
            "has_preferences": len(user_preferences) > 0,
            "preference_count": len(user_preferences)
        }
        
        # 为前8个推荐生成AI健康建议
        logger.info(f"为前8个推荐生成AI健康建议...")
        for i, drink in enumerate(recommended_drinks[:3]):
            try:
                ai_advice = await deepseek_agent.generate_health_advice(
                    drink_info=drink,
                    user_context=user_context,
                    rank=i+1
                )
                drink["ai_health_advice"] = ai_advice
                logger.info(f"生成第{i+1}个推荐的AI建议: {drink['drink_name']}")
            except Exception as e:
                logger.error(f"生成AI建议失败 (第{i+1}个): {e}")
                drink["ai_health_advice"] = "暂无AI健康建议"
        
        rec_strategy = "智能推荐（优先考虑饮品偏好，结合健康档案和健康记录）"
        
        return {
            "success": True,
            "recommendations": recommended_drinks,
            "user_sugar_limit": profile['sugar_limit'],
            "recommendation_strategy": rec_strategy,
            "recommendation_count": len(recommended_drinks)
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"饮品推荐失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.SERVICE_HOST,
        port=settings.SERVICE_PORT,
        reload=True
    )

