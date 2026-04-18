#!/bin/bash

# 智能健康管理系统部署脚本
# 使用方法: ./scripts/deploy.sh [environment]
# environment: dev, staging, production

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 环境参数
ENVIRONMENT=${1:-staging}
PROJECT_DIR=$(cd "$(dirname "$0")/.." && pwd)
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/${TIMESTAMP}"

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查环境
check_environment() {
    log_info "检查部署环境: ${ENVIRONMENT}"
    
    if [[ ! "$ENVIRONMENT" =~ ^(dev|staging|production)$ ]]; then
        log_error "无效的环境参数: ${ENVIRONMENT}"
        log_info "有效的环境: dev, staging, production"
        exit 1
    fi
    
    # 检查必要的命令
    command -v docker >/dev/null 2>&1 || { log_error "Docker未安装"; exit 1; }
    command -v docker-compose >/dev/null 2>&1 || { log_error "Docker Compose未安装"; exit 1; }
    
    # 检查配置文件
    if [[ ! -f "${PROJECT_DIR}/.env" ]]; then
        log_error "配置文件 .env 不存在"
        log_info "请复制 .env.example 为 .env 并配置"
        exit 1
    fi
    
    log_info "环境检查通过"
}

# 备份数据库
backup_database() {
    if [[ "$ENVIRONMENT" == "production" ]]; then
        log_info "开始备份数据库..."
        
        mkdir -p $BACKUP_DIR
        
        # 加载环境变量
        source ${PROJECT_DIR}/.env
        
        # 备份MySQL
        docker-compose exec -T db mysqldump \
            -u root -p${MYSQL_ROOT_PASSWORD} \
            ${MYSQL_DATABASE} > ${BACKUP_DIR}/mysql_backup.sql
        
        # 压缩备份
        gzip ${BACKUP_DIR}/mysql_backup.sql
        
        # 备份Redis
        docker-compose exec redis redis-cli -a ${REDIS_PASSWORD} BGSAVE
        docker cp health-redis:/data/dump.rdb ${BACKUP_DIR}/redis_backup.rdb
        
        log_info "数据库备份完成: ${BACKUP_DIR}"
    else
        log_info "非生产环境，跳过数据库备份"
    fi
}

# 拉取最新代码
pull_code() {
    log_info "拉取最新代码..."
    cd ${PROJECT_DIR}
    git fetch origin
    
    if [[ "$ENVIRONMENT" == "production" ]]; then
        git checkout main
        git pull origin main
    elif [[ "$ENVIRONMENT" == "staging" ]]; then
        git checkout develop
        git pull origin develop
    else
        log_info "开发环境，使用当前分支"
    fi
    
    log_info "代码更新完成"
}

# 构建镜像
build_images() {
    log_info "构建Docker镜像..."
    cd ${PROJECT_DIR}
    
    # 构建后端镜像
    log_info "构建后端服务镜像..."
    docker build -t health-backend:${ENVIRONMENT} ./backend
    
    # 构建AI服务镜像
    log_info "构建AI服务镜像..."
    docker build -t health-ai:${ENVIRONMENT} ./ai-service
    
    log_info "镜像构建完成"
}

# 停止旧服务
stop_services() {
    log_info "停止旧服务..."
    cd ${PROJECT_DIR}
    
    if docker-compose ps | grep -q "Up"; then
        docker-compose down
        log_info "服务已停止"
    else
        log_info "没有运行中的服务"
    fi
}

# 启动新服务
start_services() {
    log_info "启动新服务..."
    cd ${PROJECT_DIR}
    
    # 根据环境选择配置文件
    if [[ "$ENVIRONMENT" == "production" ]]; then
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
    else
        docker-compose up -d
    fi
    
    log_info "服务启动中..."
    
    # 等待服务启动
    sleep 10
    
    log_info "检查服务状态..."
    docker-compose ps
}

