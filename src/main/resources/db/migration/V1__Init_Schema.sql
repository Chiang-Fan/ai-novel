-- V1__Init_Schema.sql
-- AI智能小说创作系统数据库初始化脚本

-- 小说表
CREATE TABLE novels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(255) NOT NULL COMMENT '书名',
    outline TEXT COMMENT '大纲',
    sample_text TEXT COMMENT '用于风格提取的样本正文',
    writing_style JSON COMMENT '提取的创作风格特征',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '小说状态: DRAFT/WRITING/COMPLETED/PAUSED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小说表';

-- 场景表
CREATE TABLE scenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    scene_number INT NOT NULL COMMENT '场景号',
    title VARCHAR(255) NOT NULL COMMENT '场景标题',
    scene_outline TEXT COMMENT '场景大纲',
    target_chapters INT COMMENT '预计章节数',
    target_word_count INT COMMENT '预计总字数',
    atmosphere VARCHAR(500) COMMENT '场景氛围',
    main_conflict TEXT COMMENT '主要冲突',
    key_events JSON COMMENT '关键事件列表',
    scene_style_notes TEXT COMMENT '场景特定风格说明',
    current_chapters INT DEFAULT 0 COMMENT '当前已写章节数',
    current_word_count INT DEFAULT 0 COMMENT '当前已写字数',
    status VARCHAR(20) DEFAULT 'PLANNING' COMMENT '场景状态: PLANNING/WRITING/COMPLETED/PAUSED',
    order_index INT COMMENT '排序索引',
    parent_volume_id BIGINT COMMENT '所属卷ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_status (status),
    INDEX idx_order (order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场景表';

-- 章节表
CREATE TABLE chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    scene_id BIGINT COMMENT '所属场景ID',
    chapter_number INT NOT NULL COMMENT '章节号(全书)',
    chapter_in_scene INT COMMENT '场景内章节号',
    title VARCHAR(255) COMMENT '章节标题',
    content MEDIUMTEXT COMMENT '章节完整内容',
    summary TEXT COMMENT '章节摘要',
    writing_direction TEXT COMMENT '续写时的方向指导',
    word_count INT DEFAULT 0 COMMENT '字数统计',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '章节状态: DRAFT/COMPLETED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_scene_id (scene_id),
    INDEX idx_chapter_number (chapter_number),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节表';

-- 大纲节点表
CREATE TABLE outline_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    node_type VARCHAR(20) NOT NULL COMMENT '节点类型: VOLUME/SCENE/CHAPTER',
    title VARCHAR(255) NOT NULL COMMENT '节点标题',
    description TEXT COMMENT '节点描述',
    parent_id BIGINT COMMENT '父节点ID',
    order_index INT COMMENT '同级排序',
    level INT DEFAULT 1 COMMENT '层级深度',
    scene_id BIGINT COMMENT '关联场景ID',
    chapter_id BIGINT COMMENT '关联章节ID',
    target_word_count INT COMMENT '目标字数',
    target_chapters INT COMMENT '目标章节数',
    notes TEXT COMMENT '备注',
    is_completed TINYINT DEFAULT 0 COMMENT '是否完成: 0否/1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES outline_nodes(id) ON DELETE CASCADE,
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE SET NULL,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
    INDEX idx_novel_id (novel_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_order (order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大纲节点表';

-- 角色表
CREATE TABLE characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    name VARCHAR(100) NOT NULL COMMENT '人物姓名',
    alias VARCHAR(200) COMMENT '别名/外号,逗号分隔',
    age INT COMMENT '年龄',
    gender VARCHAR(20) COMMENT '性别',
    appearance TEXT COMMENT '外貌描述',
    distinctive_features TEXT COMMENT '显著特征',
    clothing_style TEXT COMMENT '着装风格',
    personality TEXT COMMENT '性格描述',
    personality_tags VARCHAR(500) COMMENT '性格标签,逗号分隔',
    background TEXT COMMENT '背景故事',
    motivation TEXT COMMENT '核心动机',
    fears TEXT COMMENT '恐惧/弱点',
    secrets TEXT COMMENT '秘密',
    abilities TEXT COMMENT '能力/技能',
    power_level VARCHAR(50) COMMENT '实力等级',
    importance_level VARCHAR(20) DEFAULT 'MINOR' COMMENT '重要程度: MAIN/SECONDARY/SUPPORTING/MINOR/CAMEO',
    camp VARCHAR(50) DEFAULT 'NEUTRAL' COMMENT '所属阵营: PROTAGONIST/ANTAGONIST/NEUTRAL/UNKNOWN',
    tags VARCHAR(500) COMMENT '自定义标签,逗号分隔',
    character_type VARCHAR(100) COMMENT '角色类型',
    current_status TEXT COMMENT '当前状态',
    current_location VARCHAR(200) COMMENT '当前位置',
    alive TINYINT DEFAULT 1 COMMENT '是否存活: 0死亡/1存活',
    first_appearance_chapter INT COMMENT '首次出现章节',
    last_appearance_chapter INT COMMENT '最后出现章节',
    appearance_count INT DEFAULT 0 COMMENT '出场次数',
    key_scenes JSON COMMENT '关键场景列表',
    speech_pattern TEXT COMMENT '说话风格/口癖',
    extra_info JSON COMMENT '其他扩展信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_importance (importance_level),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 角色状态更新表
CREATE TABLE character_updates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    character_id BIGINT NOT NULL COMMENT '角色ID',
    chapter_id BIGINT NOT NULL COMMENT '章节ID',
    status_change TEXT NOT NULL COMMENT '状态变化描述',
    emotional_state VARCHAR(100) COMMENT '情感状态',
    physical_state VARCHAR(100) COMMENT '身体状态',
    relationship_changes TEXT COMMENT '关系变化',
    location VARCHAR(200) COMMENT '出现位置',
    key_actions TEXT COMMENT '关键行动',
    dialogue_summary TEXT COMMENT '重要对话摘要',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    FOREIGN KEY (character_id) REFERENCES characters(id) ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
    INDEX idx_character_id (character_id),
    INDEX idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色状态更新表';

-- 角色关系表
CREATE TABLE character_relationships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    source_character_id BIGINT NOT NULL COMMENT '源角色ID',
    target_character_id BIGINT NOT NULL COMMENT '目标角色ID',
    relationship_type VARCHAR(50) NOT NULL COMMENT '关系类型: FAMILY/FRIEND/LOVER/ENEMY/RIVAL等',
    relationship_desc TEXT COMMENT '关系详细描述',
    intimacy_level INT DEFAULT 50 COMMENT '亲密度: 0-100',
    established_chapter INT COMMENT '关系建立章节',
    last_interaction_chapter INT COMMENT '最后互动章节',
    relationship_history JSON COMMENT '关系发展历史',
    is_active TINYINT DEFAULT 1 COMMENT '关系是否有效: 0失效/1有效',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    FOREIGN KEY (source_character_id) REFERENCES characters(id) ON DELETE CASCADE,
    FOREIGN KEY (target_character_id) REFERENCES characters(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_source_character (source_character_id),
    INDEX idx_target_character (target_character_id),
    UNIQUE KEY uk_relationship (source_character_id, target_character_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色关系表';

-- 情节线/伏笔表
CREATE TABLE plot_threads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    title VARCHAR(255) NOT NULL COMMENT '伏笔标题',
    description TEXT NOT NULL COMMENT '伏笔内容描述',
    planted_chapter INT COMMENT '埋下伏笔的章节号',
    expected_reveal_chapter INT COMMENT '期望展开的章节号',
    actual_reveal_chapter INT COMMENT '实际展开的章节号',
    status VARCHAR(20) DEFAULT 'PLANTED' COMMENT '伏笔状态: PLANTED/DEVELOPING/REVEALED/ABANDONED',
    importance_level VARCHAR(20) DEFAULT 'MINOR' COMMENT '重要程度: MAJOR/MINOR',
    tags VARCHAR(500) COMMENT '标签,逗号分隔',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情节线表';

-- 世界观设定表
CREATE TABLE world_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    category VARCHAR(100) NOT NULL COMMENT '设定类别',
    name VARCHAR(255) NOT NULL COMMENT '设定名称',
    description TEXT NOT NULL COMMENT '详细描述',
    rules TEXT COMMENT '相关规则',
    first_mentioned_chapter INT COMMENT '首次提及章节',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='世界观设定表';

-- 编辑历史表
CREATE TABLE edit_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    novel_id BIGINT NOT NULL COMMENT '所属小说ID',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型: scene/outline/chapter/character',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型: create/update/delete',
    field_name VARCHAR(100) COMMENT '修改的字段名',
    old_value TEXT COMMENT '旧值',
    new_value TEXT COMMENT '新值',
    snapshot JSON COMMENT '完整快照(用于恢复)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='编辑历史表';
