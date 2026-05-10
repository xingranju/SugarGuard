"""Verify MySQL database on remote server"""
import paramiko

SSH_HOST = "connect.westc.seetacloud.com"
SSH_PORT = 57685
SSH_USER = "root"
SSH_PASS = "ZAg3N5aMoVNS"

CMD = """mysql -u root -p123456 Android_health_db -e "
SELECT TABLE_NAME, TABLE_ROWS
FROM information_schema.tables
WHERE TABLE_SCHEMA='Android_health_db'
ORDER BY TABLE_NAME;
" 2>&1"""

def main():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(SSH_HOST, port=SSH_PORT, username=SSH_USER, password=SSH_PASS, timeout=30)
    stdin, stdout, stderr = client.exec_command(CMD, timeout=30)
    print(stdout.read().decode('utf-8', errors='replace'))
    err = stderr.read().decode('utf-8', errors='replace')
    if err: print("STDERR:", err)
    client.close()

if __name__ == "__main__":
    main()
