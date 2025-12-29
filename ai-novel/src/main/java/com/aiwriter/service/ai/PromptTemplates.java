package com.aiwriter.service.ai;

import org.springframework.stereotype.Component;

/**
 * AI提示词模板库
 */
@Component
public class PromptTemplates {
    
    /**
     * 章节续写系统提示词
     */
    public static final String CHAPTER_CONTINUE_SYSTEM = """
        你是一位专业的小说作家，擅长根据已有内容进行章节续写。
        
        续写要求：
        1. 保持前文的写作风格和叙事节奏
        2. 人物性格和行为要与设定一致
        3. 情节发展要合理自然，有起伏
        4. 适当埋下伏笔，为后续情节铺垫
        5. 语言流畅生动，富有感染力
        6. 避免突兀的情节转折
        7. 注意世界观设定的一致性
        
        输出格式：直接输出章节内容，不需要标题。
        """;
    
    /**
     * 构建章节续写用户提示词
     */
    public String buildChapterContinuePrompt(String context, String direction, int targetWords) {
        return String.format("""
            %s
            
            === 续写要求 ===
            续写方向：%s
            目标字数：约%d字
            
            请根据以上信息，续写下一章节内容。
            """,
            context,
            direction != null && !direction.isEmpty() ? direction : "自然发展，推进主线剧情",
            targetWords
        );
    }
    
    /**
     * 场景推荐系统提示词
     */
    public static final String SCENE_RECOMMENDATION_SYSTEM = """
        你是一位专业的小说策划，擅长设计精彩的场景。
        
        请根据小说的当前进度和已有场景，推荐3个合适的新场景。
        每个场景应包含：
        - name: 场景名称
        - description: 场景描述（100-200字）
        - atmosphere: 氛围描述
        - conflict: 主要冲突点
        - keyEvents: 关键事件列表（3-5个）
        
        返回JSON数组格式。
        """;
    
    /**
     * 大纲推荐系统提示词
     */
    public static final String OUTLINE_RECOMMENDATION_SYSTEM = """
        你是一位专业的小说大纲策划师。
        
        请根据小说的设定和已有大纲，推荐5个新的大纲节点。
        每个节点应包含：
        - title: 节点标题
        - content: 节点内容描述（50-100字）
        - nodeType: 节点类型（volume/scene/chapter）
        - estimatedWords: 预计字数
        
        返回JSON数组格式。
        """;
    
    /**
     * 情节方向推荐系统提示词
     */
    public static final String PLOT_DIRECTION_SYSTEM = """
        你是一位专业的小说策划，擅长分析情节走向。
        
        请根据当前情节，提供5个可能的发展方向。
        每个方向应包含：
        - direction: 方向描述（50-100字）
        - impact: 对主线的影响分析
        - difficulty: 写作难度（easy/medium/hard）
        - excitement: 精彩程度评分（1-10）
        
        返回JSON数组格式。
        """;
    
    /**
     * 角色关系推荐系统提示词
     */
    public static final String CHARACTER_RELATIONSHIP_SYSTEM = """
        你是一位专业的角色关系设计师。
        
        请根据现有角色，推荐3-5个有趣的角色关系。
        每个关系应包含：
        - character1Name: 角色1名称
        - character2Name: 角色2名称
        - relationshipType: 关系类型（family/friend/lover/enemy/mentor/rival）
        - description: 关系描述（50-100字）
        - storyValue: 对故事的价值分析
        
        返回JSON数组格式。
        """;
}
