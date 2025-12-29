package com.aiwriter.service;

import com.aiwriter.dto.OutlineRecommendationRequest;
import com.aiwriter.dto.OutlineRecommendationResponse;
import com.aiwriter.entity.Character;
import com.aiwriter.entity.Novel;
import com.aiwriter.entity.Outline;
import com.aiwriter.repository.OutlineRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class OutlineService {
    
    private final OutlineRepository outlineRepository;
    private final NovelService novelService;
    private final CharacterService characterService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    
    @Transactional(readOnly = true)
    public List<Outline> getOutlinesByNovel(Long novelId) {
        return outlineRepository.findByNovelIdOrderBySequenceNumberAsc(novelId);
    }
    
    @Transactional(readOnly = true)
    public List<Outline> getRootOutlines(Long novelId) {
        return outlineRepository.findByNovelIdAndParentIdIsNullOrderBySequenceNumberAsc(novelId);
    }
    
    @Transactional(readOnly = true)
    public List<Outline> getChildOutlines(Long novelId, Long parentId) {
        return outlineRepository.findByNovelIdAndParentIdOrderBySequenceNumberAsc(novelId, parentId);
    }
    
    @Transactional(readOnly = true)
    public Outline getOutlineById(Long id) {
        return outlineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("大纲节点不存在"));
    }
    
    @Transactional
    public Outline createOutline(Outline outline) {
        Outline saved = outlineRepository.save(outline);
        log.info("创建大纲节点成功: {} (小说ID: {})", saved.getTitle(), saved.getNovelId());
        return saved;
    }
    
    @Transactional
    public Outline updateOutline(Long id, Outline outline) {
        Outline existing = getOutlineById(id);
        
        // 更新字段
        existing.setParentId(outline.getParentId());
        existing.setNodeType(outline.getNodeType());
        existing.setSequenceNumber(outline.getSequenceNumber());
        existing.setTitle(outline.getTitle());
        existing.setSummary(outline.getSummary());
        existing.setTargetWordCount(outline.getTargetWordCount());
        existing.setKeyEvents(outline.getKeyEvents());
        existing.setCharacterFocus(outline.getCharacterFocus());
        existing.setPlotPoints(outline.getPlotPoints());
        existing.setThemes(outline.getThemes());
        existing.setStatus(outline.getStatus());
        existing.setChapterId(outline.getChapterId());
        existing.setNotes(outline.getNotes());
        
        Outline updated = outlineRepository.save(existing);
        log.info("更新大纲节点成功: {}", updated.getTitle());
        return updated;
    }
    
    @Transactional
    public void deleteOutline(Long id) {
        outlineRepository.deleteById(id);
        log.info("删除大纲节点成功: {}", id);
    }
    
    /**
     * AI智能推荐大纲节点
     */
    public List<OutlineRecommendationResponse> recommendOutlines(OutlineRecommendationRequest request) {
        try {
            Novel novel = novelService.getNovel(request.getNovelId());
            List<Outline> existingOutlines = getOutlinesByNovel(request.getNovelId());
            List<Character> characters = characterService.getCharactersByNovel(request.getNovelId());
            
            Outline parentOutline = null;
            if (request.getParentId() != null) {
                parentOutline = getOutlineById(request.getParentId());
            }
            
            String systemPrompt = buildOutlineRecommendationSystemPrompt();
            String userPrompt = buildOutlineRecommendationUserPrompt(novel, existingOutlines, 
                                                                      characters, parentOutline, 
                                                                      request.getNodeType(), request.getCount());
            
            String aiResult = aiService.chatJson(systemPrompt, userPrompt);
            log.info("AI大纲推荐原始结果: {}", aiResult);
            
            // 解析JSON结果
            List<OutlineRecommendationResponse> recommendations = objectMapper.readValue(
                aiResult, 
                new TypeReference<List<OutlineRecommendationResponse>>() {}
            );
            
            log.info("成功生成{}个大纲推荐", recommendations.size());
            return recommendations;
            
        } catch (Exception e) {
            log.error("AI大纲推荐失败", e);
            return generateRandomOutlineRecommendations(request.getCount(), request.getNodeType());
        }
    }
    
    private String buildOutlineRecommendationSystemPrompt() {
        return """
你是一位专业的小说大纲规划专家，擅长根据小说类型、已有大纲和角色设计合理的情节结构。

## 核心任务
根据小说的基本信息、已有大纲和角色，推荐适合的新大纲节点。新节点应该：
1. 符合小说的类型、风格和整体叙事结构
2. 与已有大纲形成合理的逻辑和进展关系
3. 包含具体的情节要素和角色安排
4. 推动故事发展，制造冲突和高潮

## 大纲节点类型
- **ARC**: 故事弧，大型情节单元，通常包含多个卷或章节
- **VOLUME**: 卷，中型情节单元，包含多个章节
- **CHAPTER**: 章，基本叙事单元
- **SECTION**: 节，章节内的细分单元

## 大纲设计要点
- **nodeType**: ARC/VOLUME/CHAPTER/SECTION（根据请求指定）
- **title**: 节点标题，简洁有力
- **summary**: 内容概要，至少100字，描述主要情节和发展
- **targetWordCount**: 目标字数（章节通常3000-5000字，卷可累加）
- **keyEvents**: 关键事件列表，数组格式，每个事件简短描述
- **plotPoints**: 情节点列表，数组格式，标注重要转折
- **themes**: 主题标签列表，数组格式，如["成长"、"友情"、"复仇"]

## 响应格式
返回JSON数组，每个节点包含上述所有字段。确保JSON格式正确，可直接解析。

示例：
[
  {
    "nodeType": "CHAPTER",
    "title": "初遇",
    "summary": "...",
    "targetWordCount": 3500,
    "keyEvents": ["主角来到新城市", "偶遇女主角", "发生小冲突"],
    "plotPoints": ["建立基本设定", "引入核心矛盾"],
    "themes": ["陌生", "好奇", "误会"]
  }
]
""";
    }
    
    private String buildOutlineRecommendationUserPrompt(Novel novel, List<Outline> existingOutlines, 
                                                        List<Character> characters, Outline parentOutline,
                                                        String nodeType, int count) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== 小说基本信息 ===\n");
        sb.append("书名：").append(novel.getTitle()).append("\n");
        sb.append("类型：").append(novel.getGenre() != null ? novel.getGenre() : "未指定").append("\n");
        sb.append("简介：").append(novel.getDescription() != null ? novel.getDescription() : "无").append("\n");
        sb.append("创作风格：").append(novel.getWritingStyle() != null ? novel.getWritingStyle() : "未指定").append("\n\n");
        
        if (!characters.isEmpty()) {
            sb.append("=== 主要角色 ===\n");
            for (Character c : characters) {
                sb.append("- ").append(c.getName())
                  .append("（").append(translateRoleType(c.getRoleType())).append("）\n");
            }
            sb.append("\n");
        }
        
        if (parentOutline != null) {
            sb.append("=== 父节点信息 ===\n");
            sb.append("标题：").append(parentOutline.getTitle()).append("\n");
            sb.append("类型：").append(translateNodeType(parentOutline.getNodeType())).append("\n");
            if (parentOutline.getSummary() != null) {
                sb.append("概要：").append(parentOutline.getSummary()).append("\n");
            }
            sb.append("\n");
        }
        
        if (!existingOutlines.isEmpty()) {
            sb.append("=== 已有大纲 ===\n");
            for (Outline o : existingOutlines) {
                sb.append(o.getSequenceNumber()).append(". ")
                  .append(o.getTitle())
                  .append("（").append(translateNodeType(o.getNodeType())).append("）\n");
            }
            sb.append("\n");
        } else {
            sb.append("=== 当前状态 ===\n");
            sb.append("这是一个全新的小说项目，还没有创建任何大纲。请根据书名和类型，推荐适合的初始大纲结构。\n\n");
        }
        
        sb.append("=== 推荐需求 ===\n");
        sb.append("节点类型：").append(translateNodeType(nodeType)).append("\n");
        sb.append("推荐数量：").append(count).append(" 个\n\n");
        sb.append("请确保：\n");
        sb.append("1. 大纲节点之间逻辑连贯，情节递进合理\n");
        sb.append("2. 充分利用已有角色，安排合适的角色活动\n");
        sb.append("3. 包含冲突、转折和高潮等关键情节要素\n");
        sb.append("4. 符合小说类型的典型叙事结构\n");
        
        return sb.toString();
    }
    
    private String translateNodeType(String nodeType) {
        if (nodeType == null) return "未知";
        return switch (nodeType) {
            case "ARC" -> "故事弧";
            case "VOLUME" -> "卷";
            case "CHAPTER" -> "章";
            case "SECTION" -> "节";
            default -> nodeType;
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
    
    private List<OutlineRecommendationResponse> generateRandomOutlineRecommendations(int count, String nodeType) {
        List<OutlineRecommendationResponse> recommendations = new ArrayList<>();
        
        String[] titles = {"开篇", "发展", "高潮", "结局"};
        
        for (int i = 0; i < Math.min(count, 3); i++) {
            OutlineRecommendationResponse rec = new OutlineRecommendationResponse();
            rec.setNodeType(nodeType != null ? nodeType : "CHAPTER");
            rec.setTitle(i < titles.length ? titles[i] : "章节" + (i + 1));
            rec.setSummary("大纲概要待定，请根据故事需要进行详细设计");
            rec.setTargetWordCount(3000);
            rec.setKeyEvents(Arrays.asList("关键事件1", "关键事件2"));
            rec.setPlotPoints(Arrays.asList("情节点1", "情节点2"));
            rec.setThemes(Arrays.asList("主题1", "主题2"));
            recommendations.add(rec);
        }
        
        return recommendations;
    }
}
