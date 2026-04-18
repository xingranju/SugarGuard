"""
RAG (Retrieval-Augmented Generation) 知识库系统
使用FAISS向量数据库 + LangChain实现健康知识检索
"""
from langchain_community.vectorstores import FAISS
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_core.documents import Document
from typing import List, Dict, Optional
import os
import pickle
import logging

from config.settings import settings

logger = logging.getLogger(__name__)

# 设置环境变量,禁用自动下载,仅使用本地模型
os.environ['TRANSFORMERS_OFFLINE'] = '1'
os.environ['HF_HUB_OFFLINE'] = '1'


class HealthKnowledgeRAG:
    """健康知识检索增强生成系统"""
    
    def __init__(self):
        """初始化RAG系统"""
        self.embeddings = None
        self.vectorstore = None
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=500,
            chunk_overlap=50,
            length_function=len,
        )
        
        # 确保目录存在
        os.makedirs(settings.FAISS_INDEX_PATH, exist_ok=True)
        os.makedirs(settings.KNOWLEDGE_BASE_PATH, exist_ok=True)
        
        logger.info("RAG系统初始化开始...")
        self._initialize_embeddings()
        self._load_or_create_vectorstore()
        logger.info("RAG系统初始化完成")
    
    
    def _initialize_embeddings(self):
        """初始化嵌入模型"""
        try:
            logger.info(f"从本地加载嵌入模型: {settings.HF_EMBEDDING_MODEL}")
            self.embeddings = HuggingFaceEmbeddings(
                model_name=settings.HF_EMBEDDING_MODEL,
                cache_folder=settings.HF_MODEL_CACHE_DIR,
                model_kwargs={
                    'device': 'cpu',
                    'local_files_only': True  # 仅使用本地文件,不下载
                },
                encode_kwargs={'normalize_embeddings': True}
            )
            logger.info("嵌入模型从本地加载成功")
        except Exception as e:
            logger.error(f"嵌入模型加载失败: {e}")
            raise
    
    
    def _load_or_create_vectorstore(self):
        """加载或创建向量数据库"""
        index_path = os.path.join(settings.FAISS_INDEX_PATH, "health_knowledge")
        
        if os.path.exists(index_path):
            try:
                logger.info("加载现有向量数据库...")
                self.vectorstore = FAISS.load_local(
                    index_path, 
                    self.embeddings,
                    allow_dangerous_deserialization=True
                )
                logger.info(f"向量数据库加载成功,共有 {self.vectorstore.index.ntotal} 条知识")
            except Exception as e:
                logger.warning(f"加载向量数据库失败: {e}, 将创建新的数据库")
                self._create_initial_vectorstore()
        else:
            logger.info("创建新的向量数据库...")
            self._create_initial_vectorstore()
    
    
    def _create_initial_vectorstore(self):
        """创建初始向量数据库并添加基础健康知识"""
        # 基础健康知识
        initial_knowledge = [
            {
                "title": "糖分摄入与健康",
                "content": """糖分摄入过多会导致多种健康问题。世界卫生组织（WHO）建议,成人和儿童每日游离糖摄入量应控制在总能量摄入的10%以下,最好能进一步限制在5%以下。对于青少年来说,每日糖分摄入最好不超过50克。

过量摄入糖分的危害:
1. 肥胖: 糖分摄入过多会转化为脂肪储存
2. 糖尿病风险: 长期高糖饮食增加2型糖尿病风险
3. 心血管疾病: 高糖饮食与心脏病风险增加相关
4. 牙齿健康: 糖分是导致龋齿的主要原因
5. 皮肤问题: 高糖饮食可能导致痤疮等皮肤问题

建议: 减少含糖饮料摄入,选择水果代替甜食,阅读食品标签关注添加糖含量。""",
                "category": "nutrition",
                "tags": ["糖分", "健康风险", "WHO建议"]
            },
            {
                "title": "青少年健康饮食指南",
                "content": """青少年正处于生长发育的关键时期,需要充足均衡的营养。健康饮食应遵循以下原则:

1. 三餐规律: 按时吃早餐、午餐和晚餐,不要跳过任何一餐
2. 营养均衡: 包括碳水化合物、蛋白质、脂肪、维生素和矿物质
3. 多喝水: 每天至少8杯水(约2000ml),少喝含糖饮料
4. 多吃水果蔬菜: 每天至少5种不同颜色的蔬果
5. 控制零食: 选择健康零食如坚果、酸奶、水果
6. 减少加工食品: 少吃快餐、油炸食品和高糖零食

青少年每日营养需求参考:
- 热量: 2000-2800千卡(根据活动量)
- 蛋白质: 50-75克
- 碳水化合物: 250-350克
- 脂肪: 60-80克
- 糖分: 不超过50克""",
                "category": "nutrition",
                "tags": ["青少年", "饮食指南", "营养需求"]
            },
            {
                "title": "常见饮品的糖分含量",
                "content": """了解常见饮品的糖分含量有助于做出更健康的选择:

高糖饮品(每500ml):
- 可乐: 约52克糖
- 珍珠奶茶: 35-50克糖
- 果汁饮料: 40-50克糖
- 奶盖奶茶: 45-60克糖
- 运动饮料: 30-35克糖

中等糖分饮品:
- 鲜榨果汁: 20-30克糖
- 乳酸菌饮料: 15-25克糖
- 豆浆(加糖): 10-15克糖

低糖/无糖饮品:
- 纯净水: 0克糖
- 无糖茶: 0克糖
- 黑咖啡: 0克糖
- 无糖酸奶: 4-8克糖(天然乳糖)
- 椰子水: 6-9克糖(天然糖分)

建议: 尽量选择低糖或无糖饮品,如果想喝甜味饮料,可以选择无糖版本或自制饮品。""",
                "category": "drinks",
                "tags": ["饮品", "糖分", "营养成分"]
            },
            {
                "title": "糖尿病预防与管理",
                "content": """糖尿病是一种慢性代谢疾病,青少年也可能患糖尿病。预防和管理糖尿病需要注意:

预防措施:
1. 控制体重: 保持健康的BMI(18.5-23.9)
2. 规律运动: 每周至少150分钟中等强度运动
3. 健康饮食: 低糖、低脂、高纤维饮食
4. 定期检查: 每年检查血糖水平
5. 控制压力: 学会管理学习和生活压力

血糖正常值参考:
- 空腹血糖: 3.9-6.1 mmol/L
- 餐后2小时血糖: <7.8 mmol/L
- 糖尿病诊断: 空腹血糖≥7.0 mmol/L 或餐后2小时≥11.1 mmol/L

如果有糖尿病家族史、肥胖或其他风险因素,应更加注意血糖控制和定期检查。""",
                "category": "disease",
                "tags": ["糖尿病", "预防", "血糖"]
            },
            {
                "title": "健康饮品替代方案",
                "content": """用健康饮品替代高糖饮料是控糖的有效方法:

替代方案:
1. 可乐 → 气泡水 + 柠檬片
2. 奶茶 → 无糖茶 + 低脂牛奶
3. 果汁饮料 → 鲜榨果汁(1:1兑水)或整个水果
4. 运动饮料 → 椰子水或淡盐水
5. 甜咖啡 → 美式咖啡 + 少量牛奶

自制健康饮品推荐:
- 柠檬薄荷水: 清爽解渴,富含维C
- 黄瓜姜片水: 促进代谢,低热量
- 玫瑰花茶: 美容养颜,无糖
- 山楂乌梅茶: 开胃消食,微酸
- 冰镇绿茶: 抗氧化,提神醒脑

小贴士: 逐步减少糖分摄入,让味蕾适应清淡口味,3-4周后就会习惯。""",
                "category": "drinks",
                "tags": ["健康替代", "自制饮品", "控糖"]
            },
            {
                "title": "BMI与健康体重",
                "content": """BMI(身体质量指数)是评估体重是否健康的常用指标。

BMI计算公式: BMI = 体重(kg) / [身高(m)]²

BMI分类标准(中国):
- 偏瘦: BMI < 18.5
- 正常: 18.5 ≤ BMI < 24
- 超重: 24 ≤ BMI < 28
- 肥胖: BMI ≥ 28

青少年BMI标准:
青少年的BMI标准需要考虑年龄和性别,通常使用生长曲线图评估。一般来说:
- 正常范围: BMI在第5-85百分位
- 超重: BMI在第85-95百分位
- 肥胖: BMI≥第95百分位

保持健康体重的方法:
1. 均衡饮食,控制热量摄入
2. 每天运动至少1小时
3. 充足睡眠(8-10小时)
4. 减少久坐时间
5. 定期监测体重变化

注意: BMI只是参考指标,还需要考虑肌肉量、骨密度等因素。""",
                "category": "health_metrics",
                "tags": ["BMI", "健康体重", "评估标准"]
            },
            {
                "title": "运动与健康",
                "content": """规律运动对青少年健康至关重要,有助于控制体重和血糖。

青少年运动建议:
- 每天至少60分钟中等到高强度运动
- 每周至少3次高强度有氧运动
- 每周至少3次肌肉力量训练
- 每周至少3次骨骼强化运动

适合青少年的运动:
有氧运动: 跑步、游泳、骑自行车、跳绳、打篮球
力量训练: 俯卧撑、仰卧起坐、引体向上、深蹲
柔韧性训练: 瑜伽、拉伸、体操

运动对血糖的影响:
- 运动可以降低血糖水平
- 提高胰岛素敏感性
- 帮助控制体重
- 改善心血管健康

运动注意事项:
1. 运动前适当进食,避免低血糖
2. 运动时携带糖果以备不时之需
3. 循序渐进,避免过度运动
4. 保持水分摄入
5. 如有糖尿病,运动前后监测血糖""",
                "category": "lifestyle",
                "tags": ["运动", "健康生活", "血糖控制"]
            }
        ]
        
        # 转换为Document对象
        documents = []
        for item in initial_knowledge:
            # 文本分块
            chunks = self.text_splitter.split_text(item["content"])
            for i, chunk in enumerate(chunks):
                doc = Document(
                    page_content=chunk,
                    metadata={
                        "title": item["title"],
                        "category": item["category"],
                        "tags": ",".join(item["tags"]),
                        "chunk_id": i,
                        "source": "initial_knowledge"
                    }
                )
                documents.append(doc)
        
        # 创建向量数据库
        if documents:
            self.vectorstore = FAISS.from_documents(documents, self.embeddings)
            
            # 保存向量数据库
            index_path = os.path.join(settings.FAISS_INDEX_PATH, "health_knowledge")
            self.vectorstore.save_local(index_path)
            logger.info(f"初始知识库创建成功,共 {len(documents)} 个文档块")
        else:
            logger.warning("没有初始知识可添加")
    
    
    def search(self, query: str, top_k: int = 3) -> List[Dict]:
        """
        检索相关知识
        
        Args:
            query: 查询文本
            top_k: 返回top k个结果
            
        Returns:
            相关知识列表
        """
        try:
            if not self.vectorstore:
                logger.warning("向量数据库未初始化")
                return []
            
            # 相似度搜索
            results = self.vectorstore.similarity_search_with_score(query, k=top_k)
            
            knowledge_list = []
            for doc, score in results:
                knowledge_list.append({
                    "content": doc.page_content,
                    "title": doc.metadata.get("title", ""),
                    "category": doc.metadata.get("category", ""),
                    "tags": doc.metadata.get("tags", "").split(","),
                    "relevance_score": float(1 / (1 + score))  # 转换为0-1的相关性分数
                })
            
            logger.info(f"检索到 {len(knowledge_list)} 条相关知识")
            return knowledge_list
            
        except Exception as e:
            logger.error(f"知识检索失败: {e}")
            return []
    
    
    def add_knowledge(self, title: str, content: str, category: str, tags: List[str]):
        """
        添加新知识到向量数据库
        
        Args:
            title: 标题
            content: 内容
            category: 分类
            tags: 标签列表
        """
        try:
            # 文本分块
            chunks = self.text_splitter.split_text(content)
            
            documents = []
            for i, chunk in enumerate(chunks):
                doc = Document(
                    page_content=chunk,
                    metadata={
                        "title": title,
                        "category": category,
                        "tags": ",".join(tags),
                        "chunk_id": i,
                        "source": "user_added"
                    }
                )
                documents.append(doc)
            
            # 添加到向量数据库
            if self.vectorstore:
                self.vectorstore.add_documents(documents)
                
                # 保存更新后的数据库
                index_path = os.path.join(settings.FAISS_INDEX_PATH, "health_knowledge")
                self.vectorstore.save_local(index_path)
                
                logger.info(f"成功添加知识: {title}, 共 {len(documents)} 个文档块")
            else:
                logger.error("向量数据库未初始化,无法添加知识")
                
        except Exception as e:
            logger.error(f"添加知识失败: {e}")
    
    
    def get_relevant_context(self, query: str, max_length: int = 1000) -> str:
        """
        获取相关上下文(用于RAG)
        
        Args:
            query: 查询文本
            max_length: 最大长度
            
        Returns:
            上下文文本
        """
        results = self.search(query, top_k=3)
        
        if not results:
            return ""
        
        context_parts = []
        current_length = 0
        
        for item in results:
            content = item["content"]
            if current_length + len(content) > max_length:
                # 截断
                remaining = max_length - current_length
                if remaining > 100:  # 至少保留100字符
                    context_parts.append(content[:remaining] + "...")
                break
            
            context_parts.append(f"【{item['title']}】\n{content}")
            current_length += len(content)
        
        return "\n\n".join(context_parts)


# 全局实例
_rag_system = None


def get_rag_system() -> HealthKnowledgeRAG:
    """获取RAG系统单例"""
    global _rag_system
    if _rag_system is None:
        _rag_system = HealthKnowledgeRAG()
    return _rag_system

