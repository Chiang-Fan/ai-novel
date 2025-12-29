# 多阶段构建Dockerfile

# ===== 阶段1: Maven构建 =====
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 复制pom.xml和源码
COPY pom.xml .
COPY src ./src

# 下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

# 构建应用（包含前端构建）
RUN mvn clean package -DskipTests -B

# ===== 阶段2: 运行时镜像 =====
FROM eclipse-temurin:17-jre-alpine

# 安装必要工具
RUN apk add --no-cache curl

# 创建应用目录
WORKDIR /app

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 从构建阶段复制JAR文件
COPY --from=build /app/target/ai-novel-writer.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
