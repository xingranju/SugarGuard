"""
检查数据库连接和数据
"""
import pymysql
import os
from dotenv import load_dotenv

load_dotenv('config.example.env')

config = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', 3306)),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', '123456'),
    'database': os.getenv('DB_NAME', 'Android_health_db'),
    'charset': 'utf8mb4'
}

print("=" * 60)
print("检查数据库数据")
print("=" * 60)
print(f"连接到: {config['host']}:{config['port']}/{config['database']}\n")

try:
    connection = pymysql.connect(**config)
    cursor = connection.cursor()
    
    # 检查users表
    print("1. 检查users表:")
    cursor.execute("SELECT id, username, email FROM users LIMIT 5")
    results = cursor.fetchall()
    for row in results:
        print(f"   ID: {row[0]}, Username: {row[1]}, Email: {row[2]}")
    
    # 检查user_health_profile表
    print("\n2. 检查user_health_profile表:")
    cursor.execute("SELECT profile_id, user_id, age, gender, height, weight FROM user_health_profile")
    results = cursor.fetchall()
    if results:
        for row in results:
            print(f"   Profile ID: {row[0]}, User ID: {row[1]}, Age: {row[2]}, Gender: {row[3]}, Height: {row[4]}, Weight: {row[5]}")
    else:
        print("   ⚠️  表为空!")
    
    # 检查drinks表
    print("\n3. 检查drinks表:")
    cursor.execute("SELECT COUNT(*) FROM drinks")
    count = cursor.fetchone()[0]
    print(f"   共有 {count} 条饮品记录")
    
    # 检查health_knowledge表
    print("\n4. 检查health_knowledge表:")
    cursor.execute("SELECT COUNT(*) FROM health_knowledge")
    count = cursor.fetchone()[0]
    print(f"   共有 {count} 条健康知识记录")
    
    # 检查daily_health_records表
    print("\n5. 检查daily_health_records表:")
    cursor.execute("SELECT COUNT(*) FROM daily_health_records WHERE user_id = 1")
    count = cursor.fetchone()[0]
    print(f"   用户1共有 {count} 条健康记录")
    
    # 检查user_drink_preferences表
    print("\n6. 检查user_drink_preferences表:")
    cursor.execute("SELECT COUNT(*) FROM user_drink_preferences WHERE user_id = 1")
    count = cursor.fetchone()[0]
    print(f"   用户1共有 {count} 条饮品偏好记录")
    
    print("\n" + "=" * 60)
    print("✅ 数据库检查完成")
    print("=" * 60)
    
except pymysql.Error as e:
    print(f"❌ 数据库错误: {e}")
except Exception as e:
    print(f"❌ 错误: {e}")
    import traceback
    traceback.print_exc()
finally:
    if 'cursor' in locals():
        cursor.close()
    if 'connection' in locals():
        connection.close()

