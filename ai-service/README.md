# SugarGuard AI Service

青少年智能控糖助手 - AI服务后端

## 技术栈

- **Python**: 3.10+
- **FastAPI**: Web框架
- **LangChain**: 智能体框架（待完整集成）
- **Hugging Face Transformers**: 图像识别
- **MySQL**: 数据存储
- **SQLAlchemy**: ORM

## 快速开始

### 1. 安装依赖

```bash
cd ai-service
pip install -r requirements.txt
```

### 2. 配置环境变量

复制配置文件并修改：
```bash
cp config.example.env .env
```

编辑 `.env` 文件，配置以下内容：
- DeepSeek API密钥
- MySQL数据库连接信息

### 3. 启动服务

```bash
python main.py
```

服务将在 `http://localhost:8000` 启动

### 4. 查看API文档

访问：`http://localhost:8000/docs`

## API端点

### 1. 饮品识别
```
POST /api/recognize-drink
Content-Type: multipart/form-data

参数:
- file: 图片文件
- user_id: 用户ID

返回: 识别结果、营养信息、健康建议
```

### 2. 健康分析
```
GET /api/health-analysis/{user_id}?days=7

返回: BMI分析、糖分评估、健康记录
```

### 3. 智能对话
```
POST /api/chat
Content-Type: application/json

{
  "user_id": 1,
  "message": "如何控制糖分摄入？"
}

返回: 对话回复、意图识别、风险检测
```

### 4. 饮品推荐
```
GET /api/recommend-drinks/{user_id}

返回: 推荐饮品列表
```

## 项目结构

```
ai-service/
├── main.py                 # FastAPI主应用
├── config/
│   └── settings.py        # 配置管理
├── database/
│   ├── models.py          # 数据库模型
│   └── database.py        # 数据库连接
├── agents/
│   └── tools/
│       ├── image_recognition.py    # 图像识别工具
│       ├── database_query.py       # 数据库查询工具
│       └── health_assessment.py    # 健康评估工具
├── requirements.txt       # Python依赖
└── README.md             # 说明文档
```

## 开发状态

### ✅ 已完成
- [x] FastAPI框架搭建
- [x] 数据库模型设计
- [x] 图像识别工具（Hugging Face ViT）
- [x] 健康评估工具（BMI、糖分评估）
- [x] 数据库查询工具
- [x] 基础API端点

### 🚧 待完成
- [ ] DeepSeek API集成
- [ ] LangChain Agent完整实现
- [ ] RAG知识库（FAISS）
- [ ] Scrapy爬虫
- [ ] 协同过滤推荐算法
- [ ] 单元测试

## 与Spring Boot集成

Spring Boot后端（端口8080）将转发AI请求到Python服务（端口8000）：

```
Android App (前端)
    ↓
Spring Boot API (8080) - 用户认证、主业务逻辑
    ↓
Python AI Service (8000) - AI智能体服务
```

## 注意事项

1. **首次运行需要下载Hugging Face模型**，可能需要一些时间
2. **确保MySQL数据库已启动**并且配置正确
3. **生产环境建议使用Gunicorn或uWSGI**部署
4. **图片上传限制为10MB**

## 故障排除

### 模型下载失败
如果Hugging Face模型下载失败，可以：
1. 设置国内镜像：`export HF_ENDPOINT=https://hf-mirror.com`
2. 手动下载模型到 `models_cache` 目录

### 数据库连接失败
检查：
1. MySQL服务是否启动
2. 数据库名称、用户名、密码是否正确
3. 是否有创建数据库的权限

## 后续计划

1. **第一优先级**: 集成DeepSeek API实现智能对话
2. **第二优先级**: 实现RAG知识库
3. **第三优先级**: Scrapy爬虫采集饮品数据
4. **第四优先级**: 协同过滤推荐算法

