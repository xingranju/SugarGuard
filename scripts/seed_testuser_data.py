# -*- coding: utf-8 -*-
"""Seed demo data for testuser in MySQL"""
import mysql.connector
from datetime import datetime, timedelta, date
import random
import json

DB_CONFIG = {
    'host': 'localhost', 'port': 3306,
    'database': 'Android_health_db', 'user': 'root', 'password': '123456'
}

def get_connection():
    return mysql.connector.connect(**DB_CONFIG)

def main():
    conn = get_connection()
    cursor = conn.cursor()
    print("=" * 50)

    cursor.execute("SELECT id, username FROM users WHERE username = 'testuser'")
    row = cursor.fetchone()
    if not row:
        print("testuser not found!")
        return
    uid = row[0]
    print(f"testuser id={uid}")

    # 1. Update user profile (avatar, phone, birthday)
    avatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop&crop=face"
    cursor.execute("""
        UPDATE users SET avatar_url=%s, phone='13800138000', gender='male',
        birthday='2004-06-15', updated_at=NOW() WHERE id=%s
    """, (avatar, uid))
    conn.commit()
    print("[OK] User profile updated")

    # 2. Health profile
    cursor.execute("DELETE FROM user_health_profile WHERE user_id=%s", (uid,))
    cursor.execute("""
        INSERT INTO user_health_profile
        (user_id, age, gender, height, weight, health_conditions, allergies,
         activity_level, sugar_limit, calorie_limit, water_goal, created_at, updated_at)
        VALUES (%s, 21, 'male', 175.0, 68.0, NULL, NULL,
                'moderate', 25.0, 2200.0, 2000.0, NOW(), NOW())
    """, (uid,))
    conn.commit()
    print("[OK] Health profile created")

    # 3. 30-day health records
    cursor.execute("DELETE FROM daily_health_records WHERE user_id=%s", (uid,))
    today = date.today()
    for i in range(30):
        d = today - timedelta(days=29-i)
        sugar = round(random.uniform(12 if i > 20 else 15, 25 if i > 20 else 35), 1)
        cal = round(random.uniform(1600, 2400))
        water = round(random.uniform(1200, 2500))
        exercise = round(random.uniform(0, 60))
        sleep = round(random.uniform(6, 9), 1)
        sys_bp = round(random.uniform(110, 130))
        dia_bp = round(random.uniform(65, 85))
        glucose = round(random.uniform(4.5, 6.5), 1)
        weight = round(67.5 + random.uniform(-1, 1), 1)
        mood = random.choice(['excellent','good','good','good','normal','normal','bad'])
        cursor.execute("""
            INSERT INTO daily_health_records
            (user_id, record_date, total_sugar_intake, total_calories, water_intake,
             exercise_minutes, sleep_hours, systolic_bp, diastolic_bp, blood_glucose,
             weight, mood, created_at)
            VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,NOW())
        """, (uid, d.isoformat(), sugar, cal, water, exercise, sleep, sys_bp, dia_bp, glucose, weight, mood))
    conn.commit()
    print("[OK] 30 health records created")

    # 4. Meal records (30 days)
    cursor.execute("DELETE FROM meal_records WHERE user_id=%s", (uid,))
    meals_tpl = [
        ("breakfast","07:30","yogurt+bread",6.0,280,"https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400"),
        ("breakfast","08:00","egg+oatmeal",3.0,220,"https://images.unsplash.com/photo-1525351484163-7529414344d8?w=400"),
        ("breakfast","07:45","sandwich+coffee",5.0,310,"https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400"),
        ("lunch","12:15","chicken_salad",4.0,350,"https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400"),
        ("lunch","12:00","rice+fish",8.0,420,"https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400"),
        ("lunch","12:30","tomato_noodle",12.0,480,"https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=400"),
        ("lunch","11:45","veggie_wrap",6.0,380,"https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400"),
        ("dinner","18:30","broccoli+tofu",3.0,250,"https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=400"),
        ("dinner","19:00","salmon+quinoa",5.0,420,"https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400"),
        ("snack","15:30","blueberry+walnut",8.0,150,"https://images.unsplash.com/photo-1498557850523-fd3d118b962e?w=400"),
        ("snack","16:00","apple+yogurt",12.0,180,"https://images.unsplash.com/photo-1568702846914-96b305d2ead1?w=400"),
        ("snack","10:30","mixed_nuts",3.0,160,"https://images.unsplash.com/photo-1599599810694-b5b37304c041?w=400"),
    ]
    food_names_cn = {
        "yogurt+bread": "\u65e0\u7cd6\u9178\u5976+\u5168\u9ea6\u9762\u5305",
        "egg+oatmeal": "\u6c34\u716e\u9e21\u86cb+\u71d5\u9ea6\u7ca5",
        "sandwich+coffee": "\u5168\u9ea6\u4e09\u660e\u6cbb+\u9ed1\u5496\u5561",
        "chicken_salad": "\u9e21\u80f8\u8089\u6c99\u62c9",
        "rice+fish": "\u7cdf\u7c73\u996d+\u6e05\u84b8\u9c88\u9c7c",
        "tomato_noodle": "\u756a\u8304\u725b\u8089\u9762",
        "veggie_wrap": "\u852c\u83dc\u9e21\u8089\u5377",
        "broccoli+tofu": "\u6e05\u7092\u897f\u5170\u82b1+\u8c46\u8150",
        "salmon+quinoa": "\u4e09\u6587\u9c7c\u914d\u85dc\u9ea6",
        "blueberry+walnut": "\u84dd\u8393+\u6838\u6843",
        "apple+yogurt": "\u82f9\u679c+\u65e0\u7cd6\u9178\u5976",
        "mixed_nuts": "\u575a\u679c\u6df7\u5408",
    }
    count = 0
    for i in range(30):
        d = today - timedelta(days=29-i)
        n = random.choice([3, 3, 4, 4, 4, 5])
        day_meals = random.sample(meals_tpl, min(n, len(meals_tpl)))
        for m in day_meals:
            mtype, mtime_str, fname, sugar, cal, img = m
            h, mi = mtime_str.split(":")
            meal_dt = datetime(d.year, d.month, d.day, int(h), int(mi), 0)
            s = round(max(0, sugar + random.uniform(-2, 2)), 1)
            c = round(max(0, cal + random.uniform(-30, 30)))
            cn_name = food_names_cn.get(fname, fname)
            cursor.execute("""
                INSERT INTO meal_records
                (user_id, meal_date, meal_time, meal_type, food_name,
                 sugar_content, calories, image_path, created_at)
                VALUES (%s,%s,%s,%s,%s,%s,%s,%s,NOW())
            """, (uid, d.isoformat(), meal_dt, mtype, cn_name, s, c, img))
            count += 1
    conn.commit()
    print(f"[OK] {count} meal records created")

    # 5. Drink preferences (uses drink_id + preference_score)
    cursor.execute("DELETE FROM user_drink_preferences WHERE user_id=%s", (uid,))
    cursor.execute("SELECT drink_id FROM drinks LIMIT 10")
    drink_ids = [r[0] for r in cursor.fetchall()]
    for did in drink_ids[:7]:
        score = random.choice([3, 4, 5])
        times = random.randint(1, 15)
        cursor.execute("""
            INSERT INTO user_drink_preferences
            (user_id, drink_id, preference_score, times_consumed, consumption_count, created_at, updated_at)
            VALUES (%s,%s,%s,%s,%s,NOW(),NOW())
        """, (uid, did, score, times, times))
    conn.commit()
    print(f"[OK] {min(len(drink_ids),7)} drink preferences created")

    # 6. AI conversation history (message + response format)
    cursor.execute("DELETE FROM conversation_history WHERE user_id=%s", (uid,))
    convs = [
        ("\u63a8\u8350\u4f4e\u7cd6\u996e\u54c1",
         "\u6839\u636e\u4f60\u7684\u53e3\u5473\u504f\u597d\uff0c\u63a8\u8350\u4ee5\u4e0b\u4f4e\u7cd6\u996e\u54c1\uff1a\n\n1. **\u4f4e\u7cd6\u62ff\u94c1** - \u542b\u7cd6 8g\n2. **\u65e0\u7cd6\u9178\u5976** - \u542b\u7cd6 0g\n3. **\u6c14\u6ce1\u6c34** - \u542b\u7cd6 0g",
         "recommend", None, timedelta(hours=2)),
        ("\u4eca\u5929\u5403\u4e86\u4e00\u676f\u5976\u8336\uff0c\u5927\u6982\u591a\u5c11\u7cd6\uff1f",
         "\u4e00\u676f\u666e\u901a\u5976\u8336(500ml)\u542b\u7cd6\u91cf\u5927\u7ea6\u5728 **35-50g** \u4e4b\u95f4\u3002\u5efa\u8bae\u4e0b\u6b21\u9009\u62e9\"\u5c11\u7cd6\"\u6216\"\u4e09\u5206\u7cd6\"\uff0c\u5927\u7ea6 15-20g\u3002",
         "nutrition_query", None, timedelta(hours=1)),
        ("\u5982\u4f55\u63a7\u5236\u8840\u7cd6\uff1f",
         "\u63a7\u5236\u8840\u7cd6\u7684\u51e0\u4e2a\u5173\u952e\u5efa\u8bae\uff1a\n\n\u201c\u996e\u98df\u201d\u9009\u62e9\u4f4eGI\u98df\u7269\uff0c\u589e\u52a0\u81b3\u98df\u7ea4\u7ef4\u3002\n\u201c\u8fd0\u52a8\u201d\u6bcf\u5929\u81f3\u5c1130\u5206\u949f\u4e2d\u7b49\u5f3a\u5ea6\u3002\n\u201c\u4f5c\u606f\u201d\u4fdd\u8bc17-8\u5c0f\u65f6\u7761\u7720\u3002",
         "health_advice", None, timedelta(minutes=30)),
        ("\u5e2e\u6211\u5206\u6790\u8fd9\u5468\u7684\u996e\u98df\u60c5\u51b5",
         "\u672c\u5468\u5206\u6790: \u5e73\u5747\u6bcf\u65e5\u7cd6\u5206\u6444\u516522.3g\uff08\u8fbe\u6807\uff09\uff0c\u86cb\u767d\u8d28\u6444\u5165\u5145\u8db3\u3002\u5468\u4e09\u52a0\u9910\u7cd6\u5206\u504f\u9ad8(38g)\u3002\u5efa\u8bae\u52a0\u9910\u65f6\u53ef\u4ee5\u7528\u575a\u679c\u66ff\u4ee3\u751c\u54c1\u3002",
         "analysis", None, timedelta(days=1)),
    ]
    now = datetime.now()
    for msg, resp, intent, ctx, td in convs:
        t = now - td
        cursor.execute("""
            INSERT INTO conversation_history
            (user_id, message, response, intent, context_data, feedback, created_at)
            VALUES (%s,%s,%s,%s,%s,%s,%s)
        """, (uid, msg, resp, intent, json.dumps(ctx) if ctx else None, 1, t))
    conn.commit()
    print(f"[OK] {len(convs)} conversations created")

    print("=" * 50)
    print(f"All data seeded for testuser (id={uid})")
    print("=" * 50)
    cursor.close()
    conn.close()

if __name__ == "__main__":
    main()
