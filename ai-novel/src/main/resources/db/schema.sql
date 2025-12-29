-- AI智能小说创作系统 - 数据库初始化脚本
-- MySQL 5.7+ / 8.0+
-- 字符集: utf8mb4

-- ==================== 创建数据库 ====================
CREATE DATABASE IF NOT EXISTS ai_novel_writer 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE ai_novel_writer;

-- ==================== 核心业务表 ====================

-- 小说表
CREATE TABLE IF NOT EXISTS novels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '小说标题',
    author VARCHAR(100) COMMENT '作者',
    genre VARCHAR(50) COMMENT '类型',
    synopsis TEXT COMMENT '简介',
    outline TEXT COMMENT '大纲',
    settings TEXT COMMENT '设定',
    status VARCHAR(20) DEFAULT 'PLANNING' COMMENT '状态: PLANNING, WRITING, COMPLETED',
    total_chapters INT DEFAULT 0 COMMENT '总章节数',
    total_words INT DEFAULT 0 COMMENT '总字数',
    target_words INT DEFAULT 0 COMMENT '目标字数',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说表';

-- 章节表
CREATE TABLE IF NOT EXISTS chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    chapter_number INT NOT NULL COMMENT '章节序号',
    title VARCHAR(200) NOT NULL COMMENT '章节标题',
    content TEXT COMMENT '章节内容',
    word_count INT DEFAULT 0 COMMENT '字数',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT, COMPLETED, PUBLISHED',
    version INT DEFAULT 1 COMMENT '版本号',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    UNIQUE KEY uk_novel_chapter (novel_id, chapter_number),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节表';

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ==================== 创作辅助表 ====================

