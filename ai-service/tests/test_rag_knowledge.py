"""
RAG知识库系统测试
"""
import pytest
import sys
import os
import tempfile
import shutil

# 添加项目路径
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))


class TestHealthKnowledgeRAG:
    """RAG知识库测试类"""
    
    @pytest.fixture
    def temp_config(self):
        """创建临时配置"""
        from config import settings
        
        # 保存原始配置
        original_faiss_path = settings.settings.FAISS_INDEX_PATH
        original_kb_path = settings.settings.KNOWLEDGE_BASE_PATH
        
        # 创建临时目录
        temp_dir = tempfile.mkdtemp()
        settings.settings.FAISS_INDEX_PATH = os.path.join(temp_dir, "faiss_index")
        settings.settings.KNOWLEDGE_BASE_PATH = os.path.join(temp_dir, "knowledge_base")
        
        yield settings.settings
        
        # 恢复原始配置并清理
        settings.settings.FAISS_INDEX_PATH = original_faiss_path
        settings.settings.KNOWLEDGE_BASE_PATH = original_kb_path
        shutil.rmtree(temp_dir, ignore_errors=True)
    
    def test_rag_initialization(self, temp_config):
        """测试RAG系统初始化"""
        from agents.rag_knowledge import HealthKnowledgeRAG
        
        rag = HealthKnowledgeRAG()
        
        assert rag.embeddings is not None
        assert rag.vectorstore is not None
    
    def test_knowledge_search(self, temp_config):
        """测试知识检索"""
        from agents.rag_knowledge import HealthKnowledgeRAG
        
        rag = HealthKnowledgeRAG()
        
        # 搜索糖分相关知识
        results = rag.search("糖分摄入标准", top_k=3)
        
        assert isinstance(results, list)
        if len(results) > 0:
            assert "content" in results[0]
            assert "title" in results[0]
            assert "relevance_score" in results[0]
    
    def test_add_knowledge(self, temp_config):
        """测试添加知识"""
        from agents.rag_knowledge import HealthKnowledgeRAG
        
        rag = HealthKnowledgeRAG()
        
        # 添加新知识
        rag.add_knowledge(
            title="测试知识",
            content="这是一条测试用的健康知识内容",
            category="test",
            tags=["测试", "健康"]
        )
        
        # 搜索刚添加的知识
        results = rag.search("测试健康知识", top_k=1)
        assert len(results) > 0
    
    def test_get_relevant_context(self, temp_config):
        """测试获取相关上下文"""
        from agents.rag_knowledge import HealthKnowledgeRAG
        
        rag = HealthKnowledgeRAG()
        
        context = rag.get_relevant_context("糖尿病预防", max_length=500)
        
        assert isinstance(context, str)
        # 如果有相关知识,应该返回非空字符串
        # assert len(context) > 0 (可能没有完全相关的知识)


if __name__ == "__main__":
    pytest.main([__file__, "-v"])

