#!/bin/bash

###############################################################################
# AI Novel Writer - 构建与测试脚本
# 用途: 编译Spring Boot项目、构建前端、打包Fat JAR、运行测试
###############################################################################

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# 显示帮助信息
show_help() {
    cat << EOF
AI Novel Writer - 构建与测试脚本

用法: ./build-and-test.sh [选项]

选项:
    -h, --help              显示帮助信息
    -c, --clean             清理构建产物
    -s, --skip-tests        跳过测试
    -f, --skip-frontend     跳过前端构建
    -r, --run               构建后启动应用
    -d, --debug             启用调试模式

示例:
    ./build-and-test.sh                    # 完整构建（含测试）
    ./build-and-test.sh -s                 # 跳过测试构建
    ./build-and-test.sh -c -r              # 清理后构建并运行
    ./build-and-test.sh -f -s -r           # 仅构建后端并运行

EOF
}

# 解析命令行参数
CLEAN_BUILD=false
SKIP_TESTS=false
SKIP_FRONTEND=false
RUN_AFTER_BUILD=false
DEBUG_MODE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -c|--clean)
            CLEAN_BUILD=true
            shift
            ;;
        -s|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -f|--skip-frontend)
            SKIP_FRONTEND=true
            shift
            ;;
        -r|--run)
            RUN_AFTER_BUILD=true
            shift
            ;;
        -d|--debug)
            DEBUG_MODE=true
            shift
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
done

###############################################################################
# 1. 环境检查
###############################################################################
log_info "步骤 1/6: 环境检查..."

# 检查Java
if ! command -v java &> /dev/null; then
    log_error "未找到Java，请安装JDK 17或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    log_error "Java版本过低（当前: $JAVA_VERSION），需要JDK 17+"
    exit 1
fi
log_success "Java版本: $(java -version 2>&1 | head -n 1)"

# 检查Maven
if ! command -v mvn &> /dev/null; then
    log_error "未找到Maven，请先安装Maven 3.6+"
    exit 1
fi
log_success "Maven版本: $(mvn -version | head -n 1)"

# 检查Node.js（如果需要构建前端）
if [ "$SKIP_FRONTEND" = false ]; then
    if ! command -v node &> /dev/null; then
        log_warning "未找到Node.js，将跳过前端构建"
        SKIP_FRONTEND=true
    else
        log_success "Node.js版本: $(node -v)"
    fi
fi

###############################################################################
# 2. 清理构建产物（可选）
###############################################################################
if [ "$CLEAN_BUILD" = true ]; then
    log_info "步骤 2/6: 清理构建产物..."
    
    mvn clean > /dev/null 2>&1 || true
    
    if [ "$SKIP_FRONTEND" = false ] && [ -d "frontend/node_modules" ]; then
        log_info "清理前端依赖..."
        rm -rf frontend/node_modules
        rm -rf frontend/dist
    fi
    
    log_success "清理完成"
else
    log_info "步骤 2/6: 跳过清理（使用 -c 选项启用）"
fi

###############################################################################
# 3. 构建前端（可选）
###############################################################################
if [ "$SKIP_FRONTEND" = false ]; then
    log_info "步骤 3/6: 构建前端..."
    
    if [ -d "frontend" ]; then
        cd frontend
        
        # 安装依赖
        if [ ! -d "node_modules" ]; then
            log_info "安装前端依赖..."
            npm install
        fi
        
        # 构建前端
        log_info "打包前端应用..."
        npm run build
        
        # 复制到Spring Boot静态资源目录
        log_info "复制前端资源到后端..."
        mkdir -p ../src/main/resources/static
        cp -r dist/* ../src/main/resources/static/
        
        cd ..
        log_success "前端构建完成"
    else
        log_warning "未找到frontend目录，跳过前端构建"
    fi
else
    log_info "步骤 3/6: 跳过前端构建（使用 -f 选项禁用）"
fi

###############################################################################
# 4. 编译Java项目
###############################################################################
log_info "步骤 4/6: 编译Java项目..."

if [ "$DEBUG_MODE" = true ]; then
    MVN_CMD="mvn compile -X"
else
    MVN_CMD="mvn compile"
fi

if $MVN_CMD; then
    log_success "Java编译成功"
else
    log_error "Java编译失败"
    exit 1
fi

###############################################################################
# 5. 运行测试（可选）
###############################################################################
if [ "$SKIP_TESTS" = false ]; then
    log_info "步骤 5/6: 运行测试..."
    
    if mvn test; then
        log_success "所有测试通过"
    else
        log_error "测试失败"
        exit 1
    fi
else
    log_info "步骤 5/6: 跳过测试（使用 -s 选项禁用）"
fi

###############################################################################
# 6. 打包Fat JAR
###############################################################################
log_info "步骤 6/6: 打包应用..."

if [ "$SKIP_TESTS" = true ]; then
    PACKAGE_CMD="mvn package -DskipTests"
else
    PACKAGE_CMD="mvn package"
fi

if [ "$DEBUG_MODE" = true ]; then
    PACKAGE_CMD="$PACKAGE_CMD -X"
fi

if $PACKAGE_CMD; then
    log_success "打包成功"
    
    JAR_FILE=$(find target -name "ai-novel-writer-*.jar" -not -name "*-sources.jar" | head -n 1)
    if [ -n "$JAR_FILE" ]; then
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        log_success "生成JAR文件: $JAR_FILE (大小: $JAR_SIZE)"
    fi
else
    log_error "打包失败"
    exit 1
fi

###############################################################################
# 7. 构建总结
###############################################################################
echo ""
log_success "========================================="
log_success "     构建成功！"
log_success "========================================="
echo ""
log_info "构建产物位置:"
log_info "  - JAR文件: target/ai-novel-writer-1.0.0.jar"
echo ""
log_info "下一步操作:"
log_info "  1. 配置数据库: 编辑 src/main/resources/application-dev.yml"
log_info "  2. 配置AI密钥: 设置 DASHSCOPE_API_KEY 环境变量"
log_info "  3. 启动应用: java -jar target/ai-novel-writer-1.0.0.jar"
log_info "  4. 访问应用: http://localhost:8080"
echo ""

###############################################################################
# 8. 启动应用（可选）
###############################################################################
if [ "$RUN_AFTER_BUILD" = true ]; then
    log_info "启动应用..."
    
    # 检查环境变量
    if [ -z "$DASHSCOPE_API_KEY" ]; then
        log_warning "未设置DASHSCOPE_API_KEY环境变量"
        read -p "是否继续启动？(y/N): " -r
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 0
        fi
    fi
    
    # 启动应用
    JAR_FILE="target/ai-novel-writer-1.0.0.jar"
    if [ -f "$JAR_FILE" ]; then
        log_info "启动Spring Boot应用..."
        java -jar "$JAR_FILE" --spring.profiles.active=dev
    else
        log_error "JAR文件不存在: $JAR_FILE"
        exit 1
    fi
fi
