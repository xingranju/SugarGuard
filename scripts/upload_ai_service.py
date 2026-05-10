"""Upload AI service code to remote server via SFTP"""
import paramiko
import os
import stat

SSH_HOST = "connect.westc.seetacloud.com"
SSH_PORT = 57685
SSH_USER = "root"
SSH_PASS = "ZAg3N5aMoVNS"
LOCAL_AI = r"E:\code\Anroid\MyApplication\ai-service"
REMOTE_AI = "/root/autodl-tmp/sugarguard/ai-service"

SKIP_DIRS = {".venv", "__pycache__", "models_cache", ".pytest_cache", "logs", "data"}
SKIP_EXTS = {".pyc"}

def ensure_remote_dir(sftp, path):
    dirs = []
    while True:
        try:
            sftp.stat(path)
            break
        except FileNotFoundError:
            dirs.append(path)
            path = os.path.dirname(path)
    for d in reversed(dirs):
        sftp.mkdir(d)

def upload_tree(sftp, local_root, remote_root):
    count = 0
    for dirpath, dirnames, filenames in os.walk(local_root):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        rel = os.path.relpath(dirpath, local_root).replace("\\", "/")
        remote_dir = remote_root if rel == "." else f"{remote_root}/{rel}"
        ensure_remote_dir(sftp, remote_dir)
        for fn in filenames:
            if any(fn.endswith(ext) for ext in SKIP_EXTS):
                continue
            local_file = os.path.join(dirpath, fn)
            remote_file = f"{remote_dir}/{fn}"
            sftp.put(local_file, remote_file)
            count += 1
            print(f"  [{count}] {rel}/{fn}")
    return count

def main():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(SSH_HOST, port=SSH_PORT, username=SSH_USER, password=SSH_PASS, timeout=30)
    sftp = client.open_sftp()
    try:
        print(f"Uploading AI service from {LOCAL_AI} to {REMOTE_AI}")
        n = upload_tree(sftp, LOCAL_AI, REMOTE_AI)
        print(f"\nDone: {n} files uploaded")
    finally:
        sftp.close()
        client.close()

if __name__ == "__main__":
    main()
