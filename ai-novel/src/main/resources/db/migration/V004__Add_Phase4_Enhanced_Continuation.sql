-- Phase 4: AI续写增强 - 数据库迁移脚本

-- 1. 扩展 ai_continuations 表以支持新的约束信息
ALTER TABLE ai_continuations ADD COLUMN IF NOT EXISTS `constraint_metadata` LONGTEXT COMMENT '多维约束元数据(JSON)';
ALTER TABLE ai_continuations ADD COLUMN IF NOT EXISTS `quality_score` INT COMMENT '描写质量评分(0-100)';
ALTER TABLE ai_continuations ADD COLUMN IF NOT EXISTS `consistency_score` INT COMMENT '情节一致性评分(0-100)';
ALTER TABLE ai_continuations ADD COLUMN IF NOT EXISTS `constraint_violations` TEXT COMMENT '约束违规列表(JSON)';
ALTER TABLE ai_continuations ADD COLUMN IF NOT EXISTS `refinement_iteration` INT DEFAULT 1 COMMENT '迭代优化次数';

CREATE INDEX IF NOT EXISTS idx_quality_score ON ai_continuations(quality_score);
CREATE INDEX IF NOT EXISTS idx_consistency_score ON ai_continuations(consistency_score);

-- 2. 创建约束记录表
CREATE TABLE IF NOT EXISTS `constraint_application_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `novel_id` BIGINT NOT NULL COMMENT '小说ID',
  `chapter_id` BIGINT NOT NULL COMMENT '章节ID',
  `continuation_id` BIGINT COMMENT '续写ID',
  `constraint_type` VARCHAR(50) COMMENT '约束类型(WORLD/SCENE/CHARACTER/STYLE/PLOT)',
  `constraint_content` TEXT COMMENT '约束内容',
  `applied` TINYINT DEFAULT 0 COMMENT '是否应用',
  `effectiveness_score` INT COMMENT '约束有效性评分(0-100)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
  FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
  FOREIGN KEY (continuation_id) REFERENCES ai_continuations(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='约束应用日志表';

CREATE INDEX idx_novel_constraint ON constraint_application_logs(novel_id, constraint_type);
CREATE INDEX idx_continuation_constraint ON constraint_application_logs(continuation_id);

-- 3. 创建情节一致性检查记录表
CREATE TABLE IF NOT EXISTS `plot_consistency_checks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `novel_id` BIGINT NOT NULL COMMENT '小说ID',
  `chapter_id` BIGINT NOT NULL COMMENT '章节ID',
  `continuation_id` BIGINT COMMENT '续写ID',
  `consistency_score` INT COMMENT '一致性得分(0-100)',
  `conflict_count` INT DEFAULT 0 COMMENT '冲突数量',
  `conflicts_detail` LONGTEXT COMMENT '冲突详情(JSON)',
  `logical_issues` LONGTEXT COMMENT '逻辑问题(JSON)',
  `style_consistency_score` INT COMMENT '风格一致性评分',
  `character_consistency_score` INT COMMENT '角色一致性评分',
  `timeline_valid` TINYINT DEFAULT 1 COMMENT '时间线是否有效',
  `recommendations` LONGTEXT COMMENT '改进建议(JSON)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
  FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,
  FOREIGN KEY (continuation_id) REFERENCES ai_continuations(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情节一致性检查记录表';

CREATE INDEX idx_consistency_novel ON plot_consistency_checks(novel_id);
CREATE INDEX idx_consistency_chapter ON plot_consistency_checks(chapter_id);
CREATE INDEX idx_consistency_score ON plot_consistency_checks(consistency_score);

-- 4. 创建描写质量评分记录表
CREATE TABLE IF NOT EXISTS `description_quality_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `novel_id` BIGINT NOT NULL COMMENT '小说ID',
  `chapter_id` BIGINT COMMENT '章节ID',
  `continuation_id` BIGINT COMMENT '续写ID',
  `overall_score` INT COMMENT '总体得分(0-100)',
  `naturalness_score` INT COMMENT '自然度评分',
  `detail_level_score` INT COMMENT '细节度评分',
  `dialogue_score` INT COMMENT '对话评分',
  `psychological_score` INT COMMENT '心理描写评分',
  `environment_score` INT COMMENT '环境描写评分',
  `emotional_score` INT COMMENT '情绪描写评分',
  `action_score` INT COMMENT '动作描写评分',
  `feedbacks` LONGTEXT COMMENT '详细反馈(JSON)',
  `suggestions` LONGTEXT COMMENT '改进建议(JSON)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
  FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE SET NULL,
  FOREIGN KEY (continuation_id) REFERENCES ai_continuations(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='描写质量评分记录表';

CREATE INDEX idx_quality_novel ON description_quality_records(novel_id);
CREATE INDEX idx_quality_score ON description_quality_records(overall_score);
CREATE INDEX idx_quality_continuation ON description_quality_records(continuation_id);

-- 5. 创建约束优化迭代记录表
CREATE TABLE IF NOT EXISTS `continuation_refinement_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `continuation_id` BIGINT NOT NULL COMMENT '续写ID',
  `iteration_number` INT COMMENT '迭代次数',
  `previous_text` LONGTEXT COMMENT '优化前文本',
  `refined_text` LONGTEXT COMMENT '优化后文本',
  `quality_score_before` INT COMMENT '优化前评分',
  `quality_score_after` INT COMMENT '优化后评分',
  `refinement_prompt` LONGTEXT COMMENT '优化提示词',
  `improvement_points` LONGTEXT COMMENT '改进点(JSON)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (continuation_id) REFERENCES ai_continuations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='续写优化迭代历史表';

CREATE INDEX idx_refinement_continuation ON continuation_refinement_history(continuation_id);
CREATE INDEX idx_refinement_iteration ON continuation_refinement_history(iteration_number);

-- 6. 创建约束实时监控表
CREATE TABLE IF NOT EXISTS `constraint_effectiveness_metrics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  `novel_id` BIGINT NOT NULL COMMENT '小说ID',
  `constraint_type` VARCHAR(50) COMMENT '约束类型',
  `total_applications` INT DEFAULT 0 COMMENT '总应用次数',
  `successful_applications` INT DEFAULT 0 COMMENT '成功应用次数',
  `average_effectiveness` DECIMAL(5,2) COMMENT '平均有效性',
  `user_satisfaction` INT COMMENT '用户满意度评分(0-100)',
  `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
  UNIQUE KEY uk_novel_constraint (novel_id, constraint_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='约束有效性监控表';

-- 7. 更新 ai_continuations 表的索引以优化查询
ALTER TABLE ai_continuations ADD INDEX IF NOT EXISTS idx_novel_style (novel_id, style);
ALTER TABLE ai_continuations ADD INDEX IF NOT EXISTS idx_quality_metrics (quality_score, consistency_score);

-- 8. 添加评论/反馈表支持用户反馈约束有效性
ALTER TABLE ai_continuations ADD COLUMN IF NOT EXISTS `user_feedback` VARCHAR(50) COMMENT '用户反馈(EXCELLENT/GOOD/FAIR/POOR)';
ALTER TABLE ai_continuations ADD COLUMN IF NOT EXISTS `feedback_timestamp` TIMESTAMP COMMENT '反馈时间';

COMMIT;
