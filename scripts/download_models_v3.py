# -*- coding: utf-8 -*-
"""在AutoDL上通过HF镜像下载AI模型"""
import paramiko

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

CACHE = "/root/autodl-tmp/sugarguard/ai-service/models_cache"

print("Step 1: Download ViT model via HF mirror...")
vit_cmd = f"""export PATH=/root/miniconda3/bin:/usr/bin:/bin:$PATH
export HF_ENDPOINT=https://hf-mirror.com
export HF_HOME={CACHE}
export TRANSFORMERS_CACHE={CACHE}
cd /root/autodl-tmp/sugarguard/ai-service
python -c "
import os
os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'
os.environ['HF_HOME'] = '{CACHE}'
os.environ['TRANSFORMERS_CACHE'] = '{CACHE}'
from transformers import AutoModelForImageClassification, AutoFeatureExtractor
print('Downloading ViT...')
m = AutoModelForImageClassification.from_pretrained('google/vit-base-patch16-224', cache_dir='{CACHE}')
print('ViT model OK')
e = AutoFeatureExtractor.from_pretrained('google/vit-base-patch16-224', cache_dir='{CACHE}')
print('ViT extractor OK')
print('VIT_DONE')
"
"""
out, err = run_cmd(vit_cmd, timeout=600)
print(out[-800:] if len(out) > 800 else out)
if "VIT_DONE" not in out:
    print("ERR:", err[-500:])

print("\nStep 2: Download sentence-transformers via HF mirror...")
st_cmd = f"""export PATH=/root/miniconda3/bin:/usr/bin:/bin:$PATH
export HF_ENDPOINT=https://hf-mirror.com
export HF_HOME={CACHE}
cd /root/autodl-tmp/sugarguard/ai-service
python -c "
import os
os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'
os.environ['HF_HOME'] = '{CACHE}'
from sentence_transformers import SentenceTransformer
print('Downloading sentence-transformers...')
model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2', cache_folder='{CACHE}')
print('ST_DONE')
"
"""
out, err = run_cmd(st_cmd, timeout=600)
print(out[-800:] if len(out) > 800 else out)
if "ST_DONE" not in out:
    print("ERR:", err[-500:])

print("\nStep 3: Verify cache...")
out, err = run_cmd(f"du -sh {CACHE} && ls {CACHE}/")
print(out)

client.close()
print("=== ALL DONE ===")