# 健康检查
health_check() {
    log_info "执行健康检查..."
    
    max_attempts=30
    attempt=1
    
    # 检查后端服务
    log_info "检查后端服务..."
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
            log_info "后端服务健康"
            break
        else
            log_warn "后端服务未就绪，等待... (${attempt}/${max_attempts})"
            sleep 5
            ((attempt++))
        fi
    done
    
    if [[ $attempt -gt $max_attempts ]]; then
        log_error "后端服务健康检查失败"
        return 1
    fi
    
    # 检查AI服务
    log_info "检查AI服务..."
    attempt=1
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f http://localhost:8000/health > /dev/null 2>&1; then
            log_info "AI服务健康"
            break
        else
            log_warn "AI服务未就绪，等待... (${attempt}/${max_attempts})"
            sleep 5
            ((attempt++))
        fi
    done
    
    if [[ $attempt -gt $max_attempts ]]; then
        log_error "AI服务健康检查失败"
        return 1
    fi
    
    log_info "所有服务健康检查通过"
    return 0
}

# 回滚
rollback() {
    log_error "部署失败，开始回滚..."
    
    cd ${PROJECT_DIR}
    docker-compose down
    
    if [[ -d "$BACKUP_DIR" ]]; then
        log_info "恢复数据库备份..."
        source ${PROJECT_DIR}/.env
        
        # 恢复MySQL
        gunzip -c ${BACKUP_DIR}/mysql_backup.sql.gz | \
            docker-compose exec -T db mysql -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE}
        
        # 恢复Redis
        docker cp ${BACKUP_DIR}/redis_backup.rdb health-redis:/data/dump.rdb
        docker-compose restart redis
        
        log_info "数据库已恢复"
    fi
    
    # 切换回旧版本
    git checkout HEAD~1
    docker-compose up -d
    
    log_error "已回滚到上一个版本"
}

# 清理旧镜像
cleanup() {
    log_info "清理未使用的Docker资源..."
    docker system prune -f
    log_info "清理完成"
}

# 发送通知
send_notification() {
    local status=$1
    local message=$2
    
    # 如果配置了Slack Webhook，发送通知
    if [[ -n "$SLACK_WEBHOOK_URL" ]]; then
        if [[ "$status" == "success" ]]; then
            icon=":white_check_mark:"
            color="good"
        else
            icon=":x:"
            color="danger"
        fi
        
        curl -X POST "$SLACK_WEBHOOK_URL" \
            -H 'Content-Type: application/json' \
            -d "{
                \"text\": \"${icon} 部署${status}\",
                \"attachments\": [{
                    \"color\": \"${color}\",
                    \"fields\": [
                        {\"title\": \"Environment\", \"value\": \"${ENVIRONMENT}\", \"short\": true},
                        {\"title\": \"Time\", \"value\": \"${TIMESTAMP}\", \"short\": true},
                        {\"title\": \"Message\", \"value\": \"${message}\", \"short\": false}
                    ]
                }]
            }"
    fi
}

# 主流程
main() {
    log_info "================================"
    log_info "开始部署: ${ENVIRONMENT}"
    log_info "时间: ${TIMESTAMP}"
    log_info "================================"
    
    # 1. 检查环境
    check_environment
    
    # 2. 备份数据库（仅生产环境）
    backup_database
    
    # 3. 拉取代码
    pull_code
    
    # 4. 构建镜像
    build_images
    
    # 5. 停止旧服务
    stop_services
    
    # 6. 启动新服务
    start_services
    
    # 7. 健康检查
    if health_check; then
        log_info "================================"
        log_info "✅ 部署成功！"
        log_info "环境: ${ENVIRONMENT}"
        log_info "时间: ${TIMESTAMP}"
        log_info "================================"
        
        # 清理
        cleanup
        
        # 发送成功通知
        send_notification "success" "部署成功完成"
        
        exit 0
    else
        log_error "健康检查失败"
        
        # 回滚
        rollback
        
        # 发送失败通知
        send_notification "failed" "健康检查失败，已自动回滚"
        
        exit 1
    fi
}

# 捕获错误并回滚
trap 'rollback' ERR

# 执行主流程
main

