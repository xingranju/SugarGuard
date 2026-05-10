"""SSH remote command executor for AutoDL deployment"""
import paramiko
import sys
import os

SSH_HOST = "connect.westc.seetacloud.com"
SSH_PORT = 57685
SSH_USER = "root"
SSH_PASS = "ZAg3N5aMoVNS"

def exec_cmd(cmd, timeout=120):
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        client.connect(SSH_HOST, port=SSH_PORT, username=SSH_USER, password=SSH_PASS, timeout=30)
        stdin, stdout, stderr = client.exec_command(cmd, timeout=timeout)
        out = stdout.read().decode('utf-8', errors='replace')
        err = stderr.read().decode('utf-8', errors='replace')
        exit_code = stdout.channel.recv_exit_status()
        return exit_code, out, err
    finally:
        client.close()

if __name__ == "__main__":
    timeout = 120
    args = sys.argv[1:]
    if args and args[-1].isdigit():
        timeout = int(args.pop())
    cmd = " ".join(args) if args else "echo hello"
    code, out, err = exec_cmd(cmd, timeout=timeout)
    if out: print(out)
    if err: print("[STDERR]", err, file=sys.stderr)
    sys.exit(code)
