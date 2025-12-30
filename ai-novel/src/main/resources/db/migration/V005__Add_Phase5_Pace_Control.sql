-- Phase 5: 场景节奏管理 - 数据库迁移脚本

-- ==================== 扩展现有表 ====================

-- 为 scene_usages 表添加字数追踪字段
ALTER TABLE scene_usages 
ADD COLUMN IF NOT EXISTS word_count_in_usage INT COMMENT '该场景在本次使用的字数',
ADD COLUMN IF NOT EXISTS scene_length_category VARCHAR(50) COMMENT '场景长度分类 (SHORT/MEDIUM/LONG)';

-- 为 chapters 表添加节奏标记字段
ALTER TABLE chapters 
ADD COLUMN IF NOT EXISTS pace_status VARCHAR(50) COMMENT '节奏状态 (PACE_IDEAL/PACE_SLOW/PACE_FAST)',
ADD COLUMN IF NOT EXISTS rhythm_score DOUBLE COMMENT '节奏评分 (0-1)',
ADD COLUMN IF NOT EXISTS deviates_from_avg BOOLEAN COMMENT '是否偏离平均值';

-- ==================== 创建新表 ====================

-- 场景字数目标表
CREATE TABLE IF NOT EXISTS scene_word_count_targets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_id BIGINT NOT NULL,
    novel_id BIGINT NOT NULL,
    min_words INT NOT NULL DEFAULT 500,
    max_words INT NOT NULL DEFAULT 3000,
    target_average_words INT NOT NULL DEFAULT 1500,
    enforce_strict BOOLEAN NOT NULL DEFAULT false,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    KEY idx_scene_id (scene_id),
    KEY idx_novel_id (novel_id),
    CONSTRAINT fk_scene_word_count_scene FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 场景节奏分析记录表
CREATE TABLE IF NOT EXISTS scene_rhythm_analyses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_id BIGINT NOT NULL,
    novel_id BIGINT NOT NULL,
    total_appearances BIGINT NOT NULL DEFAULT 0,
    average_interval INT NOT NULL DEFAULT 0,
    min_interval INT NOT NULL DEFAULT 0,
    max_interval INT NOT NULL DEFAULT 0,
    rhythm_pattern VARCHAR(50) NOT NULL COMMENT 'STABLE/ACCELERATING/DECELERATING/VARIABLE/SPARSE',
    rhythm_score DOUBLE NOT NULL DEFAULT 0.5,
    average_chapter_length INT NOT NULL DEFAULT 0,
    first_appearance_chapter INT,
    last_appearance_chapter INT,
    days_since_last_usage INT,
    interval_distribution JSON,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    KEY idx_scene_id (scene_id),
    KEY idx_novel_id (novel_id),
    KEY idx_rhythm_pattern (rhythm_pattern),
    CONSTRAINT fk_scene_rhythm_scene FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 字数控制反馈表
CREATE TABLE IF NOT EXISTS pace_control_feedbacks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chapter_id BIGINT NOT NULL,
    novel_id BIGINT NOT NULL,
    actual_word_count INT NOT NULL,
    min_target INT DEFAULT 500,
    max_target INT DEFAULT 5000,
    ideal_target INT DEFAULT 2000,
    validation_status VARCHAR(50) NOT NULL COMMENT 'ACCEPTABLE/TOO_SHORT/TOO_LONG',
    deviation INT DEFAULT 0,
    pace_status VARCHAR(50) COMMENT 'PACE_IDEAL/PACE_SLOW/PACE_FAST/PACE_UNSTABLE',
    content_quality JSON,
    improvement_suggestions JSON,
    warnings JSON,
    generated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    KEY idx_chapter_id (chapter_id),
    KEY idx_novel_id (novel_id),
    KEY idx_validation_status (validation_status),
    CONSTRAINT fk_pace_feedback_chapter FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 节奏优化建议表
CREATE TABLE IF NOT EXISTS rhythm_optimization_suggestions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_id BIGINT NOT NULL,
    novel_id BIGINT NOT NULL,
    current_pattern VARCHAR(50),
    current_score DOUBLE,
    issue VARCHAR(255),
    recommendation TEXT,
    priority VARCHAR(50) NOT NULL COMMENT 'HIGH/MEDIUM/LOW',
    note TEXT,
    target_score DOUBLE,
    suggested_improvement TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    KEY idx_scene_id (scene_id),
    KEY idx_novel_id (novel_id),
    KEY idx_priority (priority),
    CONSTRAINT fk_rhythm_sugg_scene FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 节奏控制报告表
CREATE TABLE IF NOT EXISTS pace_control_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id BIGINT NOT NULL,
    word_control_analysis JSON NOT NULL,
    rhythm_analysis JSON NOT NULL,
    novel_feedback JSON NOT NULL,
    overall_score DOUBLE,
    assessment VARCHAR(1000),
    generated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    KEY idx_novel_id (novel_id),
    KEY idx_created_at (created_at),
    CONSTRAINT fk_pace_report_novel FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== 创建索引 ====================

-- 创建复合索引用于快速查询
CREATE INDEX IF NOT EXISTS idx_scene_novel_analysis ON scene_rhythm_analyses(novel_id, scene_id);
CREATE INDEX IF NOT EXISTS idx_chapter_novel_feedback ON pace_control_feedbacks(novel_id, chapter_id);
CREATE INDEX IF NOT EXISTS idx_scene_novel_target ON scene_word_count_targets(novel_id, scene_id);

-- ==================== 创建视图用于快速查询 ====================

-- 场景字数使用统计视图
CREATE OR REPLACE VIEW vw_scene_word_count_stats AS
SELECT 
    s.id AS scene_id,
    s.name AS scene_name,
    s.novel_id,
    COUNT(DISTINCT su.id) AS total_usages,
    SUM(c.word_count) AS total_words,
    ROUND(AVG(c.word_count), 0) AS avg_words,
    MIN(c.word_count) AS min_words,
    MAX(c.word_count) AS max_words,
    MAX(su.usage_time) AS last_used_at
FROM scenes s
LEFT JOIN scene_usages su ON s.id = su.scene_id
LEFT JOIN chapters c ON su.chapter_id = c.id
GROUP BY s.id, s.name, s.novel_id;

-- 节奏控制质量评分视图
CREATE OR REPLACE VIEW vw_pace_control_quality AS
SELECT 
    n.id AS novel_id,
    COUNT(DISTINCT c.id) AS total_chapters,
    ROUND(AVG(c.word_count), 0) AS avg_chapter_length,
    MIN(c.word_count) AS min_chapter_length,
    MAX(c.word_count) AS max_chapter_length,
    SUM(CASE 
        WHEN c.word_count >= 500 AND c.word_count <= 5000 THEN 1 
        ELSE 0 
    END) AS chapters_in_range,
    ROUND(100.0 * SUM(CASE 
        WHEN c.word_count >= 500 AND c.word_count <= 5000 THEN 1 
        ELSE 0 
    END) / COUNT(DISTINCT c.id), 2) AS compliance_rate
FROM novels n
LEFT JOIN chapters c ON n.id = c.novel_id
GROUP BY n.id;

-- ==================== 添加注释 ====================

ALTER TABLE scene_word_count_targets COMMENT='场景字数目标管理表';
ALTER TABLE scene_rhythm_analyses COMMENT='场景节奏分析记录表';
ALTER TABLE pace_control_feedbacks COMMENT='字数控制实时反馈表';
ALTER TABLE rhythm_optimization_suggestions COMMENT='节奏优化建议表';
ALTER TABLE pace_control_reports COMMENT='节奏控制综合报告表';

-- 完成 Phase 5 数据库迁移
