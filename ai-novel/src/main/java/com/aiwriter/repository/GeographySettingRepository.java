package com.aiwriter.repository;

import com.aiwriter.entity.GeographySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 地理位置设定仓储
 */
@Repository
public interface GeographySettingRepository extends JpaRepository<GeographySetting, Long> {
    /**
     * 查询世界观下的所有地理位置
     */
    List<GeographySetting> findByWorldSettingIdOrderBySortOrder(Long worldSettingId);
}
