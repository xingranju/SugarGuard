import pymysql
conn = pymysql.connect(host='localhost',port=3306,user='root',password='123456',database='Android_health_db',charset='utf8mb4')
cur = conn.cursor()
cur.execute("SELECT DISTINCT food_name, image_path FROM meal_records WHERE user_id=20 ORDER BY food_name")
for r in cur.fetchall():
    print(r)
conn.close()
