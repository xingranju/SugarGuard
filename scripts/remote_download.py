# -*- coding: utf-8 -*-
"""在AutoDL后台下载模型，不阻塞SSH连接"""
import paramiko
import sys

HOST = "connect.westc.seetacloud.com"
PORT = 57685
USER = "root"
PASS = "ZAg3N5aMoVNS"

CACHE = "/root/autodl-tmp/sugarguard/ai-service/models_cache"
LOG = "/root/autodl-tmp/sugarguard/model_download.log"

DOWNLOAD_PY = f'''
import os
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"
os.environ["HF_HOME"] = "{CACHE}"
os.environ["TRANSFORMERS_CACHE"] = "{CACHE}"

print("=== ViT download start ===", flush=True)
from transformers import AutoModelForImageClassification, AutoFeatureExtractor
m = AutoModelForImageClassification.from_pretrained("google/vit-base-patch16-224", cache_dir="{CACHE}")
print("ViT model OK", flush=True)
e = AutoFeatureExtractor.from_pretrained("google/vit-base-patch16-224", cache_dir="{CACHE}")
print("ViT extractor OK", flush=True)

print("=== sentence-transformers download start ===", flush=True)
from sentence_transformers import SentenceTransformer
model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2", cache_folder="{CACHE}")
print("sentence-transformers OK", flush=True)

print("=== ALL_MODELS_DONE ===", flush=True)
'''

action = sys.argv[1] if len(sys.argv) > 1 else "start"

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(HOST, port=PORT, username=USER, password=PASS, timeout=15)

if action == "start":
    script_path = "/root/autodl-tmp/sugarguard/download_models.py"
    sftp = client.open_sftp()
    with sftp.file(script_path, "w") as f:
        f.write(DOWNLOAD_PY)
    sftp.close()
    print("Script uploaded.")

    transport = client.get_transport()
    channel = transport.open_session()
    cmd = f"export PATH=/root/miniconda3/bin:/usr/bin:/bin:$PATH && export HF_ENDPOINT=https://hf-mirror.com && nohup python {script_path} > {LOG} 2>&1 & echo STARTED_PID=$!"
    channel.exec_command(cmd)
    import time
    time.sleep(2)
    if channel.recv_ready():
        print(channel.recv(4096).decode("utf-8", errors="replace").strip())
    else:
        print("Background download command sent.")
    channel.close()

elif action == "check":
    _, stdout, _ = client.exec_command(f"tail -10 {LOG} 2>/dev/null && echo --- && ps aux | grep download_models | grep -v grep", timeout=10)
    out = stdout.read().decode("utf-8", errors="replace")
    print(out)

elif action == "verify":
    _, stdout, _ = client.exec_command(f"du -sh {CACHE} && ls {CACHE}/ && echo --- && grep -c ALL_MODELS_DONE {LOG}", timeout=10)
    out = stdout.read().decode("utf-8", errors="replace")
    print(out)

client.close()
