"""Deploy all services and configuration to remote server"""
import paramiko
import os

SSH_HOST = "connect.westc.seetacloud.com"
SSH_PORT = 57685
SSH_USER = "root"
SSH_PASS = "ZAg3N5aMoVNS"

DOCKER_COMPOSE_YML = r'''version: "3.8"

# ====================================================================
# SugarGuard AI 生产部署配置 (docker-compose)
# 注意: AutoDL 容器环境不支持 Docker-in-Docker，此文件用于
# 标准 Linux 服务器部署场景。AutoDL 上使用 screen + Nginx 方案。
# ====================================================================

services:
  mysql:
    image: mysql:8.0
    container_name: sugarguard-mysql
    restart: unless-stopped
    ports:
      - "127.0.0.1:3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: "123456"
      MYSQL_DATABASE: "Android_health_db"
      TZ: "Asia/Shanghai"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./db_dump:/docker-entrypoint-initdb.d
    command: >
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-p123456"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend-api:
    image: openjdk:11-jre-slim
    container_name: sugarguard-backend
    restart: unless-stopped
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: "jdbc:mysql://mysql:3306/Android_health_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4"
      SPRING_DATASOURCE_USERNAME: "root"
      SPRING_DATASOURCE_PASSWORD: "123456"
      TZ: "Asia/Shanghai"
    volumes:
      - ./backend-api/target:/app
    working_dir: /app
    command: ["java", "-jar", "user-management-api-1.0.0.jar", "--server.port=8080"]

  ai-service:
    build:
      context: ./ai-service
      dockerfile: Dockerfile
    container_name: sugarguard-ai
    restart: unless-stopped
    ports:
      - "8000:8000"
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: Android_health_db
      DB_USER: root
      DB_PASSWORD: "123456"
      SERVICE_HOST: "0.0.0.0"
      SERVICE_PORT: 8000
      NVIDIA_VISIBLE_DEVICES: all
      NVIDIA_DRIVER_CAPABILITIES: compute,utility
    volumes:
      - ./ai-service/models_cache:/app/models_cache
      - ./ai-service/data:/app/data
      - ./ai-service/logs:/app/logs
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: 1
              capabilities: [gpu]

  nginx:
    image: nginx:1.24-alpine
    container_name: sugarguard-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - backend-api
      - ai-service
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro

volumes:
  mysql_data:
'''

AI_DOCKERFILE = r'''# SugarGuard AI Service Dockerfile (GPU)
FROM python:3.10-slim

WORKDIR /app

# 系统依赖
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc g++ libmysqlclient-dev && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cu128 && \
    pip install --no-cache-dir -r requirements.txt

COPY . .

# 模型和数据目录作为 volume 挂载
VOLUME ["/app/models_cache", "/app/data", "/app/logs"]

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/health')" || exit 1

CMD ["python", "-m", "uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
'''

AI_DOCKERFILE_CPU = r'''# SugarGuard AI Service Dockerfile (CPU fallback)
FROM python:3.10-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc g++ libmysqlclient-dev && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu && \
    pip install --no-cache-dir -r requirements.txt

COPY . .

VOLUME ["/app/models_cache", "/app/data", "/app/logs"]

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8000/health')" || exit 1

CMD ["python", "-m", "uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
'''

NGINX_CONF = r'''worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent"';
    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    keepalive_timeout 65;
    client_max_body_size 50m;

    # Gzip
    gzip on;
    gzip_types text/plain application/json application/javascript text/css;

    # Backend API upstream
    upstream backend_api {
        server 127.0.0.1:8080;
    }

    # AI Service upstream
    upstream ai_service {
        server 127.0.0.1:8000;
    }

    server {
        listen 80;
        server_name _;

        # Backend API
        location /api/ {
            proxy_pass http://backend_api;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 30s;
            proxy_read_timeout 120s;
        }

        # AI Service
        location /ai/ {
            rewrite ^/ai/(.*) /$1 break;
            proxy_pass http://ai_service;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 30s;
            proxy_read_timeout 300s;
            proxy_buffering off;
        }

        # AI Service direct (for Android client compatibility)
        location /api/recognize-drink {
            proxy_pass http://ai_service;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_connect_timeout 30s;
            proxy_read_timeout 300s;
            client_max_body_size 50m;
        }

        location /api/chat {
            proxy_pass http://ai_service;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_connect_timeout 30s;
            proxy_read_timeout 300s;
        }

        location /api/health-analysis/ {
            proxy_pass http://ai_service;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_connect_timeout 30s;
            proxy_read_timeout 120s;
        }

        location /api/recommend-drinks {
            proxy_pass http://ai_service;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_connect_timeout 30s;
            proxy_read_timeout 120s;
        }

        # Health check
        location /health {
            proxy_pass http://ai_service;
        }

        location / {
            return 200 '{"service":"SugarGuard Gateway","status":"running"}';
            add_header Content-Type application/json;
        }
    }
}
'''

