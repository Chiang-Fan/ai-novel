#!/bin/bash

###############################################################################
# AI Novel Writer - Docker 一键部署脚本
# 功能: 构建镜像、启动服务、验证部署
###############################################################################

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo ""
    echo -e "${CYAN}===================================================================${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}===================================================================${NC}"
    echo ""
}

# 显示帮助信息
show_help() {
    cat << EOF
AI Novel Writer - Docker 一键部署脚本

用法: ./docker-deploy.sh [选项]

选项:
    -h, --help              显示帮助信息
    -b, --build             构建Docker镜像
    -u, --up                启动服务
    -d, --down              停止服务
    -r, --restart           重启服务
    -l, --logs              查看日志
    -s, --status            查看服务状态
    -c, --clean             清理所有容器和数据卷（危险！）
    --init                  初始化配置文件
    --verify                验证部署

示例:
    ./docker-deploy.sh --init              # 初始化配置
    ./docker-deploy.sh -b -u               # 构建并启动
    ./docker-deploy.sh -r                  # 重启服务
    ./docker-deploy.sh -l                  # 查看日志
    ./docker-deploy.sh --verify            # 验证部署

EOF
}

# 检查Docker环境
check_docker() {
    log_step "步骤 1: 检查Docker环境"
    
    if ! command -v docker &> /dev/null; then
        log_error "未找到Docker，请先安装Docker"
        exit 1
    fi
    log_success "Docker版本: $(docker --version)"
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "未找到Docker Compose，请先安装Docker Compose"
        exit 1
    fi
    
    if docker compose version &> /dev/null; then
        COMPOSE_CMD="docker compose"
        log_success "Docker Compose版本: $(docker compose version)"
    else
        COMPOSE_CMD="docker-compose"
        log_success "Docker Compose版本: $(docker-compose --version)"
    fi
    
    # 检查Docker守护进程
    if ! docker info &> /dev/null; then
        log_error "Docker守护进程未运行，请启动Docker"
        exit 1
    fi
    log_success "Docker守护进程运行正常"
}

# 初始化配置
init_config() {
    log_step "初始化配置文件"
    
    # 创建.env文件
    if [ ! -f .env ]; then
        log_info "创建 .env 配置文件..."
        cp .env.example .env
        log_success ".env 文件已创建"
        
        # 提示用户配置API密钥
        log_warning "请编辑 .env 文件并配置以下必填项:"
        echo "  - DASHSCOPE_API_KEY（通义千问API密钥）"
        echo "  - MYSQL_ROOT_PASSWORD（MySQL root密码）"
        echo "  - MYSQL_PASSWORD（应用数据库密码）"
        echo ""
        read -p "是否现在编辑配置文件？(y/N): " -r
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            ${EDITOR:-vi} .env
        fi
    else
        log_warning ".env 文件已存在，跳过创建"
    fi
    
    # 创建必要的目录
    log_info "创建数据持久化目录..."
    mkdir -p docker/data/mysql
    mkdir -p docker/data/app
    mkdir -p docker/logs
    mkdir -p docker/mysql/conf.d
    mkdir -p docker/mysql/init
    
    log_success "目录结构创建完成"
    
    # 检查API密钥
    if [ -f .env ]; then
        source .env
        if [ -z "$DASHSCOPE_API_KEY" ] || [ "$DASHSCOPE_API_KEY" = "sk-your-api-key-here" ]; then
            log_error "请在 .env 文件中配置 DASHSCOPE_API_KEY"
            exit 1
        fi
        log_success "配置检查通过"
    fi
}

# 构建Docker镜像
build_image() {
    log_step "步骤 2: 构建Docker镜像"
    
    log_info "开始构建应用镜像..."
    $COMPOSE_CMD build --no-cache
    
    log_success "镜像构建完成"
    
    # 显示镜像信息
    log_info "镜像列表:"
    docker images | grep ai-novel
}

# 启动服务
start_services() {
    log_step "步骤 3: 启动服务"
    
    log_info "启动MySQL和应用服务..."
    $COMPOSE_CMD up -d
    
    log_success "服务启动命令已执行"
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 5
    
    # 检查服务状态
    $COMPOSE_CMD ps
}

# 停止服务
stop_services() {
    log_step "停止服务"
    
    log_info "停止所有服务..."
    $COMPOSE_CMD down
    
    log_success "服务已停止"
}

# 重启服务
restart_services() {
    log_step "重启服务"
    
    log_info "重启所有服务..."
    $COMPOSE_CMD restart
    
    log_success "服务已重启"
    
    # 显示服务状态
    $COMPOSE_CMD ps
}

# 查看日志
view_logs() {
    log_step "查看服务日志"
    
    echo "选择要查看的日志:"
    echo "  1) 应用日志"
    echo "  2) MySQL日志"
    echo "  3) 所有日志"
    read -p "请选择 (1-3): " choice
    
    case $choice in
        1)
            $COMPOSE_CMD logs -f app
            ;;
        2)
            $COMPOSE_CMD logs -f mysql
            ;;
        3)
            $COMPOSE_CMD logs -f
            ;;
        *)
            log_error "无效选择"
            ;;
    esac
}

