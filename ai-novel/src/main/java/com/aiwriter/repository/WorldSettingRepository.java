package com.aiwriter.repository;

import com.aiwriter.entity.WorldSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 世界观设定仓储
 */
@Repository
public interface WorldSettingRepository extends JpaRepository<WorldSetting, Long> {
    /**
     * 查询小说的世界观设定
     */
    List<WorldSetting> findByNovelId(Long novelId);

    /**
     * 查询小说的主要世界观
     */
    Optional<WorldSetting> findFirstByNovelIdOrderByCreatedAtDesc(Long novelId);
}
