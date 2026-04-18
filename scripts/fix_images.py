# -*- coding: utf-8 -*-
import mysql.connector

conn = mysql.connector.connect(
    host='localhost', port=3306,
    database='Android_health_db', user='root', password='123456',
    charset='utf8mb4'
)
c = conn.cursor()

c.execute("UPDATE meal_records SET image_path = SUBSTRING(image_path, 2) WHERE image_path LIKE '/https%%' AND user_id=20")
print(f"Fixed {c.rowcount} image paths")
conn.commit()

c.execute("SELECT image_path FROM meal_records WHERE user_id=20 LIMIT 3")
for r in c.fetchall():
    print(f"  {r[0]}")

c.close()
conn.close()
print("Done!")
