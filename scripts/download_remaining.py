# -*- coding: utf-8 -*-
"""继续下载剩余模型（ViT processor + sentence-transformers）"""
import paramiko

HOST = "connect.westc.seetacloud.com"
PORT = 57685
USER = "root"
PASS = "ZAg3N5aMoVNS"

CACHE = "/root/autodl-tmp/sugarguard/ai-service/models_cache"
LOG = "/root/autodl-tmp/sugarguard/model_download2.log"

SCRIPT = f'''
import os
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"
os.environ["HF_HOME"] = "{CACHE}"
os.environ["TRANSFORMERS_CACHE"] = "{CACHE}"

print("=== Step1: ViT ImageProcessor ===", flush=True)
from transformers import AutoImageProcessor
p = AutoImageProcessor.from_pretrained("google/vit-base-patch16-224", cache_dir="{CACHE}")
print("ViT processor OK", flush=True)

print("=== Step2: sentence-transformers ===", flush=True)
from sentence_transformers import SentenceTransformer
model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2", cache_folder="{CACHE}")
print("sentence-transformers OK", flush=True)

print("=== ALL_MODELS_DONE ===", flush=True)
'''

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(HOST, port=PORT, username=USER, password=PASS, timeout=15)

script_path = "/root/autodl-tmp/sugarguard/download_models2.py"
sftp = client.open_sftp()
with sftp.file(script_path, "w") as f:
    f.write(SCRIPT)
sftp.close()
print("Script uploaded.")

transport = client.get_transport()
channel = transport.open_session()
cmd = f"export PATH=/root/miniconda3/bin:/usr/bin:/bin:$PATH && export HF_ENDPOINT=https://hf-mirror.com && nohup python {script_path} > {LOG} 2>&1 & echo STARTED"
channel.exec_command(cmd)
import time
time.sleep(2)
if channel.recv_ready():
    print(channel.recv(4096).decode("utf-8", errors="replace").strip())
channel.close()
client.close()
print("Done - check with: python remote_download.py check")
