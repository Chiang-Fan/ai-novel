package com.aiwriter.service;

import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 多维约束融合引擎
 * 核心功能：整合世界观、场景、角色、写作风格等多维约束，为AI续写提供精准指导
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MultiDimensionalConstraintEngine {

    private final WorldSettingRepository worldSettingRepository;
    private final SceneRepository sceneRepository;
    private final CharacterRepository characterRepository;
    private final ChapterRepository chapterRepository;
    private final ContentAnalysisService contentAnalysisService;
    private final WorldSettingValidationService validationService;
    private final GeographySettingRepository geographySettingRepository;
    private final RaceSettingRepository raceSettingRepository;
    private final NovelRepository novelRepository;
    private final OutlineRepository outlineRepository;

    /**
     * 综合约束信息
     */
    @Data
    public static class CompositeConstraints {
        private Long novelId;
        private Long chapterId;
        
        // 世界观约束
        private WorldConstraints worldConstraints;
        
        // 场景约束
        private SceneConstraints sceneConstraints;
        
        // 角色约束
        private CharacterConstraints characterConstraints;
        
        // 风格约束
        private StyleConstraints styleConstraints;
        
        // 情节约束
        private PlotConstraints plotConstraints;
        
        // 约束优先级
        private ConstraintPriority priority;
        
        // 综合约束强度 (0-100)
        private Integer totalConstraintStrength;
    }

    /**
     * 世界观约束
     */
    @Data
    public static class WorldConstraints {
        // 宇宙背景规则
        private String universeRules;
        
        // 地理限制
        private List<String> geographicalLimitations;
        
        // 时代背景
        private String timePeriodContext;
        
        // 种族系统约束
        private List<RaceConstraint> raceConstraints;
        
        // 魔法规则
        private String magicSystemRules;
        
        // 禁忌元素
        private List<String> tabooElements;
        
        // 约束强度
        private Integer constraintStrength;
        
        // Getter/Setter for tabooElements
        public List<String> getTabooElements() {
            return tabooElements != null ? tabooElements : new ArrayList<>();
        }
        
        public void setTabooElements(List<String> tabooElements) {
            this.tabooElements = tabooElements;
        }
    }

    /**
     * 场景约束
     */
    @Data
    public static class SceneConstraints {
        // 场景名称
        private String sceneName;
        
        // 场景类型
        private String sceneType;
        
        // 地点限制
        private String location;
        
        // 天气/环境
        private String atmosphere;
        
        // 时间限制
        private String timePeriod;
        
        // 可用道具
        private List<String> availableProps;
        
        // 场景重要性
        private Integer importanceScore;
        
        // 已出现字数
        private Integer wordCountInScene;
    }

    /**
     * 角色约束
     */
    @Data
    public static class CharacterConstraints {
        // 主角信息
        private PrincipalCharacter protagonist;
        
        // 配角列表
        private List<SupportingCharacter> supportingCharacters;
        
        // 角色关系
        private List<CharacterRelation> relationships;
        
        // 角色成长阶段
        private Map<Long, String> characterGrowthPhase;
    }

    /**
     * 风格约束
     */
    @Data
    public static class StyleConstraints {
        // 小说总体风格
        private String novelStyle;
        
        // 当前段落风格
        private String paragraphStyle;
        
        // 节奏要求
        private String pacing;
        
        // 语气要求
        private String tone;
        
        // 描写风格
        private String descriptionStyle;
    }

    /**
     * 情节约束
     */
    @Data
    public static class PlotConstraints {
        // 大纲关键点
        private List<String> outlineKeyPoints;
        
        // 当前情节阶段
        private String plotPhase;
        
        // 冲突要素
        private List<String> conflicts;
        
        // 伏笔线索
        private List<String> foreshadows;
        
        // 禁忌元素
        private List<String> tabooElements;
    }

    /**
     * 约束优先级
     */
    @Data
    public static class ConstraintPriority {
        // 0-低优先级, 1-中优先级, 2-高优先级
        private Integer worldConstraintPriority = 1;
        private Integer sceneConstraintPriority = 2;
        private Integer characterConstraintPriority = 2;
        private Integer styleConstraintPriority = 1;
        private Integer plotConstraintPriority = 2;
    }

    /**
     * 种族约束
     */
    @Data
    public static class RaceConstraint {
        private String raceName;
        private String characteristics;
        private String abilities;
        private String limitations;
        private String culturalTraits;
        private String interracialRelations;
    }

    /**
     * 主角信息
     */
    @Data
    public static class PrincipalCharacter {
        private Long characterId;
        private String name;
        private String personality;
        private String abilities;
        private String currentState;
        private Integer psychologicalState; // 0-100
    }

    /**
     * 配角信息
     */
    @Data
    public static class SupportingCharacter {
        private Long characterId;
        private String name;
        private String relationship;
        private String role;
    }

    /**
     * 角色关系
     */
    @Data
    public static class CharacterRelation {
        private Long character1Id;
        private Long character2Id;
        private String relationshipType;
        private String dynamics;
    }

    /**
     * 为AI续写构建多维约束
     */
    @Transactional(readOnly = true)
    public CompositeConstraints buildCompositeConstraints(Long novelId, Long chapterId) {
        log.info("构建多维约束: novelId={}, chapterId={}", novelId, chapterId);
        
        CompositeConstraints constraints = new CompositeConstraints();
        constraints.setNovelId(novelId);
        constraints.setChapterId(chapterId);
        constraints.setPriority(new ConstraintPriority());
        
        // 1. 构建世界观约束
        constraints.setWorldConstraints(buildWorldConstraints(novelId));
        
        // 2. 构建场景约束
        constraints.setSceneConstraints(buildSceneConstraints(novelId, chapterId));
        
        // 3. 构建角色约束
        constraints.setCharacterConstraints(buildCharacterConstraints(novelId, chapterId));
        
        // 4. 构建风格约束
        constraints.setStyleConstraints(buildStyleConstraints(novelId, chapterId));
        
        // 5. 构建情节约束
        constraints.setPlotConstraints(buildPlotConstraints(novelId, chapterId));
        
        // 6. 计算总约束强度
        constraints.setTotalConstraintStrength(calculateTotalConstraintStrength(constraints));
        
        return constraints;
    }

    /**
     * 构建世界观约束
     */
    private WorldConstraints buildWorldConstraints(Long novelId) {
        WorldConstraints constraints = new WorldConstraints();
        
        List<WorldSetting> worldSettings = worldSettingRepository.findByNovelId(novelId);
        if (worldSettings.isEmpty()) {
            constraints.setConstraintStrength(0);
            return constraints;
        }
        
        WorldSetting worldSetting = worldSettings.get(0);
        
        constraints.setUniverseRules(worldSetting.getUniverseRules());
        constraints.setTimePeriodContext(worldSetting.getTimePeriodContext());
        constraints.setMagicSystemRules(worldSetting.getMagicSystemRules());
        
        // 地理限制
        constraints.setGeographicalLimitations(
            geographySettingRepository.findByWorldSettingIdOrderBySortOrder(worldSetting.getId())
                .stream()
                .map(GeographySetting::getDescription)
                .collect(Collectors.toList())
        );
        
        // 种族约束
        constraints.setRaceConstraints(
            raceSettingRepository.findByWorldSettingIdOrderByImportanceLevelDesc(worldSetting.getId())
                .stream()
                .map(race -> {
                    RaceConstraint rc = new RaceConstraint();
                    rc.setRaceName(race.getName());
                    rc.setCharacteristics(race.getPhysicalCharacteristics());
                    rc.setAbilities(race.getAbilities());
                    rc.setLimitations(race.getSocialStatus());  // 使用社会地位作为限制
                    rc.setCulturalTraits(race.getCulturalCustoms());
                    rc.setInterracialRelations(race.getRaceRelations());
                    return rc;
                })
                .collect(Collectors.toList())
        );
        
        constraints.setConstraintStrength(75); // 世界观约束强度
        return constraints;
    }

    /**
     * 构建场景约束
     */
    private SceneConstraints buildSceneConstraints(Long novelId, Long chapterId) {
        SceneConstraints constraints = new SceneConstraints();
        
        // 从章节获取相关场景
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        // 获取章节中提到的主要场景
        List<Scene> scenes = sceneRepository.findByNovelIdOrderByImportanceScoreDesc(novelId);
        if (scenes.isEmpty()) {
            return constraints;
        }
        
        // 使用第一个匹配的场景作为主场景
        Scene mainScene = scenes.get(0);
        constraints.setSceneName(mainScene.getName());
        constraints.setSceneType(mainScene.getSceneType());
        constraints.setLocation(mainScene.getLocation());
        constraints.setAtmosphere(mainScene.getAtmosphere());
        constraints.setTimePeriod(mainScene.getTimePeriod());
        
        // 解析道具
        if (mainScene.getProps() != null && !mainScene.getProps().isEmpty()) {
            constraints.setAvailableProps(Arrays.asList(mainScene.getProps().split(",")));
        }
        
        constraints.setImportanceScore(mainScene.getImportanceScore());
        constraints.setWordCountInScene(chapter.getWordCount());
        
        return constraints;
    }

    /**
     * 构建角色约束
     */
    private CharacterConstraints buildCharacterConstraints(Long novelId, Long chapterId) {
        CharacterConstraints constraints = new CharacterConstraints();
        
        List<com.aiwriter.entity.Character> characters = characterRepository.findByNovelIdOrderByRoleTypeAsc(novelId);
        
        if (characters.isEmpty()) {
            return constraints;
        }
        
        // 设置主角
        com.aiwriter.entity.Character protagonist = characters.stream()
                .filter(c -> "PROTAGONIST".equals(c.getRoleType()))
                .findFirst()
                .orElse(characters.get(0));
        
        PrincipalCharacter pc = new PrincipalCharacter();
        pc.setCharacterId(protagonist.getId());
        pc.setName(protagonist.getName());
        pc.setPersonality(protagonist.getPersonality());
        pc.setAbilities(protagonist.getAbilities());
        pc.setCurrentState(protagonist.getBackground());  // 使用背景作为当前状态
        
        constraints.setProtagonist(pc);
        
        // 收集配角
        constraints.setSupportingCharacters(
            characters.stream()
                .filter(c -> !c.getId().equals(protagonist.getId()))
                .map(c -> {
                    SupportingCharacter sc = new SupportingCharacter();
                    sc.setCharacterId(c.getId());
                    sc.setName(c.getName());
                    sc.setRole(c.getRoleType());
                    return sc;
                })
                .collect(Collectors.toList())
        );
        
        return constraints;
    }

    /**
     * 构建风格约束
     */
    private StyleConstraints buildStyleConstraints(Long novelId, Long chapterId) {
        StyleConstraints constraints = new StyleConstraints();
        
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        Novel novel = novelRepository.findById(chapter.getNovelId())
                .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        // 从小说配置读取风格
        constraints.setNovelStyle(novel.getWritingStyle() != null 
            ? novel.getWritingStyle() 
            : "NEUTRAL");
        
        constraints.setParagraphStyle("DESCRIPTIVE");
        constraints.setPacing("MODERATE");
        constraints.setTone("NEUTRAL");
        constraints.setDescriptionStyle("VIVID");
        
        return constraints;
    }

    /**
     * 构建情节约束
     */
    private PlotConstraints buildPlotConstraints(Long novelId, Long chapterId) {
        PlotConstraints constraints = new PlotConstraints();
        
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        Novel novel = novelRepository.findById(chapter.getNovelId())
                .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        // 获取大纲信息
        List<Outline> outlines = outlineRepository.findByNovelIdOrderBySequenceNumberAsc(novelId);
        
        constraints.setOutlineKeyPoints(
            outlines.stream()
                .map(Outline::getTitle)
                .collect(Collectors.toList())
        );
        
        constraints.setPlotPhase("DEVELOPING");
        constraints.setConflicts(new ArrayList<>());
        constraints.setForeshadows(new ArrayList<>());
        constraints.setTabooElements(new ArrayList<>());
        
        return constraints;
    }

    /**
     * 计算总约束强度
     */
    private Integer calculateTotalConstraintStrength(CompositeConstraints constraints) {
        int totalStrength = 0;
        int count = 0;
        
        if (constraints.getWorldConstraints() != null && constraints.getWorldConstraints().getConstraintStrength() != null) {
            totalStrength += constraints.getWorldConstraints().getConstraintStrength() * 
                           constraints.getPriority().getWorldConstraintPriority();
            count++;
        }
        
        if (count > 0) {
            return Math.min(100, totalStrength / count);
        }
        
        return 50;
    }

    /**
     * 生成约束提示词
     */
    public String generateConstraintPrompt(CompositeConstraints constraints) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("【多维约束信息】\n");
        prompt.append("请在以下约束条件下进行续写：\n\n");
        
        // 世界观约束
        if (constraints.getWorldConstraints() != null) {
            prompt.append("【世界观设定】\n");
            if (constraints.getWorldConstraints().getUniverseRules() != null) {
                prompt.append("宇宙规则: ").append(constraints.getWorldConstraints().getUniverseRules()).append("\n");
            }
            if (constraints.getWorldConstraints().getMagicSystemRules() != null) {
                prompt.append("魔法规则: ").append(constraints.getWorldConstraints().getMagicSystemRules()).append("\n");
            }
            prompt.append("\n");
        }
        
        // 场景约束
        if (constraints.getSceneConstraints() != null) {
            prompt.append("【场景约束】\n");
            prompt.append("场景: ").append(constraints.getSceneConstraints().getSceneName()).append("\n");
            prompt.append("地点: ").append(constraints.getSceneConstraints().getLocation()).append("\n");
            prompt.append("氛围: ").append(constraints.getSceneConstraints().getAtmosphere()).append("\n");
            prompt.append("\n");
        }
        
        // 角色约束
        if (constraints.getCharacterConstraints() != null && 
            constraints.getCharacterConstraints().getProtagonist() != null) {
            PrincipalCharacter pc = constraints.getCharacterConstraints().getProtagonist();
            prompt.append("【主角约束】\n");
            prompt.append("姓名: ").append(pc.getName()).append("\n");
            prompt.append("性格: ").append(pc.getPersonality()).append("\n");
            prompt.append("能力: ").append(pc.getAbilities()).append("\n");
            prompt.append("\n");
        }
        
        // 风格约束
        if (constraints.getStyleConstraints() != null) {
            prompt.append("【风格约束】\n");
            prompt.append("小说风格: ").append(constraints.getStyleConstraints().getNovelStyle()).append("\n");
            prompt.append("节奏: ").append(constraints.getStyleConstraints().getPacing()).append("\n");
            prompt.append("\n");
        }
        
        prompt.append("约束强度: ").append(constraints.getTotalConstraintStrength()).append("/100\n");
        prompt.append("请确保续写内容完全符合以上所有约束条件。\n");
        
        return prompt.toString();
    }

    /**
     * 验证续写内容是否违反约束
     */
    public ConstraintValidationResult validateAgainstConstraints(
            String continuationText, 
            CompositeConstraints constraints) {
        
        ConstraintValidationResult result = new ConstraintValidationResult();
        result.setValid(true);
        result.setViolations(new ArrayList<>());
        
        // 1. 检查世界观约束
        validateWorldConstraints(continuationText, constraints, result);
        
        // 2. 检查场景约束
        validateSceneConstraints(continuationText, constraints, result);
        
        // 3. 检查角色约束
        validateCharacterConstraints(continuationText, constraints, result);
        
        return result;
    }

    /**
     * 验证世界观约束
     */
    private void validateWorldConstraints(String text, CompositeConstraints constraints, 
                                         ConstraintValidationResult result) {
        WorldConstraints world = constraints.getWorldConstraints();
        if (world == null) return;
        
        // 检查禁止的元素
        if (world.getTabooElements() != null) {
            for (String taboo : world.getTabooElements()) {
                if (text.contains(taboo)) {
                    result.addViolation("世界观", "包含禁止元素: " + taboo);
                    result.setValid(false);
                }
            }
        }
    }

    /**
     * 验证场景约束
     */
    private void validateSceneConstraints(String text, CompositeConstraints constraints, 
                                         ConstraintValidationResult result) {
        SceneConstraints scene = constraints.getSceneConstraints();
        if (scene == null) return;
        
        // 检查场景一致性
        if (scene.getLocation() != null && !text.toLowerCase().contains(scene.getLocation().toLowerCase())) {
            // 可以允许不显式提及但逻辑一致的场景
        }
    }

    /**
     * 验证角色约束
     */
    private void validateCharacterConstraints(String text, CompositeConstraints constraints, 
                                             ConstraintValidationResult result) {
        CharacterConstraints chars = constraints.getCharacterConstraints();
        if (chars == null || chars.getProtagonist() == null) return;
        
        PrincipalCharacter protagonist = chars.getProtagonist();
        
        // 检查主角是否出现
        if (!text.contains(protagonist.getName())) {
            result.addViolation("角色", "主角未在续写中出现");
        }
    }

    /**
     * 约束验证结果
     */
    @Data
    public static class ConstraintValidationResult {
        private Boolean valid;
        private List<ConstraintViolation> violations;
        
        public void addViolation(String category, String message) {
            ConstraintViolation violation = new ConstraintViolation();
            violation.setCategory(category);
            violation.setMessage(message);
            violations.add(violation);
        }
    }

    /**
     * 约束违规
     */
    @Data
    public static class ConstraintViolation {
        private String category;
        private String message;
    }
}