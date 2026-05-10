"""Verify installed Python dependencies on remote server"""
import paramiko

SSH_HOST = "connect.westc.seetacloud.com"
SSH_PORT = 57685
SSH_USER = "root"
SSH_PASS = "ZAg3N5aMoVNS"

VERIFY_SCRIPT = r'''
cd /root/autodl-tmp/sugarguard/ai-service && source venv/bin/activate
python3 -c "
import torch
print('PyTorch:', torch.__version__, 'CUDA:', torch.cuda.is_available())
if torch.cuda.is_available():
    print('GPU:', torch.cuda.get_device_name(0))
import transformers
print('Transformers:', transformers.__version__)
import fastapi
print('FastAPI:', fastapi.__version__)
import langchain
print('LangChain:', langchain.__version__)
import faiss
print('FAISS:', faiss.__version__ if hasattr(faiss,'__version__') else 'OK')
import sentence_transformers
print('SentenceTransformers:', sentence_transformers.__version__)
import PIL
print('Pillow:', PIL.__version__)
import sqlalchemy
print('SQLAlchemy:', sqlalchemy.__version__)
print('ALL DEPS OK')
"
'''

def main():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(SSH_HOST, port=SSH_PORT, username=SSH_USER, password=SSH_PASS, timeout=30)
    stdin, stdout, stderr = client.exec_command(VERIFY_SCRIPT, timeout=120)
    print(stdout.read().decode('utf-8', errors='replace'))
    err = stderr.read().decode('utf-8', errors='replace')
    if err: print("STDERR:", err)
    client.close()

if __name__ == "__main__":
    main()
