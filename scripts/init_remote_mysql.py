# -*- coding: utf-8 -*-
"""在AutoDL服务器上初始化MySQL数据库"""
import paramiko

HOST = "connect.westc.seetacloud.com"
PORT = 57685
USER = "root"
PASS = "ZAg3N5aMoVNS"

COMMANDS = [
    "export PATH=/root/miniconda3/bin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH && killall mysqld 2>/dev/null; sleep 2; /etc/init.d/mysql restart && sleep 3 && echo RESTARTED",
    "mysql -u root -e \"ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456'; FLUSH PRIVILEGES;\"",
    "mysql -u root -p123456 -e 'SHOW DATABASES;'",
]

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(HOST, port=PORT, username=USER, password=PASS, timeout=15)

for cmd in COMMANDS:
    print(f">>> {cmd[:80]}...")
    _, stdout, stderr = client.exec_command(cmd, timeout=30)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    print(out + err)

client.close()
print("=== MySQL init done ===")