-- 角色表
CREATE TABLE IF NOT EXISTS characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_type VARCHAR(20) NOT NULL COMMENT '角色类型: PROTAGONIST, ANTAGONIST, SUPPORTING, MINOR',
    gender VARCHAR(10) COMMENT '性别: MALE, FEMALE, OTHER',
    age INT COMMENT '年龄',
    personality TEXT COMMENT '性格特征',
    background TEXT COMMENT '背景故事',
    appearance TEXT COMMENT '外貌描述',
    abilities TEXT COMMENT '能力特长',
    relationships TEXT COMMENT '人物关系JSON',
    motivation TEXT COMMENT '动机目标',
    arc TEXT COMMENT '角色弧光',
    importance_level VARCHAR(20) DEFAULT 'MINOR' COMMENT '重要性: PRIMARY, SECONDARY, TERTIARY, MINOR',
    is_global_protagonist BOOLEAN DEFAULT FALSE COMMENT '是否全局主角',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_role_type (role_type),
    INDEX idx_importance_level (importance_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 场景表
CREATE TABLE IF NOT EXISTS scenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL COMMENT '场景名称',
    scene_type VARCHAR(20) NOT NULL COMMENT '场景类型: LOCATION, EVENT, TIME_PERIOD',
    description TEXT COMMENT '场景描述',
    atmosphere TEXT COMMENT '氛围描述',
    time_period VARCHAR(100) COMMENT '时间段',
    location VARCHAR(200) COMMENT '地点',
    weather VARCHAR(50) COMMENT '天气',
    props TEXT COMMENT '道具JSON数组',
    involved_characters TEXT COMMENT '相关角色ID JSON数组',
    chapter_references TEXT COMMENT '出现章节JSON数组',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_scene_type (scene_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景表';

-- 大纲表
CREATE TABLE IF NOT EXISTS outlines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    parent_id BIGINT COMMENT '父节点ID，用于树形结构',
    node_type VARCHAR(20) NOT NULL COMMENT '节点类型: ARC, VOLUME, CHAPTER, SECTION',
    sequence_number INT NOT NULL COMMENT '序号',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    summary TEXT COMMENT '概要',
    target_word_count INT COMMENT '目标字数',
    key_events TEXT COMMENT '关键事件JSON数组',
    character_focus TEXT COMMENT '焦点角色JSON数组',
    plot_points TEXT COMMENT '情节点JSON数组',
    themes TEXT COMMENT '主题标签JSON数组',
    status VARCHAR(20) DEFAULT 'PLANNED' COMMENT '状态: PLANNED, IN_PROGRESS, COMPLETED',
    chapter_id BIGINT COMMENT '关联的实际章节ID',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES outlines(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_node_type (node_type),
    INDEX idx_sequence (novel_id, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大纲表';

-- ==================== 辅助功能表 ====================

-- 编辑历史表
CREATE TABLE IF NOT EXISTS edit_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    user_id BIGINT COMMENT '编辑用户ID（可选）',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型: CREATE, UPDATE, DELETE, REGENERATE',
    content_before TEXT COMMENT '修改前内容',
    content_after TEXT COMMENT '修改后内容',
    change_summary TEXT COMMENT '变更摘要',
    word_count_diff INT COMMENT '字数变化',
    edit_reason VARCHAR(200) COMMENT '编辑原因',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编辑历史表';

-- 角色关系表
CREATE TABLE IF NOT EXISTS character_relationships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL COMMENT '角色ID',
    related_character_id BIGINT NOT NULL COMMENT '相关角色ID',
    relationship_type VARCHAR(50) NOT NULL COMMENT '关系类型',
    description TEXT COMMENT '关系描述',
    strength INT DEFAULT 0 COMMENT '关系强度 0-10',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE,
    FOREIGN KEY (related_character_id) REFERENCES characters(id) ON DELETE CASCADE,
    UNIQUE KEY uk_relationship (character_id, related_character_id),
    INDEX idx_character_id (character_id),
    INDEX idx_related_character_id (related_character_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色关系表';

-- 世界设定表
CREATE TABLE IF NOT EXISTS world_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL COMMENT '分类: GEOGRAPHY, MAGIC_SYSTEM, TECHNOLOGY, CULTURE, POLITICS, HISTORY',
    name VARCHAR(200) NOT NULL COMMENT '设定名称',
    description TEXT COMMENT '详细描述',
    rules TEXT COMMENT '规则说明',
    examples TEXT COMMENT '示例',
    related_characters TEXT COMMENT '相关角色JSON数组',
    related_scenes TEXT COMMENT '相关场景JSON数组',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='世界设定表';

-- 情节线索表
CREATE TABLE IF NOT EXISTS plot_threads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL COMMENT '线索名称',
    type VARCHAR(50) NOT NULL COMMENT '类型: MAIN, SUBPLOT, BACKGROUND',
    description TEXT COMMENT '描述',
    start_chapter INT COMMENT '起始章节',
    end_chapter INT COMMENT '结束章节',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, RESOLVED, ABANDONED',
    involved_characters TEXT COMMENT '涉及角色JSON数组',
    key_events TEXT COMMENT '关键事件JSON数组',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情节线索表';

-- 内容分析表
CREATE TABLE IF NOT EXISTS content_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    sentiment_score DOUBLE COMMENT '情感分数',
    pacing_score DOUBLE COMMENT '节奏分数',
    dialogue_ratio DOUBLE COMMENT '对话比例',
    description_ratio DOUBLE COMMENT '描写比例',
    action_ratio DOUBLE COMMENT '动作比例',
    key_themes TEXT COMMENT '关键主题JSON数组',
    keywords TEXT COMMENT '关键词JSON数组',
    suggestions TEXT COMMENT '改进建议JSON数组',
    analyzed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容分析表';

-- 续写建议表
CREATE TABLE IF NOT EXISTS continuation_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    suggestion_type VARCHAR(50) NOT NULL COMMENT '建议类型',
    content TEXT NOT NULL COMMENT '建议内容',
    reasoning TEXT COMMENT '理由说明',
    priority INT DEFAULT 0 COMMENT '优先级',
    is_adopted BOOLEAN DEFAULT FALSE COMMENT '是否采纳',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_is_adopted (is_adopted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='续写建议表';

-- 大纲节点表（树形结构辅助）
CREATE TABLE IF NOT EXISTS outline_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    parent_id BIGINT COMMENT '父节点ID',
    node_type VARCHAR(20) NOT NULL COMMENT 'ARC, VOLUME, CHAPTER, SCENE',
    title VARCHAR(200) NOT NULL,
    description TEXT,
    sequence_number INT NOT NULL,
    level INT NOT NULL COMMENT '层级: 1-ARC, 2-VOLUME, 3-CHAPTER, 4-SCENE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES outline_nodes(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大纲节点表';

-- ==================== 初始化完成 ====================
-- 查看所有表
SHOW TABLES;

-- 显示数据库信息
SELECT 
    'Database initialized successfully!' AS status,
    COUNT(*) AS table_count,
    DATABASE() AS database_name
FROM information_schema.tables 
WHERE table_schema = DATABASE();
