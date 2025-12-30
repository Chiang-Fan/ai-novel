package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.Novel;
import com.aiwriter.entity.PlotHook;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.repository.NovelRepository;
import com.aiwriter.repository.PlotHookRepository;
import com.aiwriter.service.ai.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 伏笔管理服务
 * 核心功能：
 * 1. AI 自动检测伏笔
 * 2. 伏笔生命周期管理（埋设-触发-解决）
 * 3. 超期伏笔预警
 * 4. 伏笔统计分析
 */
@Slf4j
@Service
public class PlotHookService {
    
    @Autowired
    private PlotHookRepository plotHookRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private AiService aiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * AI 自动检测章节中的伏笔
     */
    @Transactional
    public List<PlotHookDto> detectPlotHooks(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        Novel novel = novelRepository.findById(chapter.getNovelId())
            .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        log.info("开始检测章节 {} 的伏笔", chapterId);
        
        // 调用 AI 检测伏笔
        String aiResponse = callAiToDetectHooks(chapter, novel);
        
        // 解析 AI 响应
        List<PlotHook> detectedHooks = parseAiDetectionResponse(aiResponse, chapter);
        
        // 保存检测到的伏笔
        List<PlotHook> savedHooks = plotHookRepository.saveAll(detectedHooks);
        
        log.info("检测到 {} 个伏笔", savedHooks.size());
        
        return savedHooks.stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取小说的所有伏笔
     */
    public List<PlotHookDto> getHooksByNovelId(Long novelId) {
        List<PlotHook> hooks = plotHookRepository.findByNovelIdOrderByPlantedInChapterAsc(novelId);
        
        // 获取当前最新章节号
        Integer currentChapter = getCurrentChapterNumber(novelId);
        
        return hooks.stream()
            .map(hook -> mapToDto(hook, currentChapter))
            .collect(Collectors.toList());
    }
    
    /**
     * 按状态获取伏笔
     */
    public List<PlotHookDto> getHooksByStatus(Long novelId, PlotHook.Status status) {
        List<PlotHook> hooks = plotHookRepository.findByNovelIdAndStatusOrderByPlantedInChapterAsc(novelId, status);
        Integer currentChapter = getCurrentChapterNumber(novelId);
        
        return hooks.stream()
            .map(hook -> mapToDto(hook, currentChapter))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取待触发的伏笔
     */
    public List<PlotHookDto> getPendingHooks(Long novelId) {
        List<PlotHook> hooks = plotHookRepository.findPendingHooks(novelId);
        Integer currentChapter = getCurrentChapterNumber(novelId);
        
        return hooks.stream()
            .map(hook -> mapToDto(hook, currentChapter))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取超期伏笔
     */
    public List<PlotHookDto> getOverdueHooks(Long novelId) {
        Integer currentChapter = getCurrentChapterNumber(novelId);
        List<PlotHook> hooks = plotHookRepository.findOverdueHooks(novelId, currentChapter);
        
        return hooks.stream()
            .map(hook -> mapToDto(hook, currentChapter))
            .collect(Collectors.toList());
    }
    
    /**
     * 创建伏笔
     */
    @Transactional
    public PlotHookDto createHook(CreatePlotHookRequest request) {
        // 验证小说存在
        novelRepository.findById(request.getNovelId())
            .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        PlotHook hook = new PlotHook();
        hook.setNovelId(request.getNovelId());
        hook.setTitle(request.getTitle());
        hook.setDescription(request.getDescription());
        hook.setPlantedInChapter(request.getPlantedInChapter());
        hook.setExpectedChapter(request.getExpectedChapter());
        hook.setType(request.getType());
        hook.setPriority(request.getPriority() != null ? request.getPriority() : 5);
        hook.setStatus(PlotHook.Status.PENDING);
        hook.setIsAutoDetected(false);
        hook.setContentReference(request.getContentReference());
        hook.setNotes(request.getNotes());
        
        // 序列化相关角色
        if (request.getRelatedCharacters() != null && !request.getRelatedCharacters().isEmpty()) {
            try {
                hook.setRelatedCharacters(objectMapper.writeValueAsString(request.getRelatedCharacters()));
            } catch (Exception e) {
                log.warn("序列化相关角色失败", e);
            }
        }
        
        hook = plotHookRepository.save(hook);
        log.info("创建伏笔成功: {}", hook.getId());
        
        return mapToDto(hook);
    }
    
    /**
     * 更新伏笔
     */
    @Transactional
    public PlotHookDto updateHook(Long hookId, UpdatePlotHookRequest request) {
        PlotHook hook = plotHookRepository.findById(hookId)
            .orElseThrow(() -> new RuntimeException("伏笔不存在"));
        
        if (request.getTitle() != null) {
            hook.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            hook.setDescription(request.getDescription());
        }
        if (request.getExpectedChapter() != null) {
            hook.setExpectedChapter(request.getExpectedChapter());
        }
        if (request.getType() != null) {
            hook.setType(request.getType());
        }
        if (request.getPriority() != null) {
            hook.setPriority(request.getPriority());
        }
        if (request.getNotes() != null) {
            hook.setNotes(request.getNotes());
        }
        if (request.getRelatedCharacters() != null) {
            try {
                hook.setRelatedCharacters(objectMapper.writeValueAsString(request.getRelatedCharacters()));
            } catch (Exception e) {
                log.warn("序列化相关角色失败", e);
            }
        }
        
        hook = plotHookRepository.save(hook);
        log.info("更新伏笔成功: {}", hookId);
        
        return mapToDto(hook);
    }
    
    /**
     * 标记伏笔为已触发
     */
    @Transactional
    public PlotHookDto triggerHook(Long hookId, Integer chapterNumber) {
        PlotHook hook = plotHookRepository.findById(hookId)
            .orElseThrow(() -> new RuntimeException("伏笔不存在"));
        
        hook.setStatus(PlotHook.Status.TRIGGERED);
        hook.setTriggeredInChapter(chapterNumber);
        hook.setTriggeredAt(LocalDateTime.now());
        
        hook = plotHookRepository.save(hook);
        log.info("伏笔 {} 已触发于第 {} 章", hookId, chapterNumber);
        
        return mapToDto(hook);
    }
    
    /**
     * 标记伏笔为已解决
     */
    @Transactional
    public PlotHookDto resolveHook(Long hookId, Integer chapterNumber, String resolutionNote) {
        PlotHook hook = plotHookRepository.findById(hookId)
            .orElseThrow(() -> new RuntimeException("伏笔不存在"));
        
        hook.setStatus(PlotHook.Status.RESOLVED);
        hook.setResolvedInChapter(chapterNumber);
        hook.setResolvedAt(LocalDateTime.now());
        hook.setResolutionNote(resolutionNote);
        
        hook = plotHookRepository.save(hook);
        log.info("伏笔 {} 已解决于第 {} 章", hookId, chapterNumber);
        
        return mapToDto(hook);
    }
    
    /**
     * 删除伏笔
     */
    @Transactional
    public void deleteHook(Long hookId) {
        plotHookRepository.deleteById(hookId);
        log.info("删除伏笔: {}", hookId);
    }
    
    /**
     * 获取伏笔统计信息
     */
    public PlotHookStatistics getStatistics(Long novelId) {
        List<PlotHook> allHooks = plotHookRepository.findByNovelIdOrderByPlantedInChapterAsc(novelId);
        Integer currentChapter = getCurrentChapterNumber(novelId);
        
        PlotHookStatistics stats = new PlotHookStatistics();
        stats.setTotalCount(allHooks.size());
        
        // 按状态统计
        long pendingCount = allHooks.stream().filter(h -> h.getStatus() == PlotHook.Status.PENDING).count();
        long hintedCount = allHooks.stream().filter(h -> h.getStatus() == PlotHook.Status.HINTED).count();
        long triggeredCount = allHooks.stream().filter(h -> h.getStatus() == PlotHook.Status.TRIGGERED).count();
        long resolvedCount = allHooks.stream().filter(h -> h.getStatus() == PlotHook.Status.RESOLVED).count();
        
        stats.setPendingCount((int) pendingCount);
        stats.setHintedCount((int) hintedCount);
        stats.setTriggeredCount((int) triggeredCount);
        stats.setResolvedCount((int) resolvedCount);
        
        // 超期统计
        long overdueCount = allHooks.stream()
            .filter(h -> h.isOverdue(currentChapter))
            .count();
        stats.setOverdueCount((int) overdueCount);
        
        // 高优先级统计
        long highPriorityCount = allHooks.stream()
            .filter(h -> h.getPriority() >= 8 && 
                   (h.getStatus() == PlotHook.Status.PENDING || h.getStatus() == PlotHook.Status.HINTED))
            .count();
        stats.setHighPriorityCount((int) highPriorityCount);
        
        // 类型分布
        Map<String, Integer> typeDistribution = allHooks.stream()
            .collect(Collectors.groupingBy(
                h -> h.getType().name(),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        stats.setTypeDistribution(typeDistribution);
        
        return stats;
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 调用 AI 检测伏笔
     */
    private String callAiToDetectHooks(Chapter chapter, Novel novel) {
        String systemPrompt = """
            你是一位专业的小说分析专家，擅长识别故事中的伏笔和暗示。
            
            伏笔类型：
            1. 明示伏笔（EXPLICIT）：明确提示的未来事件，如"三年之约"、"十年后再见"
            2. 暗示伏笔（IMPLICIT）：隐晦的线索，如"神秘令牌"、"奇怪的疤痕"
            3. 契诃夫的枪（CHEKHOV_GUN）：出现的物品或信息必须在后续发挥作用
            
            请分析给定的章节内容，识别其中可能的伏笔。
            """;
        
        String userPrompt = String.format("""
            小说信息：
            - 标题：%s
            - 类型：%s
            
            章节信息：
            - 章节号：%d
            - 标题：%s
            - 内容：
            %s
            
            请识别这一章节中埋下的伏笔，返回 JSON 格式：
            {
                "hooks": [
                    {
                        "title": "伏笔标题",
                        "description": "伏笔描述",
                        "type": "EXPLICIT/IMPLICIT/CHEKHOV_GUN",
                        "priority": 1-10,
                        "expectedChapter": 预计揭示章节号（可选）,
                        "contentReference": "原文引用片段",
                        "relatedCharacters": ["角色1", "角色2"]
                    }
                ]
            }
            
            如果没有明显伏笔，返回空数组。
            """,
            novel.getTitle(),
            novel.getGenre(),
            chapter.getChapterNumber(),
            chapter.getTitle(),
            chapter.getContent()
        );
        
        return aiService.chatJson(systemPrompt, userPrompt);
    }
    
    /**
     * 解析 AI 检测响应
     */
    private List<PlotHook> parseAiDetectionResponse(String aiResponse, Chapter chapter) {
        List<PlotHook> hooks = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            JsonNode hooksNode = root.get("hooks");
            
            if (hooksNode != null && hooksNode.isArray()) {
                for (JsonNode hookNode : hooksNode) {
                    PlotHook hook = new PlotHook();
                    hook.setNovelId(chapter.getNovelId());
                    hook.setTitle(hookNode.get("title").asText());
                    hook.setDescription(hookNode.get("description").asText());
                    hook.setPlantedInChapter(chapter.getChapterNumber());
                    hook.setStatus(PlotHook.Status.PENDING);
                    hook.setIsAutoDetected(true);
                    
                    // 类型
                    String typeStr = hookNode.get("type").asText();
                    hook.setType(PlotHook.ForeshadowingType.valueOf(typeStr));
                    
                    // 优先级
                    if (hookNode.has("priority")) {
                        hook.setPriority(hookNode.get("priority").asInt());
                    } else {
                        hook.setPriority(5);
                    }
                    
                    // 预期章节
                    if (hookNode.has("expectedChapter")) {
                        hook.setExpectedChapter(hookNode.get("expectedChapter").asInt());
                    }
                    
                    // 原文引用
                    if (hookNode.has("contentReference")) {
                        hook.setContentReference(hookNode.get("contentReference").asText());
                    }
                    
                    // 相关角色
                    if (hookNode.has("relatedCharacters")) {
                        List<String> characters = objectMapper.convertValue(
                            hookNode.get("relatedCharacters"),
                            new TypeReference<List<String>>() {}
                        );
                        hook.setRelatedCharacters(objectMapper.writeValueAsString(characters));
                    }
                    
                    hooks.add(hook);
                }
            }
        } catch (Exception e) {
            log.error("解析 AI 伏笔检测响应失败", e);
        }
        
        return hooks;
    }
    
    /**
     * 获取当前最新章节号
     */
    private Integer getCurrentChapterNumber(Long novelId) {
        return chapterRepository.findMaxChapterNumber(novelId)
            .orElse(1);
    }
    
    /**
     * 映射为 DTO
     */
    private PlotHookDto mapToDto(PlotHook hook) {
        return mapToDto(hook, null);
    }
    
    /**
     * 映射为 DTO（带当前章节号）
     */
    private PlotHookDto mapToDto(PlotHook hook, Integer currentChapter) {
        PlotHookDto dto = PlotHookDto.builder()
            .id(hook.getId())
            .novelId(hook.getNovelId())
            .title(hook.getTitle())
            .description(hook.getDescription())
            .plantedInChapter(hook.getPlantedInChapter())
            .expectedChapter(hook.getExpectedChapter())
            .triggeredInChapter(hook.getTriggeredInChapter())
            .resolvedInChapter(hook.getResolvedInChapter())
            .status(hook.getStatus())
            .type(hook.getType())
            .priority(hook.getPriority())
            .resolutionNote(hook.getResolutionNote())
            .isAutoDetected(hook.getIsAutoDetected())
            .contentReference(hook.getContentReference())
            .notes(hook.getNotes())
            .triggeredAt(hook.getTriggeredAt())
            .resolvedAt(hook.getResolvedAt())
            .createdAt(hook.getCreatedAt())
            .updatedAt(hook.getUpdatedAt())
            .duration(hook.getDuration())
            .build();
        
        // 解析相关角色
        if (hook.getRelatedCharacters() != null) {
            try {
                List<String> characters = objectMapper.readValue(
                    hook.getRelatedCharacters(),
                    new TypeReference<List<String>>() {}
                );
                dto.setRelatedCharacters(characters);
            } catch (Exception e) {
                log.warn("解析相关角色失败", e);
            }
        }
        
        // 计算是否超期
        if (currentChapter != null) {
            dto.setIsOverdue(hook.isOverdue(currentChapter));
        }
        
        return dto;
    }
}
