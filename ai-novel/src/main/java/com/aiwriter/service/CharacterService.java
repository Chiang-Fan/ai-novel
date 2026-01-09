package com.aiwriter.service;

import com.aiwriter.dto.CharacterRecommendationRequest;
import com.aiwriter.dto.CharacterRecommendationResponse;
import com.aiwriter.entity.Character;
import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.Novel;
import com.aiwriter.entity.WorldSetting;
import com.aiwriter.entity.CharacterLorebook; // 新增：角色记忆库实体
import com.aiwriter.repository.CharacterRepository;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.repository.WorldSettingRepository;
import com.aiwriter.repository.CharacterLorebookRepository; // 新增：角色记忆库Repository
import com.aiwriter.service.ai.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CharacterService {
    
    private final CharacterRepository characterRepository;
    private final ChapterRepository chapterRepository;
    private final WorldSettingRepository worldSettingRepository;
    private final NovelService novelService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    
    @Transactional(readOnly = true)
    public List<Character> getCharactersByNovel(Long novelId) {
        return characterRepository.findByNovelIdOrderByRoleTypeAsc(novelId);
    }
    
    @Transactional(readOnly = true)
    public Character getCharacterById(Long id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
    }
    
    @Transactional
    public Character createCharacter(Character character) {
        Character saved = characterRepository.save(character);
        log.info("创建角色成功: {} (小说ID: {})", saved.getName(), saved.getNovelId());
        return saved;
    }
    
    @Transactional
    public Character updateCharacter(Long id, Character character) {
        Character existing = getCharacterById(id);
        
        // 更新字段
        existing.setName(character.getName());
        existing.setRoleType(character.getRoleType());
        existing.setGender(character.getGender());
        existing.setAge(character.getAge());
        existing.setPersonality(character.getPersonality());
        existing.setBackground(character.getBackground());
        existing.setAppearance(character.getAppearance());
        existing.setAbilities(character.getAbilities());
        existing.setRelationships(character.getRelationships());
        existing.setMotivation(character.getMotivation());
        existing.setArc(character.getArc());
        existing.setNotes(character.getNotes());
        
        Character updated = characterRepository.save(existing);
        log.info("更新角色成功: {}", updated.getName());
        return updated;
    }
    
    @Transactional
    public void deleteCharacter(Long id) {
        characterRepository.deleteById(id);
        log.info("删除角色成功: {}", id);
    }
    
    /**
     * AI智能推荐角色
     */
    public List<CharacterRecommendationResponse> recommendCharacters(CharacterRecommendationRequest request) {
        try {
            Novel novel = novelService.getNovel(request.getNovelId());
            List<Character> existingCharacters = getCharactersByNovel(request.getNovelId());
            
            // 获取世界观设定（按重要性排序）
            List<WorldSetting> worldSettings = worldSettingRepository.findByNovelId(request.getNovelId())
                .stream()
                .sorted((a, b) -> {
                    // 按重要性排序: high > medium > low
                    int orderA = getImportanceOrder(a.getImportance());
                    int orderB = getImportanceOrder(b.getImportance());
                    return Integer.compare(orderA, orderB);
                })
                .limit(10) // 限制最多10个设定
                .collect(Collectors.toList());
            
            // 获取最近章节内容作为参考
            List<Chapter> recentChapters = chapterRepository.findTopNByNovelId(request.getNovelId(), 3);
            
            String systemPrompt = buildCharacterRecommendationSystemPrompt();
            String userPrompt = buildCharacterRecommendationUserPrompt(novel, existingCharacters, worldSettings, recentChapters, request.getCount());
            
            String aiResult = aiService.chatJson(systemPrompt, userPrompt);
            log.info("AI角色推荐原始结果: {}", aiResult);
            
            // 解析JSON结果
            List<CharacterRecommendationResponse> recommendations = objectMapper.readValue(
                aiResult, 
                new TypeReference<List<CharacterRecommendationResponse>>() {}
            );
            
            log.info("成功生成{}个角色推荐", recommendations.size());
            return recommendations;
            
        } catch (Exception e) {
            log.error("AI角色推荐失败", e);
            // 返回随机推荐
            return generateRandomCharacterRecommendations(request.getCount());
        }
    }
    
    private String buildCharacterRecommendationSystemPrompt() {
        return """
你是一位专业的小说角色设计专家，擅长根据小说类型和已有角色创造富有深度的新角色。

## 核心任务
根据小说的基本信息和已有角色，推荐适合的新角色。新角色应该：
1. 符合小说的类型、风格和世界观
2. 与已有角色形成互补或冲突关系
3. 具有独特的性格和背景
4. 对情节发展有实际推动作用

## 角色设计要点
- **roleType**: PROTAGONIST(主角)/ANTAGONIST(反派)/SUPPORTING(配角)/MINOR(次要)
- **gender**: MALE/FEMALE/OTHER
- **personality**: 详细的性格特征，至少100字
- **background**: 丰富的背景故事，至少150字
- **appearance**: 具体的外貌描述，至少80字
- **abilities**: 角色的能力、技能或特长
- **motivation**: 角色的核心动机和目标
- **arc**: 角色的成长弧线或发展方向
- **importanceLevel**: 重要性级别1-10（主角10，反派9，重要配角6-8，次要角色3-5）
- **isGlobalProtagonist**: 是否是贯穿全书的核心主角（通常只有主角为true）

## 响应格式
返回JSON数组，每个角色包含上述所有字段。确保JSON格式正确，可直接解析。

示例：
[
  {
    "name": "张三",
    "roleType": "SUPPORTING",
    "gender": "MALE",
    "age": 28,
    "personality": "...",
    "background": "...",
    "appearance": "...",
    "abilities": "...",
    "motivation": "...",
    "arc": "...",
    "importanceLevel": 7,
    "isGlobalProtagonist": false
  }
]
""";
    }
    
    private String buildCharacterRecommendationUserPrompt(Novel novel, List<Character> existingCharacters, 
                                                          List<WorldSetting> worldSettings, List<Chapter> recentChapters, int count) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== 小说基本信息 ===\n");
        sb.append("书名：").append(novel.getTitle()).append("\n");
        sb.append("类型：").append(novel.getGenre() != null ? novel.getGenre() : "未指定").append("\n");
        sb.append("简介：").append(novel.getDescription() != null ? novel.getDescription() : "无").append("\n");
        sb.append("创作风格：").append(novel.getWritingStyle() != null ? novel.getWritingStyle() : "未指定").append("\n");
        sb.append("目标读者：").append(novel.getTargetAudience() != null ? novel.getTargetAudience() : "未指定").append("\n\n");
        
        // 添加世界观设定
        if (!worldSettings.isEmpty()) {
            sb.append("=== 世界观设定 ===\n");
            for (WorldSetting ws : worldSettings) {
                sb.append("【").append(ws.getCategory()).append("】").append(ws.getName()).append("\n");
                if (ws.getDescription() != null) {
                    String desc = ws.getDescription();
                    sb.append("  ").append(desc.length() > 200 ? desc.substring(0, 200) + "..." : desc).append("\n");
                }
                if (ws.getRules() != null && !ws.getRules().isEmpty()) {
                    sb.append("  规则：").append(ws.getRules().length() > 100 ? ws.getRules().substring(0, 100) + "..." : ws.getRules()).append("\n");
                }
            }
            sb.append("\n");
        }
        
        if (!existingCharacters.isEmpty()) {
            sb.append("=== 已有角色 ===\n");
            for (Character c : existingCharacters) {
                sb.append("- ").append(c.getName())
                  .append("（").append(translateRoleType(c.getRoleType())).append("）\n");
                if (c.getPersonality() != null) {
                    sb.append("  性格：").append(c.getPersonality().length() > 50 ? 
                        c.getPersonality().substring(0, 50) + "..." : c.getPersonality()).append("\n");
                }
            }
            sb.append("\n");
        } else {
            sb.append("=== 当前状态 ===\n");
            sb.append("这是一个全新的小说项目，还没有创建任何角色。请根据书名和类型，推荐适合的初始角色。\n\n");
        }
        
        // 添加最近章节内容参考
        if (!recentChapters.isEmpty()) {
            sb.append("=== 故事当前进展 ===\n");
            for (Chapter chapter : recentChapters) {
                sb.append("第").append(chapter.getChapterNumber()).append("章：").append(chapter.getTitle()).append("\n");
                if (chapter.getContent() != null) {
                    String content = chapter.getContent();
                    // 取前300字作为参考
                    String preview = content.length() > 300 ? content.substring(0, 300) + "..." : content;
                    sb.append("  内容片段：").append(preview).append("\n");
                }
            }
            sb.append("\n");
        }
        
        sb.append("=== 推荐需求 ===\n");
        sb.append("请推荐 ").append(count).append(" 个新角色，确保：\n");
        sb.append("1. 角色符合已建立的世界观设定和规则\n");
        sb.append("2. 角色与故事当前进展自然衔接，避免突兀感\n");
        sb.append("3. 角色之间有明确的关系和互动潜力\n");
        sb.append("4. 角色设定详细完整，可以直接使用\n");
        sb.append("5. 角色符合小说类型和风格\n");
        sb.append("6. 如果已有角色，新角色应与其形成合理的关系网络\n");
        
        return sb.toString();
    }
    
    /**
     * 获取重要性排序值
     */
    private int getImportanceOrder(String importance) {
        if (importance == null) return 3;
        return switch (importance.toLowerCase()) {
            case "high" -> 1;
            case "medium" -> 2;
            case "low" -> 3;
            default -> 3;
        };
    }
    
    private String translateRoleType(String roleType) {
        if (roleType == null) return "未知";
        return switch (roleType) {
            case "PROTAGONIST" -> "主角";
            case "ANTAGONIST" -> "反派";
            case "SUPPORTING" -> "配角";
            case "MINOR" -> "次要角色";
            default -> roleType;
        };
    }
    
    private List<CharacterRecommendationResponse> generateRandomCharacterRecommendations(int count) {
        List<CharacterRecommendationResponse> recommendations = new ArrayList<>();
        
        String[] names = {"李明", "王芳", "张伟", "刘婷", "陈强", "赵敏", "孙杰", "周慧"};
        String[] roleTypes = {"PROTAGONIST", "SUPPORTING", "ANTAGONIST", "MINOR"};
        String[] genders = {"MALE", "FEMALE"};
        
        for (int i = 0; i < Math.min(count, 3); i++) {
            CharacterRecommendationResponse rec = new CharacterRecommendationResponse();
            rec.setName(names[i % names.length]);
            rec.setRoleType(roleTypes[i % roleTypes.length]);
            rec.setGender(genders[i % 2]);
            rec.setAge(20 + i * 5);
            rec.setPersonality("性格待定，请根据故事需要进行详细设计");
            rec.setBackground("背景待定，请根据故事需要进行详细设计");
            rec.setAppearance("外貌待定，请根据故事需要进行详细设计");
            rec.setAbilities("能力待定，请根据故事需要进行详细设计");
            rec.setMotivation("动机待定，请根据故事需要进行详细设计");
            rec.setArc("发展待定，请根据故事需要进行详细设计");
            rec.setImportanceLevel(5);
            rec.setIsGlobalProtagonist(false);
            recommendations.add(rec);
        }
        
        return recommendations;
    }
}
