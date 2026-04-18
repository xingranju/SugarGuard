import pymysql

conn = pymysql.connect(host='localhost', user='root', password='123456', database='Android_health_db')
cur = conn.cursor()

cur.execute("SHOW TABLES LIKE 'user_notifications'")
result = cur.fetchall()
print(f"Table exists: {len(result) > 0}")

if len(result) == 0:
    print("Creating user_notifications table...")
    cur.execute("""
        CREATE TABLE user_notifications (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            user_id BIGINT NOT NULL,
            title VARCHAR(100) NOT NULL,
            content TEXT,
            type VARCHAR(30) NOT NULL,
            is_read BOOLEAN NOT NULL DEFAULT FALSE,
            target_page VARCHAR(50),
            created_at DATETIME NOT NULL,
            read_at DATETIME,
            INDEX idx_user_id (user_id),
            INDEX idx_user_read (user_id, is_read),
            INDEX idx_user_type_created (user_id, type, created_at)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    """)
    conn.commit()
    print("Table created successfully!")
else:
    cur.execute("DESCRIBE user_notifications")
    for row in cur.fetchall():
        print(row)
    cur.execute("SELECT COUNT(*) FROM user_notifications")
    print(f"Row count: {cur.fetchone()[0]}")

conn.close()
