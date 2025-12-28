package com.ai.novel.service.ai;

import org.springframework.stereotype.Component;

/**
 * 提示词管理器
 * 管理所有AI提示词模板
 */
@Component
public class PromptManager {

    /**
     * 章节续写提示词模板
     */
    public static final String CHAPTER_CONTINUE_TEMPLATE = """
            你是一位专业小说作家,正在续写《%s》的第%d章。
            
            %s
            
            【写作风格】
            %s
            
            【续写要求】
            - 续写方向: %s
            - 目标字数: %d字
            - 章节号: 第%d章(全书)
            %s
            
            【写作要点】
            1. 严格保持上述写作风格
            2. 确保与已有情节连贯
            3. 合理展现人物状态变化
            4. 适时推进或埋下伏笔
            5. 遵循世界观设定
            %s
            
            请开始续写,只输出章节正文内容,不要添加章节标题和其他说明:
            """;

    /**
     * 内容分析提示词模板
     */
    public static final String CONTENT_ANALYSIS_TEMPLATE = """
            请分析以下章节内容,提取关键信息:
            
            【章节内容】
            %s
            
            【已有角色】
            %s
            
            【已有伏笔】
            %s
            
            请以JSON格式输出分析结果:
            {
              "chapter_summary": "章节摘要(100-150字)",
              "character_updates": [
                {
                  "character_name": "角色名",
                  "status_change": "状态变化描述",
                  "emotional_state": "情感状态",
                  "physical_state": "身体状态",
                  "relationship_changes": "关系变化"
                }
              ],
              "new_characters": [
                {
                  "name": "新角色名",
                  "description": "简要描述",
                  "importance": "main/secondary/minor"
                }
              ],
              "new_plot_threads": [
                {
                  "title": "伏笔标题",
                  "description": "伏笔描述",
                  "importance": "major/minor",
                  "suggested_reveal_chapter": 相对当前章节的建议揭示章节数
                }
              ],
              "revealed_plot_threads": [
                {
                  "thread_title": "已揭示的伏笔标题",
                  "how_revealed": "揭示方式"
                }
              ],
              "new_world_settings": [
                {
                  "category": "设定类别",
                  "name": "设定名称",
                  "description": "详细描述",
                  "rules": "相关规则"
                }
              ]
            }
            """;

    /**
     * 续写方向建议提示词模板
     */
    public static final String WRITING_DIRECTION_SUGGESTION_TEMPLATE = """
            基于以下小说信息,生成%d个符合逻辑的续写方向建议。
            
            小说标题: %s
            故事大纲: %s
            当前进度: 已完成%d章
            
            最近章节摘要:
            %s
            
            主要人物状态:
            %s
            
            待处理伏笔:
            %s
            
            世界观设定:
            %s
            
            写作风格特点:
            %s
            
            请生成%d个续写方向建议,每个建议要包含:
            1. 方向描述: 简洁清晰地描述这个方向的主要内容(50-100字)
            2. 影响分析: 选择这个方向对整体故事的影响(100-150字)
            3. 关键转折点: 列出2-3个关键转折点
            4. 人物关系变化: 可能涉及的人物关系变化
            5. 情节冲突: 可能引发的主要冲突
            6. 难度等级: 简单/中等/复杂
            
            建议要求:
            - 符合现有情节逻辑
            - 与人物性格设定一致
            - 考虑待处理伏笔的展开
            - 保持风格连贯性
            - 提供多样化的发展路径(如: 推进主线、展开支线、人物成长、世界观扩展等)
            
            请以JSON格式输出,格式如下:
            {
              "suggestions": [
                {
                  "direction": "续写方向描述",
                  "analysis": "影响分析",
                  "key_points": ["转折点1", "转折点2", "转折点3"],
                  "character_impact": "人物关系变化描述",
                  "plot_conflicts": "情节冲突描述",
                  "difficulty": "中等"
                }
              ]
            }
            """;

    /**
     * 场景建议提示词模板
     */
    public static final String SCENE_SUGGESTION_TEMPLATE = """
            基于以下小说信息,推荐%d个适合的场景设定:
            
            小说标题: %s
            故事大纲: %s
            当前进度: 已完成%d章,%d个场景
            
            已有场景:
            %s
            
            主要角色:
            %s
            
            请推荐%d个新场景,每个场景包含:
            1. 场景标题
            2. 场景大纲(200-300字)
            3. 氛围设定
            4. 主要冲突
            5. 关键事件列表
            6. 预计章节数
            
            以JSON格式输出:
            {
              "scenes": [
                {
                  "title": "场景标题",
                  "outline": "场景大纲",
                  "atmosphere": "氛围",
                  "main_conflict": "主要冲突",
                  "key_events": ["事件1", "事件2"],
                  "target_chapters": 预计章节数
                }
              ]
            }
            """;

    /**
     * 风格提取提示词
     */
    public static final String STYLE_EXTRACTION_TEMPLATE = """
            请分析以下小说样本文本,提取作者的写作风格特征:
            
            【样本文本】
            %s
            
            请从以下维度分析写作风格:
            1. 叙述视角: 第一人称/第三人称全知/第三人称限知等
            2. 语言风格: 简洁/华丽/诗意/口语化等
            3. 节奏特点: 快节奏/慢节奏/张弛有度等
            4. 描写偏好: 环境描写/心理描写/动作描写/对话等的比重
            5. 修辞手法: 常用的修辞手法
            6. 句式特点: 长句/短句/句式变化等
            7. 情感基调: 轻松/严肃/幽默/悲伤等
            
            以JSON格式输出:
            {
              "narrative_perspective": "叙述视角",
              "language_style": "语言风格描述",
              "pacing": "节奏特点",
              "description_focus": {
                "environment": 比重(0-100),
                "psychology": 比重(0-100),
                "action": 比重(0-100),
                "dialogue": 比重(0-100)
              },
              "rhetoric_devices": ["修辞手法1", "修辞手法2"],
              "sentence_patterns": "句式特点",
              "emotional_tone": "情感基调",
              "style_summary": "风格总结(100字)"
            }
            """;

    /**
     * 构建章节续写提示词
     */
    public String buildChapterContinuePrompt(
            String novelTitle,
            int chapterNumber,
            String contextInfo,
            String styleInfo,
            String writingDirection,
            int targetWordCount,
            String sceneInfo,
            String additionalPoints
    ) {
        String sceneInfoStr = sceneInfo != null && !sceneInfo.isEmpty() 
            ? "- 场景内容: " + sceneInfo 
            : "";
        
        String additionalPointsStr = additionalPoints != null && !additionalPoints.isEmpty()
            ? additionalPoints
            : "";

        return String.format(
            CHAPTER_CONTINUE_TEMPLATE,
            novelTitle,
            chapterNumber,
            contextInfo,
            styleInfo,
            writingDirection,
            targetWordCount,
            chapterNumber,
            sceneInfoStr,
            additionalPointsStr
        );
    }
}
