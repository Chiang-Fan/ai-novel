好的！以下是为你量身定制的 详细、结构化、可直接交付给 AI Coding 助手（如 Cursor、Trae、通义灵码等）进行编码的完整方案设计文档。
✅ 技术栈明确：JDK 17 + Spring Boot 3.3 + Spring AI + MySQL + Qwen-Max (DashScope)
✅ 目标：零歧义，AI 可直接生成代码
✅ 包含：模块划分、数据模型、API 设计、AI 集成逻辑、配置说明

📘 AI 小说创作助手 —— 系统设计方案（Spring AI + Qwen Max + MySQL）
一、项目目标

构建一个 Web 应用，使用户（尤其是文笔小白）能：
1. 新建小说：输入书名、故事大纲、初始正文 → 系统自动提取写作风格
2. 智能续写：提供简要剧情方向 → AI 生成符合原风格的新章节
3. 自动管理状态：每次续写后，自动解析并更新：
本章摘要（≤50 字）
人物状态变更
新增伏笔（含建议展开章节）
已触发的伏笔
4. 防止脱节：所有生成严格基于当前小说上下文（人物/伏笔/世界观/上一章）

二、技术栈

类别 技术
------ ------
语言 Java 17
框架 Spring Boot 3.3.x
AI 集成 Spring AI 1.0+（使用 spring-ai-alibaba 模块）
大模型 阿里云 Qwen-Max（通过 DashScope API）
数据库 MySQL 8.0+
ORM Spring Data JPA + Hibernate
构建工具 Maven
API RESTful JSON
异步 @Async + TaskExecutor（用于状态解析）
配置 application.yml + 环境变量
⚠️ 注意：Spring AI 官方支持 Alibaba DashScope，可直接调用 Qwen。

三、核心领域模型（JPA Entity）
1. Novel.java
java
@Entity
@Table(name = "novels")
public class Novel {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, length = 255)
private String title;

@Column(columnDefinition = "TEXT", nullable = false)
private String outline;

private LocalDateTime createdAt = LocalDateTime.now();
private LocalDateTime updatedAt;
}
2. Chapter.java
java
@Entity
@Table(name = "chapters", uniqueConstraints = @UniqueConstraint(columnNames = {"novel_id", "chapter_number"}))
public class Chapter {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "novel_id", nullable = false)
private Novel novel;

@Column(nullable = false)
private Integer chapterNumber; // 从 1 开始

@Column(columnDefinition = "TEXT", nullable = false)
private String content;

@Column(columnDefinition = "TEXT", nullable = false)
private String summary; // AI 生成的摘要（≤50字）

@Column(columnDefinition = "TEXT")
private String userPrompt; // 用户提供的续写方向

private LocalDateTime generatedAt = LocalDateTime.now();
}
3. WritingStyle.java
java
@Entity
@Table(name = "writing_styles")
public class WritingStyle {
@Id
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "novel_id", nullable = false)
private Novel novel;

@Column(nullable = false, length = 20) // "first", "third"
private String perspective;

@Column(nullable = false, length = 50)
private String tone; // "dark", "lyrical", etc.

@Column(nullable = false, length = 50)
private String sentenceStyle; // "short", "dialogue-heavy"

@Column(columnDefinition = "JSON", nullable = false)
private List<String> keywords; // 使用 Hibernate Type 或 Jackson

@Column(columnDefinition = "TEXT", nullable = false)
private String rawSample;
}
💡 JSON 支持：在 application.yml 中启用 Hibernate 的 JSON 支持（见第五部分）
4. Character.java
java
@Entity
@Table(name = "characters")
public class Character {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "novel_id", nullable = false)
private Novel novel;

@Column(nullable = false, length = 100)
private String name;

@Column(columnDefinition = "TEXT", nullable = false)
private String status;

@Column(columnDefinition = "TEXT")
private String goals;

@Column(columnDefinition = "TEXT")
private String secrets;

@Column(nullable = false)
private Integer lastUpdatedChapter = 0;
}
5. PlotHook.java
java
@Entity
@Table(name = "plot_hooks")
public class PlotHook {
public enum Status { pending, triggered, resolved }

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "novel_id", nullable = false)
private Novel novel;

@Column(columnDefinition = "TEXT", nullable = false)
private String description;

@Column(nullable = false)
private Integer expectedChapter;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Status status = Status.pending;

private Integer triggeredAtChapter;
}
6. WorldSetting.java
java
@Entity
@Table(name = "world_settings")
public class WorldSetting {
@Id
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "novel_id", nullable = false)
private Novel novel;

