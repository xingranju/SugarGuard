"""SSH check remote server status."""
import paramiko
import sys

HOST = "connect.westc.seetacloud.com"
PORT = 57685
USER = "root"
PASS = "ZAg3N5aMoVNS"

def run_cmd(ssh, cmd, timeout=30):
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    return out + err

def main():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    print(f"Connecting to {HOST}:{PORT}...")
    try:
        ssh.connect(HOST, port=PORT, username=USER, password=PASS, timeout=15)
    except Exception as e:
        print(f"FAILED: {e}")
        sys.exit(1)
    print("CONNECTED!\n")

    checks = [
        ("OS", "cat /etc/os-release | head -4"),
        ("Memory", "free -h | head -2"),
        ("Disk", "df -h / /root/autodl-tmp 2>/dev/null || df -h /"),
        ("GPU", "nvidia-smi --query-gpu=name,memory.total --format=csv,noheader 2>/dev/null || echo 'No GPU detected'"),
        ("Docker", "docker --version 2>/dev/null || echo 'Docker not installed'"),
        ("Java", "java -version 2>&1 | head -1 || echo 'Java not installed'"),
        ("Python", "python3 --version 2>&1 || echo 'Python3 not installed'"),
        ("MySQL", "mysql --version 2>&1 || echo 'MySQL not installed'"),
        ("Existing Deploy", "ls -la /opt/sugarguard/ 2>/dev/null || echo 'No /opt/sugarguard'"),
        ("AutoDL tmp", "ls /root/autodl-tmp/ 2>/dev/null | head -20 || echo 'No autodl-tmp'"),
        ("Services", "ss -lntp 2>/dev/null | grep -E '8080|8000|3306' || echo 'No target services'"),
        ("Docker containers", "docker ps -a 2>/dev/null || echo 'Docker not available'"),
    ]

    for name, cmd in checks:
        print(f"=== {name} ===")
        result = run_cmd(ssh, cmd)
        print(result.strip())
        print()

    ssh.close()

if __name__ == "__main__":
    main()
