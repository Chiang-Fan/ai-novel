-- 修复writing_styles表结构
-- 首先备份当前数据
CREATE TABLE writing_styles_backup AS SELECT * FROM writing_styles;

-- 删除原表
DROP TABLE writing_styles;

-- 创建具有正确结构的新表
CREATE TABLE writing_styles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL UNIQUE,
    avg_sentence_length INTEGER,
    frequent_verbs JSON,
    banned_words JSON,
    emotional_distance VARCHAR(255),
    moral_stance VARCHAR(255),
    humor_style VARCHAR(255),
    description_profile JSON,
    author_rules JSON,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    FOREIGN KEY (novel_id) REFERENCES novels(id)
);

-- 从备份恢复数据
INSERT INTO writing_styles (novel_id, avg_sentence_length, frequent_verbs, banned_words, emotional_distance, moral_stance, humor_style, description_profile, author_rules, created_at, updated_at)
SELECT novel_id, avg_sentence_length, frequent_verbs, banned_words, emotional_distance, moral_stance, humor_style, description_profile, author_rules, created_at, updated_at
FROM writing_styles_backup;

-- 删除备份表
DROP TABLE writing_styles_backup;