@Column(columnDefinition = "JSON", nullable = false)
private List<String> rules; // e.g., ["太阳有毒", "猫会说话"]
}

四、REST API 设计（Controller）
基础路径：/api/v1/novels

方法 路径 功能 请求体 响应
------ ------ ------ -------- ------
POST / 创建新小说 CreateNovelRequest NovelDto
GET /{id} 获取小说完整状态 — NovelFullStateDto
POST /{id}/chapters 生成新章节 GenerateChapterRequest ChapterDto
DTO 示例（简化）

java
// CreateNovelRequest.java
public class CreateNovelRequest {
private String title;
private String outline;
private String initialContent; // 用于提取风格
}

// GenerateChapterRequest.java
public class GenerateChapterRequest {
private String userPrompt; // 可为空
}

// NovelFullStateDto.java
public class NovelFullStateDto {
private NovelDto novel;
private WritingStyleDto writingStyle;
private WorldSettingDto worldSetting;
private List<CharacterDto> characters;
private List<PlotHookDto> plotHooks;
private ChapterDto latestChapter;
}

五、Spring AI + Qwen 集成
1. Maven 依赖（pom.xml）
xml
<dependencies>
<!-- Spring Boot -->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL -->
<dependency>
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>
<scope>runtime</scope>
</dependency>

<!-- Spring AI Alibaba (Qwen) -->
<dependency>
<groupId>org.springframework.ai</groupId>
<artifactId>spring-ai-alibaba-spring-boot-starter</artifactId>
<version>1.0.0-M1</version> <!-- 使用最新 Milestone -->
</dependency>

<!-- JSON 支持 for MySQL -->
<dependency>
<groupId>com.fasterxml.jackson.core</groupId>
<artifactId>jackson-databind</artifactId>
</dependency>
</dependencies>

<repositories>
<repository>
<id>spring-milestones</id>
<name>Spring Milestones</name>
<url>https://repo.spring.io/milestone</url>
<snapshots><enabled>false</enabled></snapshots>
</repository>
</repositories>
2. 配置文件（application.yml）
yaml
spring:
datasource:
url: jdbc:mysql://localhost:3306/novel_ai?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
username: ${DB_USERNAME:root}
password: ${DB_PASSWORD:password}
driver-class-name: com.mysql.cj.jdbc.Driver

jpa:
hibernate:
ddl-auto: update
show-sql: false
properties:
hibernate:
dialect: org.hibernate.dialect.MySQL8Dialect
format_sql: true
types:
json_default_value: "{}"

ai:
alibaba:
dashscope:
api-key: ${DASHSCOPE_API_KEY}
chat:
options:
model: qwen-max # 使用 Qwen-Max
启用异步
spring.task.execution.pool.core-size: 4
3. 自定义 JSON Type（支持 List<String> 存入 MySQL JSON 列）
java
// JsonStringListConverter.java
@Converter
public class JsonStringListConverter implements AttributeConverter<List<String>, String> {
private final ObjectMapper mapper = new ObjectMapper();

@Override
public String convertToDatabaseColumn(List<String> attribute) {
try {
return attribute == null ? "[]" : mapper.writeValueAsString(attribute);
} catch (Exception e) {
throw new RuntimeException("Failed to convert list to JSON", e);
}
}

@Override
public List<String> convertToEntityAttribute(String dbData) {
try {
return dbData == null dbData.isEmpty() ?
new ArrayList<>() : mapper.readValue(dbData, new TypeReference<>() {});
} catch (Exception e) {
throw new RuntimeException("Failed to convert JSON to list", e);
}
}
}

// 在 Entity 中使用：
@Column(columnDefinition = "JSON")
@Convert(converter = JsonStringListConverter.class)
private List<String> keywords;

六、核心服务逻辑
1. NovelService.java 关键方法

