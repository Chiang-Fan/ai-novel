package com.aiwriter.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 描写风格模板服务 - Phase 6
 * 提供多种预设风格模板和自定义模板管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DescriptionStyleTemplateService {

    /**
     * 风格模板
     */
    @Data
    public static class StyleTemplate {
        private String id;
        private String name;
        private String description;
        private String category;
        private Map<String, Object> parameters;
        private List<String> exampleTexts;
        private String promptTemplate;
        private Boolean isBuiltIn;
        private Boolean isActive;

        public StyleTemplate() {
            this.parameters = new HashMap<>();
            this.exampleTexts = new ArrayList<>();
            this.isBuiltIn = false;
            this.isActive = true;
        }
    }

    /**
     * 风格分析结果
     */
    @Data
    public static class StyleAnalysisResult {
        private String dominantStyle;
        private Map<String, Double> styleScores;
        private List<String> characteristics;
        private List<String> suggestions;

        public StyleAnalysisResult() {
            this.styleScores = new HashMap<>();
            this.characteristics = new ArrayList<>();
            this.suggestions = new ArrayList<>();
        }
    }

    // 内置风格模板库
    private final Map<String, StyleTemplate> builtInTemplates = initBuiltInTemplates();

    /**
     * 初始化内置模板
     */
    private Map<String, StyleTemplate> initBuiltInTemplates() {
        Map<String, StyleTemplate> templates = new HashMap<>();

        // 1. 古典文学风格
        StyleTemplate classical = new StyleTemplate();
        classical.setId("classical");
        classical.setName("古典文学");
        classical.setDescription("典雅含蓄，意境深远，善用典故和意象");
        classical.setCategory("文学风格");
        classical.setIsBuiltIn(true);
        classical.setPromptTemplate("""
            请使用古典文学风格进行描写：
            - 语言典雅，用词考究
            - 善用典故和意象
            - 注重意境营造
            - 含蓄内敛，留有余韵
            - 可适当使用文言词汇
            """);
        classical.getExampleTexts().add("月色如水，洒落庭前。远处传来几声犬吠，更显夜的幽静。");
        templates.put("classical", classical);

        // 2. 现代简约风格
        StyleTemplate modernMinimal = new StyleTemplate();
        modernMinimal.setId("modern_minimal");
        modernMinimal.setName("现代简约");
        modernMinimal.setDescription("简洁有力，直击要害，不拖泥带水");
        modernMinimal.setCategory("文学风格");
        modernMinimal.setIsBuiltIn(true);
        modernMinimal.setPromptTemplate("""
            请使用现代简约风格进行描写：
            - 语言简洁，不堆砌辞藻
            - 动词有力，形容词精准
            - 节奏明快，不拖沓
            - 留白恰当，给读者想象空间
            """);
        modernMinimal.getExampleTexts().add("他站起来。门开了。风灌进来。");
        templates.put("modern_minimal", modernMinimal);

        // 3. 华丽浪漫风格
        StyleTemplate romantic = new StyleTemplate();
        romantic.setId("romantic");
        romantic.setName("华丽浪漫");
        romantic.setDescription("辞藻华美，情感充沛，富有诗意");
        romantic.setCategory("文学风格");
        romantic.setIsBuiltIn(true);
        romantic.setPromptTemplate("""
            请使用华丽浪漫风格进行描写：
            - 辞藻华美，修辞丰富
            - 情感充沛，富有感染力
            - 善用比喻、拟人等修辞
            - 色彩鲜明，画面感强
            - 追求语言的美感和韵律
            """);
        romantic.getExampleTexts().add("夕阳如醉，将天边染成绚烂的橘红，云朵仿佛燃烧的火焰，在苍穹中肆意舞动。");
        templates.put("romantic", romantic);

        // 4. 悬疑紧张风格
        StyleTemplate suspense = new StyleTemplate();
        suspense.setId("suspense");
        suspense.setName("悬疑紧张");
        suspense.setDescription("营造紧张氛围，制造悬念，引人入胜");
        suspense.setCategory("类型风格");
        suspense.setIsBuiltIn(true);
        suspense.setPromptTemplate("""
            请使用悬疑紧张风格进行描写：
            - 营造紧张压抑的氛围
            - 善用短句增强节奏感
            - 适当留白，制造悬念
            - 注重细节暗示
            - 控制信息披露节奏
            """);
        suspense.getExampleTexts().add("走廊尽头，有什么东西在动。灯光闪了闪。脚步声停了。");
        templates.put("suspense", suspense);

        // 5. 温馨治愈风格
        StyleTemplate healing = new StyleTemplate();
        healing.setId("healing");
        healing.setName("温馨治愈");
        healing.setDescription("温暖柔和，治愈人心，充满希望");
        healing.setCategory("情感风格");
        healing.setIsBuiltIn(true);
        healing.setPromptTemplate("""
            请使用温馨治愈风格进行描写：
            - 语言温暖柔和
            - 注重生活细节的美好
            - 情感真挚，不矫情
            - 传递希望和温暖
            - 善用暖色调描写
            """);
        healing.getExampleTexts().add("阳光透过窗帘的缝隙，在地板上画出金色的条纹。猫咪蜷缩在光斑里，发出满足的呼噜声。");
        templates.put("healing", healing);

        // 6. 史诗宏大风格
        StyleTemplate epic = new StyleTemplate();
        epic.setId("epic");
        epic.setName("史诗宏大");
        epic.setDescription("气势磅礴，格局宏大，充满力量感");
        epic.setCategory("类型风格");
        epic.setIsBuiltIn(true);
        epic.setPromptTemplate("""
            请使用史诗宏大风格进行描写：
            - 气势磅礴，格局宏大
            - 善用排比、对仗增强气势
            - 注重场面的壮观感
            - 语言庄重有力
            - 适当使用夸张手法
            """);
        epic.getExampleTexts().add("千军万马，铁蹄轰鸣。旌旗蔽日，号角震天。这是一场决定命运的战役。");
        templates.put("epic", epic);

        // 7. 黑色幽默风格
        StyleTemplate darkHumor = new StyleTemplate();
        darkHumor.setId("dark_humor");
        darkHumor.setName("黑色幽默");
        darkHumor.setDescription("讽刺辛辣，荒诞不经，发人深省");
        darkHumor.setCategory("特殊风格");
        darkHumor.setIsBuiltIn(true);
        darkHumor.setPromptTemplate("""
            请使用黑色幽默风格进行描写：
            - 讽刺辛辣但不刻薄
            - 荒诞中见真实
            - 善用反讽和夸张
            - 轻描淡写严肃事物
            - 在悲剧中寻找喜剧元素
            """);
        darkHumor.getExampleTexts().add("他终于找到了人生的意义。可惜，那是在葬礼上。");
        templates.put("dark_humor", darkHumor);

        // 8. 意识流风格
        StyleTemplate streamOfConsciousness = new StyleTemplate();
        streamOfConsciousness.setId("stream_of_consciousness");
        streamOfConsciousness.setName("意识流");
        streamOfConsciousness.setDescription("思绪跳跃，自由联想，打破常规叙事");
        streamOfConsciousness.setCategory("特殊风格");
        streamOfConsciousness.setIsBuiltIn(true);
        streamOfConsciousness.setPromptTemplate("""
            请使用意识流风格进行描写：
            - 模拟真实的思维流动
            - 允许思绪跳跃和联想
            - 打破时间线性叙事
            - 混合感知、记忆、幻想
            - 标点可以非常规使用
            """);
        streamOfConsciousness.getExampleTexts().add("咖啡的香气——母亲的围裙——那个夏天——蝉鸣——他的手——不，不要想这些——");
        templates.put("stream_of_consciousness", streamOfConsciousness);

        return templates;
    }

    /**
     * 获取所有内置模板
     */
    public List<StyleTemplate> getAllBuiltInTemplates() {
        return new ArrayList<>(builtInTemplates.values());
    }

    /**
     * 根据ID获取模板
     */
    public StyleTemplate getTemplateById(String templateId) {
        return builtInTemplates.get(templateId);
    }

    /**
     * 根据分类获取模板
     */
    public List<StyleTemplate> getTemplatesByCategory(String category) {
        return builtInTemplates.values().stream()
                .filter(t -> category.equals(t.getCategory()))
                .toList();
    }

    /**
     * 分析文本风格
     */
    public StyleAnalysisResult analyzeStyle(String text) {
        log.info("分析文本风格");
        StyleAnalysisResult result = new StyleAnalysisResult();

        // 计算各风格得分
        result.getStyleScores().put("classical", calculateClassicalScore(text));
        result.getStyleScores().put("modern_minimal", calculateModernMinimalScore(text));
        result.getStyleScores().put("romantic", calculateRomanticScore(text));
        result.getStyleScores().put("suspense", calculateSuspenseScore(text));
        result.getStyleScores().put("healing", calculateHealingScore(text));

        // 确定主导风格
        String dominant = result.getStyleScores().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("modern_minimal");
        result.setDominantStyle(dominant);

        // 提取特征
        result.setCharacteristics(extractCharacteristics(text));

        // 生成建议
        result.setSuggestions(generateStyleSuggestions(text, result));

        return result;
    }

    /**
     * 根据风格模板生成提示词
     */
    public String generatePromptFromTemplate(String templateId, Map<String, Object> params) {
        StyleTemplate template = builtInTemplates.get(templateId);
        if (template == null) {
            return "";
        }

        StringBuilder prompt = new StringBuilder(template.getPromptTemplate());

        if (params != null && !params.isEmpty()) {
            prompt.append("\n\n=== 具体要求 ===\n");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                prompt.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
        }

        return prompt.toString();
    }

    /**
     * 混合多种风格
     */
    public String blendStyles(List<String> templateIds, Map<String, Double> weights) {
        StringBuilder blendedPrompt = new StringBuilder("请融合以下风格进行描写：\n\n");

        for (String templateId : templateIds) {
            StyleTemplate template = builtInTemplates.get(templateId);
            if (template != null) {
                Double weight = weights.getOrDefault(templateId, 1.0);
                blendedPrompt.append("【").append(template.getName()).append("】")
                        .append("（权重：").append(String.format("%.0f%%", weight * 100)).append("）\n");
                blendedPrompt.append(template.getPromptTemplate()).append("\n");
            }
        }

        return blendedPrompt.toString();
    }

    // ==================== 风格评分方法 ====================

    private Double calculateClassicalScore(String text) {
        double score = 0.0;
        String[] classicalWords = {"之", "乎", "者", "也", "矣", "焉", "哉", "兮"};
        String[] classicalPhrases = {"月色", "清风", "明月", "流水", "落花", "飞鸟"};

        for (String word : classicalWords) {
            if (text.contains(word)) score += 5;
        }
        for (String phrase : classicalPhrases) {
            if (text.contains(phrase)) score += 8;
        }

        return Math.min(100.0, score);
    }

    private Double calculateModernMinimalScore(String text) {
        double score = 50.0;
        
        // 短句加分
        String[] sentences = text.split("[。！？]");
        double avgLength = Arrays.stream(sentences)
                .mapToInt(String::length)
                .average()
                .orElse(20);
        if (avgLength < 15) score += 20;
        else if (avgLength < 25) score += 10;

        // 少用形容词加分
        String[] adjectives = {"非常", "十分", "极其", "特别", "格外"};
        for (String adj : adjectives) {
            if (text.contains(adj)) score -= 5;
        }

        return Math.max(0, Math.min(100.0, score));
    }

    private Double calculateRomanticScore(String text) {
        double score = 0.0;
        String[] romanticWords = {"绚烂", "璀璨", "绮丽", "瑰丽", "斑斓", "缤纷"};
        String[] rhetoricalMarkers = {"像", "如同", "仿佛", "宛如", "好似"};

        for (String word : romanticWords) {
            if (text.contains(word)) score += 10;
        }
        for (String marker : rhetoricalMarkers) {
            if (text.contains(marker)) score += 8;
        }

        return Math.min(100.0, score);
    }

    private Double calculateSuspenseScore(String text) {
        double score = 0.0;
        String[] suspenseWords = {"突然", "忽然", "猛然", "骤然", "陡然"};
        String[] tensionMarkers = {"……", "——", "？", "！"};

        for (String word : suspenseWords) {
            if (text.contains(word)) score += 10;
        }
        for (String marker : tensionMarkers) {
            int count = text.length() - text.replace(marker, "").length();
            score += count * 3;
        }

        return Math.min(100.0, score);
    }

    private Double calculateHealingScore(String text) {
        double score = 0.0;
        String[] healingWords = {"温暖", "柔和", "轻轻", "缓缓", "静静", "暖暖"};
        String[] warmElements = {"阳光", "微风", "花香", "笑容", "拥抱"};

        for (String word : healingWords) {
            if (text.contains(word)) score += 10;
        }
        for (String element : warmElements) {
            if (text.contains(element)) score += 8;
        }

        return Math.min(100.0, score);
    }

    private List<String> extractCharacteristics(String text) {
        List<String> characteristics = new ArrayList<>();

        if (text.length() / (text.split("[。！？]").length + 1) < 15) {
            characteristics.add("短句为主");
        } else {
            characteristics.add("长句为主");
        }

        if (text.contains("像") || text.contains("如同")) {
            characteristics.add("善用比喻");
        }

        if (text.matches(".*[心想|暗自|内心].*")) {
            characteristics.add("注重心理描写");
        }

        if (text.matches(".*[色彩|光|影|红|蓝|绿].*")) {
            characteristics.add("色彩丰富");
        }

        return characteristics;
    }

    private List<String> generateStyleSuggestions(String text, StyleAnalysisResult result) {
        List<String> suggestions = new ArrayList<>();

        if (result.getStyleScores().get("romantic") < 30) {
            suggestions.add("可以增加一些修辞手法，如比喻、拟人等");
        }

        if (result.getStyleScores().get("suspense") < 20 && text.length() > 200) {
            suggestions.add("可以适当使用短句增强节奏感");
        }

        return suggestions;
    }
}
