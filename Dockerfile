###############################################################################
# AI Novel Writer - Production Dockerfile
# 多阶段构建：Maven编译 + JRE运行
###############################################################################

# ============================================================================
# 阶段1: 构建阶段 - 使用Maven编译项目
# ============================================================================
FROM maven:3.9-amazoncorretto-17 AS builder

# 设置工作目录
WORKDIR /build

# 复制Maven配置文件（利用Docker缓存）
COPY pom.xml .

# 下载依赖（这一层会被缓存，除非pom.xml改变）
RUN mvn dependency:go-offline -B || true

# 复制源代码
COPY src ./src

# 编译打包（跳过测试加快构建速度）
RUN mvn clean package -DskipTests -B

# 查找生成的JAR文件
RUN find /build/target -name "*.jar" -not -name "*-sources.jar" -exec cp {} /build/app.jar \;

# ============================================================================
# 阶段2: 运行阶段 - 使用精简的JRE镜像
# ============================================================================
FROM amazoncorretto:17-alpine

# 安装必要的工具
RUN apk add --no-cache \
    curl \
    bash \
    tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 创建应用用户（安全最佳实践：不使用root用户运行应用）
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制JAR文件
COPY --from=builder /build/app.jar /app/app.jar

# 创建日志目录
RUN mkdir -p /app/logs && \
    chown -R appuser:appuser /app

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# JVM优化参数 - 增加内存并优化GC
ENV JAVA_OPTS="-Xms128m -Xmx4096m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:G1HeapRegionSize=16m \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -XX:+UseCompressedOops \
    -XX:+UseCompressedClassPointers \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Shanghai"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
