# -*- coding: utf-8 -*-
import mysql.connector

conn = mysql.connector.connect(
    host='localhost', port=3306,
    database='Android_health_db', user='root', password='123456',
    charset='utf8mb4'
)
c = conn.cursor()

c.execute("SELECT meal_id, food_name, sugar_content FROM meal_records WHERE user_id=20 ORDER BY meal_date DESC, meal_time DESC LIMIT 10")
print("Current food names:")
for r in c.fetchall():
    print(f"  id={r[0]}, name='{r[1]}', sugar={r[2]}")

name_map = {
    "yogurt+bread": "无糖酸奶+全麦面包",
    "egg+oatmeal": "水煮鸡蛋+燕麦粥",
    "sandwich+coffee": "全麦三明治+黑咖啡",
    "chicken_salad": "鸡胸肉沙拉",
    "rice+fish": "糙米饭+清蒸鲈鱼",
    "tomato_noodle": "番茄牛肉面",
    "veggie_wrap": "蔬菜鸡肉卷",
    "broccoli+tofu": "清炒西兰花+豆腐",
    "salmon+quinoa": "三文鱼配藜麦",
    "blueberry+walnut": "蓝莓+核桃",
    "apple+yogurt": "苹果+无糖酸奶",
    "mixed_nuts": "坚果混合",
}

for eng, cn in name_map.items():
    c.execute("UPDATE meal_records SET food_name=%s WHERE user_id=20 AND food_name=%s", (cn, eng))
    print(f"  Updated {c.rowcount} rows: {eng} -> {cn}")

c.execute("UPDATE meal_records SET food_name='无糖酸奶+全麦面包' WHERE user_id=20 AND food_name LIKE '%\\u%'")
print(f"  Fixed unicode escapes: {c.rowcount} rows")

conn.commit()

print("\nAfter fix:")
c.execute("SELECT meal_id, food_name, sugar_content FROM meal_records WHERE user_id=20 ORDER BY meal_date DESC LIMIT 10")
for r in c.fetchall():
    print(f"  id={r[0]}, name='{r[1]}', sugar={r[2]}")

c.close()
conn.close()
print("\nDone!")
