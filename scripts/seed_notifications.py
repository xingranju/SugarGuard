import pymysql
from datetime import datetime

conn = pymysql.connect(host='localhost', user='root', password='123456', database='Android_health_db')
cur = conn.cursor()

now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

notifications = [
    (20, "饮食记录提醒", "你今天还没有记录饮食，坚持记录有助于更好地控制糖分摄入哦！", "record_reminder", "diary"),
    (20, "饮水提醒", "今日饮水量不足1000ml，记得多喝水保持身体健康！", "water_reminder", "health_record"),
    (20, "今日糖分超标提醒", "今日已摄入39g糖分，超出目标30g的30%，建议控制后续饮食。", "sugar_alert", "analysis"),
]

for user_id, title, content, ntype, target_page in notifications:
    cur.execute("""
        INSERT INTO user_notifications (user_id, title, content, type, is_read, target_page, created_at)
        VALUES (%s, %s, %s, %s, FALSE, %s, %s)
    """, (user_id, title, content, ntype, target_page, now))

conn.commit()
print(f"Inserted {len(notifications)} notifications for testuser (id=20)")

cur.execute("SELECT id, title, type, is_read FROM user_notifications WHERE user_id = 20")
for row in cur.fetchall():
    print(row)

conn.close()
