"""
知识库检索工具
用于LangChain Agent调用
"""
from langchain_core.tools import Tool
from typing import List
import logging

logger = logging.getLogger(__name__)


def create_knowledge_search_tool(rag_system) -> Tool:
    """
    创建知识库检索工具
    
    Args:
        rag_system: RAG系统实例
        
    Returns:
        LangChain Tool
    """
    
    def search_knowledge(query: str) -> str:
        """
        检索健康知识库
        
        Args:
            query: 查询问题
            
        Returns:
            相关知识内容
        """
        try:
            logger.info(f"知识库检索: {query}")
            context = rag_system.get_relevant_context(query, max_length=800)
            
            if context:
                return f"找到相关健康知识:\n\n{context}"
            else:
                return "未找到相关知识,请基于你的专业知识回答。"
                
        except Exception as e:
            logger.error(f"知识检索失败: {e}")
            return "知识库检索出现问题,请基于已知信息回答。"
    
    return Tool(
        name="search_health_knowledge",
        func=search_knowledge,
        description="""当需要查询健康、营养、疾病预防等专业知识时使用此工具。
输入: 关于健康的问题,如"糖尿病如何预防"、"青少年每日糖分摄入标准"等
输出: 相关的健康知识内容"""
    )


def create_drink_info_tool(db_tool) -> Tool:
    """
    创建饮品信息查询工具
    
    Args:
        db_tool: 数据库查询工具
        
    Returns:
        LangChain Tool
    """
    
    def search_drink_info(drink_name: str) -> str:
        """
        查询饮品营养信息
        
        Args:
            drink_name: 饮品名称
            
        Returns:
            饮品信息
        """
        try:
            logger.info(f"查询饮品信息: {drink_name}")
            drink = db_tool.search_drink_by_name(drink_name)
            
            if drink:
                return f"""饮品信息:
名称: {drink['drink_name']}
品牌: {drink.get('brand', '未知')}
类别: {drink.get('category', '未知')}
糖分: {drink['sugar_content']}g
热量: {drink['calories']}kcal
咖啡因: {drink.get('caffeine', 0)}mg
健康评分: {drink.get('health_score', 50)}/100
描述: {drink.get('description', '暂无描述')}"""
            else:
                return f"数据库中未找到'{drink_name}'的详细信息。"
                
        except Exception as e:
            logger.error(f"饮品信息查询失败: {e}")
            return "查询饮品信息时出现问题。"
    
    return Tool(
        name="get_drink_nutrition",
        func=search_drink_info,
        description="""当用户询问具体饮品的营养成分、糖分含量等信息时使用此工具。
输入: 饮品名称,如"可乐"、"珍珠奶茶"、"芝芝莓莓"等
输出: 该饮品的详细营养信息"""
    )


def create_health_calculation_tool() -> Tool:
    """
    创建健康计算工具
    
    Returns:
        LangChain Tool
    """
    
    def calculate_health_metrics(input_str: str) -> str:
        """
        计算健康指标
        
        Args:
            input_str: 格式为"BMI:身高,体重" 或 "热量:年龄,性别,体重,身高,活动量"
            
        Returns:
            计算结果
        """
        try:
            if input_str.startswith("BMI:"):
                # BMI计算
                parts = input_str.replace("BMI:", "").split(",")
                if len(parts) == 2:
                    height_cm = float(parts[0].strip())
                    weight_kg = float(parts[1].strip())
                    height_m = height_cm / 100
                    bmi = weight_kg / (height_m ** 2)
                    
                    if bmi < 18.5:
                        status = "偏瘦"
                    elif bmi < 24:
                        status = "正常"
                    elif bmi < 28:
                        status = "超重"
                    else:
                        status = "肥胖"
                    
                    return f"BMI: {bmi:.1f} ({status})"
            
            return "计算格式不正确,请使用'BMI:身高,体重'格式"
            
        except Exception as e:
            logger.error(f"健康指标计算失败: {e}")
            return "计算时出现错误"
    
    return Tool(
        name="calculate_health_metrics",
        func=calculate_health_metrics,
        description="""计算健康指标,如BMI等。
输入格式: "BMI:身高(cm),体重(kg)" 例如 "BMI:170,65"
输出: 计算结果和健康评估"""
    )

