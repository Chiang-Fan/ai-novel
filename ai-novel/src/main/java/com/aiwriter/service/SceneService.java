package com.aiwriter.service;

import com.aiwriter.dto.SceneRecommendationRequest;
import com.aiwriter.dto.SceneRecommendationResponse;
import com.aiwriter.entity.Character;
import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.Novel;
import com.aiwriter.entity.Scene;
import com.aiwriter.entity.WorldSetting;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.repository.SceneRepository;
import com.aiwriter.repository.WorldSettingRepository;
import com.aiwriter.service.ai.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SceneService {
    
    private final SceneRepository sceneRepository;
    private final ChapterRepository chapterRepository;
    private final WorldSettingRepository worldSettingRepository;
    private final NovelService novelService;
    private final CharacterService characterService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    
    @Transactional(readOnly = true)
    public List<Scene> getScenesByNovel(Long novelId) {
        return sceneRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
    }
    
    @Transactional(readOnly = true)
    public Scene getSceneById(Long id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场景不存在"));
    }
    
    @Transactional
    public Scene createScene(Scene scene) {
        Scene saved = sceneRepository.save(scene);
        log.info("创建场景成功: {} (小说ID: {})", saved.getName(), saved.getNovelId());
        return saved;
    }
    
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
        log.info("更新场景成功: {}", updated.getName());
        return updated;
    }
    
    @Transactional
    public void deleteScene(Long id) {
        sceneRepository.deleteById(id);
        log.info("删除场景成功: {}", id);
    }
    
    /**
     * AI智能推荐场景
     */
    public List<SceneRecommendationResponse> recommendScenes(SceneRecommendationRequest request) {
        try {
            Novel novel = novelService.getNovel(request.getNovelId());
            List<Scene> existingScenes = getScenesByNovel(request.getNovelId());
            List<Character> characters = characterService.getCharactersByNovel(request.getNovelId());
            
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
                                                      List<Character> characters, List<WorldSetting> worldSettings,
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
            for (Character c : characters) {
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
