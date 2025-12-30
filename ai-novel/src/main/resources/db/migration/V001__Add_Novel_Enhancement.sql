-- Phase 1: 小说创建强化 - 添加必填大纲和场景字段
-- 添加日期: Phase 1 Week 1

USE ai_novel_writer;

-- 添加初始大纲ID列
ALTER TABLE novels ADD COLUMN initial_outline_id BIGINT COMMENT '初始大纲ID（必填）' AFTER target_words;

-- 添加初始场景ID列
ALTER TABLE novels ADD COLUMN initial_scene_id BIGINT COMMENT '初始场景ID（必填）' AFTER initial_outline_id;

-- 添加AI推荐标志列
ALTER TABLE novels ADD COLUMN use_ai_recommendation BOOLEAN DEFAULT TRUE COMMENT '是否使用AI推荐辅助' AFTER initial_scene_id;

-- 添加外键约束 - 初始大纲
ALTER TABLE novels ADD CONSTRAINT fk_novel_initial_outline 
FOREIGN KEY (initial_outline_id) REFERENCES outlines(id) ON DELETE SET NULL;

-- 添加外键约束 - 初始场景
ALTER TABLE novels ADD CONSTRAINT fk_novel_initial_scene 
FOREIGN KEY (initial_scene_id) REFERENCES scenes(id) ON DELETE SET NULL;

-- 创建索引以提升查询性能
CREATE INDEX idx_novel_initial_outline ON novels(initial_outline_id);
CREATE INDEX idx_novel_initial_scene ON novels(initial_scene_id);
CREATE INDEX idx_novel_ai_recommendation ON novels(use_ai_recommendation);

-- 添加新的约束表用于存储创建时的推荐数据 (Phase 1 Extension)
CREATE TABLE IF NOT EXISTS novel_creation_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT UNIQUE NOT NULL,
    story_framework TEXT COMMENT '故事框架',
    three_act_structure JSON COMMENT '三幕结构 (JSON)',
    main_plot_points JSON COMMENT '主要情节点 (JSON数组)',
    initial_scene_recommendation JSON COMMENT '初始场景推荐 (JSON)',
    character_recommendations JSON COMMENT '角色推荐 (JSON数组)',
    themes JSON COMMENT '主题 (JSON数组)',
    style_elements JSON COMMENT '写作风格要素 (JSON数组)',
    word_count_range JSON COMMENT '字数范围 (JSON)',
    estimated_reading_time INT COMMENT '预估阅读时间(分钟)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说创建推荐表';
