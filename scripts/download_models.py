# -*- coding: utf-8 -*-
"""在AutoDL上预下载AI模型"""
import paramiko

HOST = "connect.westc.seetacloud.com"
PORT = 57685
USER = "root"
PASS = "ZAg3N5aMoVNS"

DOWNLOAD_SCRIPT = r"""
export PATH=/root/miniconda3/bin:/usr/bin:/bin:$PATH
cd /root/autodl-tmp/sugarguard/ai-service

python -c "
from transformers import AutoModelForImageClassification, AutoFeatureExtractor
print('=== Downloading ViT model ===')
m = AutoModelForImageClassification.from_pretrained('google/vit-base-patch16-224', cache_dir='./models_cache')
e = AutoFeatureExtractor.from_pretrained('google/vit-base-patch16-224', cache_dir='./models_cache')
print('ViT OK')
"

python -c "
from sentence_transformers import SentenceTransformer
print('=== Downloading sentence-transformers ===')
model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2', cache_folder='./models_cache')
print('Embedding model OK')
"

echo "=== All models downloaded ==="
ls -lh models_cache/
"""

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(HOST, port=PORT, username=USER, password=PASS, timeout=15)

print("Starting model download (may take several minutes)...")
_, stdout, stderr = client.exec_command(DOWNLOAD_SCRIPT, timeout=600)
out = stdout.read().decode("utf-8", errors="replace")
err = stderr.read().decode("utf-8", errors="replace")
print(out)
if err:
    print("STDERR:", err[-500:])

client.close()
print("=== Done ===")
