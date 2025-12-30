package com.aiwriter.service;

import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 世界观验证与约束服务
 * 确保世界观的一致性和逻辑合理性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorldSettingValidationService {

    private final GeographySettingRepository geographySettingRepository;
    private final RaceSettingRepository raceSettingRepository;
    private final MagicSystemSettingRepository magicSystemSettingRepository;
    private final TimePeriodSettingRepository timePeriodSettingRepository;

    /**
     * 验证世界观的完整性
     */
    public ValidationResult validateWorldSettingCompleteness(Long worldSettingId) {
        log.info("验证世界观 {} 的完整性", worldSettingId);

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // 检查基本设定
        List<GeographySetting> geographies = geographySettingRepository.findByWorldSettingIdOrderBySortOrder(worldSettingId);
        if (geographies.isEmpty()) {
            warnings.add("未设定地理位置，建议至少添加一个主要地理单位");
        }

        List<RaceSetting> races = raceSettingRepository.findByWorldSettingIdOrderByImportanceLevelDesc(worldSettingId);
        if (races.isEmpty()) {
            warnings.add("未设定种族系统，建议定义世界中的主要种族");
        }

        List<MagicSystemSetting> magicSystems = magicSystemSettingRepository.findByWorldSettingIdOrderByImportanceLevelDesc(worldSettingId);
        if (magicSystems.isEmpty()) {
            warnings.add("未设定魔法/超能力系统");
        }

        List<TimePeriodSetting> timePeriods = timePeriodSettingRepository.findByWorldSettingIdOrderByStartYear(worldSettingId);
        if (timePeriods.isEmpty()) {
            warnings.add("未设定时代背景");
        }

        // 检查一致性
        validateRaceGeographyConsistency(worldSettingId, geographies, races, errors, warnings);
        validateTimelineConsistency(worldSettingId, timePeriods, errors, warnings);
        validateMagicSystemPlausibility(worldSettingId, magicSystems, races, errors, warnings);

        return new ValidationResult(
                errors.isEmpty(),
                errors,
                warnings,
                calculateCompleteness(geographies, races, magicSystems, timePeriods)
        );
    }

    /**
     * 检查种族与地理的一致性
     */
    private void validateRaceGeographyConsistency(
            Long worldSettingId,
            List<GeographySetting> geographies,
            List<RaceSetting> races,
            List<String> errors,
            List<String> warnings) {

        for (RaceSetting race : races) {
            if (race.getMainDistribution() == null || race.getMainDistribution().isEmpty()) {
                warnings.add("种族 '" + race.getName() + "' 未指定分布地区");
            } else {
                boolean found = geographies.stream()
                        .anyMatch(g -> race.getMainDistribution().contains(g.getName()));
                if (!found && !geographies.isEmpty()) {
                    warnings.add("种族 '" + race.getName() + "' 的分布地区未在已定义的地理位置中找到，请确保地理设定与种族分布一致");
                }
            }
        }

        // 检查是否有未被任何种族占据的重要地理位置
        for (GeographySetting geo : geographies) {
            if (geo.getImportanceLevel() >= 4) {
                boolean hasDweller = races.stream()
                        .anyMatch(r -> r.getMainDistribution() != null && r.getMainDistribution().contains(geo.getName()));
                if (!hasDweller) {
                    warnings.add("重要地理位置 '" + geo.getName() + "' 未被任何种族占据");
                }
            }
        }
    }

    /**
     * 检查时间轴的一致性
     */
    private void validateTimelineConsistency(
            Long worldSettingId,
            List<TimePeriodSetting> timePeriods,
            List<String> errors,
            List<String> warnings) {

        Integer previousEnd = null;
        for (TimePeriodSetting period : timePeriods) {
            if (period.getStartYear() != null && period.getEndYear() != null) {
                if (period.getStartYear() > period.getEndYear()) {
                    errors.add("时期 '" + period.getName() + "' 的开始年份 (" + period.getStartYear() + ") 大于结束年份 (" + period.getEndYear() + ")");
                }

                if (previousEnd != null && period.getStartYear() > previousEnd + 1) {
                    warnings.add("时期 '" + period.getName() + "' 与前一时期之间存在时间间隙");
                }
                previousEnd = period.getEndYear();
            }
        }
    }

    /**
     * 检查魔法系统的合理性
     */
    private void validateMagicSystemPlausibility(
            Long worldSettingId,
            List<MagicSystemSetting> magicSystems,
            List<RaceSetting> races,
            List<String> errors,
            List<String> warnings) {

        for (MagicSystemSetting magic : magicSystems) {
            if (magic.getPowerLevels() == null || magic.getPowerLevels().isEmpty()) {
                warnings.add("魔法系统 '" + magic.getName() + "' 未定义能力等级划分");
            }

            if (magic.getLimitations() == null || magic.getLimitations().isEmpty()) {
                warnings.add("魔法系统 '" + magic.getName() + "' 未定义使用限制，建议添加平衡性约束");
            }

            // 检查是否有种族能够使用该系统
            if (magic.getPractitioners() != null && !magic.getPractitioners().isEmpty()) {
                boolean foundPractitioner = races.stream()
                        .anyMatch(r -> magic.getPractitioners().contains(r.getName()));
                if (!foundPractitioner && !races.isEmpty()) {
                    warnings.add("魔法系统 '" + magic.getName() + "' 的使用者未在已定义的种族中找到");
                }
            }
        }
    }

    /**
     * 计算世界观完整度百分比
     */
    private int calculateCompleteness(
            List<GeographySetting> geographies,
            List<RaceSetting> races,
            List<MagicSystemSetting> magicSystems,
            List<TimePeriodSetting> timePeriods) {

        int total = 4;
        int completed = 0;

        if (!geographies.isEmpty()) completed++;
        if (!races.isEmpty()) completed++;
        if (!magicSystems.isEmpty()) completed++;
        if (!timePeriods.isEmpty()) completed++;

        return (completed * 100) / total;
    }

    /**
     * 验证场景是否与世界观一致
     */
    public boolean validateSceneConsistency(Scene scene, WorldSetting worldSetting) {
        // 检查场景的地理位置是否在世界观中被定义
        List<GeographySetting> geographies = geographySettingRepository.findByWorldSettingIdOrderBySortOrder(worldSetting.getId());

        if (scene.getLocation() != null) {
            boolean locationExists = geographies.stream()
                    .anyMatch(g -> g.getName().equals(scene.getLocation()));

            if (!locationExists) {
                log.warn("场景位置 '{}' 未在世界观中定义", scene.getLocation());
                return false;
            }
        }

        return true;
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final List<String> errors;
        public final List<String> warnings;
        public final int completenessPercentage;

        public ValidationResult(boolean isValid, List<String> errors, List<String> warnings, int completenessPercentage) {
            this.isValid = isValid;
            this.errors = errors;
            this.warnings = warnings;
            this.completenessPercentage = completenessPercentage;
        }
    }
}
