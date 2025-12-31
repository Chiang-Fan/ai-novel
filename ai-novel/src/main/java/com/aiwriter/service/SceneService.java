package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import com.aiwriter.service.ai.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SceneService {
    
    private final SceneRepository sceneRepository;
    private final ChapterRepository chapterRepository;
    private final WorldSettingRepository worldSettingRepository;
    private final SceneUsageRepository sceneUsageRepository;
    private final SceneChangeRepository sceneChangeRepository;
    private final NovelService novelService;
    private final CharacterService characterService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    
    // ========================================
    // 基础CRUD操作（返回统一DTO）
    // ========================================
    
    /**
     * 获取小说的所有场景（支持过滤）
     */
    @Transactional(readOnly = true)
    public List<SceneResponse> getScenesByNovel(Long novelId, String type, String keyword) {
        List<Scene> scenes;
        
        if (type != null && !type.isEmpty()) {
            scenes = sceneRepository.findByNovelIdAndSceneTypeOrderByNameAsc(novelId, type);
        } else if (keyword != null && !keyword.isEmpty()) {
            scenes = sceneRepository.findByNovelIdAndNameContainingOrderByNameAsc(novelId, keyword);
        } else {
            scenes = sceneRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
        }
        
        return scenes.stream()
            .map(this::toSceneResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取小说的所有场景（兼容旧代码，返回Entity）
     * @deprecated 使用 getScenesByNovel(Long, String, String) 替代
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<Scene> getScenesByNovel(Long novelId) {
        return sceneRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
    }
    
    /**
     * 根据ID获取场景Entity（兼容旧代码）
     * @deprecated 使用 getScene(Long) 替代，返回SceneResponse
     */
    @Deprecated
    @Transactional(readOnly = true)
    public Scene getSceneById(Long id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场景不存在"));
    }
    
    /**
     * 创建场景（兼容旧代码，接受Entity）
     * @deprecated 使用 createScene(CreateSceneRequest) 替代
     */
    @Deprecated
    @Transactional
    public Scene createScene(Scene scene) {
        Scene saved = sceneRepository.save(scene);
        log.info("创建场景成功(旧API): {} (小说ID: {})", saved.getName(), saved.getNovelId());
        return saved;
    }
    
    /**
     * 更新场景（兼容旧代码，接受Entity）
     * @deprecated 使用 updateScene(Long, UpdateSceneRequest) 替代
     */
    @Deprecated
    @Transactional
    public Scene updateScene(Long id, Scene scene) {
        Scene existing = getSceneById(id);
        
        // 更新字段
        existing.setName(scene.getName());
        existing.setSceneType(scene.getSceneType());
        existing.setDescription(scene.getDescription());
        existing.setAtmosphere(scene.getAtmosphere());
        existing.setTimePeriod(scene.getTimePeriod());
        existing.setLocation(scene.getLocation());
        existing.setWeather(scene.getWeather());
        existing.setProps(scene.getProps());
        existing.setInvolvedCharacters(scene.getInvolvedCharacters());
        existing.setChapterReferences(scene.getChapterReferences());
        existing.setNotes(scene.getNotes());
        
        Scene updated = sceneRepository.save(existing);
        log.info("更新场景成功(旧API): {}", updated.getName());
        return updated;
    }
    
    /**
     * 获取场景详情
     */
    @Transactional(readOnly = true)
    public SceneResponse getScene(Long id) {
        Scene scene = sceneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("场景不存在"));
        return toSceneResponse(scene);
    }
    
    /**
     * 创建场景
     */
    @Transactional
    public SceneResponse createScene(CreateSceneRequest request) {
        Scene scene = new Scene();
        scene.setNovelId(request.getNovelId());
        scene.setName(request.getSceneName());
        scene.setSceneType(request.getSceneType());
        scene.setLocation(request.getLocationDesc());
        scene.setDescription(request.getDescription());
        scene.setAtmosphere(request.getAtmosphere());
        scene.setImportanceScore(request.getImportanceScore());
        
        // 处理标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            scene.setTags(String.join(",", request.getTags()));
        }
        
        scene = sceneRepository.save(scene);
        log.info("创建场景成功: {} (小说ID: {})", scene.getName(), scene.getNovelId());
        return toSceneResponse(scene);
    }
    
    /**
     * 更新场景
     */
    @Transactional
    public SceneResponse updateScene(Long id, UpdateSceneRequest request) {
        Scene scene = sceneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("场景不存在"));
        
        if (request.getSceneName() != null) scene.setName(request.getSceneName());
        if (request.getSceneType() != null) scene.setSceneType(request.getSceneType());
        if (request.getLocationDesc() != null) scene.setLocation(request.getLocationDesc());
        if (request.getDescription() != null) scene.setDescription(request.getDescription());
        if (request.getAtmosphere() != null) scene.setAtmosphere(request.getAtmosphere());
        if (request.getImportanceScore() != null) scene.setImportanceScore(request.getImportanceScore());
        if (request.getIsRecurring() != null) scene.setIsRecurring(request.getIsRecurring());
        if (request.getTags() != null) {
            scene.setTags(String.join(",", request.getTags()));
        }
        
        scene = sceneRepository.save(scene);
        log.info("更新场景成功: {}", scene.getName());
        return toSceneResponse(scene);
    }
    
    /**
     * 删除场景
     */
    @Transactional
    public void deleteScene(Long id) {
        sceneRepository.deleteById(id);
        log.info("删除场景成功: {}", id);
    }
    
    // ========================================
    // 使用记录管理
    // ========================================
    
    /**
     * 记录场景使用
     */
    @Transactional
    public SceneUsageResponse recordSceneUsage(CreateSceneUsageRequest request) {
        SceneUsage usage = new SceneUsage();
        usage.setSceneId(request.getSceneId());
        usage.setChapterId(request.getChapterId());
        usage.setUsageTime(request.getUsageTime());
        usage.setSceneState(request.getSceneState());
        usage.setWeather(request.getWeather());
        usage.setTimeOfDay(request.getTimeOfDay());
        usage.setNotes(request.getNotes());
        
        usage = sceneUsageRepository.save(usage);
        
        // 更新场景的 isRecurring 标记
        long usageCount = sceneUsageRepository.countBySceneId(request.getSceneId());
        if (usageCount > 1) {
            Scene scene = sceneRepository.findById(request.getSceneId()).orElse(null);
            if (scene != null && !Boolean.TRUE.equals(scene.getIsRecurring())) {
                scene.setIsRecurring(true);
                sceneRepository.save(scene);
            }
        }
        
        return toSceneUsageResponse(usage);
    }
    
    /**
     * 获取场景使用历史
     */
    @Transactional(readOnly = true)
    public List<SceneUsageResponse> getSceneUsageHistory(Long sceneId) {
        List<SceneUsage> usages = sceneUsageRepository.findBySceneIdOrderByUsageTimeDesc(sceneId);
        return usages.stream()
            .map(this::toSceneUsageResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 删除场景使用记录
     */
    @Transactional
    public void deleteSceneUsage(Long usageId) {
        sceneUsageRepository.deleteById(usageId);
    }
    
    // ========================================
    // 变化记录管理
    // ========================================
    
    /**
     * 记录场景变化
     */
    @Transactional
    public SceneChangeResponse recordSceneChange(CreateSceneChangeRequest request) {
        SceneChange change = new SceneChange();
        change.setSceneId(request.getSceneId());
        change.setChangeType(request.getChangeType());
        change.setChangeDesc(request.getChangeDesc());
        change.setBeforeState(request.getBeforeState());
        change.setAfterState(request.getAfterState());
        change.setRelatedChapterId(request.getRelatedChapterId());
        
        change = sceneChangeRepository.save(change);
        return toSceneChangeResponse(change);
    }
    
    /**
     * 获取场景变化历史
     */
    @Transactional(readOnly = true)
    public List<SceneChangeResponse> getSceneChangeHistory(Long sceneId) {
        List<SceneChange> changes = sceneChangeRepository.findBySceneIdOrderByCreatedAtDesc(sceneId);
        return changes.stream()
            .map(this::toSceneChangeResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 删除场景变化记录
     */
    @Transactional
    public void deleteSceneChange(Long changeId) {
        sceneChangeRepository.deleteById(changeId);
    }
    
    // ========================================
    // 统计分析
    // ========================================
    
    /**
     * 获取场景统计
     */
    @Transactional(readOnly = true)
    public SceneStatisticsResponse getSceneStatistics(Long sceneId) {
        Scene scene = sceneRepository.findById(sceneId)
            .orElseThrow(() -> new RuntimeException("场景不存在"));
        
        long totalUsages = sceneUsageRepository.countBySceneId(sceneId);
        
        SceneUsage firstUsage = sceneUsageRepository.findFirstBySceneIdOrderByUsageTimeAsc(sceneId);
        SceneUsage lastUsage = sceneUsageRepository.findFirstBySceneIdOrderByUsageTimeDesc(sceneId);
        
        String firstChapter = "";
        String lastChapter = "";
        
        if (firstUsage != null) {
            Chapter chapter = chapterRepository.findById(firstUsage.getChapterId()).orElse(null);
            firstChapter = chapter != null ? chapter.getTitle() : "";
        }
        
        if (lastUsage != null) {
            Chapter chapter = chapterRepository.findById(lastUsage.getChapterId()).orElse(null);
            lastChapter = chapter != null ? chapter.getTitle() : "";
        }
        
        // 按时段统计
        Map<String, Long> usageByTimeOfDay = new HashMap<>();
        List<Object[]> timeStats = sceneUsageRepository.countByTimeOfDay(sceneId);
        for (Object[] stat : timeStats) {
            if (stat[0] != null) {
                usageByTimeOfDay.put((String) stat[0], ((Number) stat[1]).longValue());
            }
        }
        
        // 按天气统计
        Map<String, Long> usageByWeather = new HashMap<>();
        List<SceneUsage> allUsages = sceneUsageRepository.findBySceneIdOrderByUsageTimeDesc(sceneId);
        for (SceneUsage usage : allUsages) {
            if (usage.getWeather() != null) {
                usageByWeather.merge(usage.getWeather(), 1L, Long::sum);
            }
        }
        
        long changeCount = sceneChangeRepository.countBySceneId(sceneId);
        
        return SceneStatisticsResponse.builder()
            .sceneId(sceneId)
            .sceneName(scene.getName())
            .totalUsages(totalUsages)
            .firstUsedChapter(firstChapter)
            .lastUsedChapter(lastChapter)
            .usageByTimeOfDay(usageByTimeOfDay)
            .usageByWeather(usageByWeather)
            .changeCount(changeCount)
            .build();
    }
    
    /**
     * 批量获取场景统计
     */
    @Transactional(readOnly = true)
    public List<SceneStatisticsResponse> getBatchStatistics(Long novelId) {
        List<Scene> scenes = sceneRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
        return scenes.stream()
            .map(scene -> getSceneStatistics(scene.getId()))
            .collect(Collectors.toList());
    }
    
    // ========================================
    // 私有辅助方法：DTO转换
    // ========================================
    
    private SceneResponse toSceneResponse(Scene scene) {
        long usageCount = sceneUsageRepository.countBySceneId(scene.getId());
        SceneUsage lastUsage = sceneUsageRepository.findFirstBySceneIdOrderByUsageTimeDesc(scene.getId());
        
        List<String> tags = new ArrayList<>();
        if (scene.getTags() != null && !scene.getTags().isEmpty()) {
            tags = Arrays.asList(scene.getTags().split(","));
        }
        
        return SceneResponse.builder()
            .id(scene.getId())
            .novelId(scene.getNovelId())
            .sceneName(scene.getName())
            .sceneType(scene.getSceneType())
            .locationDesc(scene.getLocation())
            .description(scene.getDescription())
            .atmosphere(scene.getAtmosphere())
            .tags(tags)
            .importanceScore(scene.getImportanceScore())
            .isRecurring(scene.getIsRecurring())
            .usageCount(usageCount)
            .lastUsedAt(lastUsage != null ? lastUsage.getUsageTime() : null)
            .createdAt(scene.getCreatedAt())
            .updatedAt(scene.getUpdatedAt())
            .build();
    }
    
    private SceneUsageResponse toSceneUsageResponse(SceneUsage usage) {
        Scene scene = sceneRepository.findById(usage.getSceneId()).orElse(null);
        Chapter chapter = chapterRepository.findById(usage.getChapterId()).orElse(null);
        
        return SceneUsageResponse.builder()
            .id(usage.getId())
            .sceneId(usage.getSceneId())
            .sceneName(scene != null ? scene.getName() : "")
            .chapterId(usage.getChapterId())
            .chapterTitle(chapter != null ? chapter.getTitle() : "")
            .usageTime(usage.getUsageTime())
            .sceneState(usage.getSceneState())
            .weather(usage.getWeather())
            .timeOfDay(usage.getTimeOfDay())
            .notes(usage.getNotes())
            .createdAt(usage.getCreatedAt())
            .build();
    }
    
    private SceneChangeResponse toSceneChangeResponse(SceneChange change) {
        Scene scene = sceneRepository.findById(change.getSceneId()).orElse(null);
        Chapter chapter = change.getRelatedChapterId() != null ? 
            chapterRepository.findById(change.getRelatedChapterId()).orElse(null) : null;
        
        return SceneChangeResponse.builder()
            .id(change.getId())
            .sceneId(change.getSceneId())
            .sceneName(scene != null ? scene.getName() : "")
            .changeType(change.getChangeType())
            .changeDesc(change.getChangeDesc())
            .beforeState(change.getBeforeState())
            .afterState(change.getAfterState())
            .relatedChapterId(change.getRelatedChapterId())
            .chapterTitle(chapter != null ? chapter.getTitle() : "")
            .createdAt(change.getCreatedAt())
            .build();
    }
    
    // ========================================
    // AI场景推荐
    // ========================================
    
    /**
     * AI智能推荐场景
     */
    public List<SceneRecommendationResponse> recommendScenes(SceneRecommendationRequest request) {
        try {
            Novel novel = novelService.getNovel(request.getNovelId());
            List<Scene> existingScenes = sceneRepository.findByNovelIdOrderByCreatedAtDesc(request.getNovelId());
            List<com.aiwriter.entity.Character> characters = characterService.getCharactersByNovel(request.getNovelId());
            
            // 获取地理/历史类世界设定
            List<WorldSetting> worldSettings = worldSettingRepository.findByNovelId(request.getNovelId())
                .stream()
                .filter(ws -> "地理".equals(ws.getCategory()) || "历史".equals(ws.getCategory()) || "文化".equals(ws.getCategory()))
                .limit(8)
                .collect(Collectors.toList());
            
            // 获取最近章节的场景参考
            List<Chapter> recentChapters = chapterRepository.findTopNByNovelId(request.getNovelId(), 2);
            
            String systemPrompt = buildSceneRecommendationSystemPrompt();
            String userPrompt = buildSceneRecommendationUserPrompt(novel, existingScenes, characters, worldSettings, recentChapters, request.getCount());
            
            String aiResult = aiService.chatJson(systemPrompt, userPrompt);
            log.info("AI场景推荐原始结果: {}", aiResult);
            
            // 解析JSON结果
            List<SceneRecommendationResponse> recommendations = objectMapper.readValue(
                aiResult, 
                new TypeReference<List<SceneRecommendationResponse>>() {}
            );
            
            log.info("成功生成{}个场景推荐", recommendations.size());
            return recommendations;
            
        } catch (Exception e) {
            log.error("AI场景推荐失败", e);
            return generateRandomSceneRecommendations(request.getCount());
        }
    }
    
    private String buildSceneRecommendationSystemPrompt() {
        return """
你是一位专业的小说场景设计专家，擅长根据小说类型、已有场景和角色创造富有表现力的新场景。

## 核心任务
根据小说的基本信息、已有场景和角色，推荐适合的新场景。新场景应该：
1. 符合小说的类型、风格和世界观
2. 为情节发展和角色互动提供合适的舞台
3. 具有鲜明的氛围和视觉感
4. 与已有场景形成多样化的场景库

## 场景设计要点
- **name**: 场景名称，简洁明了
- **sceneType**: LOCATION(地点)/EVENT(事件)/TIME_PERIOD(时间段)
- **location**: 具体地点描述
- **timePeriod**: 时间段（如"清晨"、"深夜"、"春季"等）
- **weather**: 天气状况（如"晴朗"、"暴雨"、"雪天"等）
- **description**: 详细的场景描述，至少150字，包含视觉、听觉、嗅觉等感官细节
- **atmosphere**: 场景氛围描述，至少80字，传达情绪和基调
- **props**: 重要道具列表，数组格式，每个道具简短描述

## 响应格式
返回JSON数组，每个场景包含上述所有字段。确保JSON格式正确，可直接解析。

示例：
[
  {
    "name": "废弃工厂",
    "sceneType": "LOCATION",
    "location": "城市郊区的旧工业区",
    "timePeriod": "深夜",
    "weather": "阴雨",
    "description": "...",
    "atmosphere": "...",
    "props": ["生锈的机器", "破碎的窗户", "积水的地面"]
  }
]
""";
    }
    
    private String buildSceneRecommendationUserPrompt(Novel novel, List<Scene> existingScenes, 
                                                      List<com.aiwriter.entity.Character> characters, List<WorldSetting> worldSettings,
                                                      List<Chapter> recentChapters, int count) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== 小说基本信息 ===\n");
        sb.append("书名：").append(novel.getTitle()).append("\n");
        sb.append("类型：").append(novel.getGenre() != null ? novel.getGenre() : "未指定").append("\n");
        sb.append("简介：").append(novel.getDescription() != null ? novel.getDescription() : "无").append("\n");
        sb.append("创作风格：").append(novel.getWritingStyle() != null ? novel.getWritingStyle() : "未指定").append("\n\n");
        
        // 添加世界设定（地理/历史/文化）
        if (!worldSettings.isEmpty()) {
            sb.append("=== 世界地理与历史设定 ===\n");
            for (WorldSetting ws : worldSettings) {
                sb.append("【").append(ws.getCategory()).append("】").append(ws.getName()).append("\n");
                if (ws.getDescription() != null) {
                    String desc = ws.getDescription();
                    sb.append("  ").append(desc.length() > 150 ? desc.substring(0, 150) + "..." : desc).append("\n");
                }
            }
            sb.append("\n");
        }
        
        if (!characters.isEmpty()) {
            sb.append("=== 已有角色 ===\n");
            for (com.aiwriter.entity.Character c : characters) {
                sb.append("- ").append(c.getName()).append("\n");
            }
            sb.append("\n");
        }
        
        // 添加最近章节的场景参考
        if (!recentChapters.isEmpty()) {
            sb.append("=== 当前故事进展与场景 ===\n");
            for (Chapter chapter : recentChapters) {
                sb.append("第").append(chapter.getChapterNumber()).append("章：").append(chapter.getTitle()).append("\n");
                if (chapter.getContent() != null) {
                    String content = chapter.getContent();
                    String preview = content.length() > 200 ? content.substring(0, 200) + "..." : content;
                    sb.append("  场景描述：").append(preview).append("\n");
                }
            }
            sb.append("\n");
        }
        
        if (!existingScenes.isEmpty()) {
            sb.append("=== 已有场景 ===\n");
            for (Scene s : existingScenes) {
                sb.append("- ").append(s.getName())
                  .append("（").append(translateSceneType(s.getSceneType())).append("）\n");
                if (s.getLocation() != null) {
                    sb.append("  位置：").append(s.getLocation()).append("\n");
                }
            }
            sb.append("\n");
        } else {
            sb.append("=== 当前状态 ===\n");
            sb.append("这是一个全新的小说项目，还没有创建任何场景。请根据书名和类型，推荐适合的初始场景。\n\n");
        }
        
        sb.append("=== 推荐需求 ===\n");
        sb.append("请推荐 ").append(count).append(" 个新场景，确保：\n");
        sb.append("1. 场景符合已建立的世界地理和历史设定\n");
        sb.append("2. 场景与当前故事进度和氛围匹配\n");
        sb.append("3. 场景多样化，包含不同类型和氛围\n");
        sb.append("4. 场景描述详细生动，富有画面感\n");
        sb.append("5. 场景符合小说类型和风格\n");
        sb.append("6. 场景适合角色活动和情节展开\n");
        
        return sb.toString();
    }
    
    private String translateSceneType(String sceneType) {
        if (sceneType == null) return "未知";
        return switch (sceneType) {
            case "LOCATION" -> "地点";
            case "EVENT" -> "事件";
            case "TIME_PERIOD" -> "时间段";
            default -> sceneType;
        };
    }
    
    private List<SceneRecommendationResponse> generateRandomSceneRecommendations(int count) {
        List<SceneRecommendationResponse> recommendations = new ArrayList<>();
        
        String[] names = {"主角居所", "城市街道", "神秘森林"};
        String[] types = {"LOCATION", "LOCATION", "LOCATION"};
        String[] locations = {"市中心公寓", "繁华商业街", "郊外原始森林"};
        
        for (int i = 0; i < Math.min(count, 3); i++) {
            SceneRecommendationResponse rec = new SceneRecommendationResponse();
            rec.setName(names[i]);
            rec.setSceneType(types[i]);
            rec.setLocation(locations[i]);
            rec.setTimePeriod("白天");
            rec.setWeather("晴朗");
            rec.setDescription("场景描述待定，请根据故事需要进行详细设计");
            rec.setAtmosphere("氛围描述待定，请根据故事需要进行详细设计");
            rec.setProps(Arrays.asList("道具1", "道具2"));
            recommendations.add(rec);
        }
        
        return recommendations;
    }
}
