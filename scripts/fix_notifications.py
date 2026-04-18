import pymysql
from datetime import datetime, date

conn = pymysql.connect(host='localhost', user='root', password='123456', database='Android_health_db')
cur = conn.cursor()

# 清除之前的假通知
cur.execute("DELETE FROM user_notifications WHERE user_id = 20")
print(f"Deleted {cur.rowcount} fake notifications")

# 查看 testuser 今天的实际数据
today = date.today().strftime('%Y-%m-%d')
user_id = 20

# 今日饮食
cur.execute("SELECT COUNT(*), COALESCE(SUM(sugar_content), 0) FROM meal_records WHERE user_id = %s AND meal_date = %s", (user_id, today))
meal_count, total_sugar = cur.fetchone()
print(f"Today meals: {meal_count}, total sugar: {total_sugar}g")

# 糖分目标
cur.execute("SELECT sugar_limit FROM user_health_profile WHERE user_id = %s", (user_id,))
row = cur.fetchone()
sugar_limit = row[0] if row else 25
print(f"Sugar limit: {sugar_limit}g")

# 今日健康记录（饮水量）
cur.execute("SELECT water_intake FROM daily_health_records WHERE user_id = %s AND record_date = %s", (user_id, today))
row = cur.fetchone()
water_intake = row[0] if row else 0
print(f"Today water intake: {water_intake}ml")

# 根据实际数据生成通知
now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
count = 0

# 1. 饮食记录提醒（今天没有记录）
if meal_count == 0:
    cur.execute("""
        INSERT INTO user_notifications (user_id, title, content, type, is_read, target_page, created_at)
        VALUES (%s, %s, %s, %s, FALSE, %s, %s)
    """, (user_id, "饮食记录提醒", "你今天还没有记录饮食，坚持记录有助于更好地控制糖分摄入哦！", "record_reminder", "diary", now))
    count += 1
    print("-> 生成: 饮食记录提醒")

# 2. 饮水提醒（饮水量不足1000ml）
if water_intake < 1000:
    msg = f"今日饮水量{int(water_intake)}ml，不足1000ml，记得多喝水保持身体健康！" if water_intake > 0 else "今日还未记录饮水量，记得多喝水保持身体健康！"
    cur.execute("""
        INSERT INTO user_notifications (user_id, title, content, type, is_read, target_page, created_at)
        VALUES (%s, %s, %s, %s, FALSE, %s, %s)
    """, (user_id, "饮水提醒", msg, "water_reminder", "health_record", now))
    count += 1
    print(f"-> 生成: 饮水提醒 ({msg})")

# 3. 糖分超标提醒（仅当有记录且超标时）
if meal_count > 0 and total_sugar > sugar_limit:
    over_pct = int((total_sugar / sugar_limit - 1) * 100)
    msg = f"今日已摄入{int(total_sugar)}g糖分，超出目标{int(sugar_limit)}g的{over_pct}%，建议控制后续饮食。"
    cur.execute("""
        INSERT INTO user_notifications (user_id, title, content, type, is_read, target_page, created_at)
        VALUES (%s, %s, %s, %s, FALSE, %s, %s)
    """, (user_id, "今日糖分超标提醒", msg, "sugar_alert", "analysis", now))
    count += 1
    print(f"-> 生成: 糖分超标提醒 ({msg})")
elif meal_count > 0:
    print(f"-> 跳过: 糖分未超标 ({int(total_sugar)}g / {int(sugar_limit)}g)")
else:
    print("-> 跳过: 今日无饮食记录，不生成糖分提醒")

conn.commit()
print(f"\n共生成 {count} 条真实通知")

# 验证
cur.execute("SELECT id, title, content FROM user_notifications WHERE user_id = %s ORDER BY id", (user_id,))
for row in cur.fetchall():
    print(f"  [{row[0]}] {row[1]}: {row[2]}")

conn.close()
