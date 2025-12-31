# Controller重构执行清单

> **创建日期**: 2025-12-31  
> **负责人**: 待分配  
> **状态**: 待开始

---

## 📋 Phase 1: 场景管理合并（预计2天）

### Day 1: Service层整合

#### 任务1.1: 分析现有Service
- [ ] 阅读 `SceneService.java`
- [ ] 阅读 `SceneManagementService.java`
- [ ] 对比两者差异，列出重复方法
- [ ] 确认依赖关系

**产出**: 差异分析文档

#### 任务1.2: 合并Service
- [ ] 创建新的 `SceneService.java` (保留原文件名)
- [ ] 迁移 `SceneManagementService` 的独有方法
  - [ ] `recordSceneUsage()`
  - [ ] `getSceneUsageHistory()`
  - [ ] `deleteSceneUsage()`
  - [ ] `recordSceneChange()`
  - [ ] `getSceneChangeHistory()`
  - [ ] `deleteSceneChange()`
  - [ ] `getSceneStatistics()`
  - [ ] `getBatchStatistics()`
- [ ] 统一DTO命名
  - [ ] `Scene` → `SceneResponse`
  - [ ] 确保所有方法返回统一格式
- [ ] 编写单元测试
  - [ ] 基础CRUD测试
  - [ ] 使用记录测试
  - [ ] 变化历史测试
  - [ ] 统计功能测试

**产出**: 合并后的 `SceneService.java`

### Day 2: Controller层重构

#### 任务1.3: 创建新Controller
- [ ] 参考 `docs/refactoring-examples/SceneController-merged.java`
- [ ] 创建新的 `SceneController.java`
- [ ] 实现所有端点
  - [ ] 基础CRUD (5个方法)
  - [ ] AI推荐 (1个方法)
  - [ ] 使用记录 (3个方法)
  - [ ] 变化历史 (3个方法)
  - [ ] 统计分析 (2个方法)
- [ ] 添加完整注解
  - [ ] `@Operation` 描述
  - [ ] `@Parameter` 说明
  - [ ] `@Valid` 验证
- [ ] 支持多路径映射 (向后兼容)

```java
@RequestMapping(value = {"/api/scenes", "/api/scene-management"})
```

**产出**: 新的 `SceneController.java`

#### 任务1.4: 前端适配
- [ ] 查找前端所有调用 `/api/scene-management` 的地方
  ```bash
  grep -r "scene-management" src/main/frontend/src/
  ```
- [ ] 替换为 `/api/scenes`
- [ ] 测试前端功能

**产出**: 更新的前端代码

#### 任务1.5: 测试验证
- [ ] 编写集成测试
- [ ] 测试所有API端点
- [ ] 测试向后兼容性（旧路径仍可用）
- [ ] 性能测试（响应时间无退化）

**产出**: 集成测试套件

#### 任务1.6: 清理工作
- [ ] 删除 `SceneManagementController.java`
- [ ] 删除 `SceneManagementService.java`
- [ ] 更新API文档
- [ ] 提交PR

**产出**: 干净的代码库

---

## 📋 Phase 2: 续写功能统一（预计3天）

### Day 1: 需求分析与设计

#### 任务2.1: 功能对比
- [ ] 列出 `ContinuationController` 的所有方法
- [ ] 列出 `EnhancedContinuationController` 的所有方法
- [ ] 列出 `ChapterController.continueChapter()` 的逻辑
- [ ] 确定重叠功能和独有功能
- [ ] 设计统一接口

**产出**: 功能对比表、接口设计文档

#### 任务2.2: DTO设计
- [ ] 设计 `ContinuationRequest`
  ```java
  class ContinuationRequest {
      Long chapterId;
      String mode;  // "basic" | "enhanced" | "auto"
      Integer targetWordCount;
      List<Constraint> constraints;  // 可选
      Boolean validationEnabled;     // 可选
  }
  ```
- [ ] 设计 `ContinuationResponse`
- [ ] 设计其他相关DTO
  - `ConstraintPreset`
  - `ConstraintValidationResult`
  - `ContinuationAnalysisResult`
  - `WritingSuggestion`

**产出**: DTO类文件

