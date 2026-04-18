"""
简单的API测试脚本
用于测试SugarGuard AI服务的各个端点
"""
import requests
import json
from pathlib import Path
import sys
import io

# 设置标准输出编码为UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# API基础URL
BASE_URL = "http://localhost:8000"

def test_health_check():
    """测试健康检查端点"""
    print("=" * 50)
    print("测试 1: 健康检查")
    print("=" * 50)
    
    response = requests.get(f"{BASE_URL}/health")
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.json()}")
    print()

def test_recognize_drink(image_path: str, user_id: int = 1):
    """测试饮品识别"""
    print("=" * 50)
    print("测试 2: 饮品识别")
    print("=" * 50)
    
    if not Path(image_path).exists():
        print(f"❌ 图片文件不存在: {image_path}")
        return
    
    with open(image_path, 'rb') as f:
        files = {'file': (Path(image_path).name, f, 'image/webp')}  # 注意参数名是 'file' 不是 'image'
        data = {'user_id': user_id}
        
        response = requests.post(
            f"{BASE_URL}/api/recognize-drink",
            files=files,
            data=data
        )
    
    print(f"状态码: {response.status_code}")
    print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
    print()

def test_chat(message: str, user_id: int = 1):
    """测试健康问答"""
    print("=" * 50)
    print("测试 3: 健康问答")
    print("=" * 50)
    
    data = {
        "user_id": user_id,
        "message": message
    }
    
    response = requests.post(
        f"{BASE_URL}/api/chat",
        json=data
    )
    
    print(f"状态码: {response.status_code}")
    print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
    print()

def test_recommend_drinks(user_id: int = 1, limit: int = 5):
    """测试饮品推荐"""
    print("=" * 50)
    print("测试 4: 饮品推荐")
    print("=" * 50)
    
    data = {
        "user_id": user_id,
        "strategy": "mixed",
        "limit": limit
    }
    
    response = requests.post(
        f"{BASE_URL}/api/recommend-drinks",
        json=data
    )
    
    print(f"状态码: {response.status_code}")
    print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
    print()

def test_health_analysis(user_id: int = 1):
    """测试健康分析"""
    print("=" * 50)
    print("测试 5: 健康分析")
    print("=" * 50)
    
    response = requests.get(
        f"{BASE_URL}/api/health-analysis/{user_id}",
        params={"days": 7}
    )
    
    print(f"状态码: {response.status_code}")
    print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
    print()

if __name__ == "__main__":
    print("\n")
    print("🧪 开始测试 SugarGuard AI 服务")
    print("=" * 50)
    print()
    
    try:
        # 1. 健康检查
        test_health_check()
        
        # 2. 饮品识别 (需要提供图片路径)
        image_path = r"C:\Users\xingranju\Desktop\OIP (3).webp"
        if Path(image_path).exists():
            test_recognize_drink(image_path, user_id=4)
        else:
            print(f"⚠️  跳过饮品识别测试: 图片不存在 {image_path}\n")
        
        # 3. 健康问答
        test_chat("我如何避免糖尿病？", user_id=1)
        
        # 4. 饮品推荐
        test_recommend_drinks(user_id=4, limit=3)
        
        # 5. 健康分析
        test_health_analysis(user_id=4)
        
        print("=" * 50)
        print("✅ 所有测试完成!")
        print("=" * 50)
        
    except requests.exceptions.ConnectionError:
        print("❌ 错误: 无法连接到AI服务")
        print("请确保服务已启动: python main.py")
    except Exception as e:
        print(f"❌ 测试失败: {e}")
        import traceback
        traceback.print_exc()

