# -*- coding: utf-8 -*-
"""Extend testuser data to 6 months for analysis views"""
import mysql.connector
from datetime import datetime, timedelta, date
import random

conn = mysql.connector.connect(
    host='localhost', port=3306,
    database='Android_health_db', user='root', password='123456',
    charset='utf8mb4'
)
c = conn.cursor()
uid = 20
today = date.today()

# Delete old records and recreate for full 180 days
c.execute("DELETE FROM daily_health_records WHERE user_id=%s", (uid,))
c.execute("DELETE FROM meal_records WHERE user_id=%s", (uid,))

meals_pool = [
    ("breakfast", "07:30", "无糖酸奶+全麦面包", 6.0, 280, "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400"),
    ("breakfast", "08:00", "水煮鸡蛋+燕麦粥", 3.0, 220, "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=400"),
    ("breakfast", "07:45", "全麦三明治+黑咖啡", 5.0, 310, "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400"),
    ("lunch", "12:15", "鸡胸肉沙拉", 4.0, 350, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400"),
    ("lunch", "12:00", "糙米饭+清蒸鲈鱼", 8.0, 420, "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400"),
    ("lunch", "12:30", "番茄牛肉面", 12.0, 480, "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400"),
    ("lunch", "11:45", "蔬菜鸡肉卷", 6.0, 380, "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400"),
    ("dinner", "18:30", "清炒西兰花+豆腐", 3.0, 250, "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400"),
    ("dinner", "19:00", "三文鱼配藜麦", 5.0, 420, "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400"),
    ("snack", "15:30", "蓝莓+核桃", 8.0, 150, "https://images.unsplash.com/photo-1498557850523-fd3d118b962e?w=400"),
    ("snack", "16:00", "苹果+无糖酸奶", 12.0, 180, "https://images.unsplash.com/photo-1505253758473-96b7015fcd40?w=400"),
    ("snack", "10:30", "坚果混合", 3.0, 160, "https://images.unsplash.com/photo-1599599810694-b5b37304c041?w=400"),
    ("lunch", "12:20", "麻婆豆腐盖饭", 15.0, 520, "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400"),
    ("dinner", "18:00", "白灼虾+蒸蛋", 2.0, 280, "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400"),
    ("snack", "20:00", "杨枝甘露", 38.0, 320, "https://images.unsplash.com/photo-1505253758473-96b7015fcd40?w=400"),
]

meal_count = 0
health_count = 0

for i in range(180):
    d = today - timedelta(days=179 - i)

    # Simulate gradual improvement: earlier months have higher sugar
    month_offset = i / 30.0
    base_sugar_adj = max(0, 8 - month_offset)

    # 3-5 meals per day
    n = random.choice([3, 3, 4, 4, 4, 5])
    day_meals = random.sample(meals_pool, min(n, len(meals_pool)))
    day_sugar = 0.0
    day_cal = 0.0

    for m in day_meals:
        mtype, mtime_str, fname, sugar, cal, img = m
        h, mi = mtime_str.split(":")
        meal_dt = datetime(d.year, d.month, d.day, int(h), int(mi), 0)
        s = round(max(0, sugar + random.uniform(-2, 2) + base_sugar_adj * 0.3), 1)
        cl = round(max(0, cal + random.uniform(-30, 30)))
        day_sugar += s
        day_cal += cl
        c.execute("""
            INSERT INTO meal_records
            (user_id, meal_date, meal_time, meal_type, food_name,
             sugar_content, calories, image_path, created_at)
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,NOW())
        """, (uid, d.isoformat(), meal_dt, mtype, fname, s, cl, img))
        meal_count += 1

    # Health record
    exercise = round(random.uniform(0, 60))
    sleep = round(random.uniform(6, 9), 1)
    sys_bp = round(random.uniform(110, 130))
    dia_bp = round(random.uniform(65, 85))
    glucose = round(random.uniform(4.5, 6.5), 1)
    weight = round(68.5 - month_offset * 0.3 + random.uniform(-0.5, 0.5), 1)
    water = round(random.uniform(1200, 2500))
    mood = random.choice(['excellent', 'good', 'good', 'good', 'normal', 'normal', 'bad'])

    c.execute("""
        REPLACE INTO daily_health_records
        (user_id, record_date, total_sugar_intake, total_calories, water_intake,
         exercise_minutes, sleep_hours, systolic_bp, diastolic_bp, blood_glucose,
         weight, mood, created_at)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,NOW())
    """, (uid, d.isoformat(), round(day_sugar, 1), round(day_cal), water, exercise,
          sleep, sys_bp, dia_bp, glucose, weight, mood))
    health_count += 1

conn.commit()
print(f"[OK] {meal_count} meal records (180 days)")
print(f"[OK] {health_count} health records (180 days)")
c.close()
conn.close()
