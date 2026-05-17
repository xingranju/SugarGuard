"""
为 testuser 插入近半年饮食日记展示数据
"""
import pymysql
import random
from datetime import datetime, timedelta, date

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "Android_health_db",
    "charset": "utf8mb4"
}

FOODS = {
    "breakfast": [
        ("牛奶", 66, 5.0, 250),
        ("豆浆", 31, 1.2, 300),
        ("全麦面包", 247, 4.0, 80),
        ("煎蛋", 155, 1.1, 60),
        ("小米粥", 46, 0.5, 300),
        ("包子", 220, 3.0, 100),
        ("燕麦片", 367, 1.0, 50),
        ("酸奶", 72, 12.0, 200),
        ("香蕉", 89, 12.0, 120),
        ("三明治", 250, 5.0, 150),
        ("鸡蛋饼", 195, 2.5, 120),
        ("玉米", 96, 3.2, 200),
        ("花卷", 211, 2.0, 80),
        ("馒头", 221, 1.8, 100),
        ("八宝粥", 82, 8.0, 250),
    ],
    "lunch": [
        ("米饭+红烧肉", 580, 5.0, 350),
        ("番茄炒蛋+米饭", 380, 6.0, 300),
        ("宫保鸡丁+米饭", 450, 8.0, 350),
        ("鱼香肉丝+米饭", 420, 7.5, 350),
        ("青椒肉丝+米饭", 400, 4.5, 300),
        ("麻婆豆腐+米饭", 370, 3.0, 300),
        ("西红柿牛腩+米饭", 480, 5.5, 350),
        ("清炒时蔬+米饭", 280, 2.0, 300),
        ("糖醋排骨+米饭", 520, 15.0, 350),
        ("回锅肉+米饭", 510, 6.0, 350),
        ("牛肉面", 450, 3.0, 400),
        ("鸡腿饭", 550, 4.0, 380),
        ("沙拉+鸡胸肉", 320, 3.5, 300),
        ("饺子", 400, 2.5, 300),
        ("炒面", 380, 4.0, 300),
        ("盖浇饭", 500, 6.0, 350),
        ("黄焖鸡+米饭", 460, 5.0, 350),
        ("土豆烧牛肉+米饭", 490, 4.0, 350),
    ],
    "dinner": [
        ("蔬菜汤+馒头", 220, 2.0, 300),
        ("清蒸鱼+米饭", 350, 1.5, 300),
        ("炒青菜+粥", 180, 1.0, 300),
        ("紫菜蛋花汤+面条", 280, 2.5, 350),
        ("水煮虾+米饭", 320, 1.0, 300),
        ("白灼西兰花+鸡胸肉", 250, 1.5, 280),
        ("凉拌黄瓜+小米粥", 150, 1.0, 300),
        ("玉米排骨汤+米饭", 380, 3.0, 350),
        ("酸辣土豆丝+米饭", 300, 2.5, 300),
        ("清炒豆角+米饭", 280, 2.0, 300),
        ("番茄鸡蛋面", 320, 5.0, 350),
        ("烤红薯", 90, 6.0, 200),
        ("火锅(蔬菜为主)", 400, 5.0, 400),
        ("砂锅粥", 200, 3.0, 350),
    ],
    "snack": [
        ("苹果", 52, 10.0, 200),
        ("橙子", 47, 9.0, 180),
        ("葡萄", 69, 16.0, 150),
        ("草莓", 33, 5.0, 150),
        ("坚果", 607, 4.0, 30),
        ("酸奶", 72, 12.0, 150),
        ("奶茶", 200, 35.0, 500),
        ("可乐", 42, 11.0, 330),
        ("果汁", 45, 10.0, 250),
        ("饼干", 430, 18.0, 50),
        ("蛋糕", 350, 25.0, 80),
        ("冰淇淋", 207, 22.0, 100),
        ("巧克力", 546, 48.0, 40),
        ("西瓜", 30, 6.0, 300),
        ("蓝莓", 57, 10.0, 100),
        ("无糖茶", 1, 0.0, 350),
        ("黑咖啡", 2, 0.0, 250),
        ("柠檬水", 15, 2.0, 300),
    ],
}

HEALTH_ADVICES = [
    "注意控制糖分摄入，建议选择低糖食物。",
    "这款食物营养均衡，是不错的选择！",
    "蛋白质含量较高，有助于增强体质。",
    "建议搭配蔬菜一起食用，营养更全面。",
    "热量适中，适合日常食用。",
    "含糖量较高，建议适量食用。",
    "富含膳食纤维，有助于消化。",
    "低脂低糖，非常健康的选择！",
    "建议控制食用量，避免摄入过多热量。",
    "维生素含量丰富，对健康有益。",
]


def get_user_id(cursor):
    cursor.execute("SELECT id FROM users WHERE username = 'testuser'")
    row = cursor.fetchone()
    if row:
        return row[0]
    return None


def meal_time_for(d: date, meal_type: str) -> datetime:
    hour_ranges = {
        "breakfast": (7, 9),
        "lunch": (11, 13),
        "dinner": (17, 19),
        "snack": (14, 16),
    }
    h_min, h_max = hour_ranges[meal_type]
    h = random.randint(h_min, h_max)
    m = random.randint(0, 59)
    s = random.randint(0, 59)
    return datetime(d.year, d.month, d.day, h, m, s)


def generate_records(user_id: int):
    records = []
    end_date = date(2026, 5, 17)
    start_date = end_date - timedelta(days=180)

    current = start_date
    while current <= end_date:
        day_meals = ["breakfast", "lunch", "dinner"]
        if random.random() < 0.5:
            day_meals.append("snack")
        if random.random() < 0.15:
            day_meals.append("snack")

        for mt in day_meals:
            food_name, cal_per_100, sugar_per_100, portion = random.choice(FOODS[mt])

            variation = random.uniform(0.8, 1.2)
            actual_portion = round(portion * variation, 1)
            actual_cal = round(cal_per_100 * actual_portion / 100, 1)
            actual_sugar = round(sugar_per_100 * actual_portion / 100, 1)

            mt_time = meal_time_for(current, mt)
            advice = random.choice(HEALTH_ADVICES) if random.random() < 0.6 else None

            records.append((
                user_id,
                current.isoformat(),
                mt_time.strftime("%Y-%m-%d %H:%M:%S"),
                mt,
                food_name,
                actual_portion,
                actual_cal,
                actual_sugar,
                advice,
                mt_time.strftime("%Y-%m-%d %H:%M:%S"),
            ))

        current += timedelta(days=1)

    return records


def main():
    conn = pymysql.connect(**DB_CONFIG)
    try:
        cursor = conn.cursor()

        user_id = get_user_id(cursor)
        if not user_id:
            print("ERROR: testuser not found!")
            return

        print(f"testuser user_id = {user_id}")

        cursor.execute(
            "SELECT COUNT(*) FROM meal_records WHERE user_id = %s", (user_id,)
        )
        existing = cursor.fetchone()[0]
        print(f"existing meal records: {existing}")

        records = generate_records(user_id)
        print(f"generated {len(records)} new records (past 180 days)")

        sql = """
        INSERT INTO meal_records
            (user_id, meal_date, meal_time, meal_type, food_name,
             portion_size, calories, sugar_content, ai_advice, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        cursor.executemany(sql, records)
        conn.commit()

        cursor.execute(
            "SELECT COUNT(*) FROM meal_records WHERE user_id = %s", (user_id,)
        )
        total = cursor.fetchone()[0]
        print(f"insert OK. total meal records now: {total}")

    finally:
        conn.close()


if __name__ == "__main__":
    main()