### Day 2: Service层重构

#### 任务2.3: 创建WritingAssistantService
- [ ] 创建新Service: `WritingAssistantService.java`
- [ ] 注入现有服务
  ```java
  @Service
  public class WritingAssistantService {
      private final SmartContinuationService basicService;
      private final EnhancedContinuationService enhancedService;
      private final ChapterService chapterService;
  }
  ```
- [ ] 实现模式路由逻辑
  ```java
  public ContinuationResponse generateContinuation(ContinuationRequest request) {
      switch (request.getMode()) {
          case "basic": return basicService.generate(request);
          case "enhanced": return enhancedService.generate(request);
          case "auto": return autoSelectAndGenerate(request);
      }
  }
  ```
- [ ] 实现约束管理
- [ ] 实现建议生成
- [ ] 编写单元测试

**产出**: `WritingAssistantService.java`

### Day 3: Controller层重构

#### 任务2.4: 创建WritingAssistantController
- [ ] 参考 `docs/refactoring-examples/WritingAssistantController.java`
- [ ] 实现所有端点（共20+个方法）
- [ ] 添加完整文档注释
- [ ] 实现快速续写快捷方式

**产出**: `WritingAssistantController.java`

#### 任务2.5: 调整ChapterController
- [ ] 保留 `POST /chapters/{id}/continue` 作为快捷入口
- [ ] 内部委托给 `WritingAssistantService`
- [ ] 添加弃用警告

```java
@PostMapping("/{id}/continue")
@Deprecated
@Operation(summary = "【建议使用】/api/writing-assistant/continuation")
public ApiResponse<Chapter> quickContinue(@PathVariable Long id) {
    // 委托给WritingAssistantService
}
```

**产出**: 更新的 `ChapterController.java`

#### 任务2.6: 创建兼容层
- [ ] 创建 `ContinuationControllerLegacy.java`
- [ ] 保留旧路径 `/api/continuation`
- [ ] 所有方法委托给新Controller
- [ ] 添加 `@Deprecated` 注解
- [ ] 响应头添加迁移提示

```java
@Deprecated
@RestController
@RequestMapping("/api/continuation")
public class ContinuationControllerLegacy {
    private final WritingAssistantService service;
    
    @PostMapping("/generate")
    public ApiResponse<ContinuationResponse> generate(@RequestBody ContinuationRequest request) {
        // 委托
        return service.generateContinuation(request)
            .withHeader("X-API-Deprecated", "true")
            .withHeader("X-API-Migrate-To", "/api/writing-assistant/continuation");
    }
}
```

**产出**: 兼容层Controller

#### 任务2.7: 前端适配
- [ ] 查找所有调用续写API的地方
  ```bash
  grep -r "/api/continuation" src/main/frontend/
  grep -r "continueChapter" src/main/frontend/
  ```
- [ ] 替换为新API
- [ ] 测试功能

**产出**: 更新的前端代码

#### 任务2.8: 测试与清理
- [ ] 编写集成测试
- [ ] 测试所有续写场景
- [ ] 测试约束验证
- [ ] 测试建议生成
- [ ] 3个月后删除旧Controller (设置日历提醒)

**产出**: 完整的测试套件

---

## 📋 Phase 3: 文本增强统一（预计2天）

### 任务3.1: Service合并
- [ ] 分析三个Service的差异
  - `OptimizationService`
  - `DescriptionEnhancementService`
  - `StyleTransferService`
- [ ] 创建 `TextEnhancementService`
- [ ] 实现类型化增强
  ```java
  public EnhancementResult enhance(EnhancementRequest request) {
      switch (request.getType()) {
          case "optimize": return optimizeText(request);
          case "description": return enhanceDescription(request);
          case "style-transfer": return transferStyle(request);
          case "rhythm": return adjustRhythm(request);
      }
  }
  ```
- [ ] 编写单元测试

**产出**: `TextEnhancementService.java`

### 任务3.2: Controller实现
- [ ] 创建 `TextEnhancementController`
- [ ] 实现统一增强入口
- [ ] 实现批量处理
- [ ] 实现质量评分
- [ ] 添加文档注释

**产出**: `TextEnhancementController.java`

