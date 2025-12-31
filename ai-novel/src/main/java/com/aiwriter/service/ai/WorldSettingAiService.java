package com.aiwriter.service.ai;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.entity.Novel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 世界观设定AI辅助服务
 * 通过AI生成创意世界观建议
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorldSettingAiService {

    private final AiService aiService;
    private final ObjectMapper objectMapper;

    /**
     * 为小说生成世界观建议
     */
    public Map<String, Object> generateWorldSettingRecommendation(Novel novel) {
        log.info("为小说 '{}' 生成世界观建议", novel.getTitle());

        try {
            String prompt = buildWorldSettingPrompt(novel);
            String aiResponse = aiService.chat("你是一个专业的小说世界观设计师", prompt);
            return parseWorldSettingResponse(aiResponse);
        } catch (Exception e) {
            log.error("AI生成世界观建议失败", e);
            return getDefaultWorldSetting();
        }
    }

    /**
     * 构建世界观提示词
     */
    private String buildWorldSettingPrompt(Novel novel) {
        return String.format(
                """
                        根据以下小说信息，为其设计一个完整的世界观体系。请以JSON格式返回，包含以下内容：
                        
                        小说标题: %s
                        小说类型: %s
                        小说简介: %s
                        创作风格: %s
                        
                        请生成包含以下字段的JSON响应（每个字段都应该是具体的、创意的内容）：
                        {
                          "cosmicBackground": "世界的宇宙背景和起源故事（50-100字）",
                          "mainGeographies": [
                            {
                              "name": "地名",
                              "type": "地理类型（大陆/国家/城市等）",
                              "terrain": "地形特征",
                              "climate": "气候",
                              "specialFeatures": "特色或重要资源"
                            }
                          ],
                          "timePeriods": [
                            {
                              "name": "时代名称",
                              "periodType": "时代类型",
                              "description": "时代特征描述",
                              "keyEvents": "重要历史事件"
                            }
                          ],
                          "races": [
                            {
                              "name": "种族名称",
                              "category": "种族分类",
                              "characteristics": "物理特征和性格特点",
                              "abilities": "特殊能力",
                              "socialStatus": "在世界中的社会地位"
                            }
                          ],
                          "magicSystem": {
                            "name": "魔法系统名称",
                            "type": "系统类型（魔法/科技/修仙等）",
                            "coreRules": "核心规则（50-100字）",
                            "powerLevels": "能力等级划分",
                            "limitations": "使用限制"
                          }
                        }
                        """,
                novel.getTitle(),
                novel.getGenre(),
                novel.getDescription(),
                novel.getWritingStyle()
        );
    }

    /**
     * 解析AI响应
     */
    private Map<String, Object> parseWorldSettingResponse(String aiResponse) {
        try {
            Map<String, Object> result = objectMapper.readValue(aiResponse, Map.class);
            log.info("成功解析AI世界观建议");
            return result;
        } catch (Exception e) {
            log.warn("解析AI响应失败，使用默认建议: {}", e.getMessage());
            return getDefaultWorldSetting();
        }
    }

    /**
     * 默认的世界观设定
     */
    private Map<String, Object> getDefaultWorldSetting() {
        Map<String, Object> result = new HashMap<>();

        result.put("cosmicBackground", "一个充满奥秘的平行世界，神秘力量与自然和谐共存");

        Map<String, Object> geography1 = new HashMap<>();
        geography1.put("name", "中央大陆");
        geography1.put("type", "大陆");
        geography1.put("terrain", "多样地形（平原、山脉、森林）");
        geography1.put("climate", "温带气候");
        geography1.put("specialFeatures", "丰富的元素矿产和魔法资源");
        result.put("mainGeography", geography1);

        Map<String, Object> timePeriod = new HashMap<>();
        timePeriod.put("name", "第二纪元");
        timePeriod.put("periodType", "中世纪奇幻");
        timePeriod.put("description", "文明快速发展的时代");
        timePeriod.put("keyEvents", "古老帝国的衰落与新势力的兴起");
        result.put("timePeriod", timePeriod);

        Map<String, Object> magicSystem = new HashMap<>();
        magicSystem.put("name", "元素魔法系统");
        magicSystem.put("type", "魔法");
        magicSystem.put("coreRules", "通过吸收天地灵气，凝聚成不同属性的魔法力量");
        magicSystem.put("powerLevels", "学徒->正式法师->高阶法师->大法师->魔法之主");
        magicSystem.put("limitations", "消耗大量精神力，过度使用会导致昏迷");
        result.put("magicSystem", magicSystem);

        return result;
    }

    /**
     * 生成魔法系统的详细规则
     */
    public Map<String, Object> generateMagicSystemDetails(String systemType, String worldBackground) {
        log.info("生成 {} 系统的详细规则", systemType);

        String prompt = String.format(
                """
                        基于以下背景，为 '%s' 类型的系统生成详细规则：
                        
                        世界背景: %s
                        
                        请返回JSON格式的详细规则，包括：
                        {
                          "powerLevels": ["等级1", "等级2", ...],
                          "cultivationMethods": "修炼方法详解",
                          "limitations": "系统限制和禁忌",
                          "resourceRequirements": "所需资源"
                        }
                        """,
                systemType,
                worldBackground
        );

        try {
            String aiResponse = aiService.chat("你是一个专业的魔法系统设计师", prompt);
            return objectMapper.readValue(aiResponse, Map.class);
        } catch (Exception e) {
            log.error("生成魔法系统详情失败", e);
            return getDefaultMagicSystemDetails();
        }
    }

    private Map<String, Object> getDefaultMagicSystemDetails() {
        Map<String, Object> result = new HashMap<>();
        result.put("powerLevels", new String[]{"初学者", "正式成员", "高阶", "大师", "传奇"});
        result.put("cultivationMethods", "通过日常修炼积累经验和力量");
        result.put("limitations", "每天有修炼上限，超出会损害身体");
        result.put("resourceRequirements", "需要特殊的修炼资源和环境");
        return result;
    }
}
