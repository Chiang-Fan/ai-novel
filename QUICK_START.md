# 🚀 AI Novel Writer - 快速开始指南

## 📋 前置要求

- **Java**: JDK 17 或更高版本
- **Maven**: 3.6 或更高版本
- **MySQL**: 8.0 或更高版本
- **Node.js**: 16+ (可选，仅前端开发需要)

## ⚡️ 30秒快速启动

### 方式一：使用构建脚本（推荐）

```bash
# 1. 克隆项目（如果还没有）
git clone <your-repo-url>
cd ai-write-agent

# 2. 配置数据库和API密钥（见下方配置说明）

# 3. 一键构建并运行
./build-and-test.sh -s -r
# -s: 跳过测试（加快速度）
# -r: 构建后自动运行
```

### 方式二：手动步骤

```bash
# 1. 编译打包
mvn clean package -DskipTests

# 2. 启动应用
java -jar target/ai-novel-writer-1.0.0.jar --spring.profiles.active=dev
```

### 方式三：使用IDE

1. 导入Maven项目到IDEA/Eclipse
2. 配置`application-dev.yml`
3. 运行`AiNovelApplication`主类

---

## ⚙️ 配置说明

### 1. 数据库配置

编辑`src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_novel_writer?useUnicode=true&characterEncoding=utf8mb4
    username: root
    password: your_password  # 修改为你的密码
```

**创建数据库**:
```sql
CREATE DATABASE ai_novel_writer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. AI服务配置

**方式一：环境变量（推荐）**
```bash
export DASHSCOPE_API_KEY="your_api_key_here"
```

**方式二：配置文件**

编辑`application-dev.yml`:
```yaml
spring:
  ai:
    openai:
      api-key: "your_api_key_here"
```

### 3. 服务端口配置（可选）

默认端口为8080，如需修改：
```yaml
server:
  port: 9090
```

---

## 📦 构建选项

### 完整构建（含测试）
```bash
./build-and-test.sh
```

### 跳过测试（快速构建）
```bash
./build-and-test.sh -s
```

### 清理后重新构建
```bash
./build-and-test.sh -c
```

### 仅构建后端（跳过前端）
```bash
./build-and-test.sh -f -s
```

### 构建后自动运行
```bash
./build-and-test.sh -s -r
```

### 调试模式
```bash
./build-and-test.sh -d
```

---

## 🧪 验证安装

### 1. 健康检查

```bash
curl http://localhost:8080/api/health
```

**预期响应**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "status": "UP",
    "timestamp": "2025-12-27T10:00:00",
    "application": "AI Novel Writer",
    "version": "1.0.0"
  }
}
```

### 2. 创建测试小说

```bash
curl -X POST http://localhost:8080/api/novels \
  -H "Content-Type: application/json" \
  -d '{
    "title": "我的第一本小说",
    "author": "测试用户",
    "type": "玄幻",
    "targetWordCount": 100000
  }'
```

### 3. AI续写测试

```bash
curl -X POST http://localhost:8080/api/chapters/continue \
  -H "Content-Type: application/json" \
  -d '{
    "novelId": 1,
    "chapterNumber": 1,
    "direction": "主角穿越到异世界",
    "temperature": 0.7,
    "maxLength": 1000
  }'
```

---

## 🎯 使用流程

### 标准工作流程

```
1. 创建小说 (POST /api/novels)
   ↓
2. 创建角色 (POST /api/characters)
   ↓
3. 创建场景 (POST /api/scenes) [可选]
   ↓
4. AI续写章节 (POST /api/chapters/continue)
   ↓
5. 查看/编辑章节 (GET/PUT /api/chapters/{id})
   ↓
6. 重复步骤4-5，完成整部小说
```

### 示例：创建一个玄幻小说项目

```bash
# 1. 创建小说
NOVEL_ID=$(curl -s -X POST http://localhost:8080/api/novels \
  -H "Content-Type: application/json" \
  -d '{
    "title": "仙道至尊",
    "author": "张三",
    "type": "玄幻",
    "writingStyle": "热血、轻松",
    "targetWordCount": 1000000
  }' | jq -r '.data.id')

echo "小说ID: $NOVEL_ID"

# 2. 创建主角
curl -X POST http://localhost:8080/api/characters \
  -H "Content-Type: application/json" \
  -d "{
    \"novelId\": $NOVEL_ID,
    \"name\": \"李逍遥\",
    \"importance\": \"MAIN\",
    \"age\": 18,
    \"gender\": \"男\",
    \"description\": \"资质平庸但心性坚韧的少年\"
  }"

# 3. AI续写第一章
curl -X POST http://localhost:8080/api/chapters/continue \
  -H "Content-Type: application/json" \
  -d "{
    \"novelId\": $NOVEL_ID,
    \"chapterNumber\": 1,
    \"direction\": \"主角在家乡遭遇危机，被迫踏上修仙之路\",
    \"temperature\": 0.7,
    \"maxLength\": 2000
  }"
```

---

## 🔧 常见问题

### Q1: 数据库连接失败
```
A: 检查MySQL是否启动，用户名密码是否正确
   sudo systemctl status mysql  # Linux
   ps aux | grep mysql          # macOS
```

### Q2: AI续写没有响应
```
A: 检查DASHSCOPE_API_KEY是否设置
   echo $DASHSCOPE_API_KEY
   
   检查网络连接是否正常
   curl https://dashscope.aliyuncs.com
```

### Q3: 端口被占用
```
A: 修改application-dev.yml中的server.port
   或者杀死占用进程:
   lsof -i :8080
   kill -9 <PID>
```

### Q4: Maven构建失败
```
A: 清理缓存后重试
   mvn clean
   rm -rf ~/.m2/repository
   mvn package -DskipTests
```

### Q5: 前端资源404
```
A: 确保前端已构建并复制到static目录
   cd frontend && npm run build
   cp -r build/* ../src/main/resources/static/
```

---

## 📚 进阶使用

### 1. 自定义提示词

编辑`PromptManager.java`中的提示词模板以适配你的写作风格。

### 2. 调整AI参数

- `temperature`: 0.0-2.0，越高越有创意（推荐0.7-0.9）
- `maxLength`: 生成长度，建议1000-3000字

### 3. 数据库优化

生产环境建议：
- 启用MySQL慢查询日志
- 配置连接池参数
- 定期备份数据

---

## 🐳 Docker部署

```bash
# 1. 构建镜像
docker build -t ai-novel-writer:latest .

# 2. 启动容器
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DASHSCOPE_API_KEY=your_key \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/ai_novel_writer \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  --name ai-novel-writer \
  ai-novel-writer:latest
```

---

## 📖 相关文档

- [API文档](./API_DOCUMENTATION.md) - 完整的RESTful API说明
- [README](./README_SPRING_BOOT.md) - 项目详细说明
- [迁移总结](./MIGRATION_SUMMARY.md) - Python到Java迁移说明

---

## 💡 提示

- 首次启动会自动创建数据库表结构（Flyway）
- 建议使用Postman或curl测试API
- 查看日志: `tail -f logs/app.log`
- 监控指标: http://localhost:8080/actuator/metrics

---

## 🆘 获取帮助

如遇问题，请检查：
1. 日志文件: `logs/app.log`
2. 数据库连接状态
3. API密钥是否有效
4. 端口是否被占用

祝你使用愉快！🎉
