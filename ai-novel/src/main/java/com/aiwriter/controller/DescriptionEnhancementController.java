package com.aiwriter.controller;

import com.aiwriter.service.DescriptionOptimizationService;
import com.aiwriter.service.DescriptionStyleTemplateService;
import com.aiwriter.service.MultiDimensionalDescriptionEngine;
import com.aiwriter.service.RhythmAwareDescriptionService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 描写增强控制器 - Phase 6 API
 */
@RestController
@RequestMapping("/api/description")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DescriptionEnhancementController {

    private final MultiDimensionalDescriptionEngine descriptionEngine;
    private final DescriptionStyleTemplateService styleTemplateService;
    private final RhythmAwareDescriptionService rhythmService;
    private final DescriptionOptimizationService optimizationService;

    // ==================== 多维描写生成 ====================

    /**
     * 生成多维描写
     */
    @PostMapping("/generate")
    public ResponseEntity<MultiDimensionalDescriptionEngine.DescriptionGenerationResult> generateDescription(
            @RequestBody DescriptionGenerateRequest request) {
        
        log.info("生成多维描写: type={}, style={}", request.getDescriptionType(), request.getStyle());

        MultiDimensionalDescriptionEngine.DescriptionGenerationRequest engineRequest = 
                new MultiDimensionalDescriptionEngine.DescriptionGenerationRequest();
        engineRequest.setNovelId(request.getNovelId());
        engineRequest.setSceneId(request.getSceneId());
        engineRequest.setCharacterId(request.getCharacterId());
        engineRequest.setDescriptionType(
                MultiDimensionalDescriptionEngine.DescriptionType.valueOf(request.getDescriptionType()));
        engineRequest.setStyle(
                MultiDimensionalDescriptionEngine.DescriptionStyle.valueOf(request.getStyle()));
        engineRequest.setContext(request.getContext());
        engineRequest.setTargetMood(request.getTargetMood());
        engineRequest.setTargetLength(request.getTargetLength());
        engineRequest.setIntensity(request.getIntensity());
        engineRequest.setKeywords(request.getKeywords());
        engineRequest.setAdditionalParams(request.getAdditionalParams());

        return ResponseEntity.ok(descriptionEngine.generateDescription(engineRequest));
    }

    /**
     * 生成环境描写
     */
    @PostMapping("/generate/environment")
    public ResponseEntity<MultiDimensionalDescriptionEngine.DescriptionGenerationResult> generateEnvironmentDescription(
            @RequestBody EnvironmentDescriptionRequest request) {
        
        log.info("生成环境描写: novelId={}", request.getNovelId());

        MultiDimensionalDescriptionEngine.EnvironmentDescriptionParams params = 
                new MultiDimensionalDescriptionEngine.EnvironmentDescriptionParams();
        params.setLocation(request.getLocation());
        params.setTimeOfDay(request.getTimeOfDay());
        params.setWeather(request.getWeather());
        params.setSeason(request.getSeason());
        params.setAtmosphere(request.getAtmosphere());
        params.setSensoryDetails(request.getSensoryDetails());
        params.setIncludeSymbolism(request.getIncludeSymbolism());

        MultiDimensionalDescriptionEngine.DescriptionStyle style = 
                request.getStyle() != null ? 
                        MultiDimensionalDescriptionEngine.DescriptionStyle.valueOf(request.getStyle()) :
                        MultiDimensionalDescriptionEngine.DescriptionStyle.DETAILED;

        return ResponseEntity.ok(
                descriptionEngine.generateEnvironmentDescription(request.getNovelId(), params, style));
    }

    /**
     * 生成心理描写
     */
    @PostMapping("/generate/psychological")
    public ResponseEntity<MultiDimensionalDescriptionEngine.DescriptionGenerationResult> generatePsychologicalDescription(
            @RequestBody PsychologicalDescriptionRequest request) {
        
        log.info("生成心理描写: novelId={}, characterId={}", request.getNovelId(), request.getCharacterId());

        MultiDimensionalDescriptionEngine.PsychologicalDescriptionParams params = 
                new MultiDimensionalDescriptionEngine.PsychologicalDescriptionParams();
        params.setCharacterName(request.getCharacterName());
        params.setCurrentEmotion(request.getCurrentEmotion());
        params.setTriggerEvent(request.getTriggerEvent());
        params.setInternalConflict(request.getInternalConflict());
        params.setMemories(request.getMemories());
        params.setShowNotTell(request.getShowNotTell());

        MultiDimensionalDescriptionEngine.DescriptionStyle style = 
                request.getStyle() != null ? 
                        MultiDimensionalDescriptionEngine.DescriptionStyle.valueOf(request.getStyle()) :
                        MultiDimensionalDescriptionEngine.DescriptionStyle.DETAILED;

        return ResponseEntity.ok(
                descriptionEngine.generatePsychologicalDescription(
                        request.getNovelId(), request.getCharacterId(), params, style));
    }

    /**
     * 生成情绪描写
     */
    @PostMapping("/generate/emotional")
    public ResponseEntity<MultiDimensionalDescriptionEngine.DescriptionGenerationResult> generateEmotionalDescription(
            @RequestBody EmotionalDescriptionRequest request) {
        
        log.info("生成情绪描写: novelId={}, characterId={}", request.getNovelId(), request.getCharacterId());

        MultiDimensionalDescriptionEngine.EmotionalDescriptionParams params = 
                new MultiDimensionalDescriptionEngine.EmotionalDescriptionParams();
        params.setEmotion(request.getEmotion());
        params.setIntensity(request.getIntensity());
        params.setPhysicalManifestation(request.getPhysicalManifestation());
        params.setBehaviorChange(request.getBehaviorChange());
        params.setGradualTransition(request.getGradualTransition());
        params.setPreviousEmotion(request.getPreviousEmotion());

        MultiDimensionalDescriptionEngine.DescriptionStyle style = 
                request.getStyle() != null ? 
                        MultiDimensionalDescriptionEngine.DescriptionStyle.valueOf(request.getStyle()) :
                        MultiDimensionalDescriptionEngine.DescriptionStyle.DETAILED;

        return ResponseEntity.ok(
                descriptionEngine.generateEmotionalDescription(
                        request.getNovelId(), request.getCharacterId(), params, style));
    }

    /**
     * 生成动作描写
     */
    @PostMapping("/generate/action")
    public ResponseEntity<MultiDimensionalDescriptionEngine.DescriptionGenerationResult> generateActionDescription(
            @RequestBody ActionDescriptionRequest request) {
        
        log.info("生成动作描写: novelId={}, characterId={}", request.getNovelId(), request.getCharacterId());

        MultiDimensionalDescriptionEngine.ActionDescriptionParams params = 
                new MultiDimensionalDescriptionEngine.ActionDescriptionParams();
        params.setActionType(request.getActionType());
        params.setCharacterName(request.getCharacterName());
        params.setPurpose(request.getPurpose());
        params.setSetting(request.getSetting());
        params.setSpeed(request.getSpeed());
        params.setIncludeReaction(request.getIncludeReaction());

        MultiDimensionalDescriptionEngine.DescriptionStyle style = 
                request.getStyle() != null ? 
                        MultiDimensionalDescriptionEngine.DescriptionStyle.valueOf(request.getStyle()) :
                        MultiDimensionalDescriptionEngine.DescriptionStyle.DETAILED;

        return ResponseEntity.ok(
                descriptionEngine.generateActionDescription(
                        request.getNovelId(), request.getCharacterId(), params, style));
    }

    // ==================== 风格模板 ====================

    /**
     * 获取所有风格模板
     */
    @GetMapping("/styles")
    public ResponseEntity<List<DescriptionStyleTemplateService.StyleTemplate>> getAllStyles() {
        return ResponseEntity.ok(styleTemplateService.getAllBuiltInTemplates());
    }

    /**
     * 获取指定风格模板
     */
    @GetMapping("/styles/{templateId}")
    public ResponseEntity<DescriptionStyleTemplateService.StyleTemplate> getStyleById(
            @PathVariable String templateId) {
        DescriptionStyleTemplateService.StyleTemplate template = 
                styleTemplateService.getTemplateById(templateId);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(template);
    }

    /**
     * 分析文本风格
     */
    @PostMapping("/styles/analyze")
    public ResponseEntity<DescriptionStyleTemplateService.StyleAnalysisResult> analyzeStyle(
            @RequestBody StyleAnalyzeRequest request) {
        return ResponseEntity.ok(styleTemplateService.analyzeStyle(request.getText()));
    }

    /**
     * 混合风格
     */
    @PostMapping("/styles/blend")
    public ResponseEntity<Map<String, String>> blendStyles(@RequestBody StyleBlendRequest request) {
        String blendedPrompt = styleTemplateService.blendStyles(
                request.getTemplateIds(), request.getWeights());
        return ResponseEntity.ok(Map.of("blendedPrompt", blendedPrompt));
    }

    // ==================== 节奏感知描写 ====================

    /**
     * 分析节奏
     */
    @PostMapping("/rhythm/analyze")
    public ResponseEntity<RhythmAwareDescriptionService.RhythmAnalysis> analyzeRhythm(
            @RequestBody RhythmAnalyzeRequest request) {
        return ResponseEntity.ok(rhythmService.analyzeRhythm(request.getContent()));
    }

    /**
     * 生成节奏感知描写
     */
    @PostMapping("/rhythm/generate")
    public ResponseEntity<RhythmAwareDescriptionService.RhythmAwareDescriptionResult> generateRhythmAwareDescription(
            @RequestBody RhythmAwareGenerateRequest request) {
        
        log.info("生成节奏感知描写: chapterId={}", request.getChapterId());

        RhythmAwareDescriptionService.RhythmAwareDescriptionRequest serviceRequest = 
                new RhythmAwareDescriptionService.RhythmAwareDescriptionRequest();
        serviceRequest.setChapterId(request.getChapterId());
        serviceRequest.setCurrentContent(request.getCurrentContent());
        serviceRequest.setTargetScene(request.getTargetScene());
        serviceRequest.setTargetLength(request.getTargetLength());
        serviceRequest.setFocusAreas(request.getFocusAreas());

        if (request.getOverrideRhythm() != null) {
            serviceRequest.setOverrideRhythm(
                    RhythmAwareDescriptionService.RhythmType.valueOf(request.getOverrideRhythm()));
        }

        return ResponseEntity.ok(rhythmService.generateRhythmAwareDescription(serviceRequest));
    }

    /**
     * 调整描写节奏
     */
    @PostMapping("/rhythm/adjust")
    public ResponseEntity<Map<String, String>> adjustRhythm(@RequestBody RhythmAdjustRequest request) {
        RhythmAwareDescriptionService.RhythmType targetRhythm = 
                RhythmAwareDescriptionService.RhythmType.valueOf(request.getTargetRhythm());
        String adjusted = rhythmService.adjustDescriptionForRhythm(
                request.getOriginalDescription(), targetRhythm);
        return ResponseEntity.ok(Map.of("adjustedDescription", adjusted));
    }

    // ==================== 描写优化 ====================

    /**
     * 优化描写
     */
    @PostMapping("/optimize")
    public ResponseEntity<DescriptionOptimizationService.OptimizationResult> optimizeDescription(
            @RequestBody OptimizeRequest request) {
        
        log.info("优化描写: types={}", request.getOptimizationTypes());

        DescriptionOptimizationService.OptimizationRequest serviceRequest = 
                new DescriptionOptimizationService.OptimizationRequest();
        serviceRequest.setOriginalText(request.getOriginalText());
        serviceRequest.setOptimizationTypes(
                request.getOptimizationTypes().stream()
                        .map(DescriptionOptimizationService.OptimizationType::valueOf)
                        .toList());
        serviceRequest.setTargetStyle(request.getTargetStyle());
        serviceRequest.setTargetQualityScore(request.getTargetQualityScore());
        serviceRequest.setPreserveLength(request.getPreserveLength());

        return ResponseEntity.ok(optimizationService.optimizeDescription(serviceRequest));
    }

    /**
     * 诊断描写问题
     */
    @PostMapping("/diagnose")
    public ResponseEntity<DescriptionOptimizationService.DiagnosisResult> diagnoseDescription(
            @RequestBody DiagnoseRequest request) {
        return ResponseEntity.ok(optimizationService.diagnoseDescription(request.getText()));
    }

    /**
     * 智能改写
     */
    @PostMapping("/rewrite")
    public ResponseEntity<Map<String, String>> intelligentRewrite(@RequestBody RewriteRequest request) {
        String rewritten = optimizationService.intelligentRewrite(
                request.getText(), request.getInstruction());
        return ResponseEntity.ok(Map.of("rewrittenText", rewritten));
    }

    /**
     * 获取优化建议
     */
    @PostMapping("/suggestions")
    public ResponseEntity<List<String>> getOptimizationSuggestions(@RequestBody SuggestionsRequest request) {
        return ResponseEntity.ok(optimizationService.getOptimizationSuggestions(request.getText()));
    }

    // ==================== 请求DTO ====================

    @Data
    public static class DescriptionGenerateRequest {
        private Long novelId;
        private Long sceneId;
        private Long characterId;
        private String descriptionType;
        private String style;
        private String context;
        private String targetMood;
        private Integer targetLength;
        private Double intensity;
        private List<String> keywords;
        private Map<String, Object> additionalParams;
    }

    @Data
    public static class EnvironmentDescriptionRequest {
        private Long novelId;
        private String location;
        private String timeOfDay;
        private String weather;
        private String season;
        private String atmosphere;
        private List<String> sensoryDetails;
        private Boolean includeSymbolism;
        private String style;
    }

    @Data
    public static class PsychologicalDescriptionRequest {
        private Long novelId;
        private Long characterId;
        private String characterName;
        private String currentEmotion;
        private String triggerEvent;
        private String internalConflict;
        private List<String> memories;
        private Boolean showNotTell;
        private String style;
    }

    @Data
    public static class EmotionalDescriptionRequest {
        private Long novelId;
        private Long characterId;
        private String emotion;
        private Double intensity;
        private String physicalManifestation;
        private String behaviorChange;
        private Boolean gradualTransition;
        private String previousEmotion;
        private String style;
    }

    @Data
    public static class ActionDescriptionRequest {
        private Long novelId;
        private Long characterId;
        private String actionType;
        private String characterName;
        private String purpose;
        private String setting;
        private Double speed;
        private Boolean includeReaction;
        private String style;
    }

    @Data
    public static class StyleAnalyzeRequest {
        private String text;
    }

    @Data
    public static class StyleBlendRequest {
        private List<String> templateIds;
        private Map<String, Double> weights;
    }

    @Data
    public static class RhythmAnalyzeRequest {
        private String content;
    }

    @Data
    public static class RhythmAwareGenerateRequest {
        private Long chapterId;
        private String currentContent;
        private String targetScene;
        private String overrideRhythm;
        private Integer targetLength;
        private List<String> focusAreas;
    }

    @Data
    public static class RhythmAdjustRequest {
        private String originalDescription;
        private String targetRhythm;
    }

    @Data
    public static class OptimizeRequest {
        private String originalText;
        private List<String> optimizationTypes;
        private String targetStyle;
        private Double targetQualityScore;
        private Boolean preserveLength;
    }

    @Data
    public static class DiagnoseRequest {
        private String text;
    }

    @Data
    public static class RewriteRequest {
        private String text;
        private String instruction;
    }

    @Data
    public static class SuggestionsRequest {
        private String text;
    }
}
