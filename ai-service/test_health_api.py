"""
单独测试健康分析API
"""
import requests
import json
import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

BASE_URL = "http://localhost:8000"

print("测试健康分析API...")
print("=" * 60)

# 测试用户1的健康分析
print("\n1. 测试 GET /api/health-analysis/1")
response = requests.get(f"{BASE_URL}/api/health-analysis/1", params={"days": 7})
print(f"状态码: {response.status_code}")
print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")

# 测试饮品推荐
print("\n2. 测试 POST /api/recommend-drinks")
response = requests.post(
    f"{BASE_URL}/api/recommend-drinks",
    json={"user_id": 1, "strategy": "mixed", "limit": 3}
)
print(f"状态码: {response.status_code}")
print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")

print("\n" + "=" * 60)

