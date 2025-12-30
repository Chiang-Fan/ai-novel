package com.aiwriter.service.ai;

import com.aiwriter.dto.NovelCreationRecommendationResponse;
import com.aiwriter.dto.NovelCreateRequest;
import com.aiwriter.entity.NovelCreationRecommendation;
import com.aiwriter.repository.NovelCreationRecommendationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 小说创建AI推荐服务
 * 生成大纲和初始场景的智能推荐
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NovelCreationAiRecommendationService {
    
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final NovelCreationRecommendationRepository recommendationRepository;
    
    /**
     * 生成小说创建推荐
     * 
     * @param request 小说创建请求
     * @return 推荐响应
     */
    public NovelCreationRecommendationResponse generateRecommendations(NovelCreateRequest request) {
        try {
            // 构建Prompt
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(request);
            
            // 调用AI获取推荐
            String aiResponse = aiService.chatJson(systemPrompt, userPrompt);
            
            // 解析JSON响应
            NovelCreationRecommendationResponse recommendation = 
                objectMapper.readValue(aiResponse, NovelCreationRecommendationResponse.class);
            
            log.info("成功生成小说创建推荐: title={}", request.getTitle());
            return recommendation;
            
        } catch (Exception e) {
            log.error("生成小说创建推荐失败", e);
            // 返回默认推荐
            return buildDefaultRecommendation(request);
        }
    }
    
    /**
     * 保存推荐到数据库
     * 
     * @param novelId 小说ID
     * @param recommendation 推荐数据
     */
    public void saveRecommendation(Long novelId, NovelCreationRecommendationResponse recommendation) {
        try {
            NovelCreationRecommendation entity = NovelCreationRecommendation.builder()
                .novelId(novelId)
                .storyFramework(recommendation.getStoryFramework())
                .threeActStructure(objectMapper.writeValueAsString(recommendation.getThreeActStructure()))
                .mainPlotPoints(objectMapper.writeValueAsString(recommendation.getMainPlotPoints()))
                .initialSceneRecommendation(objectMapper.writeValueAsString(recommendation.getInitialScene()))
                .characterRecommendations(objectMapper.writeValueAsString(recommendation.getCharacters()))
                .themes(objectMapper.writeValueAsString(recommendation.getThemes()))
                .styleElements(objectMapper.writeValueAsString(recommendation.getStyleElements()))
                .wordCountRange(objectMapper.writeValueAsString(recommendation.getWordCountRange()))
                .estimatedReadingTime(recommendation.getEstimatedReadingTime())
                .build();
            
            recommendationRepository.save(entity);
            log.info("保存推荐成功: novelId={}", novelId);
        } catch (Exception e) {
            log.error("保存推荐失败: novelId={}", novelId, e);
        }
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return """
            你是一位经验丰富的小说创作顾问。你的任务是为小说作者生成创意推荐。
            
            你需要提供以下结构化的推荐：
            1. 故事框架 - 说明故事的基本架构
            2. 三幕结构 - Setup（铺垫）、Confrontation（冲突）、Resolution（解决）
            3. 主要情节点 - 列出5-7个关键情节点
            4. 初始场景推荐 - 包括设定、氛围、关键要素、开场钩子和建议字数
            5. 角色推荐 - 主要角色的基本设定
            6. 主题和中心思想 - 故事想要传达的主要主题
            7. 写作风格要素 - 建议的描写手法和文风
            8. 字数范围 - 预估的总字数和章节数
            9. 预估阅读时长
            
            所有响应必须以JSON格式返回，字段名必须与以下类对应。
            """;
    }
    
    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(NovelCreateRequest request) {
        return String.format("""
            请为以下小说生成创作推荐：
            
            标题：%s
            类型：%s
            目标读者：%s
            简介：%s
            写作风格：%s
            
            请根据这些信息生成详细的创作推荐。
            """,
            request.getTitle(),
            request.getGenre() != null ? request.getGenre() : "未指定",
            request.getTargetAudience() != null ? request.getTargetAudience() : "未指定",
            request.getDescription() != null ? request.getDescription() : "未提供",
            request.getWritingStyle() != null ? request.getWritingStyle() : "未指定"
        );
    }
    
    /**
     * 构建默认推荐（当AI调用失败时）
     */
    private NovelCreationRecommendationResponse buildDefaultRecommendation(NovelCreateRequest request) {
        return NovelCreationRecommendationResponse.builder()
            .storyFramework("经典三幕结构故事框架")
            .threeActStructure(NovelCreationRecommendationResponse.ThreeActStructure.builder()
                .setup("介绍主角、世界观和基本冲突")
                .confrontation("主角面临一系列挑战和障碍，推动情节发展")
                .resolution("主角解决核心冲突，故事达成高潮和结尾")
                .build())
            .mainPlotPoints(Arrays.asList(
                "故事开端：引入主角和设定",
                "第一转折：改变局面的事件",
                "中点：故事复杂化",
                "第二转折：走向高潮的关键点",
                "高潮：主要冲突的顶点",
                "结局：故事的解决与反思"
            ))
            .initialScene(NovelCreationRecommendationResponse.InitialSceneRecommendation.builder()
                .setting("清晰的时间和地点设定")
                .atmosphere("营造引人入胜的氛围")
                .keyElements(Arrays.asList("主角介绍", "环境描写", "冲突暗示"))
                .suggestedOpeningHook("使用悬念或有趣的细节吸引读者")
                .suggestedWordCount(800)
                .build())
            .characters(Arrays.asList(
                NovelCreationRecommendationResponse.CharacterRecommendation.builder()
                    .name("主角")
                    .role("故事的中心")
                    .personalityTraits("定义明确的性格特征")
                    .motivation("清晰的目标和动机")
                    .arcSummary("角色从开始到结束的成长")
                    .build()
            ))
            .themes(Arrays.asList("勇气", "成长", "自我发现"))
            .styleElements(Arrays.asList("细节描写", "心理刻画", "对话互动", "环境烘托"))
            .wordCountRange(NovelCreationRecommendationResponse.WordCountRange.builder()
                .minWords(50000)
                .maxWords(200000)
                .recommendedChapters(30)
                .build())
            .estimatedReadingTime(480)
            .build();
    }
}
