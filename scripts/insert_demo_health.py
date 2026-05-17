"""
为 testuser 补充近半年 daily_health_records 展示数据
基于已有的 meal_records 聚合每日糖分和热量，同时补充水、运动、睡眠等数据
"""
import pymysql
import random
from datetime import date, timedelta, datetime

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "Android_health_db",
    "charset": "utf8mb4"
}

USER_ID = 20

MOODS = ["excellent", "good", "normal", "bad", "terrible"]
MOOD_WEIGHTS = [0.10, 0.35, 0.35, 0.15, 0.05]


def main():
    conn = pymysql.connect(**DB_CONFIG)
    try:
        cursor = conn.cursor()

        end_date = date(2026, 5, 17)
        start_date = end_date - timedelta(days=180)

        cursor.execute(
            "SELECT record_date FROM daily_health_records WHERE user_id = %s",
            (USER_ID,)
        )
        existing_dates = {r[0] for r in cursor.fetchall()}
        print(f"existing daily_health_records: {len(existing_dates)}")

        cursor.execute(
            """
            SELECT meal_date, SUM(sugar_content), SUM(calories)
            FROM meal_records
            WHERE user_id = %s AND meal_date BETWEEN %s AND %s
            GROUP BY meal_date
            """,
            (USER_ID, start_date.isoformat(), end_date.isoformat())
        )
        meal_agg = {r[0]: (float(r[1] or 0), float(r[2] or 0)) for r in cursor.fetchall()}

        base_weight = 62.0
        records = []
        current = start_date
        while current <= end_date:
            if current in existing_dates:
                current += timedelta(days=1)
                continue

            sugar, cal = meal_agg.get(current, (
                round(random.uniform(20, 60), 1),
                round(random.uniform(1200, 2200), 1)
            ))

            water = round(random.uniform(1200, 2500), 0)
            exercise = round(random.uniform(0, 90), 0)
            sleep = round(random.uniform(5.5, 9.5), 1)

            systolic = round(random.gauss(115, 8), 0)
            diastolic = round(random.gauss(72, 5), 0)
            glucose = round(random.gauss(5.2, 0.6), 1)
            glucose = max(3.5, min(8.0, glucose))

            weight_drift = random.gauss(0, 0.3)
            base_weight += weight_drift * 0.05
            weight = round(base_weight + random.uniform(-0.5, 0.5), 1)

            mood = random.choices(MOODS, weights=MOOD_WEIGHTS, k=1)[0]

            created = datetime(current.year, current.month, current.day, 22, random.randint(0, 59), random.randint(0, 59))

            records.append((
                USER_ID,
                current.isoformat(),
                round(sugar, 1),
                round(cal, 1),
                water,
                exercise,
                sleep,
                systolic,
                diastolic,
                glucose,
                weight,
                mood,
                created.strftime("%Y-%m-%d %H:%M:%S"),
            ))

            current += timedelta(days=1)

        if not records:
            print("no new records to insert")
            return

        sql = """
        INSERT INTO daily_health_records
            (user_id, record_date, total_sugar_intake, total_calories,
             water_intake, exercise_minutes, sleep_hours,
             systolic_bp, diastolic_bp, blood_glucose, weight, mood, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        cursor.executemany(sql, records)
        conn.commit()

        cursor.execute(
            "SELECT COUNT(*) FROM daily_health_records WHERE user_id = %s", (USER_ID,)
        )
        total = cursor.fetchone()[0]
        print(f"inserted {len(records)} new daily_health_records")
        print(f"total daily_health_records now: {total}")

    finally:
        conn.close()


if __name__ == "__main__":
    main()
