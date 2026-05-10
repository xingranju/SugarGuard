"""SSH deployment helper for SugarGuard."""
import paramiko
import sys
import time
import os

HOST = "connect.westc.seetacloud.com"
PORT = 57685
USER = "root"
PASS = "ZAg3N5aMoVNS"

def get_ssh():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(HOST, port=PORT, username=USER, password=PASS, timeout=15)
    return ssh

def run(ssh, cmd, timeout=300, show=True):
    if show:
        print(f">>> {cmd}")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    result = out + err
    if show:
        for line in result.strip().split("\n")[-30:]:
            print(f"    {line}")
    return result

def upload(ssh, local_path, remote_path):
    sftp = ssh.open_sftp()
    size = os.path.getsize(local_path)
    print(f"Uploading {local_path} -> {remote_path} ({size/1024/1024:.1f} MB)")
    sftp.put(local_path, remote_path, callback=lambda sent, total: print(f"\r  {sent*100//total}%", end="", flush=True))
    print(" Done")
    sftp.close()

def main():
    action = sys.argv[1] if len(sys.argv) > 1 else "check"
    ssh = get_ssh()
    print(f"Connected. Action: {action}\n")

    if action == "check":
        run(ssh, "ls -la /root/autodl-tmp/sugarguard/ 2>/dev/null || echo 'empty'")
        run(ssh, "ls -laR /root/autodl-tmp/sugarguard/ 2>/dev/null | head -50")

    elif action == "install_deps":
        print("=== Installing system dependencies ===")
        run(ssh, "apt-get update -qq", timeout=120)
        run(ssh, "apt-get install -y -qq curl wget vim unzip net-tools lsof screen nginx", timeout=120)
        
        print("\n=== Installing JDK 11 ===")
        run(ssh, "apt-get install -y -qq openjdk-11-jdk", timeout=120)
        run(ssh, "java -version 2>&1")

        print("\n=== Installing Python 3.10 ===")
        run(ssh, "apt-get install -y -qq python3 python3-venv python3-dev python3-pip", timeout=120)
        run(ssh, "python3 --version")
        run(ssh, "pip3 --version 2>&1")

        print("\n=== Installing Docker ===")
        run(ssh, """
curl -fsSL https://get.docker.com | sh 2>&1 | tail -5
""", timeout=300)
        run(ssh, "docker --version 2>&1")
        run(ssh, """
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose 2>&1 | tail -3
chmod +x /usr/local/bin/docker-compose
docker-compose --version 2>&1
""", timeout=120)

    elif action == "setup_dirs":
        print("=== Setting up directory structure ===")
        run(ssh, """
mkdir -p /root/autodl-tmp/sugarguard/{backend-api/target,ai-service,db_dump,logs,nginx}
ln -sfn /root/autodl-tmp/sugarguard /opt/sugarguard
ls -la /opt/sugarguard/
""")

    elif action == "setup_mysql":
        print("=== Setting up MySQL ===")
        run(ssh, "service mysql start || systemctl start mysql 2>/dev/null || echo 'MySQL may already be running'")
        time.sleep(3)
        run(ssh, """mysql -e "
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456';
CREATE DATABASE IF NOT EXISTS Android_health_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
FLUSH PRIVILEGES;
SHOW DATABASES;
" 2>&1""")

    elif action == "import_db":
        dump_file = sys.argv[2] if len(sys.argv) > 2 else ""
        if not dump_file:
            run(ssh, "ls -la /opt/sugarguard/db_dump/*.sql 2>/dev/null || echo 'No SQL files found'")
        else:
            print(f"Importing {dump_file}...")
            run(ssh, f"""
mysql -uroot -p123456 -e "
    DROP DATABASE IF EXISTS Android_health_db;
    CREATE DATABASE Android_health_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
"
mysql -uroot -p123456 --default-character-set=utf8mb4 Android_health_db < {dump_file}
echo "Import done. Checking tables..."
mysql -uroot -p123456 -e "
    USE Android_health_db;
    SHOW TABLES;
    SELECT 'users' AS tbl, COUNT(*) AS cnt FROM users UNION ALL
    SELECT 'drinks', COUNT(*) FROM drinks UNION ALL
    SELECT 'food_nutrition', COUNT(*) FROM food_nutrition UNION ALL
    SELECT 'meal_records', COUNT(*) FROM meal_records;
"
""", timeout=120)

    elif action == "upload_jar":
        jar_path = sys.argv[2]
        upload(ssh, jar_path, "/opt/sugarguard/backend-api/target/user-management-api-1.0.0.jar")

    elif action == "upload_file":
        local = sys.argv[2]
        remote = sys.argv[3]
        upload(ssh, local, remote)

    elif action == "start_backend":
        run(ssh, "screen -S backend -X quit 2>/dev/null || true")
        run(ssh, """
screen -dmS backend bash -c '
cd /opt/sugarguard/backend-api
java -Xms512m -Xmx1024m -jar target/user-management-api-1.0.0.jar 2>&1 | tee -a /opt/sugarguard/logs/backend.log
'
""")
        time.sleep(8)
        run(ssh, "screen -ls")
        run(ssh, "tail -20 /opt/sugarguard/logs/backend.log 2>/dev/null")

    elif action == "start_ai":
        run(ssh, "screen -S ai -X quit 2>/dev/null || true")
        run(ssh, """
screen -dmS ai bash -c '
cd /opt/sugarguard/ai-service
source venv/bin/activate
python3 main.py 2>&1 | tee -a /opt/sugarguard/logs/ai.log
'
""")
        time.sleep(10)
        run(ssh, "screen -ls")
        run(ssh, "tail -20 /opt/sugarguard/logs/ai.log 2>/dev/null")

    elif action == "verify":
        run(ssh, "ss -lntp | grep -E '8080|8000|3306'")
        run(ssh, "curl -s http://127.0.0.1:8000/health 2>/dev/null || curl -s http://127.0.0.1:8000/ 2>/dev/null || echo 'AI not responding'")
        run(ssh, """curl -s http://127.0.0.1:8080/api/auth/login -H 'Content-Type: application/json' -d '{"username":"testuser","password":"123456"}' 2>/dev/null || echo 'Backend not responding'""")

    elif action == "custom":
        cmd = " ".join(sys.argv[2:])
        run(ssh, cmd)

    ssh.close()

if __name__ == "__main__":
    main()
