package com.aiwriter.repository;

import com.aiwriter.entity.TimePeriodSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 时代背景设定仓储
 */
@Repository
public interface TimePeriodSettingRepository extends JpaRepository<TimePeriodSetting, Long> {
    /**
     * 查询世界观下的所有时代背景
     */
    List<TimePeriodSetting> findByWorldSettingIdOrderByStartYear(Long worldSettingId);
}
