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

-- 写作风格表
CREATE TABLE IF NOT EXISTS writing_styles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL UNIQUE COMMENT '小说ID（一对一）',
    perspective VARCHAR(30) NOT NULL COMMENT '叙述视角',
    tone VARCHAR(50) NOT NULL COMMENT '语气风格',
    sentence_style VARCHAR(50) NOT NULL COMMENT '句式特点',
    keywords TEXT COMMENT '关键词JSON数组',
    raw_sample TEXT NOT NULL COMMENT '原始样本文本',
    avg_sentence_length INT COMMENT '平均句子长度',
    dialogue_ratio DOUBLE COMMENT '对话占比0-1',
    description_density VARCHAR(20) COMMENT '描写密度',
    extracted_at TIMESTAMP COMMENT '提取时间',
    last_validated_at TIMESTAMP COMMENT '最后验证时间',
    confidence_score DOUBLE COMMENT '置信度0-1',
    style_description TEXT COMMENT 'AI生成的风格描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_perspective (perspective),
    INDEX idx_tone (tone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='写作风格表';

-- 伏笔管理表
CREATE TABLE IF NOT EXISTS plot_hooks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    title VARCHAR(200) NOT NULL COMMENT '伏笔标题',
    description TEXT NOT NULL COMMENT '伏笔描述',
    planted_in_chapter INT NOT NULL COMMENT '埋设章节号',
    expected_chapter INT COMMENT '建议揭示章节号',
    triggered_in_chapter INT COMMENT '实际触发章节号',
    resolved_in_chapter INT COMMENT '解决章节号',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/HINTED/TRIGGERED/RESOLVED',
    type VARCHAR(20) NOT NULL COMMENT '类型：EXPLICIT/IMPLICIT/CHEKHOV_GUN',
    priority INT NOT NULL DEFAULT 5 COMMENT '优先级1-10',
    resolution_note TEXT COMMENT '解决说明',
    related_characters TEXT COMMENT '相关角色JSON数组',
    is_auto_detected BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否AI自动检测',
    content_reference TEXT COMMENT '伏笔内容引用',
    triggered_at TIMESTAMP COMMENT '触发时间',
    resolved_at TIMESTAMP COMMENT '解决时间',
    notes TEXT COMMENT '用户备注',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_status (status),
    INDEX idx_planted_chapter (planted_in_chapter),
    INDEX idx_expected_chapter (expected_chapter)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='伏笔管理表';

-- 章节深度分析结果表
CREATE TABLE IF NOT EXISTS chapter_analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    chapter_number INT NOT NULL COMMENT '章节号',
    
    -- 情节点
    plot_points TEXT COMMENT '情节点列表JSON',
    conflict_type VARCHAR(20) COMMENT '冲突类型',
    conflict_intensity INT COMMENT '冲突强度1-10',
    conflict_direction VARCHAR(20) COMMENT '冲突方向',
    
    -- 主题
    themes TEXT COMMENT '主题列表JSON',
    theme_intensity INT COMMENT '主题强度1-10',
    theme_description TEXT COMMENT '主题描述',
    
    -- 角色弧光
    character_arcs TEXT COMMENT '角色发展JSON',
    protagonist_stage VARCHAR(30) COMMENT '主角阶段',
    
    -- 节奏
    pacing VARCHAR(20) COMMENT '节奏',
    action_ratio DOUBLE COMMENT '动作占比',
    dialogue_ratio DOUBLE COMMENT '对话占比',
    description_ratio DOUBLE COMMENT '描写占比',
    introspection_ratio DOUBLE COMMENT '内心独白占比',
    
    -- 情感曲线
    emotion_curve TEXT COMMENT '情感曲线JSON',
    emotion_trend VARCHAR(20) COMMENT '情感趋势',
    
    -- 叙事技巧
    narrative_techniques TEXT COMMENT '叙事技巧JSON',
    turning_points_count INT COMMENT '转折点数量',
    suspense_level INT COMMENT '悬念强度1-10',
    
    -- 世界观
    world_building_elements TEXT COMMENT '世界观元素JSON',
    world_building_score INT COMMENT '世界观完整度1-10',
    
    -- 质量评估
    quality_score INT COMMENT '整体质量1-10',
    readability_score INT COMMENT '可读性1-10',
    creativity_score INT COMMENT '创意性1-10',
    coherence_score INT COMMENT '连贯性1-10',
    
    -- 改进建议
    improvement_suggestions TEXT COMMENT '改进建议JSON',
    
    -- 元数据
    analysis_duration_ms BIGINT COMMENT '分析耗时毫秒',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_chapter_number (chapter_number),
    INDEX idx_quality_score (quality_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节深度分析结果表';

-- 智能续写历史表
CREATE TABLE IF NOT EXISTS continuation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL COMMENT '所属章节ID',
    position INT NOT NULL COMMENT '续写位置（字符偏移）',
    
    context_before LONGTEXT COMMENT '续写前的上下文',
    context_length INT COMMENT '上下文长度（字数）',
    generated_content LONGTEXT NOT NULL COMMENT '生成的续写内容',
    
    settings TEXT COMMENT '续写配置JSON',
    quality_score DOUBLE COMMENT '质量评分0.0-1.0',
    style_consistency DOUBLE COMMENT '文风一致性0.0-1.0',
    plot_coherence DOUBLE COMMENT '情节连贯性0.0-1.0',
    
    accepted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否被采纳',
    accepted_at TIMESTAMP COMMENT '采纳时间',
    
    variant_number INT DEFAULT 1 COMMENT '方案编号',
    batch_id VARCHAR(50) COMMENT '批次ID',
    
    feedback TEXT COMMENT '用户反馈',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_batch_id (batch_id),
    INDEX idx_accepted (accepted),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能续写历史表';

-- AI对话会话表
CREATE TABLE IF NOT EXISTS conversation_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    chapter_id BIGINT COMMENT '关联章节ID（可选）',
    
    session_title VARCHAR(200) COMMENT '会话标题',
    session_type VARCHAR(50) COMMENT '会话类型: WRITING_ADVICE, PLOT_CONSULTATION, STYLE_GUIDANCE, GENERAL',
    
    context_snapshot TEXT COMMENT '上下文快照JSON',
    total_messages INT DEFAULT 0 COMMENT '总消息数',
    
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, ARCHIVED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话会话表';

-- AI对话消息表
CREATE TABLE IF NOT EXISTS conversation_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '所属会话ID',
    
    role VARCHAR(20) NOT NULL COMMENT '角色: USER, ASSISTANT, SYSTEM',
    content LONGTEXT NOT NULL COMMENT '消息内容',
    
    message_type VARCHAR(50) COMMENT '消息类型: TEXT, SUGGESTION, CODE_EXAMPLE',
    metadata TEXT COMMENT '元数据JSON',
    
    token_count INT COMMENT 'Token数量',
    response_time INT COMMENT '响应时间（毫秒）',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (session_id) REFERENCES conversation_sessions(id) ON DELETE CASCADE,
    INDEX idx_session_id (session_id),
    INDEX idx_role (role),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话消息表';

-- 章节版本表
CREATE TABLE IF NOT EXISTS chapter_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL COMMENT '所属章节ID',
    version_number INT NOT NULL COMMENT '版本号',
    
    content LONGTEXT NOT NULL COMMENT '版本内容',
    word_count INT DEFAULT 0 COMMENT '字数',
    
    version_tag VARCHAR(100) COMMENT '版本标签',
    version_note TEXT COMMENT '版本备注',
    
    created_by VARCHAR(100) COMMENT '创建者',
    created_type VARCHAR(50) COMMENT '创建类型: MANUAL, AUTO_SAVE, AUTO_SNAPSHOT, ROLLBACK',
    
    is_current BOOLEAN DEFAULT FALSE COMMENT '是否当前版本',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_version_number (version_number),
    INDEX idx_created_at (created_at),
    INDEX idx_is_current (is_current)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节版本表';

-- 版本对比缓存表
CREATE TABLE IF NOT EXISTS version_comparisons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id_from BIGINT NOT NULL COMMENT '源版本ID',
    version_id_to BIGINT NOT NULL COMMENT '目标版本ID',
    
    diff_result LONGTEXT COMMENT '差异结果JSON',
    
    added_count INT DEFAULT 0 COMMENT '新增行数',
    deleted_count INT DEFAULT 0 COMMENT '删除行数',
    modified_count INT DEFAULT 0 COMMENT '修改行数',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_version_pair (version_id_from, version_id_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='版本对比缓存表';

-- 智能推荐表
CREATE TABLE IF NOT EXISTS auto_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    chapter_id BIGINT COMMENT '相关章节ID',
    chapter_number INT COMMENT '章节号',
    
    type VARCHAR(30) NOT NULL COMMENT '推荐类型',
    title VARCHAR(200) NOT NULL COMMENT '推荐标题',
    content TEXT NOT NULL COMMENT '推荐内容',
    
    priority INT NOT NULL COMMENT '优先级1-10',
    relevance_score DOUBLE NOT NULL COMMENT '相关性评分0.0-1.0',
    
    basis TEXT COMMENT '推荐依据JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    expected_impact TEXT COMMENT '预期影响JSON',
    
    reference_id BIGINT COMMENT '参考数据ID',
    reference_type VARCHAR(30) COMMENT '参考类型',
    
    feedback TEXT COMMENT '用户反馈',
    accepted_at TIMESTAMP COMMENT '采纳时间',
    rejected_at TIMESTAMP COMMENT '拒绝时间',
    expires_at TIMESTAMP COMMENT '过期时间',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_type (type),
    INDEX idx_priority (priority),
    INDEX idx_status (status),
    INDEX idx_relevance_score (relevance_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能推荐表';

-- 情节推演模拟表
CREATE TABLE IF NOT EXISTS plot_simulations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    chapter_id BIGINT COMMENT '关联章节ID',
    
    simulation_name VARCHAR(200) NOT NULL COMMENT '模拟名称',
    description TEXT COMMENT '模拟描述',
    
    starting_point TEXT NOT NULL COMMENT '情节起点',
    current_state TEXT COMMENT '当前状态',
    
    status VARCHAR(50) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, ARCHIVED, COMPLETED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情节推演模拟表';

-- 模拟分支表
CREATE TABLE IF NOT EXISTS simulation_branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    simulation_id BIGINT NOT NULL COMMENT '所属模拟ID',
    parent_branch_id BIGINT COMMENT '父分支ID',
    
    branch_name VARCHAR(200) NOT NULL COMMENT '分支名称',
    decision_point TEXT NOT NULL COMMENT '决策点描述',
    decision_content TEXT NOT NULL COMMENT '决策内容',
    
    predicted_outcome TEXT COMMENT '预测后果',
    character_impact TEXT COMMENT '角色影响',
    plot_impact TEXT COMMENT '情节影响',
    
    probability_score DECIMAL(5,2) COMMENT '可能性评分',
    quality_score DECIMAL(5,2) COMMENT '质量评分',
    
    depth_level INT DEFAULT 0 COMMENT '深度层级',
    is_ending BOOLEAN DEFAULT FALSE COMMENT '是否结局分支',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (simulation_id) REFERENCES plot_simulations(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_branch_id) REFERENCES simulation_branches(id) ON DELETE SET NULL,
    INDEX idx_simulation_id (simulation_id),
    INDEX idx_parent_branch_id (parent_branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟分支表';

-- 角色成长记录表
CREATE TABLE IF NOT EXISTS character_growth_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL COMMENT '角色ID',
    chapter_id BIGINT COMMENT '关联章节ID',
    
    record_time TIMESTAMP NOT NULL COMMENT '记录时间点',
    attributes JSON COMMENT '属性快照JSON',
    notes TEXT COMMENT '备注说明',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_character_id (character_id),
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_record_time (record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色成长记录表';

-- 角色里程碑表
CREATE TABLE IF NOT EXISTS character_milestones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    character_id BIGINT NOT NULL COMMENT '角色ID',
    chapter_id BIGINT COMMENT '发生章节ID',
    
    milestone_type VARCHAR(50) COMMENT '类型: POSITIVE, NEGATIVE, NEUTRAL',
    event_name VARCHAR(200) NOT NULL COMMENT '事件名称',
    description TEXT COMMENT '详细描述',
    
    impact_level INT COMMENT '影响程度 (1-10)',
    affected_attributes JSON COMMENT '影响的属性JSON',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_character_id_milestones (character_id),
    INDEX idx_chapter_id_milestones (chapter_id),
    INDEX idx_milestone_type (milestone_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色里程碑表';

-- 场景表
CREATE TABLE IF NOT EXISTS scenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    
    name VARCHAR(200) NOT NULL COMMENT '场景名称',
    scene_type VARCHAR(50) COMMENT '场景类型: INDOOR, OUTDOOR, SPECIAL, LOCATION, EVENT, TIME_PERIOD',
    location VARCHAR(200) COMMENT '地点/位置描述',
    description TEXT COMMENT '详细描述',
    atmosphere TEXT COMMENT '氛围描述',
    
    time_period VARCHAR(100) COMMENT '时间段',
    weather VARCHAR(50) COMMENT '天气',
    props TEXT COMMENT '道具JSON数组',
    involved_characters TEXT COMMENT '相关角色ID JSON数组',
    chapter_references TEXT COMMENT '出现章节JSON数组',
    
    tags VARCHAR(500) COMMENT '标签（逗号分隔）',
    importance_score INT COMMENT '重要性评分 (1-10)',
    is_recurring BOOLEAN DEFAULT FALSE COMMENT '是否重复出现',
    notes TEXT COMMENT '备注',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_scene_type (scene_type),
    INDEX idx_is_recurring (is_recurring)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景表';

-- 场景使用记录表
CREATE TABLE IF NOT EXISTS scene_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scene_id BIGINT NOT NULL COMMENT '场景ID',
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    
    usage_time TIMESTAMP NOT NULL COMMENT '使用时间点',
    scene_state TEXT COMMENT '场景状态描述',
    weather VARCHAR(50) COMMENT '天气',
    time_of_day VARCHAR(50) COMMENT '时段',
    notes TEXT COMMENT '备注',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_scene_id_usages (scene_id),
    INDEX idx_chapter_id_usages (chapter_id),
    INDEX idx_usage_time (usage_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景使用记录表';

-- 场景变化记录表
CREATE TABLE IF NOT EXISTS scene_changes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scene_id BIGINT NOT NULL COMMENT '场景ID',
    
    change_type VARCHAR(50) COMMENT '变化类型',
    change_desc TEXT NOT NULL COMMENT '变化描述',
    before_state TEXT COMMENT '变化前状态',
    after_state TEXT COMMENT '变化后状态',
    related_chapter_id BIGINT COMMENT '相关章节ID',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE,
    FOREIGN KEY (related_chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_scene_id_changes (scene_id),
    INDEX idx_change_type (change_type),
    INDEX idx_created_at_changes (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景变化记录表';

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

-- 角色关系历史表
CREATE TABLE IF NOT EXISTS relationship_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    relationship_id BIGINT NOT NULL COMMENT '关系ID',
    change_type VARCHAR(50) NOT NULL COMMENT '变化类型: STRENGTH_CHANGE, TYPE_CHANGE, EVENT',
    old_strength INT COMMENT '旧强度值',
    new_strength INT COMMENT '新强度值',
    old_type VARCHAR(50) COMMENT '旧关系类型',
    new_type VARCHAR(50) COMMENT '新关系类型',
    event_desc TEXT COMMENT '事件描述',
    related_chapter_id BIGINT COMMENT '关联章节ID',
    change_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (relationship_id) REFERENCES character_relationships(id) ON DELETE CASCADE,
    FOREIGN KEY (related_chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_relationship_id (relationship_id),
    INDEX idx_change_time (change_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色关系历史表';

-- 氛围模板表
CREATE TABLE IF NOT EXISTS atmosphere_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '模板名称',
    category VARCHAR(50) NOT NULL COMMENT '分类: TIME, WEATHER, EMOTION, ACTION',
    atmosphere_type VARCHAR(50) NOT NULL COMMENT '氛围类型: PEACEFUL, TENSE, ROMANTIC, MYSTERIOUS, EXCITING, MELANCHOLIC, HORROR, JOYFUL',
    description TEXT COMMENT '描述',
    keywords TEXT COMMENT '关键词(JSON数组)',
    sensory_details TEXT COMMENT '感官细节(JSON)',
    example_text TEXT COMMENT '示例文本',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    is_system BOOLEAN DEFAULT false COMMENT '是否系统模板',
    created_by BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_atmosphere_type (atmosphere_type),
    INDEX idx_usage (usage_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='氛围模板表';

-- 场景氛围表
CREATE TABLE IF NOT EXISTS scene_atmosphere (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scene_id BIGINT NOT NULL COMMENT '场景ID',
    template_id BIGINT COMMENT '使用的模板ID',
    atmosphere_type VARCHAR(50) NOT NULL COMMENT '氛围类型',
    generated_text TEXT NOT NULL COMMENT '生成的氛围描写',
    prompt_used TEXT COMMENT '使用的提示词',
    ai_model VARCHAR(100) COMMENT 'AI模型',
    version INT DEFAULT 1 COMMENT '版本号',
    rating INT COMMENT '评分(1-5)',
    is_applied BOOLEAN DEFAULT false COMMENT '是否已应用',
    applied_at TIMESTAMP COMMENT '应用时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES atmosphere_templates(id) ON DELETE SET NULL,
    INDEX idx_scene_id (scene_id),
    INDEX idx_template_id (template_id),
    INDEX idx_atmosphere_type (atmosphere_type),
    INDEX idx_is_applied (is_applied)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景氛围表';

-- AI续写记录表
CREATE TABLE IF NOT EXISTS ai_continuations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    source_text TEXT NOT NULL COMMENT '上文内容',
    continuation_text TEXT NOT NULL COMMENT '续写内容',
    style VARCHAR(50) COMMENT '风格类型: SERIOUS, LIGHT, SUSPENSE, ROMANTIC, ACTION',
    length VARCHAR(50) COMMENT '长度类型: SENTENCE, PARAGRAPH, SECTION',
    prompt_used TEXT COMMENT '使用的提示词',
    ai_model VARCHAR(100) COMMENT 'AI模型',
    version INT DEFAULT 1 COMMENT '版本号',
    rating INT COMMENT '评分(1-5)',
    is_applied BOOLEAN DEFAULT false COMMENT '是否已应用',
    applied_at TIMESTAMP COMMENT '应用时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_is_applied (is_applied),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI续写记录表';

-- 写作建议表
CREATE TABLE IF NOT EXISTS writing_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    suggestion_type VARCHAR(50) NOT NULL COMMENT '建议类型: PLOT, CONFLICT, CHARACTER, DIALOGUE, PACING',
    suggestion TEXT NOT NULL COMMENT '建议内容',
    reasoning TEXT COMMENT '推理依据',
    priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级: LOW, MEDIUM, HIGH',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, ACCEPTED, REJECTED',
    context_text TEXT COMMENT '上下文',
    related_outline_id BIGINT COMMENT '关联大纲ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    FOREIGN KEY (related_outline_id) REFERENCES outlines(id) ON DELETE SET NULL,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_status (status),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='写作建议表';

-- 内容优化记录表
CREATE TABLE IF NOT EXISTS content_optimizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    original_text TEXT NOT NULL COMMENT '原始文本',
    optimized_text TEXT NOT NULL COMMENT '优化后文本',
    optimization_type VARCHAR(50) NOT NULL COMMENT '优化类型: POLISH, GRAMMAR, DIALOGUE, DESCRIPTION, RHYTHM',
    changes_summary TEXT COMMENT '修改摘要(JSON数组)',
    prompt_used TEXT COMMENT '使用的提示词',
    ai_model VARCHAR(100) COMMENT 'AI模型',
    version INT DEFAULT 1 COMMENT '版本号',
    rating INT COMMENT '评分(1-5)',
    is_applied BOOLEAN DEFAULT false COMMENT '是否已应用',
    applied_at TIMESTAMP COMMENT '应用时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_optimization_type (optimization_type),
    INDEX idx_is_applied (is_applied)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容优化记录表';

-- 优化规则表
CREATE TABLE IF NOT EXISTS optimization_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型: GRAMMAR, STYLE, CONSISTENCY, REDUNDANCY',
    pattern TEXT COMMENT '匹配模式(正则表达式)',
    description TEXT COMMENT '规则描述',
    suggestion TEXT COMMENT '修改建议模板',
    severity VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '严重程度: LOW, MEDIUM, HIGH',
    is_enabled BOOLEAN DEFAULT true COMMENT '是否启用',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rule_type (rule_type),
    INDEX idx_is_enabled (is_enabled),
    INDEX idx_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优化规则表';

-- 风格库表
CREATE TABLE IF NOT EXISTS writing_styles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '风格名称',
    author VARCHAR(200) COMMENT '作者/来源',
    description TEXT COMMENT '风格描述',
    sample_text TEXT NOT NULL COMMENT '示例文本',
    style_features TEXT COMMENT '风格特征(JSON)',
    category VARCHAR(50) COMMENT '分类: CLASSIC, MODERN, FANTASY, SCIFI, MYSTERY, ROMANCE',
    language_complexity VARCHAR(20) COMMENT '语言复杂度: SIMPLE, MEDIUM, COMPLEX',
    sentence_length VARCHAR(20) COMMENT '句子长度: SHORT, MEDIUM, LONG',
    tone VARCHAR(50) COMMENT '语调: FORMAL, CASUAL, POETIC, HUMOROUS, SERIOUS',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    rating DECIMAL(3,2) COMMENT '平均评分',
    is_system BOOLEAN DEFAULT false COMMENT '是否系统风格',
    created_by BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_usage (usage_count DESC),
    INDEX idx_rating (rating DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风格库表';

-- 风格转换记录表
CREATE TABLE IF NOT EXISTS style_conversions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    source_text TEXT NOT NULL COMMENT '原文',
    converted_text TEXT NOT NULL COMMENT '转换后文本',
    source_style_id BIGINT COMMENT '源风格ID',
    target_style_id BIGINT NOT NULL COMMENT '目标风格ID',
    conversion_notes TEXT COMMENT '转换说明',
    style_match_score INT COMMENT '风格匹配度(0-100)',
    prompt_used TEXT COMMENT '使用的提示词',
    ai_model VARCHAR(100) COMMENT 'AI模型',
    version INT DEFAULT 1 COMMENT '版本号',
    rating INT COMMENT '评分(1-5)',
    is_applied BOOLEAN DEFAULT false COMMENT '是否已应用',
    applied_at TIMESTAMP COMMENT '应用时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    FOREIGN KEY (source_style_id) REFERENCES writing_styles(id) ON DELETE SET NULL,
    FOREIGN KEY (target_style_id) REFERENCES writing_styles(id) ON DELETE CASCADE,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_target_style (target_style_id),
    INDEX idx_is_applied (is_applied)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风格转换记录表';

-- 风格分析结果表
CREATE TABLE IF NOT EXISTS style_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    text_sample TEXT NOT NULL COMMENT '分析文本',
    detected_style_id BIGINT COMMENT '检测到的风格ID',
    analysis_result TEXT NOT NULL COMMENT '分析结果(JSON)',
    confidence_score INT COMMENT '置信度(0-100)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    FOREIGN KEY (detected_style_id) REFERENCES writing_styles(id) ON DELETE SET NULL,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_detected_style (detected_style_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风格分析结果表';

-- 情节发展建议表
CREATE TABLE IF NOT EXISTS plot_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    chapter_id BIGINT COMMENT '关联章节ID',
    suggestion_type VARCHAR(50) NOT NULL COMMENT '建议类型: PLOT_TWIST, CONFLICT_ESCALATION, CHARACTER_ARC, FORESHADOWING, PACING',
    title VARCHAR(500) NOT NULL COMMENT '建议标题',
    description TEXT NOT NULL COMMENT '详细描述',
    reasoning TEXT COMMENT '推理依据',
    priority VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '优先级: LOW, MEDIUM, HIGH, URGENT',
    impact_score INT COMMENT '影响力评分(0-100)',
    related_plot_hook_id BIGINT COMMENT '关联伏笔ID',
    related_character_id BIGINT COMMENT '关联角色ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING, ACCEPTED, REJECTED, APPLIED',
    applied_chapter_id BIGINT COMMENT '应用章节ID',
    applied_at TIMESTAMP COMMENT '应用时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    FOREIGN KEY (related_plot_hook_id) REFERENCES plot_hooks(id) ON DELETE SET NULL,
    FOREIGN KEY (related_character_id) REFERENCES characters(id) ON DELETE SET NULL,
    FOREIGN KEY (applied_chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_suggestion_type (suggestion_type),
    INDEX idx_priority (priority),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情节发展建议表';

-- 情节推演记录表
CREATE TABLE IF NOT EXISTS plot_projections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    current_chapter_id BIGINT NOT NULL COMMENT '当前章节ID',
    projection_text TEXT NOT NULL COMMENT '推演内容',
    projection_type VARCHAR(50) COMMENT '推演类型: SHORT_TERM, MEDIUM_TERM, LONG_TERM',
    chapters_ahead INT COMMENT '推演章节数',
    confidence_score INT COMMENT '置信度(0-100)',
    key_events TEXT COMMENT '关键事件(JSON数组)',
    character_changes TEXT COMMENT '角色变化(JSON)',
    plot_threads TEXT COMMENT '情节线(JSON数组)',
    prompt_used TEXT COMMENT '使用的提示词',
    ai_model VARCHAR(100) COMMENT 'AI模型',
    is_adopted BOOLEAN DEFAULT false COMMENT '是否采纳',
    adopted_at TIMESTAMP COMMENT '采纳时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (current_chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_projection_type (projection_type),
    INDEX idx_is_adopted (is_adopted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情节推演记录表';

-- 冲突追踪表
CREATE TABLE IF NOT EXISTS conflict_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    conflict_type VARCHAR(50) NOT NULL COMMENT '冲突类型: INTERNAL, INTERPERSONAL, SOCIAL, NATURAL, SUPERNATURAL',
    title VARCHAR(500) NOT NULL COMMENT '冲突标题',
    description TEXT NOT NULL COMMENT '冲突描述',
    involved_characters TEXT COMMENT '涉及角色(JSON数组)',
    intensity_level INT DEFAULT 50 COMMENT '冲突强度(0-100)',
    introduced_chapter_id BIGINT COMMENT '引入章节ID',
    escalation_points TEXT COMMENT '升级节点(JSON数组)',
    resolution_chapter_id BIGINT COMMENT '解决章节ID',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: BUILDING, ACTIVE, CLIMAX, RESOLVED',
    resolution_type VARCHAR(50) COMMENT '解决方式: WIN, LOSE, COMPROMISE, POSTPONED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (introduced_chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    FOREIGN KEY (resolution_chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_conflict_type (conflict_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='冲突追踪表';

-- AI生成图片表
CREATE TABLE IF NOT EXISTS generated_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    chapter_id BIGINT COMMENT '章节ID',
    character_id BIGINT COMMENT '角色ID',
    scene_id BIGINT COMMENT '场景ID',
    image_type VARCHAR(50) NOT NULL COMMENT '图片类型: CHARACTER, SCENE, COVER, ILLUSTRATION',
    prompt TEXT NOT NULL COMMENT '生成提示词',
    negative_prompt TEXT COMMENT '负面提示词',
    style VARCHAR(100) COMMENT '绘画风格: REALISTIC, ANIME, COMIC, WATERCOLOR, OIL_PAINTING, SKETCH',
    image_url VARCHAR(1000) COMMENT '图片URL',
    local_path VARCHAR(500) COMMENT '本地存储路径',
    width INT COMMENT '宽度',
    height INT COMMENT '高度',
    file_size BIGINT COMMENT '文件大小(字节)',
    ai_model VARCHAR(100) COMMENT 'AI模型',
    generation_params TEXT COMMENT '生成参数(JSON)',
    quality_score INT COMMENT '质量评分(0-100)',
    is_adopted BOOLEAN DEFAULT false COMMENT '是否采用',
    adopted_at TIMESTAMP COMMENT '采用时间',
    tags TEXT COMMENT '标签(JSON数组)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE SET NULL,
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_image_type (image_type),
    INDEX idx_is_adopted (is_adopted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成图片表';

-- 图片生成历史表
CREATE TABLE IF NOT EXISTS image_generation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    prompt TEXT NOT NULL COMMENT '提示词',
    style VARCHAR(100) COMMENT '风格',
    batch_size INT DEFAULT 1 COMMENT '批次数量',
    generated_count INT DEFAULT 0 COMMENT '生成数量',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    cost DECIMAL(10,4) COMMENT '成本',
    duration_seconds INT COMMENT '耗时(秒)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片生成历史表';

-- 图片模板表
CREATE TABLE IF NOT EXISTS image_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '模板名称',
    category VARCHAR(50) NOT NULL COMMENT '分类: CHARACTER, SCENE, COVER, ITEM',
    description TEXT COMMENT '模板描述',
    prompt_template TEXT NOT NULL COMMENT '提示词模板',
    negative_prompt_template TEXT COMMENT '负面提示词模板',
    default_style VARCHAR(100) COMMENT '默认风格',
    recommended_size VARCHAR(50) COMMENT '推荐尺寸',
    preview_url VARCHAR(1000) COMMENT '预览图URL',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    is_system BOOLEAN DEFAULT false COMMENT '是否系统模板',
    created_by BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_usage (usage_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片模板表';

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

-- 续写建议表（续写方向推荐）
CREATE TABLE IF NOT EXISTS continuation_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    analysis_id BIGINT COMMENT '关联的内容分析ID',
    title VARCHAR(200) NOT NULL COMMENT '推荐方向标题',
    description TEXT COMMENT '方向描述',
    story_development TEXT COMMENT '可能的故事发展',
    impact TEXT COMMENT '对故事的影响',
    plot_direction VARCHAR(50) COMMENT '情节走向',
    involved_characters VARCHAR(500) COMMENT '涉及角色（逗号分隔）',
    expected_word_count INT COMMENT '预期字数',
    difficulty_level INT COMMENT '难度系数（1-5）',
    priority INT NOT NULL DEFAULT 5 COMMENT '推荐优先级（1-10）',
    is_adopted BOOLEAN DEFAULT FALSE COMMENT '是否已被采用',
    generated_chapter_id BIGINT COMMENT '采用后生成的章节ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (generated_chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_analysis_id (analysis_id),
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