### 任务3.3: 兼容与测试
- [ ] 保留旧Controller作为委托（3个月）
- [ ] 前端适配
- [ ] 集成测试

---

## 📋 Phase 4: 情节管理合并（预计1天）

### 任务4.1: Service合并
- [ ] 合并 `PlotDevelopmentService` + `PlotSimulationService`
- [ ] 创建 `PlotService`
- [ ] 单元测试

### 任务4.2: Controller重构
- [ ] 创建 `PlotController`
- [ ] 可选：整合 `PlotHookController`
- [ ] 或保持独立但统一路由

### 任务4.3: 测试验证
- [ ] 集成测试
- [ ] 前端适配

---

## 📋 Phase 5: 推荐标准化（预计1周）

### 任务5.1: 接口定义
- [ ] 定义 `RecommendationProvider` 接口
- [ ] 定义统一的Request/Response格式

### 任务5.2: Controller实现
- [ ] CharacterController 实现接口
- [ ] SceneController 实现接口
- [ ] OutlineController 实现接口
- [ ] 统一文档注释

### 任务5.3: 测试
- [ ] 单元测试
- [ ] 集成测试

---

## 📋 Phase 6: 分析服务抽象（预计1周）

### 任务6.1: 抽象层设计
- [ ] 定义 `AnalysisService<T, R>` 接口
- [ ] 创建 `UnifiedAnalysisOrchestrator`

### 任务6.2: 重构现有Analyzer
- [ ] 重构 `ChapterAnalyzer`
- [ ] 重构 `ContentAnalyzer`
- [ ] 重构 `RhythmAnalyzer`

### 任务6.3: Controller适配
- [ ] 更新 `ChapterAnalysisController`
- [ ] 更新 `SmartWritingController`
- [ ] 更新 `PaceControlController`

### 任务6.4: 性能优化
- [ ] 添加分析结果缓存
- [ ] 压力测试
- [ ] 性能监控

---

## 📊 验收标准

### 功能验收
- [ ] 所有现有功能正常工作
- [ ] 旧API路径仍可访问（兼容期）
- [ ] 新API文档完整准确
- [ ] 前端功能无异常

### 质量验收
- [ ] 单元测试覆盖率 > 80%
- [ ] 集成测试通过率 100%
- [ ] SonarQube评分 > A
- [ ] 无高危代码问题

### 性能验收
- [ ] P99响应时间 < 500ms
- [ ] 错误率 < 0.1%
- [ ] 内存占用无明显增长
- [ ] CPU使用率无异常

### 文档验收
- [ ] API文档完整更新
- [ ] 迁移指南已发布
- [ ] Changelog已更新
- [ ] 代码注释完善

---

## 🔔 提醒事项

### 3个月后删除（设置日历）
- [ ] 2026-03-31: 删除 `SceneManagementController`
- [ ] 2026-03-31: 删除 `ContinuationController`
- [ ] 2026-03-31: 删除 `EnhancedContinuationController`
- [ ] 2026-03-31: 删除 `ContinuationControllerLegacy`
- [ ] 2026-03-31: 删除 `OptimizationController` (如合并)
- [ ] 2026-03-31: 删除 `DescriptionEnhancementController` (如合并)
- [ ] 2026-03-31: 删除 `StyleTransferController` (如合并)

### 监控指标
- [ ] 设置旧API调用量监控
- [ ] 设置错误率告警
- [ ] 设置性能指标监控

---

## 📞 协作沟通

### 会议安排
- [ ] Kickoff会议：重构方案讲解
- [ ] Daily Standup：每日进度同步
- [ ] Phase Review：每阶段完成后Review
- [ ] 最终验收会议

### 干系人
- **开发**: 待分配
- **前端**: 待分配
- **测试**: 待分配
- **Tech Lead**: 待分配
- **Product Owner**: 待分配

---

## 📝 备注

- 每个Phase完成后提交独立PR
- Code Review必须通过才能合并
- 重要变更需要通知相关团队
- 遇到阻塞及时升级

---

**清单创建时间**: 2025-12-31  
**预计完成时间**: 2026-01-31 (4周)  
**实际完成时间**: _待填写_
