package com.ai.novel.repository;

import com.ai.novel.entity.WorldSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 世界观设定Repository
 */
@Repository
public interface WorldSettingRepository extends JpaRepository<WorldSetting, Long> {

    /**
     * 查询小说的所有世界观设定
     */
    List<WorldSetting> findByNovelId(Long novelId);

    /**
     * 根据类别查询
     */
    List<WorldSetting> findByNovelIdAndCategory(Long novelId, String category);
}
