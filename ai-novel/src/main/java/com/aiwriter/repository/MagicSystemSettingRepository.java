package com.aiwriter.repository;

import com.aiwriter.entity.MagicSystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 魔法系统设定仓储
 */
@Repository
public interface MagicSystemSettingRepository extends JpaRepository<MagicSystemSetting, Long> {
    /**
     * 查询世界观下的所有魔法系统
     */
    List<MagicSystemSetting> findByWorldSettingIdOrderByImportanceLevelDesc(Long worldSettingId);
}
