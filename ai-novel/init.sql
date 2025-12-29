-- MySQL初始化脚本
-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库（docker-compose会自动创建，这里仅做备份）
-- CREATE DATABASE IF NOT EXISTS ai_novel_writer 
-- CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_novel_writer;

-- 注意：Spring Boot会通过JPA自动创建表结构
-- 这里只放一些初始化数据或特殊配置

-- 设置时区
SET time_zone = '+08:00';

-- 打印初始化完成信息
SELECT 'Database initialized successfully!' AS message;
