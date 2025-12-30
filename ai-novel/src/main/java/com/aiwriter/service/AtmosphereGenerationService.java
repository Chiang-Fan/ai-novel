package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.AtmosphereTemplate;
import com.aiwriter.entity.Scene;
import com.aiwriter.entity.SceneAtmosphere;
import com.aiwriter.repository.AtmosphereTemplateRepository;
import com.aiwriter.repository.SceneAtmosphereRepository;
import com.aiwriter.repository.SceneRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 氛围生成服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AtmosphereGenerationService {
    
    private final SceneRepository sceneRepository;
    private final AtmosphereTemplateRepository templateRepository;
    private final SceneAtmosphereRepository atmosphereRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 生成场景氛围描写
     */
    @Transactional
    public SceneAtmosphereResponse generateAtmosphere(AtmosphereGenerateRequest request) {
        log.info("生成场景氛围: sceneId={}, type={}", request.getSceneId(), request.getAtmosphereType());
        
        // 获取场景信息
        Scene scene = sceneRepository.findById(request.getSceneId())
                .orElseThrow(() -> new RuntimeException("场景不存在"));
        
        // 获取模板（如果指定）
        AtmosphereTemplate template = null;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId()).orElse(null);
            if (template != null) {
                template.setUsageCount(template.getUsageCount() + 1);
                templateRepository.save(template);
            }
        }
        
        // 构建提示词
        String prompt = buildAtmospherePrompt(scene, request, template);
        
        // 调用AI生成（使用模拟数据）
        String generatedText = generateTextWithAI(prompt);
        
        // 获取当前版本号
        List<SceneAtmosphere> existing = atmosphereRepository.findBySceneIdAndAtmosphereTypeOrderByVersionDesc(
                request.getSceneId(), request.getAtmosphereType());
        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersion() + 1;
        
        // 保存生成结果
        SceneAtmosphere atmosphere = SceneAtmosphere.builder()
                .sceneId(request.getSceneId())
                .templateId(request.getTemplateId())
                .atmosphereType(request.getAtmosphereType())
                .generatedText(generatedText)
                .promptUsed(prompt)
                .aiModel("deepseek-chat")
                .version(nextVersion)
                .isApplied(false)
                .build();
        atmosphere = atmosphereRepository.save(atmosphere);
        
        return toResponse(atmosphere, template != null ? template.getName() : null);
    }
    
    /**
     * 应用氛围到场景
     */
    @Transactional
    public void applyAtmosphere(Long atmosphereId) {
        log.info("应用氛围: {}", atmosphereId);
        
        SceneAtmosphere atmosphere = atmosphereRepository.findById(atmosphereId)
                .orElseThrow(() -> new RuntimeException("氛围不存在"));
        
        // 取消同场景其他氛围的应用状态
        atmosphereRepository.findBySceneIdAndIsAppliedTrue(atmosphere.getSceneId())
                .ifPresent(existing -> {
                    existing.setIsApplied(false);
                    existing.setAppliedAt(null);
                    atmosphereRepository.save(existing);
                });
        
        // 应用当前氛围
        atmosphere.setIsApplied(true);
        atmosphere.setAppliedAt(LocalDateTime.now());
        atmosphereRepository.save(atmosphere);
    }
    
    /**
     * 评分氛围
     */
    @Transactional
    public void rateAtmosphere(Long atmosphereId, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
        
        SceneAtmosphere atmosphere = atmosphereRepository.findById(atmosphereId)
                .orElseThrow(() -> new RuntimeException("氛围不存在"));
        atmosphere.setRating(rating);
        atmosphereRepository.save(atmosphere);
    }
    
    /**
     * 获取场景的所有氛围
     */
    public List<SceneAtmosphereResponse> getSceneAtmospheres(Long sceneId) {
        List<SceneAtmosphere> atmospheres = atmosphereRepository.findBySceneIdOrderByCreatedAtDesc(sceneId);
        return atmospheres.stream()
                .map(a -> toResponse(a, getTemplateName(a.getTemplateId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取氛围匹配建议
     */
    public AtmosphereMatchResponse getAtmosphereMatch(Long sceneId, String plotContext) {
        log.info("获取氛围匹配建议: sceneId={}", sceneId);
        
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new RuntimeException("场景不存在"));
        
        // 构建分析提示词
        String prompt = buildMatchPrompt(scene, plotContext);
        
        // 调用AI分析（使用模拟数据）
        String aiResponse = generateTextWithAI(prompt);
        
        // 解析AI响应并构建建议
        return parseMatchResponse(aiResponse, scene);
    }
    
    /**
     * 创建氛围模板
     */
    @Transactional
    public AtmosphereTemplateResponse createTemplate(AtmosphereTemplateRequest request) {
        log.info("创建氛围模板: {}", request.getName());
        
        try {
            AtmosphereTemplate template = AtmosphereTemplate.builder()
                    .name(request.getName())
                    .category(request.getCategory())
                    .atmosphereType(request.getAtmosphereType())
                    .description(request.getDescription())
                    .keywords(objectMapper.writeValueAsString(request.getKeywords()))
                    .sensoryDetails(objectMapper.writeValueAsString(request.getSensoryDetails()))
                    .exampleText(request.getExampleText())
                    .usageCount(0)
                    .isSystem(false)
                    .build();
            
            template = templateRepository.save(template);
            return toTemplateResponse(template);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }
    
    /**
     * 获取所有模板
     */
    public List<AtmosphereTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 按分类获取模板
     */
    public List<AtmosphereTemplateResponse> getTemplatesByCategory(String category) {
        return templateRepository.findByCategoryOrderByUsageCountDesc(category).stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 按氛围类型获取模板
     */
    public List<AtmosphereTemplateResponse> getTemplatesByType(String atmosphereType) {
        return templateRepository.findByAtmosphereTypeOrderByUsageCountDesc(atmosphereType).stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 构建氛围生成提示词
     */
    private String buildAtmospherePrompt(Scene scene, AtmosphereGenerateRequest request, AtmosphereTemplate template) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为以下场景生成").append(request.getAtmosphereType()).append("氛围的描写：\n\n");
        
        // 场景基本信息
        prompt.append("【场景信息】\n");
        prompt.append("场景名称：").append(scene.getName()).append("\n");
        prompt.append("场景类型：").append(scene.getSceneType()).append("\n");
        if (scene.getDescription() != null) {
            prompt.append("场景描述：").append(scene.getDescription()).append("\n");
        }
        
        // 情节上下文
        if (request.getPlotContext() != null) {
            prompt.append("\n【情节上下文】\n").append(request.getPlotContext()).append("\n");
        }
        
        // 情感基调
        if (request.getEmotionalTone() != null) {
            prompt.append("\n【情感基调】\n").append(request.getEmotionalTone()).append("\n");
        }
        
        // 模板参考
        if (template != null) {
            prompt.append("\n【参考模板】\n");
            prompt.append("模板名称：").append(template.getName()).append("\n");
            if (template.getKeywords() != null) {
                prompt.append("关键词：").append(template.getKeywords()).append("\n");
            }
            if (template.getSensoryDetails() != null) {
                prompt.append("感官细节参考：").append(template.getSensoryDetails()).append("\n");
            }
            if (template.getExampleText() != null) {
                prompt.append("示例：").append(template.getExampleText()).append("\n");
            }
        }
        
        // 生成要求
        prompt.append("\n【生成要求】\n");
        prompt.append("1. 营造").append(request.getAtmosphereType()).append("的氛围\n");
        prompt.append("2. 运用五感描写（视觉、听觉、嗅觉、触觉、味觉）\n");
        prompt.append("3. 语言生动优美，富有画面感\n");
        prompt.append("4. 字数控制在").append(request.getLength() != null ? request.getLength() : 200).append("字左右\n");
        prompt.append("5. 只输出氛围描写内容，不要额外说明\n");
        
        return prompt.toString();
    }
    
    /**
     * 构建匹配建议提示词
     */
    private String buildMatchPrompt(Scene scene, String plotContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下场景和情节，推荐最合适的氛围类型：\n\n");
        
        prompt.append("【场景信息】\n");
        prompt.append("场景名称：").append(scene.getName()).append("\n");
        prompt.append("场景类型：").append(scene.getSceneType()).append("\n");
        if (scene.getDescription() != null) {
            prompt.append("场景描述：").append(scene.getDescription()).append("\n");
        }
        
        if (plotContext != null) {
            prompt.append("\n【情节内容】\n").append(plotContext).append("\n");
        }
        
        prompt.append("\n【可选氛围类型】\n");
        prompt.append("PEACEFUL(平和)、TENSE(紧张)、ROMANTIC(浪漫)、MYSTERIOUS(神秘)、\n");
        prompt.append("EXCITING(激动)、MELANCHOLIC(忧郁)、HORROR(恐怖)、JOYFUL(欢乐)\n");
        
        prompt.append("\n请按以下格式回答：\n");
        prompt.append("推荐氛围：[氛围类型]\n");
        prompt.append("推荐理由：[简要说明]\n");
        prompt.append("一致性：[是否与情节一致，如有问题请说明]\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析匹配响应
     */
    private AtmosphereMatchResponse parseMatchResponse(String aiResponse, Scene scene) {
        // 简单解析AI响应
        String recommendedType = extractValue(aiResponse, "推荐氛围");
        String reason = extractValue(aiResponse, "推荐理由");
        String consistency = extractValue(aiResponse, "一致性");
        
        // 获取推荐的模板
        List<AtmosphereMatchResponse.TemplateRecommendation> templates = new ArrayList<>();
        if (recommendedType != null) {
            List<AtmosphereTemplate> matchingTemplates = 
                    templateRepository.findByAtmosphereTypeOrderByUsageCountDesc(recommendedType);
            for (AtmosphereTemplate template : matchingTemplates.stream().limit(3).collect(Collectors.toList())) {
                templates.add(AtmosphereMatchResponse.TemplateRecommendation.builder()
                        .templateId(template.getId())
                        .templateName(template.getName())
                        .atmosphereType(template.getAtmosphereType())
                        .matchScore(80 + template.getUsageCount() % 20)
                        .matchReason("该模板使用" + template.getUsageCount() + "次，效果良好")
                        .build());
            }
        }
        
        // 构建一致性检查
        boolean isConsistent = consistency != null && 
                (consistency.contains("一致") || consistency.contains("合适"));
        
        return AtmosphereMatchResponse.builder()
                .recommendedType(recommendedType)
                .reason(reason)
                .templates(templates)
                .consistencyCheck(AtmosphereMatchResponse.ConsistencyCheck.builder()
                        .isConsistent(isConsistent)
                        .issue(isConsistent ? null : "氛围与情节可能不够协调")
                        .suggestion(isConsistent ? "氛围选择恰当" : consistency)
                        .build())
                .build();
    }
    
    /**
     * 使用AI生成文本
     */
    private String generateTextWithAI(String prompt) {
        // TODO: 集成实际的AI服务
        // 现在返回模拟数据用于演示
        log.info("AI生成请求: {}", prompt.substring(0, Math.min(100, prompt.length())));
        
        if (prompt.contains("推荐最合适的氛围类型")) {
            return "推荐氛围：PEACEFUL\n推荐理由：场景描述平和宁静，适合营造平和氛围\n一致性：与情节一致，氛围选择恰当";
        }
        
        return "夕阳西下，金色的余晖透过古老的窗棂洒进屋内，在地面上投下斑驳的光影。" +
               "微风轻拂，带来远处花园淡淡的花香。空气中弥漫着岁月的痕迹，" +
               "墙角的青苔在湿润的砖石上悄然生长，诉说着时光的故事。" +
               "寂静中，只听得见窗外树叶的沙沙声，偶尔传来几声鸟鸣，" +
               "一切都显得那么宁静祥和，仿佛时间在这里也变得缓慢起来。";
    }
    
    /**
     * 从AI响应中提取值
     */
    private String extractValue(String text, String key) {
        if (text == null || key == null) return null;
        
        int start = text.indexOf(key);
        if (start == -1) return null;
        
        start = text.indexOf("：", start);
        if (start == -1) start = text.indexOf(":", start);
        if (start == -1) return null;
        
        int end = text.indexOf("\n", start);
        if (end == -1) end = text.length();
        
        return text.substring(start + 1, end).trim();
    }
    
    /**
     * 获取模板名称
     */
    private String getTemplateName(Long templateId) {
        if (templateId == null) return null;
        return templateRepository.findById(templateId)
                .map(AtmosphereTemplate::getName)
                .orElse(null);
    }
    
    /**
     * 转换为响应对象
     */
    private SceneAtmosphereResponse toResponse(SceneAtmosphere atmosphere, String templateName) {
        return SceneAtmosphereResponse.builder()
                .id(atmosphere.getId())
                .sceneId(atmosphere.getSceneId())
                .templateId(atmosphere.getTemplateId())
                .templateName(templateName)
                .atmosphereType(atmosphere.getAtmosphereType())
                .generatedText(atmosphere.getGeneratedText())
                .promptUsed(atmosphere.getPromptUsed())
                .aiModel(atmosphere.getAiModel())
                .version(atmosphere.getVersion())
                .rating(atmosphere.getRating())
                .isApplied(atmosphere.getIsApplied())
                .appliedAt(atmosphere.getAppliedAt())
                .createdAt(atmosphere.getCreatedAt())
                .build();
    }
    
    /**
     * 转换为模板响应对象
     */
    private AtmosphereTemplateResponse toTemplateResponse(AtmosphereTemplate template) {
        return AtmosphereTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .category(template.getCategory())
                .atmosphereType(template.getAtmosphereType())
                .description(template.getDescription())
                .keywords(template.getKeywords())
                .sensoryDetails(template.getSensoryDetails())
                .exampleText(template.getExampleText())
                .usageCount(template.getUsageCount())
                .isSystem(template.getIsSystem())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
