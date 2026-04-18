"""
测试数据库查询
"""
from database.database import SessionLocal
from agents.tools.database_query import DatabaseQueryTool

print("=" * 60)
print("测试数据库查询工具")
print("=" * 60)

# 创建数据库会话
db = SessionLocal()

try:
    # 创建查询工具
    db_tool = DatabaseQueryTool(db)
    
    # 测试查询用户档案
    print("\n1. 查询用户档案 (user_id=1):")
    profile = db_tool.get_user_profile(1)
    if profile:
        print(f"   ✓ 成功获取档案:")
        for key, value in profile.items():
            print(f"     - {key}: {value}")
    else:
        print("   ✗ 未找到用户档案")
    
    # 测试查询健康记录
    print("\n2. 查询健康记录 (user_id=1, 最近7天):")
    records = db_tool.get_daily_health_records(1, 7)
    print(f"   找到 {len(records)} 条记录")
    for record in records[:2]:  # 只显示前2条
        print(f"   - {record.get('record_date')}: 糖分{record.get('total_sugar_intake')}g")
    
    # 测试查询饮品偏好
    print("\n3. 查询饮品偏好 (user_id=1):")
    prefs = db_tool.get_all_user_preferences(1)
    print(f"   找到 {len(prefs)} 条偏好记录")
    for pref in prefs[:2]:  # 只显示前2条
        print(f"   - 饮品ID {pref['drink_id']}: 评分{pref['preference_score']}, 饮用{pref['times_consumed']}次")
    
    print("\n" + "=" * 60)
    print("✓ 数据库查询测试完成")
    print("=" * 60)
    
except Exception as e:
    print(f"\n✗ 发生错误: {e}")
    import traceback
    traceback.print_exc()
finally:
    db.close()

