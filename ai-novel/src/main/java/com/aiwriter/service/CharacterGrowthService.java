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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterGrowthService {
    
    private final CharacterGrowthRecordRepository growthRecordRepository;
    private final CharacterMilestoneRepository milestoneRepository;
    private final CharacterRepository characterRepository;
    private final ChapterRepository chapterRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建成长记录
     */
    @Transactional
    public GrowthRecordResponse createGrowthRecord(CreateGrowthRecordRequest request) {
        CharacterGrowthRecord record = new CharacterGrowthRecord();
        record.setCharacterId(request.getCharacterId());
        record.setChapterId(request.getChapterId());
        record.setRecordTime(request.getRecordTime());
        record.setNotes(request.getNotes());
        
        try {
            record.setAttributes(objectMapper.writeValueAsString(request.getAttributes()));
        } catch (Exception e) {
            log.error("Failed to serialize attributes", e);
            throw new RuntimeException("属性序列化失败");
        }
        
        record = growthRecordRepository.save(record);
        return toGrowthRecordResponse(record);
    }
    
    /**
     * 获取角色成长轨迹
     */
    public List<GrowthRecordResponse> getGrowthTrajectory(Long characterId) {
        List<CharacterGrowthRecord> records = growthRecordRepository
            .findByCharacterIdOrderByRecordTimeAsc(characterId);
        
        return records.stream()
            .map(this::toGrowthRecordResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 添加里程碑
     */
    @Transactional
    public MilestoneResponse createMilestone(CreateMilestoneRequest request) {
        CharacterMilestone milestone = new CharacterMilestone();
        milestone.setCharacterId(request.getCharacterId());
        milestone.setChapterId(request.getChapterId());
        milestone.setMilestoneType(request.getMilestoneType());
        milestone.setEventName(request.getEventName());
        milestone.setDescription(request.getDescription());
        milestone.setImpactLevel(request.getImpactLevel());
        
        try {
            milestone.setAffectedAttributes(objectMapper.writeValueAsString(request.getAffectedAttributes()));
        } catch (Exception e) {
            log.error("Failed to serialize affected attributes", e);
            throw new RuntimeException("属性序列化失败");
        }
        
        milestone = milestoneRepository.save(milestone);
        return toMilestoneResponse(milestone);
    }
    
    /**
     * 获取角色里程碑列表
     */
    public List<MilestoneResponse> getMilestones(Long characterId) {
        List<CharacterMilestone> milestones = milestoneRepository
            .findByCharacterIdOrderByCreatedAtDesc(characterId);
        
        return milestones.stream()
            .map(this::toMilestoneResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 生成成长分析
     */
    public GrowthAnalysisResponse generateAnalysis(GrowthAnalysisRequest request) {
        com.aiwriter.entity.Character character = characterRepository.findById(request.getCharacterId())
            .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        // 获取成长记录
        List<CharacterGrowthRecord> records = growthRecordRepository
            .findByCharacterIdOrderByRecordTimeAsc(request.getCharacterId());
        
        if (records.isEmpty()) {
            throw new RuntimeException("暂无成长记录");
        }
        
        // 获取里程碑
        List<CharacterMilestone> milestones = milestoneRepository
            .findByCharacterIdOrderByCreatedAtDesc(request.getCharacterId());
        
        // 计算趋势
        List<GrowthAnalysisResponse.AttributeTrend> trends = calculateTrends(records);
        
        // 调用 AI 生成分析
        String aiAnalysis = generateAIAnalysis(character, records, milestones, trends);
        
        // 解析 AI 返回
        return parseAIAnalysis(character, aiAnalysis, trends);
    }
    
    /**
     * 对比不同时期
     */
    public GrowthComparisonResponse compareGrowth(GrowthComparisonRequest request) {
        com.aiwriter.entity.Character character = characterRepository.findById(request.getCharacterId())
            .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        // 查找两个时间点附近的记录
        CharacterGrowthRecord record1 = findClosestRecord(request.getCharacterId(), request.getTimePoint1());
        CharacterGrowthRecord record2 = findClosestRecord(request.getCharacterId(), request.getTimePoint2());
        
        if (record1 == null || record2 == null) {
            throw new RuntimeException("未找到对应时间点的记录");
        }
        
        Map<String, Object> attrs1 = parseAttributes(record1.getAttributes());
        Map<String, Object> attrs2 = parseAttributes(record2.getAttributes());
        
        Map<String, GrowthComparisonResponse.AttributeComparison> comparison = compareAttributes(attrs1, attrs2);
        
        return GrowthComparisonResponse.builder()
            .characterId(character.getId())
            .characterName(character.getName())
            .comparison(comparison)
            .build();
    }
    
    /**
     * 删除成长记录
     */
    @Transactional
    public void deleteGrowthRecord(Long recordId) {
        growthRecordRepository.deleteById(recordId);
    }
    
    /**
     * 删除里程碑
     */
    @Transactional
    public void deleteMilestone(Long milestoneId) {
        milestoneRepository.deleteById(milestoneId);
    }
    
    // ==================== 私有辅助方法 ====================
    
    private GrowthRecordResponse toGrowthRecordResponse(CharacterGrowthRecord record) {
        com.aiwriter.entity.Character character = characterRepository.findById(record.getCharacterId())
            .orElse(null);
        Chapter chapter = record.getChapterId() != null ? 
            chapterRepository.findById(record.getChapterId()).orElse(null) : null;
        
        return GrowthRecordResponse.builder()
            .id(record.getId())
            .characterId(record.getCharacterId())
            .characterName(character != null ? character.getName() : "")
            .chapterId(record.getChapterId())
            .chapterTitle(chapter != null ? chapter.getTitle() : "")
            .recordTime(record.getRecordTime())
            .attributes(parseAttributes(record.getAttributes()))
            .notes(record.getNotes())
            .createdAt(record.getCreatedAt())
            .build();
    }
    
    private MilestoneResponse toMilestoneResponse(CharacterMilestone milestone) {
        com.aiwriter.entity.Character character = characterRepository.findById(milestone.getCharacterId())
            .orElse(null);
        Chapter chapter = milestone.getChapterId() != null ? 
            chapterRepository.findById(milestone.getChapterId()).orElse(null) : null;
        
        Map<String, Integer> affectedAttrs = new HashMap<>();
        try {
            affectedAttrs = objectMapper.readValue(
                milestone.getAffectedAttributes(), 
                new TypeReference<Map<String, Integer>>() {}
            );
        } catch (Exception ignored) {}
        
        return MilestoneResponse.builder()
            .id(milestone.getId())
            .characterId(milestone.getCharacterId())
            .characterName(character != null ? character.getName() : "")
            .chapterId(milestone.getChapterId())
            .chapterTitle(chapter != null ? chapter.getTitle() : "")
            .milestoneType(milestone.getMilestoneType())
            .eventName(milestone.getEventName())
            .description(milestone.getDescription())
            .impactLevel(milestone.getImpactLevel())
            .affectedAttributes(affectedAttrs)
            .createdAt(milestone.getCreatedAt())
            .build();
    }
    
    private Map<String, Object> parseAttributes(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    private List<GrowthAnalysisResponse.AttributeTrend> calculateTrends(List<CharacterGrowthRecord> records) {
        if (records.size() < 2) {
            return new ArrayList<>();
        }
        
        Map<String, Object> firstAttrs = parseAttributes(records.get(0).getAttributes());
        Map<String, Object> lastAttrs = parseAttributes(records.get(records.size() - 1).getAttributes());
        
        List<GrowthAnalysisResponse.AttributeTrend> trends = new ArrayList<>();
        
        // 计算每个属性的变化趋势
        for (String key : lastAttrs.keySet()) {
            if (firstAttrs.containsKey(key)) {
                Object firstVal = firstAttrs.get(key);
                Object lastVal = lastAttrs.get(key);
                
                if (firstVal instanceof Number && lastVal instanceof Number) {
                    double change = ((Number) lastVal).doubleValue() - ((Number) firstVal).doubleValue();
                    String trend = determineTrend(change);
                    
                    trends.add(GrowthAnalysisResponse.AttributeTrend.builder()
                        .attribute(key)
                        .attributeName(getAttributeName(key))
                        .trend(trend)
                        .change(change)
                        .analysis(generateTrendAnalysis(key, trend, change))
                        .build());
                }
            }
        }
        
        return trends;
    }
    
    private String determineTrend(double change) {
        if (Math.abs(change) < 1.0) {
            return "STABLE";
        } else if (change > 0) {
            return "RISING";
        } else {
            return "FALLING";
        }
    }
    
    private String getAttributeName(String key) {
        Map<String, String> nameMap = Map.of(
            "brave", "勇气",
            "cautious", "谨慎",
            "empathy", "同理心",
            "confidence", "自信",
            "swordPlay", "剑术",
            "magic", "魔法"
        );
        return nameMap.getOrDefault(key, key);
    }
    
    private String generateTrendAnalysis(String attr, String trend, double change) {
        String attrName = getAttributeName(attr);
        if ("RISING".equals(trend)) {
            return String.format("%s提升了%.1f，显示出积极的成长", attrName, change);
        } else if ("FALLING".equals(trend)) {
            return String.format("%s下降了%.1f，需要关注这个变化", attrName, Math.abs(change));
        } else {
            return String.format("%s保持稳定，变化不大", attrName);
        }
    }
    
    private String generateAIAnalysis(com.aiwriter.entity.Character character, 
                                     List<CharacterGrowthRecord> records,
                                     List<CharacterMilestone> milestones,
                                     List<GrowthAnalysisResponse.AttributeTrend> trends) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("【角色信息】\n");
        prompt.append("角色名：").append(character.getName()).append("\n");
        prompt.append("角色类型：").append(character.getRoleType()).append("\n\n");
        
        prompt.append("【成长趋势】\n");
        for (GrowthAnalysisResponse.AttributeTrend trend : trends) {
            prompt.append(String.format("- %s: %s (变化: %.1f)\n", 
                trend.getAttributeName(), trend.getTrend(), trend.getChange()));
        }
        
        prompt.append("\n【里程碑事件】\n");
        for (CharacterMilestone milestone : milestones) {
            prompt.append(String.format("- %s (%s, 影响: %d)\n", 
                milestone.getEventName(), milestone.getMilestoneType(), milestone.getImpactLevel()));
        }
        
        String systemPrompt = "你是一个专业的小说创作顾问，擅长分析角色成长。";
        String userPrompt = prompt.toString() + "\n请提供：\n1. 成长总结（200字以内）\n2. 3-5条改进建议\n3. 警告（如有人物崩坏风险等）\n";
        
        return aiService.chat(systemPrompt, userPrompt);
    }
    
    private GrowthAnalysisResponse parseAIAnalysis(com.aiwriter.entity.Character character,
                                                   String aiResponse,
                                                   List<GrowthAnalysisResponse.AttributeTrend> trends) {
        // 简单解析 AI 返回（实际可能需要更复杂的解析）
        List<String> suggestions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        String[] lines = aiResponse.split("\n");
        boolean inSuggestions = false;
        boolean inWarnings = false;
        
        for (String line : lines) {
            line = line.trim();
            if (line.contains("建议") || line.contains("改进")) {
                inSuggestions = true;
                inWarnings = false;
            } else if (line.contains("警告") || line.contains("注意")) {
                inWarnings = true;
                inSuggestions = false;
            } else if (line.startsWith("-") || line.matches("^\\d+\\..*")) {
                String content = line.replaceFirst("^[-\\d+.\\s]+", "").trim();
                if (inSuggestions && !content.isEmpty()) {
                    suggestions.add(content);
                } else if (inWarnings && !content.isEmpty()) {
                    warnings.add(content);
                }
            }
        }
        
        // 提取总结（第一段）
        String summary = Arrays.stream(lines)
            .filter(line -> line.length() > 20 && !line.startsWith("-") && !line.matches("^\\d+\\..*"))
            .findFirst()
            .orElse("角色在故事中经历了显著的成长和变化");
        
        return GrowthAnalysisResponse.builder()
            .characterId(character.getId())
            .characterName(character.getName())
            .timeRange("全部时间")
            .summary(summary)
            .trends(trends)
            .suggestions(suggestions.isEmpty() ? List.of("继续保持角色成长的合理性") : suggestions)
            .warnings(warnings)
            .build();
    }
    
    private CharacterGrowthRecord findClosestRecord(Long characterId, LocalDateTime targetTime) {
        List<CharacterGrowthRecord> records = growthRecordRepository
            .findByCharacterIdOrderByRecordTimeAsc(characterId);
        
        if (records.isEmpty()) {
            return null;
        }
        
        return records.stream()
            .min(Comparator.comparingLong(r -> 
                Math.abs(r.getRecordTime().toEpochSecond(java.time.ZoneOffset.UTC) - 
                        targetTime.toEpochSecond(java.time.ZoneOffset.UTC))
            ))
            .orElse(null);
    }
    
    private Map<String, GrowthComparisonResponse.AttributeComparison> compareAttributes(
            Map<String, Object> attrs1, Map<String, Object> attrs2) {
        
        Map<String, GrowthComparisonResponse.AttributeComparison> result = new HashMap<>();
        
        for (String key : attrs2.keySet()) {
            if (attrs1.containsKey(key)) {
                Object val1 = attrs1.get(key);
                Object val2 = attrs2.get(key);
                
                if (val1 instanceof Number && val2 instanceof Number) {
                    double d1 = ((Number) val1).doubleValue();
                    double d2 = ((Number) val2).doubleValue();
                    double change = d2 - d1;
                    double changeRate = d1 != 0 ? (change / d1) * 100 : 0;
                    
                    result.put(key, GrowthComparisonResponse.AttributeComparison.builder()
                        .from(d1)
                        .to(d2)
                        .change(change)
                        .changeRate(changeRate)
                        .build());
                }
            }
        }
        
        return result;
    }
}
