package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SceneManagementService {
    
    private final SceneRepository sceneRepository;
    private final SceneUsageRepository sceneUsageRepository;
    private final SceneChangeRepository sceneChangeRepository;
    private final ChapterRepository chapterRepository;
    
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
        return toSceneResponse(scene);
    }
    
    /**
     * 更新场景
     */
    @Transactional
    public SceneResponse updateScene(Long sceneId, UpdateSceneRequest request) {
        Scene scene = sceneRepository.findById(sceneId)
            .orElseThrow(() -> new RuntimeException("场景不存在"));
        
        if (request.getSceneName() != null) {
            scene.setName(request.getSceneName());
        }
        if (request.getSceneType() != null) {
            scene.setSceneType(request.getSceneType());
        }
        if (request.getLocationDesc() != null) {
            scene.setLocation(request.getLocationDesc());
        }
        if (request.getDescription() != null) {
            scene.setDescription(request.getDescription());
        }
        if (request.getAtmosphere() != null) {
            scene.setAtmosphere(request.getAtmosphere());
        }
        if (request.getImportanceScore() != null) {
            scene.setImportanceScore(request.getImportanceScore());
        }
        if (request.getIsRecurring() != null) {
            scene.setIsRecurring(request.getIsRecurring());
        }
        if (request.getTags() != null) {
            scene.setTags(String.join(",", request.getTags()));
        }
        
        scene = sceneRepository.save(scene);
        return toSceneResponse(scene);
    }
    
    /**
     * 删除场景
     */
    @Transactional
    public void deleteScene(Long sceneId) {
        sceneRepository.deleteById(sceneId);
    }
    
    /**
     * 获取场景详情
     */
    public SceneResponse getScene(Long sceneId) {
        Scene scene = sceneRepository.findById(sceneId)
            .orElseThrow(() -> new RuntimeException("场景不存在"));
        return toSceneResponse(scene);
    }
    
    /**
     * 获取小说的所有场景
     */
    public List<SceneResponse> getScenesByNovel(Long novelId, String type, String keyword) {
        List<Scene> scenes;
        
        if (type != null && !type.isEmpty()) {
            scenes = sceneRepository.findByNovelIdAndSceneTypeOrderByNameAsc(novelId, type);
        } else if (keyword != null && !keyword.isEmpty()) {
            scenes = sceneRepository.findByNovelIdAndNameContainingOrderByNameAsc(novelId, keyword);
        } else {
            scenes = sceneRepository.findByNovelIdOrderByImportanceScoreDesc(novelId);
        }
        
        return scenes.stream()
            .map(this::toSceneResponse)
            .collect(Collectors.toList());
    }
    
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
    
    /**
     * 获取场景统计
     */
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
        
        // 按天气统计（简单实现）
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
    
    // ==================== 私有辅助方法 ====================
    
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
}
