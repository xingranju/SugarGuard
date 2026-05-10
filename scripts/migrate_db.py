# -*- coding: utf-8 -*-
"""将本地MySQL数据库导出上传到AutoDL并导入"""
import paramiko
import os

HOST = "connect.westc.seetacloud.com"
PORT = 57685
USER = "root"
PASS = "ZAg3N5aMoVNS"

LOCAL_SQL = r"E:\code\Anroid\MyApplication\scripts\db_export.sql"
REMOTE_SQL = "/root/autodl-tmp/sugarguard/db_export.sql"

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(HOST, port=PORT, username=USER, password=PASS, timeout=15)

print(f"Uploading {os.path.getsize(LOCAL_SQL)/1024/1024:.1f} MB ...")
sftp = client.open_sftp()
sftp.put(LOCAL_SQL, REMOTE_SQL)
sftp.close()
print("Upload done.")

cmd = f"export PATH=/usr/bin:/bin:/usr/sbin:/sbin:$PATH && mysql -u root -p123456 Android_health_db < {REMOTE_SQL} && echo IMPORT_OK"
print(f"Importing: {cmd[:60]}...")
_, stdout, stderr = client.exec_command(cmd, timeout=120)
out = stdout.read().decode("utf-8", errors="replace")
err = stderr.read().decode("utf-8", errors="replace")
print(out + err)

cmd2 = "mysql -u root -p123456 Android_health_db -e 'SHOW TABLES;'"
_, stdout2, stderr2 = client.exec_command(cmd2, timeout=30)
out2 = stdout2.read().decode("utf-8", errors="replace")
err2 = stderr2.read().decode("utf-8", errors="replace")
print("Tables:\n" + out2 + err2)

client.close()
print("=== DB migration done ===")
