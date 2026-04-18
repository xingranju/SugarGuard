"""
将 Room DatabaseSeeder 的种子数据迁移到 MySQL
运行: python scripts/migrate_seed_to_mysql.py
"""
import pymysql
from datetime import datetime, timedelta
import random

DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'Android_health_db',
    'charset': 'utf8mb4'
}

def get_conn():
    return pymysql.connect(**DB_CONFIG)

def now_str():
    return datetime.now().strftime('%Y-%m-%d %H:%M:%S')

def today_str():
    return datetime.now().strftime('%Y-%m-%d')

def migrate_testuser(conn):
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM users WHERE username='testuser'")
    if cursor.fetchone():
        print("testuser already exists in MySQL, skipping")
        return
    
    from hashlib import sha256
    # Spring Boot uses BCrypt, but for seed data we insert raw (the app uses local auth anyway)
    cursor.execute("""
        INSERT INTO users (username, email, password_hash, phone, avatar_url, gender, birthday, status, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """, (
        'testuser', 'test@sg.com', '$2a$10$dummyhashfortestuser123456789012',
        '13912345678',
        'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop',
        'MALE', '2003-03-20', 'ACTIVE', now_str(), now_str()
    ))
    testuser_id = cursor.lastrowid
    print(f"Inserted testuser with id={testuser_id}")
    return testuser_id

def migrate_demo_user(conn):
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM users WHERE username='demo'")
    if cursor.fetchone():
        print("demo user already exists, skipping")
        return
    cursor.execute("""
        INSERT INTO users (username, email, password_hash, phone, avatar_url, gender, birthday, status, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
    """, (
        'demo', 'demo@sugarguard.com', '$2a$10$dummyhashfordemo123456789012345',
        '13800138000',
        'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop',
        'MALE', '2002-06-15', 'ACTIVE', now_str(), now_str()
    ))
    print(f"Inserted demo user with id={cursor.lastrowid}")

def migrate_drinks(conn):
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM drinks")
    count = cursor.fetchone()[0]
    if count >= 20:
        print(f"drinks already has {count} records, skipping")
        return

    drinks = [
        ("珍珠奶茶", "喜茶", "奶茶", 38, 420, 500, 50, 8, 3, 120, 30, "红茶,牛奶,珍珠,糖浆", "牛奶",
         "https://images.unsplash.com/photo-1558857563-b371033873b8?w=400"),
        ("芋泥波波奶茶", "一点点", "奶茶", 42, 480, 500, 30, 10, 4, 100, 25, "芋泥,奶茶,波波,炼乳", "牛奶",
         "https://images.unsplash.com/photo-1627483262112-039e9a0a0c41?w=400"),
        ("杨枝甘露", "喜茶", "果茶", 35, 350, 500, 10, 5, 2, 50, 40, "芒果,西柚,椰浆,西米", "椰子",
         "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400"),
        ("拿铁咖啡", "星巴克", "咖啡", 12, 190, 350, 150, 7, 8, 170, 55, "浓缩咖啡,牛奶", "牛奶",
         "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=400"),
        ("美式咖啡", "瑞幸", "咖啡", 0, 15, 350, 200, 0, 0.5, 10, 80, "浓缩咖啡,水", None,
         "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400"),
        ("可口可乐", "可口可乐", "碳酸饮料", 35, 140, 330, 34, 0, 0, 45, 20, "碳酸水,高果糖浆,焦糖色", None,
         "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400"),
        ("绿茶", "自泡", "茶饮", 0, 2, 250, 30, 0, 0, 1, 95, "绿茶叶", None,
         "https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=400"),
        ("纯牛奶", "蒙牛", "乳饮", 5, 130, 250, 0, 8, 8, 100, 85, "生牛乳", "牛奶",
         "https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400"),
    ]

    for d in drinks:
        try:
            cursor.execute("""
                INSERT INTO drinks (drink_name, brand, category, sugar_content, calories, volume, 
                caffeine, fat, protein, sodium, health_score, ingredients, allergens, image_url, created_at, updated_at)
                VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
            """, (*d, now_str(), now_str()))
        except Exception as e:
            print(f"  Skip drink {d[0]}: {e}")
    print(f"Inserted {len(drinks)} drinks")

