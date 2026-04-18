# -*- coding: utf-8 -*-
import mysql.connector
conn = mysql.connector.connect(
    host='localhost', port=3306,
    database='Android_health_db', user='root', password='123456',
    charset='utf8mb4'
)
c = conn.cursor()
c.execute("SELECT meal_id, food_name, image_path FROM meal_records WHERE user_id=20 AND meal_date=CURDATE()")
for r in c.fetchall():
    print(f"id={r[0]}, name={r[1]}, img={r[2]}")

# Fix apple image - use a different working Unsplash URL
c.execute("""
    UPDATE meal_records SET image_path='https://images.unsplash.com/photo-1505253758473-96b7015fcd40?w=400'
    WHERE user_id=20 AND image_path LIKE '%photo-1568702846914%'
""")
print(f"\nFixed apple image: {c.rowcount} rows")
conn.commit()
c.close()
conn.close()
