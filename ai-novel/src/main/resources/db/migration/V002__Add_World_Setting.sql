-- 世界观设定主表
CREATE TABLE IF NOT EXISTS world_settings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cosmic_background TEXT,
    ai_assisted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 地理位置设定表
CREATE TABLE IF NOT EXISTS geography_settings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    world_setting_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    geography_type VARCHAR(50),
    terrain_type VARCHAR(50),
    description TEXT,
    importance_level INT DEFAULT 3,
    climate VARCHAR(200),
    special_features TEXT,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (world_setting_id) REFERENCES world_settings(id) ON DELETE CASCADE,
    INDEX idx_world_setting_id (world_setting_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 时代背景设定表
CREATE TABLE IF NOT EXISTS time_period_settings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    world_setting_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    period_type VARCHAR(50),
    time_description VARCHAR(200),
    historical_background TEXT,
    major_events TEXT,
    era_characteristics TEXT,
    social_structure TEXT,
    start_year INT,
    end_year INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (world_setting_id) REFERENCES world_settings(id) ON DELETE CASCADE,
    INDEX idx_world_setting_id (world_setting_id),
    INDEX idx_start_year (start_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 种族系统设定表
CREATE TABLE IF NOT EXISTS race_settings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    world_setting_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    race_category VARCHAR(50),
    physical_characteristics TEXT,
    personality_traits TEXT,
    cultural_customs TEXT,
    abilities TEXT,
    lifespan_years VARCHAR(100),
    social_status VARCHAR(50),
    race_relations TEXT,
    main_distribution VARCHAR(200),
    importance_level INT DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (world_setting_id) REFERENCES world_settings(id) ON DELETE CASCADE,
    INDEX idx_world_setting_id (world_setting_id),
    INDEX idx_importance_level (importance_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 魔法系统设定表
CREATE TABLE IF NOT EXISTS magic_system_settings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    world_setting_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    system_type VARCHAR(50),
    core_rules TEXT,
    power_levels TEXT,
    cultivation_methods TEXT,
    main_categories TEXT,
    limitations TEXT,
    energy_sources TEXT,
    practitioners VARCHAR(200),
    historical_background TEXT,
    importance_level INT DEFAULT 4,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (world_setting_id) REFERENCES world_settings(id) ON DELETE CASCADE,
    INDEX idx_world_setting_id (world_setting_id),
    INDEX idx_importance_level (importance_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