def migrate_health_profile(conn, user_id):
    cursor = conn.cursor()
    cursor.execute("SELECT profile_id FROM user_health_profile WHERE user_id=%s", (user_id,))
    if cursor.fetchone():
        print(f"Health profile for user {user_id} exists, skipping")
        return
    cursor.execute("""
        INSERT INTO user_health_profile (user_id, age, gender, height, weight, 
        health_conditions, activity_level, sugar_limit, calorie_limit, water_goal, created_at, updated_at)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
    """, (user_id, 23, 'male', 172, 65, '无特殊健康问题', 'light', 25, 2000, 2000, now_str(), now_str()))
    print(f"Inserted health profile for user {user_id}")

def migrate_health_records(conn, user_id):
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM daily_health_records WHERE user_id=%s", (user_id,))
    if cursor.fetchone()[0] > 0:
        print(f"Health records for user {user_id} exist, skipping")
        return
    
    moods = ["excellent", "good", "good", "normal", "good", "excellent", "good"]
    for i in range(6, -1, -1):
        date = (datetime.now() - timedelta(days=i)).strftime('%Y-%m-%d')
        cursor.execute("""
            INSERT INTO daily_health_records (user_id, record_date, total_sugar_intake, total_calories,
            water_intake, exercise_minutes, sleep_hours, systolic_bp, diastolic_bp, blood_glucose, weight, mood, created_at)
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
        """, (
            user_id, date,
            round(18 + random.random() * 22, 1),
            round(1400 + random.random() * 600, 0),
            round(1000 + random.random() * 1200, 0),
            round(10 + random.random() * 50, 0),
            round(6 + random.random() * 2.5, 1),
            round(108 + random.random() * 22, 0),
            round(62 + random.random() * 18, 0),
            round(4.2 + random.random() * 2.3, 1),
            round(64 + random.random() * 3, 1),
            moods[i],
            now_str()
        ))
    print(f"Inserted 7 health records for user {user_id}")

def migrate_meal_records(conn, user_id):
    cursor = conn.cursor()
    today = today_str()
    cursor.execute("SELECT COUNT(*) FROM meal_records WHERE user_id=%s AND meal_date=%s", (user_id, today))
    if cursor.fetchone()[0] > 0:
        print(f"Meal records for user {user_id} today exist, skipping")
        return

    meals = [
        ('breakfast', '无糖酸奶 + 全麦面包', 6.0, 230.0, f'{today} 07:30:00',
         'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400'),
        ('lunch', '糖醋排骨盖饭', 25.0, 520.0, f'{today} 12:15:00',
         'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400'),
        ('snack', '杨枝甘露（全糖）', 38.0, 350.0, f'{today} 15:30:00',
         'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400'),
    ]
    for m in meals:
        cursor.execute("""
            INSERT INTO meal_records (user_id, meal_date, meal_time, meal_type, food_name, 
            sugar_content, calories, image_path, created_at)
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)
        """, (user_id, today, m[4], m[0], m[1], m[2], m[3], m[5], now_str()))
    print(f"Inserted 3 meal records for user {user_id}")

def main():
    conn = get_conn()
    try:
        migrate_demo_user(conn)
        testuser_id = migrate_testuser(conn)
        
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM users WHERE username='testuser' ORDER BY id DESC LIMIT 1")
        row = cursor.fetchone()
        if row:
            testuser_id = row[0]
        else:
            testuser_id = 2

        migrate_drinks(conn)
        migrate_health_profile(conn, testuser_id)
        migrate_health_records(conn, testuser_id)
        migrate_meal_records(conn, testuser_id)
        
        conn.commit()
        print("\nMigration completed successfully!")
        
        cursor.execute("SELECT COUNT(*) FROM users")
        print(f"Total users: {cursor.fetchone()[0]}")
        cursor.execute("SELECT COUNT(*) FROM drinks")
        print(f"Total drinks: {cursor.fetchone()[0]}")
        cursor.execute("SELECT COUNT(*) FROM daily_health_records WHERE user_id=%s", (testuser_id,))
        print(f"Health records for testuser: {cursor.fetchone()[0]}")
        cursor.execute("SELECT COUNT(*) FROM meal_records WHERE user_id=%s", (testuser_id,))
        print(f"Meal records for testuser: {cursor.fetchone()[0]}")
        
    except Exception as e:
        conn.rollback()
        print(f"Migration failed: {e}")
        raise
    finally:
        conn.close()

if __name__ == '__main__':
    main()
