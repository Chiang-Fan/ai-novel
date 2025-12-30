package com.aiwriter.repository;

import com.aiwriter.entity.RaceSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 种族系统设定仓储
 */
@Repository
public interface RaceSettingRepository extends JpaRepository<RaceSetting, Long> {
    /**
     * 查询世界观下的所有种族设定
     */
    List<RaceSetting> findByWorldSettingIdOrderByImportanceLevelDesc(Long worldSettingId);
}
