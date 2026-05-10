"""Test VIT model loading on remote server"""
import paramiko
import os

SSH_HOST = "connect.westc.seetacloud.com"
SSH_PORT = 57685
SSH_USER = "root"
SSH_PASS = "ZAg3N5aMoVNS"

TEST_SCRIPT = '''import os, sys
os.chdir("/root/autodl-tmp/sugarguard/ai-service")
sys.path.insert(0, ".")
os.environ["TRANSFORMERS_CACHE"] = "./models_cache"

from dotenv import load_dotenv
load_dotenv("./.env")

print("Testing VIT model loading...")
from agents.tools.image_recognition import get_image_recognition_tool
tool = get_image_recognition_tool()
print("VIT model loaded OK!")

print("Testing MiniLM embedding model...")
from agents.rag_knowledge import get_rag_system
rag = get_rag_system()
print("RAG system loaded OK!")

print("ALL MODELS LOADED SUCCESSFULLY")
'''

def main():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(SSH_HOST, port=SSH_PORT, username=SSH_USER, password=SSH_PASS, timeout=30)
    sftp = client.open_sftp()
    sftp.open("/root/autodl-tmp/sugarguard/ai-service/_test_models.py", "w").write(TEST_SCRIPT)
    sftp.close()

    cmd = "cd /root/autodl-tmp/sugarguard/ai-service && source venv/bin/activate && timeout 90 python3 _test_models.py 2>&1"
    stdin, stdout, stderr = client.exec_command(cmd, timeout=120)
    print(stdout.read().decode('utf-8', errors='replace'))
    err = stderr.read().decode('utf-8', errors='replace')
    if err: print("STDERR:", err)
    print("EXIT:", stdout.channel.recv_exit_status())
    client.close()

if __name__ == "__main__":
    main()