java
@Service
@Transactional
public class NovelService {

@Autowired
private ChatClient chatClient; // Spring AI 注入

public NovelDto createNovel(CreateNovelRequest request) {
// 1. 保存 Novel
Novel novel = new Novel();
novel.setTitle(request.getTitle());
novel.setOutline(request.getOutline());
novel = novelRepository.save(novel);

// 2. 提取风格
WritingStyle style = extractWritingStyle(request.getInitialContent());
style.setNovel(novel);
writingStyleRepository.save(style);

// 3. 保存第1章
String summary = analyzeChapterSummary(request.getInitialContent());
Chapter chapter = new Chapter();
chapter.setNovel(novel);
chapter.setChapterNumber(1);
chapter.setContent(request.getInitialContent());
chapter.setSummary(summary);
chapterRepository.save(chapter);

return mapToDto(novel);
}

public ChapterDto generateNextChapter(Long novelId, String userPrompt) {
// 加载完整上下文
NovelContext context = buildNovelContext(novelId);

// 构建 Prompt（见下方模板）
String prompt = buildGenerationPrompt(context, userPrompt);

// 调用 Qwen
String content = chatClient.call(prompt).getResult().getOutput().getContent();

// 解析新章节
ChapterAnalysis analysis = parseChapter(content);

// 保存章节
Chapter chapter = new Chapter();
chapter.setNovel(context.getNovel());
chapter.setChapterNumber(context.getNextChapterNumber());
chapter.setContent(content);
chapter.setSummary(analysis.getSummary());
chapter.setUserPrompt(userPrompt);
chapter = chapterRepository.save(chapter);

// 异步更新状态
updateNovelStateAsync(novelId, analysis, chapter.getChapterNumber());

return mapToDto(chapter);
}
}
2. Prompt 模板（关键！）

java
private String buildGenerationPrompt(NovelContext ctx, String userPrompt) {
return String.format("""
你是一位专业小说家，请续写下一章。

【书名】%s
【故事大纲】%s
【当前文风】叙述视角：%s；语气：%s；句式：%s；关键词：%s
【世界观规则】
%s
【当前人物状态】
%s
【待展开伏笔】
%s
【上一章结尾】
%s
【用户续写方向】
%s

要求：
1. 严格保持原有文风；
2. 人物行为必须符合其当前状态；
3. 若本章接近伏笔的 expectedChapter，请考虑自然触发；
4. 章节长度约 800-1200 字；
5. 结尾留有悬念或转折；
6. 只输出小说正文，不要任何说明、标题或注释。
""",
ctx.getTitle(),
ctx.getOutline(),
ctx.getStyle().getPerspective(),
ctx.getStyle().getTone(),
ctx.getStyle().getSentenceStyle(),
String.join(", ", ctx.getStyle().getKeywords()),
formatRules(ctx.getWorldRules()),
formatCharacters(ctx.getCharacters()),
formatPlotHooks(ctx.getPendingHooks()),
getLastLines(ctx.getLastChapterContent(), 500),
userPrompt != null ? userPrompt : "请自然推进剧情"
);
}
3. 章节解析（调用 AI）

java
private ChapterAnalysis parseChapter(String content) {
String prompt = """
请阅读以下小说章节，严格按照 JSON 格式输出分析结果：
{
"summary": "50字以内摘要",
"new_characters": [{"name": "...", "status": "..."}],
"updated_characters": [{"name": "...", "new_status": "..."}],
"new_plot_hooks": [{"description": "...", "suggested_chapter": 5}],
"triggered_hooks": ["伏笔描述1", "伏笔描述2"]
}

小说章节：
%s
""".formatted(content);

String response = chatClient.call(prompt).getResult().getOutput().getContent();
return objectMapper.readValue(response, ChapterAnalysis.class);
}

七、部署说明
本地运行
1. 安装 MySQL 8.0+
2. 创建数据库：CREATE DATABASE novel_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
3. 设置环境变量：
bash
export DASHSCOPE_API_KEY=your_key_here
export DB_USERNAME=root
export DB_PASSWORD=your_password

4. 启动应用：mvn spring-boot:run
Docker（可选）
提供 Dockerfile 和 docker-compose.yml（可后续生成）

八、交付给 AI Coding 的指令总结
请作为资深 Java/Spring Boot 工程师，基于以上方案，生成以下内容：
1. 完整的 JPA Entity（含 JSON Converter）
2. Repository 接口
3. DTO 和 Mapper（可用 MapStruct 或手动）
4. NovelService 实现（含 Prompt 构建和 AI 调用）
5. NovelController
6. application.yml 配置
7. pom.xml 依赖
要求：
使用 Spring AI 的 ChatClient 调用 Qwen-Max
所有 JSON 字段使用自定义 Converter 存入 MySQL
状态更新使用 @Async
代码符合 Spring Boot 最佳实践

这份方案已足够详细，AI Coding 助手可直接按模块生成代码。如需我继续生成 具体某一部分的完整代码（如 Entity + Converter + Service），请告诉我！