START_SERVICES_SH = r'''#!/bin/bash
# SugarGuard 服务启动脚本 (AutoDL screen 模式)
set -e
BASE_DIR="/root/autodl-tmp/sugarguard"

echo "=== Starting SugarGuard Services ==="

# 1. MySQL
echo "[1/4] Checking MySQL..."
if ! mysqladmin ping -u root -p123456 --silent 2>/dev/null; then
    echo "  Starting MySQL..."
    mysqld_safe &
    sleep 5
    echo "  MySQL started"
else
    echo "  MySQL already running"
fi

# 2. Backend API (Spring Boot)
echo "[2/4] Starting Backend API..."
screen -dmS backend bash -c "cd $BASE_DIR/backend-api/target && java -jar user-management-api-1.0.0.jar --server.port=8080 2>&1 | tee $BASE_DIR/logs/backend.log"
sleep 3
echo "  Backend API starting on port 8080"

# 3. AI Service (FastAPI + GPU)
echo "[3/4] Starting AI Service..."
screen -dmS ai-service bash -c "cd $BASE_DIR/ai-service && source venv/bin/activate && python -m uvicorn main:app --host 0.0.0.0 --port 8000 2>&1 | tee $BASE_DIR/logs/ai-service.log"
sleep 3
echo "  AI Service starting on port 8000"

# 4. Nginx
echo "[4/4] Starting Nginx..."
nginx -t && nginx -s reload 2>/dev/null || nginx
echo "  Nginx started on port 80"

echo ""
echo "=== All Services Started ==="
echo "Backend API: http://localhost:8080"
echo "AI Service:  http://localhost:8000"
echo "Nginx:       http://localhost:80"
echo ""
echo "Active screens:"
screen -ls
'''

STOP_SERVICES_SH = r'''#!/bin/bash
# SugarGuard 服务停止脚本
echo "=== Stopping SugarGuard Services ==="
screen -X -S ai-service quit 2>/dev/null && echo "AI Service stopped" || echo "AI Service not running"
screen -X -S backend quit 2>/dev/null && echo "Backend API stopped" || echo "Backend not running"
nginx -s stop 2>/dev/null && echo "Nginx stopped" || echo "Nginx not running"
echo "=== Done ==="
'''

def deploy():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(SSH_HOST, port=SSH_PORT, username=SSH_USER, password=SSH_PASS, timeout=30)
    sftp = client.open_sftp()

    base = "/root/autodl-tmp/sugarguard"

    files = {
        f"{base}/docker-compose.yml": DOCKER_COMPOSE_YML,
        f"{base}/ai-service/Dockerfile": AI_DOCKERFILE,
        f"{base}/ai-service/Dockerfile.cpu": AI_DOCKERFILE_CPU,
        f"{base}/nginx/nginx.conf": NGINX_CONF,
        f"{base}/start_services.sh": START_SERVICES_SH,
        f"{base}/stop_services.sh": STOP_SERVICES_SH,
    }

    for path, content in files.items():
        d = os.path.dirname(path)
        try:
            sftp.stat(d)
        except FileNotFoundError:
            sftp.mkdir(d)
        with sftp.open(path, "w") as f:
            f.write(content)
        print(f"  Written: {path}")

    sftp.close()

    for script in [f"{base}/start_services.sh", f"{base}/stop_services.sh"]:
        client.exec_command(f"chmod +x {script}")

    print("\nDeployment files written successfully!")
    client.close()

if __name__ == "__main__":
    deploy()