# 查看服务状态
view_status() {
    log_step "服务状态"
    
    $COMPOSE_CMD ps
    
    echo ""
    log_info "容器详细信息:"
    docker ps --filter "name=ai-novel" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    echo ""
    log_info "数据卷使用情况:"
    docker volume ls --filter "name=ai-novel"
    
    echo ""
    log_info "网络信息:"
    docker network ls --filter "name=ai-novel"
}

# 验证部署
verify_deployment() {
    log_step "步骤 4: 验证部署"
    
    # 检查容器状态
    log_info "检查容器状态..."
    if ! docker ps | grep -q "ai-novel-mysql"; then
        log_error "MySQL容器未运行"
        exit 1
    fi
    log_success "MySQL容器运行正常"
    
    if ! docker ps | grep -q "ai-novel-app"; then
        log_error "应用容器未运行"
        exit 1
    fi
    log_success "应用容器运行正常"
    
    # 等待应用启动
    log_info "等待应用完全启动（最多60秒）..."
    for i in {1..12}; do
        if curl -sf http://localhost:8080/api/health > /dev/null 2>&1; then
            break
        fi
        echo -n "."
        sleep 5
    done
    echo ""
    
    # 健康检查
    log_info "执行健康检查..."
    HEALTH_RESPONSE=$(curl -s http://localhost:8080/api/health || echo "failed")
    
    if echo "$HEALTH_RESPONSE" | grep -q "UP"; then
        log_success "应用健康检查通过"
        echo "$HEALTH_RESPONSE" | jq . 2>/dev/null || echo "$HEALTH_RESPONSE"
    else
        log_error "应用健康检查失败"
        log_info "查看应用日志以排查问题:"
        $COMPOSE_CMD logs --tail=50 app
        exit 1
    fi
    
    # 测试数据库连接
    log_info "测试数据库连接..."
    if docker exec ai-novel-mysql mysql -unovel_user -pnovel_pass_123456 -e "SELECT 1" ai_novel_writer > /dev/null 2>&1; then
        log_success "数据库连接正常"
    else
        log_error "数据库连接失败"
        exit 1
    fi
    
    # 显示访问信息
    echo ""
    log_success "========================================="
    log_success "      部署成功！"
    log_success "========================================="
    echo ""
    log_info "服务访问地址:"
    echo "  - 应用首页: http://localhost:8080"
    echo "  - 健康检查: http://localhost:8080/api/health"
    echo "  - API文档: http://localhost:8080/swagger-ui.html"
    echo ""
    log_info "MySQL连接信息:"
    echo "  - 主机: localhost"
    echo "  - 端口: 3306"
    echo "  - 数据库: ai_novel_writer"
    echo "  - 用户名: novel_user"
    echo "  - 密码: 见.env文件"
    echo ""
    log_info "数据持久化目录:"
    echo "  - MySQL数据: $PROJECT_ROOT/docker/data/mysql"
    echo "  - 应用日志: $PROJECT_ROOT/docker/logs"
    echo ""
    log_info "常用命令:"
    echo "  - 查看日志: $COMPOSE_CMD logs -f"
    echo "  - 重启服务: $COMPOSE_CMD restart"
    echo "  - 停止服务: $COMPOSE_CMD down"
    echo ""
}

# 清理环境
clean_all() {
    log_step "清理所有容器和数据"
    
    log_warning "警告: 此操作将删除所有容器、镜像和数据卷！"
    read -p "确认删除？请输入 'YES' 确认: " confirmation
    
    if [ "$confirmation" != "YES" ]; then
        log_info "取消清理操作"
        exit 0
    fi
    
    log_info "停止并删除容器..."
    $COMPOSE_CMD down -v --remove-orphans
    
    log_info "删除镜像..."
    docker rmi ai-novel-writer:latest 2>/dev/null || true
    
    log_info "删除数据目录..."
    read -p "是否删除数据持久化目录？(y/N): " -r
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        rm -rf docker/data
        rm -rf docker/logs
        log_success "数据目录已删除"
    fi
    
    log_success "清理完成"
}

# 主函数
main() {
    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi
    
    case "$1" in
        -h|--help)
            show_help
            ;;
        --init)
            check_docker
            init_config
            ;;
        -b|--build)
            check_docker
            init_config
            build_image
            ;;
        -u|--up)
            check_docker
            start_services
            verify_deployment
            ;;
        -d|--down)
            check_docker
            stop_services
            ;;
        -r|--restart)
            check_docker
            restart_services
            ;;
        -l|--logs)
            check_docker
            view_logs
            ;;
        -s|--status)
            check_docker
            view_status
            ;;
        --verify)
            check_docker
            verify_deployment
            ;;
        -c|--clean)
            check_docker
            clean_all
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"
