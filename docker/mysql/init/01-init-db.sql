-- ==============================================================================
-- AI Novel Writer 数据库初始化脚本
-- 此脚本会在MySQL容器首次启动时自动执行
-- ==============================================================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库（docker-compose已创建，此处仅确保存在）
CREATE DATABASE IF NOT EXISTS ai_novel_writer 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE ai_novel_writer;

-- ==============================================================================
-- 授予用户权限
-- ==============================================================================
-- 注意: docker-compose会自动创建用户，此处仅确保权限正确
GRANT ALL PRIVILEGES ON ai_novel_writer.* TO 'novel_user'@'%';
FLUSH PRIVILEGES;

-- ==============================================================================
-- 创建测试数据（可选）
-- ==============================================================================
-- 注意: Flyway会自动创建表结构，此处仅添加测试数据

-- 等待Flyway创建表后，可以在这里添加初始数据
-- 例如:
-- INSERT INTO novels (title, author, type, status, created_at, updated_at) 
-- VALUES ('示例小说', '系统管理员', '玄幻', 'PLANNING', NOW(), NOW());

SELECT '数据库初始化完成' AS message;
