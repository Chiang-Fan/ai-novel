-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建角色表
CREATE TABLE IF NOT EXISTS characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    role_type VARCHAR(20) NOT NULL COMMENT 'PROTAGONIST, ANTAGONIST, SUPPORTING, MINOR',
    gender VARCHAR(10) COMMENT 'MALE, FEMALE, OTHER',
    age INT,
    personality TEXT COMMENT '性格特征',
    background TEXT COMMENT '背景故事',
    appearance TEXT COMMENT '外貌描述',
    abilities TEXT COMMENT '能力特长',
    relationships TEXT COMMENT '人物关系JSON',
    motivation TEXT COMMENT '动机目标',
    arc TEXT COMMENT '角色弧光',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_role_type (role_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建场景表
CREATE TABLE IF NOT EXISTS scenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    scene_type VARCHAR(20) NOT NULL COMMENT 'LOCATION, EVENT, TIME_PERIOD',
    description TEXT,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建大纲表
CREATE TABLE IF NOT EXISTS outlines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    parent_id BIGINT COMMENT '父节点ID，用于树形结构',
    node_type VARCHAR(20) NOT NULL COMMENT 'ARC, VOLUME, CHAPTER, SECTION',
    sequence_number INT NOT NULL COMMENT '序号',
    title VARCHAR(200) NOT NULL,
    summary TEXT COMMENT '概要',
    target_word_count INT COMMENT '目标字数',
    key_events TEXT COMMENT '关键事件JSON数组',
    character_focus TEXT COMMENT '焦点角色JSON数组',
    plot_points TEXT COMMENT '情节点JSON数组',
    themes TEXT COMMENT '主题标签JSON数组',
    status VARCHAR(20) DEFAULT 'PLANNED' COMMENT 'PLANNED, IN_PROGRESS, COMPLETED',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建编辑历史表
CREATE TABLE IF NOT EXISTS edit_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    user_id BIGINT COMMENT '编辑用户ID（可选）',
    operation_type VARCHAR(20) NOT NULL COMMENT 'CREATE, UPDATE, DELETE, REGENERATE',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
