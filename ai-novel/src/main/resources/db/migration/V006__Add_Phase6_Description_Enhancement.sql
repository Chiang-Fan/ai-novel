-- Phase 6: AI描写能力强化 - 数据库迁移脚本
-- 创建时间: 2024

-- 1. 描写模板表
CREATE TABLE IF NOT EXISTS description_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT,
    template_name VARCHAR(100) NOT NULL,
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型: ENVIRONMENT, PSYCHOLOGICAL, EMOTIONAL, ACTION, SENSORY',
    style VARCHAR(50) COMMENT '风格: CONCISE, DETAILED, POETIC, DRAMATIC, REALISTIC, IMPRESSIONISTIC',
    prompt_template TEXT NOT NULL COMMENT '提示词模板',
    example_text TEXT COMMENT '示例文本',
    parameters JSON COMMENT '模板参数配置',
    is_builtin BOOLEAN DEFAULT FALSE COMMENT '是否内置模板',
    is_active BOOLEAN DEFAULT TRUE,
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_novel_id_templates (novel_id),
    INDEX idx_template_type (template_type),
    INDEX idx_style (style)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 描写生成记录表
CREATE TABLE IF NOT EXISTS description_generations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    chapter_id BIGINT,
    scene_id BIGINT,
    character_id BIGINT,
    description_type VARCHAR(50) NOT NULL COMMENT '描写类型',
    style VARCHAR(50) COMMENT '描写风格',
    context TEXT COMMENT '上下文',
    generated_text TEXT NOT NULL COMMENT '生成的描写',
    quality_score DECIMAL(5,2) COMMENT '质量评分',
    used_techniques JSON COMMENT '使用的技巧',
    user_rating INT COMMENT '用户评分 1-5',
    is_accepted BOOLEAN DEFAULT FALSE COMMENT '是否被采用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_novel_id_generations (novel_id),
    INDEX idx_chapter_id_generations (chapter_id),
    INDEX idx_description_type (description_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 描写优化记录表
CREATE TABLE IF NOT EXISTS description_optimizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    original_text TEXT NOT NULL COMMENT '原始文本',
    optimized_text TEXT NOT NULL COMMENT '优化后文本',
    optimization_types JSON COMMENT '优化类型列表',
    original_score DECIMAL(5,2) COMMENT '原始评分',
    optimized_score DECIMAL(5,2) COMMENT '优化后评分',
    improvement_percentage DECIMAL(5,2) COMMENT '改进百分比',
    changes JSON COMMENT '变更详情',
    is_applied BOOLEAN DEFAULT FALSE COMMENT '是否已应用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_novel_id_optimizations (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 节奏分析记录表
CREATE TABLE IF NOT EXISTS rhythm_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    chapter_id BIGINT,
    content_hash VARCHAR(64) COMMENT '内容哈希用于缓存',
    rhythm_type VARCHAR(50) NOT NULL COMMENT '节奏类型: FAST, MODERATE, SLOW, CLIMAX, TRANSITION, RESOLUTION',
    intensity DECIMAL(3,2) COMMENT '强度 0-1',
    description_density DECIMAL(3,2) COMMENT '推荐描写密度',
    recommended_types JSON COMMENT '推荐描写类型',
    type_weights JSON COMMENT '类型权重',
    suggestions JSON COMMENT '建议',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_novel_id_rhythm (novel_id),
    INDEX idx_chapter_id_rhythm (chapter_id),
    INDEX idx_content_hash (content_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 描写问题诊断记录表
CREATE TABLE IF NOT EXISTS description_diagnoses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    chapter_id BIGINT,
    diagnosed_text TEXT NOT NULL COMMENT '诊断的文本',
    issues JSON COMMENT '发现的问题',
    issue_counts JSON COMMENT '问题统计',
    recommended_optimizations JSON COMMENT '推荐优化',
    health_score DECIMAL(5,2) COMMENT '健康度评分',
    is_resolved BOOLEAN DEFAULT FALSE COMMENT '是否已解决',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_novel_id_diagnoses (novel_id),
    INDEX idx_chapter_id_diagnoses (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 风格偏好表
CREATE TABLE IF NOT EXISTS style_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    preferred_style VARCHAR(50) COMMENT '偏好风格',
    style_weights JSON COMMENT '风格权重配置',
    custom_prompts JSON COMMENT '自定义提示词',
    description_density_preference DECIMAL(3,2) DEFAULT 0.6 COMMENT '描写密度偏好',
    sensory_emphasis JSON COMMENT '感官侧重配置',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_novel_style_pref (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 插入内置描写模板
INSERT INTO description_templates (template_name, template_type, style, prompt_template, example_text, is_builtin) VALUES
('环境描写-细腻', 'ENVIRONMENT', 'DETAILED', 
 '请生成细腻的环境描写，注意：\n1. 多感官描写（视觉、听觉、嗅觉、触觉）\n2. 远近结合，层次分明\n3. 动静结合\n4. 情景交融',
 '夕阳的余晖洒在古老的青石板路上，将每一块石头都染成了温暖的橘红色。远处传来几声犬吠，打破了黄昏的宁静。空气中弥漫着晚饭的香气，混合着泥土的芬芳。',
 TRUE),

('心理描写-深入', 'PSYCHOLOGICAL', 'DETAILED',
 '请生成深入的心理描写，注意：\n1. 展现内心冲突\n2. 通过行为暗示心理\n3. 心理变化要有层次\n4. 避免直接陈述',
 '她的手指不自觉地攥紧了衣角。那些话像是一把钝刀，一下一下地割着她的心。她想反驳，却发现喉咙像是被什么堵住了。',
 TRUE),

('情绪描写-强烈', 'EMOTIONAL', 'DRAMATIC',
 '请生成强烈的情绪描写，注意：\n1. 通过身体反应展现情绪\n2. 情绪要有渐进过程\n3. 结合环境烘托\n4. 避免直接说"他很..."',
 '心跳声在耳边轰鸣，像是要冲破胸腔。他的手在发抖，指甲深深嵌入掌心，却感觉不到疼痛。眼眶发酸，视线开始模糊。',
 TRUE),

('动作描写-生动', 'ACTION', 'DETAILED',
 '请生成生动的动作描写，注意：\n1. 动词精准有力\n2. 分解连贯动作\n3. 节奏张弛有度\n4. 结合环境和反应',
 '他猛地起身，椅子向后滑出，发出刺耳的摩擦声。三步并作两步冲到门口，一把拉开门，冷风瞬间灌了进来。',
 TRUE),

('感官描写-全面', 'SENSORY', 'DETAILED',
 '请生成全面的感官描写，注意：\n1. 视觉细节丰富\n2. 听觉层次分明\n3. 嗅觉触觉点缀\n4. 各感官相互呼应',
 '阳光透过树叶的缝隙，在地上投下斑驳的光影。风吹过，带来远处咖啡馆飘出的香气。脚下的落叶发出沙沙的声响，空气中带着一丝秋天特有的凉意。',
 TRUE);

-- 8. 更新场景表，增加描写相关字段
ALTER TABLE scenes 
ADD COLUMN IF NOT EXISTS description_style VARCHAR(50) COMMENT '描写风格偏好',
ADD COLUMN IF NOT EXISTS sensory_keywords JSON COMMENT '感官关键词',
ADD COLUMN IF NOT EXISTS mood_keywords JSON COMMENT '氛围关键词';

-- 9. 更新章节表，增加节奏相关字段
ALTER TABLE chapters
ADD COLUMN IF NOT EXISTS rhythm_type VARCHAR(50) COMMENT '章节节奏类型',
ADD COLUMN IF NOT EXISTS description_density DECIMAL(3,2) COMMENT '描写密度',
ADD COLUMN IF NOT EXISTS dominant_description_type VARCHAR(50) COMMENT '主要描写类型';
