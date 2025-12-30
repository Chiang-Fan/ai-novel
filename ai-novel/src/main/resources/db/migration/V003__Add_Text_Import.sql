-- 文本导入记录表
CREATE TABLE IF NOT EXISTS text_imports (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    novel_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20),
    raw_content LONGTEXT,
    cleaned_content LONGTEXT,
    chapter_count INT DEFAULT 0,
    total_words INT DEFAULT 0,
    extracted_locations TEXT,
    extracted_characters TEXT,
    extracted_events TEXT,
    status VARCHAR(20) DEFAULT 'uploaded',
    error_message TEXT,
    confirmed BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_id (novel_id),
    INDEX idx_status (status),
    INDEX idx_confirmed (confirmed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 导入的章节表
CREATE TABLE IF NOT EXISTS imported_chapters (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    text_import_id BIGINT NOT NULL,
    novel_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content LONGTEXT,
    chapter_number INT NOT NULL,
    word_count INT DEFAULT 0,
    paragraph_count INT DEFAULT 0,
    converted_to_chapter BOOLEAN DEFAULT FALSE,
    converted_chapter_id BIGINT,
    quality_score DOUBLE,
    suggestions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (text_import_id) REFERENCES text_imports(id) ON DELETE CASCADE,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_text_import_id (text_import_id),
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_number (chapter_number),
    INDEX idx_converted_to_chapter (converted_to_chapter)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建索引用于查询性能优化
CREATE INDEX idx_imported_chapters_import_and_number 
ON imported_chapters(text_import_id, chapter_number);

CREATE INDEX idx_text_imports_novel_and_status 
ON text_imports(novel_id, status);
