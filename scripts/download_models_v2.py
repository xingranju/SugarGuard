# -*- coding: utf-8 -*-
"""在AutoDL上预下载AI模型（逐个下载，带重试）"""
import paramiko
import time

HOST = "connect.westc.seetacloud.com"
PORT = 57685
USER = "root"
PASS = "ZAg3N5aMoVNS"

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(HOST, port=PORT, username=USER, password=PASS, timeout=15)

def run_cmd(cmd, timeout=600):
    _, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    return out, err

CACHE_DIR = "/root/autodl-tmp/sugarguard/ai-service/models_cache"

print("Step 1: Creating cache dir and setting env...")
out, err = run_cmd(f"mkdir -p {CACHE_DIR} && echo OK")
print(out)

print("Step 2: Downloading ViT model (google/vit-base-patch16-224)...")
vit_script = f"""export PATH=/root/miniconda3/bin:/usr/bin:/bin:$PATH
export HF_HOME={CACHE_DIR}
export TRANSFORMERS_CACHE={CACHE_DIR}
cd /root/autodl-tmp/sugarguard/ai-service
python -c "
import os
os.environ['HF_HOME'] = '{CACHE_DIR}'
os.environ['TRANSFORMERS_CACHE'] = '{CACHE_DIR}'
from transformers import AutoModelForImageClassification, AutoFeatureExtractor
print('Downloading ViT model...')
m = AutoModelForImageClassification.from_pretrained('google/vit-base-patch16-224', cache_dir='{CACHE_DIR}')
print('ViT model downloaded.')
e = AutoFeatureExtractor.from_pretrained('google/vit-base-patch16-224', cache_dir='{CACHE_DIR}')
print('ViT feature extractor downloaded.')
print('VIT_OK')
"
"""
out, err = run_cmd(vit_script, timeout=600)
print(out[-500:] if len(out) > 500 else out)
if "VIT_OK" not in out:
    print("STDERR (last 300):", err[-300:])

print("\nStep 3: Downloading sentence-transformers...")
st_script = f"""export PATH=/root/miniconda3/bin:/usr/bin:/bin:$PATH
export HF_HOME={CACHE_DIR}
cd /root/autodl-tmp/sugarguard/ai-service
python -c "
import os
os.environ['HF_HOME'] = '{CACHE_DIR}'
from sentence_transformers import SentenceTransformer
print('Downloading sentence-transformers...')
model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2', cache_folder='{CACHE_DIR}')
print('ST_OK')
"
"""
out, err = run_cmd(st_script, timeout=600)
print(out[-500:] if len(out) > 500 else out)
if "ST_OK" not in out:
    print("STDERR (last 300):", err[-300:])

print("\nStep 4: Verify...")
out, err = run_cmd(f"du -sh {CACHE_DIR} && ls {CACHE_DIR}/")
print(out)

client.close()
print("=== Model download complete ===")
