"""
配置管理模块
"""
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """应用配置"""
    
    # DeepSeek API配置（通过 Silra 聚合器，OpenAI 兼容协议）
    # 真实值以 .env 为准；此处仅作兜底占位，生产环境不应依赖默认值
    DEEPSEEK_API_KEY: str = ""
    DEEPSEEK_BASE_URL: str = "https://api.silra.cn/v1"
    DEEPSEEK_MODEL: str = "deepseek-chat"
    
    # MySQL数据库配置
    DB_HOST: str = "localhost"
    DB_PORT: int = 3306
    DB_NAME: str = "Android_health_db"
    DB_USER: str = "root"
    DB_PASSWORD: str = "123456"
    
    # 服务配置
    SERVICE_HOST: str = "0.0.0.0"
    SERVICE_PORT: int = 8000
    
    # Hugging Face模型配置
    HF_MODEL_CACHE_DIR: str = "./models_cache"
    HF_IMAGE_MODEL: str = "google/vit-base-patch16-224"
    HF_EMBEDDING_MODEL: str = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    
    # FAISS向量库配置
    FAISS_INDEX_PATH: str = "./data/faiss_index"
    KNOWLEDGE_BASE_PATH: str = "./data/knowledge_base"
    
    # 日志配置
    LOG_LEVEL: str = "INFO"
    LOG_FILE: str = "./logs/ai_service.log"
    
    @property
    def database_url(self) -> str:
        """获取数据库连接URL"""
        return f"mysql+pymysql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
    
    class Config:
        env_file = ".env"
        env_file_encoding = 'utf-8'
        case_sensitive = True


# 全局配置实例
settings = Settings()

