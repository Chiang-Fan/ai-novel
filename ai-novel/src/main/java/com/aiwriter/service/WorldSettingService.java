package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 世界观设定服务
 * 管理小说的世界观多维设定
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorldSettingService {

    private final WorldSettingRepository worldSettingRepository;
    private final GeographySettingRepository geographySettingRepository;
    private final TimePeriodSettingRepository timePeriodSettingRepository;
    private final RaceSettingRepository raceSettingRepository;
    private final MagicSystemSettingRepository magicSystemSettingRepository;

    /**
     * 创建世界观设定
     */
    @Transactional
    public WorldSetting createWorldSetting(WorldSettingRequest request) {
        log.info("创建世界观: {}", request.getName());

        WorldSetting worldSetting = new WorldSetting();
        worldSetting.setNovelId(request.getNovelId());
        worldSetting.setName(request.getName());
        worldSetting.setDescription(request.getDescription());
        worldSetting.setCosmicBackground(request.getCosmicBackground());
        worldSetting.setStatus("draft");

        return worldSettingRepository.save(worldSetting);
    }

    /**
     * 获取小说的世界观设定
     */
    public WorldSetting getWorldSetting(Long worldSettingId) {
        return worldSettingRepository.findById(worldSettingId)
                .orElseThrow(() -> new RuntimeException("世界观设定不存在"));
    }

    /**
     * 获取小说的所有世界观
     */
    public List<WorldSetting> getNovelWorldSettings(Long novelId) {
        return worldSettingRepository.findByNovelId(novelId);
    }

    /**
     * 发布世界观设定
     */
    @Transactional
    public WorldSetting publishWorldSetting(Long worldSettingId) {
        WorldSetting worldSetting = getWorldSetting(worldSettingId);
        worldSetting.setStatus("published");
        worldSetting.setVersion(worldSetting.getVersion() + 1);
        log.info("发布世界观: {} (版本: {})", worldSettingId, worldSetting.getVersion());
        return worldSettingRepository.save(worldSetting);
    }

    /**
     * 创建地理位置设定
     */
    @Transactional
    public GeographySetting createGeographySetting(GeographySettingRequest request) {
        log.info("创建地理位置: {}", request.getName());

        GeographySetting geography = new GeographySetting();
        geography.setWorldSettingId(request.getWorldSettingId());
        geography.setName(request.getName());
        geography.setGeographyType(request.getGeographyType());
        geography.setTerrainType(request.getTerrainType());
        geography.setDescription(request.getDescription());
        geography.setImportanceLevel(request.getImportanceLevel());
        geography.setClimate(request.getClimate());
        geography.setSpecialFeatures(request.getSpecialFeatures());
        geography.setSortOrder(request.getSortOrder());

        return geographySettingRepository.save(geography);
    }

    /**
     * 获取世界观的所有地理位置
     */
    public List<GeographySetting> getGeographySettings(Long worldSettingId) {
        return geographySettingRepository.findByWorldSettingIdOrderBySortOrder(worldSettingId);
    }

    /**
     * 创建时代背景设定
     */
    @Transactional
    public TimePeriodSetting createTimePeriodSetting(TimePeriodSettingRequest request) {
        log.info("创建时代背景: {}", request.getName());

        TimePeriodSetting timePeriod = new TimePeriodSetting();
        timePeriod.setWorldSettingId(request.getWorldSettingId());
        timePeriod.setName(request.getName());
        timePeriod.setPeriodType(request.getPeriodType());
        timePeriod.setTimeDescription(request.getTimeDescription());
        timePeriod.setHistoricalBackground(request.getHistoricalBackground());
        timePeriod.setMajorEvents(request.getMajorEvents());
        timePeriod.setEraCharacteristics(request.getEraCharacteristics());
        timePeriod.setSocialStructure(request.getSocialStructure());
        timePeriod.setStartYear(request.getStartYear());
        timePeriod.setEndYear(request.getEndYear());

        return timePeriodSettingRepository.save(timePeriod);
    }

    /**
     * 获取世界观的所有时代背景
     */
    public List<TimePeriodSetting> getTimePeriodSettings(Long worldSettingId) {
        return timePeriodSettingRepository.findByWorldSettingIdOrderByStartYear(worldSettingId);
    }

    /**
     * 创建种族设定
     */
    @Transactional
    public RaceSetting createRaceSetting(RaceSettingRequest request) {
        log.info("创建种族设定: {}", request.getName());

        RaceSetting race = new RaceSetting();
        race.setWorldSettingId(request.getWorldSettingId());
        race.setName(request.getName());
        race.setRaceCategory(request.getRaceCategory());
        race.setPhysicalCharacteristics(request.getPhysicalCharacteristics());
        race.setPersonalityTraits(request.getPersonalityTraits());
        race.setCulturalCustoms(request.getCulturalCustoms());
        race.setAbilities(request.getAbilities());
        race.setLifespanYears(request.getLifespanYears());
        race.setSocialStatus(request.getSocialStatus());
        race.setRaceRelations(request.getRaceRelations());
        race.setMainDistribution(request.getMainDistribution());
        race.setImportanceLevel(request.getImportanceLevel());

        return raceSettingRepository.save(race);
    }

    /**
     * 获取世界观的所有种族
     */
    public List<RaceSetting> getRaceSettings(Long worldSettingId) {
        return raceSettingRepository.findByWorldSettingIdOrderByImportanceLevelDesc(worldSettingId);
    }

    /**
     * 创建魔法系统设定
     */
    @Transactional
    public MagicSystemSetting createMagicSystemSetting(MagicSystemSettingRequest request) {
        log.info("创建魔法系统: {}", request.getName());

        MagicSystemSetting magicSystem = new MagicSystemSetting();
        magicSystem.setWorldSettingId(request.getWorldSettingId());
        magicSystem.setName(request.getName());
        magicSystem.setSystemType(request.getSystemType());
        magicSystem.setCoreRules(request.getCoreRules());
        magicSystem.setPowerLevels(request.getPowerLevels());
        magicSystem.setCultivationMethods(request.getCultivationMethods());
        magicSystem.setMainCategories(request.getMainCategories());
        magicSystem.setLimitations(request.getLimitations());
        magicSystem.setEnergySources(request.getEnergySources());
        magicSystem.setPractitioners(request.getPractitioners());
        magicSystem.setHistoricalBackground(request.getHistoricalBackground());
        magicSystem.setImportanceLevel(request.getImportanceLevel());

        return magicSystemSettingRepository.save(magicSystem);
    }

    /**
     * 获取世界观的所有魔法系统
     */
    public List<MagicSystemSetting> getMagicSystemSettings(Long worldSettingId) {
        return magicSystemSettingRepository.findByWorldSettingIdOrderByImportanceLevelDesc(worldSettingId);
    }

    /**
     * 更新魔法系统设定
     */
    @Transactional
    public MagicSystemSetting updateMagicSystemSetting(Long id, MagicSystemSettingRequest request) {
        MagicSystemSetting magicSystem = magicSystemSettingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("魔法系统设定不存在"));

        magicSystem.setName(request.getName());
        magicSystem.setSystemType(request.getSystemType());
        magicSystem.setCoreRules(request.getCoreRules());
        magicSystem.setPowerLevels(request.getPowerLevels());
        magicSystem.setCultivationMethods(request.getCultivationMethods());
        magicSystem.setMainCategories(request.getMainCategories());
        magicSystem.setLimitations(request.getLimitations());
        magicSystem.setEnergySources(request.getEnergySources());
        magicSystem.setPractitioners(request.getPractitioners());
        magicSystem.setHistoricalBackground(request.getHistoricalBackground());
        magicSystem.setImportanceLevel(request.getImportanceLevel());

        log.info("更新魔法系统: {}", id);
        return magicSystemSettingRepository.save(magicSystem);
    }

    /**
     * 删除魔法系统设定
     */
    @Transactional
    public void deleteMagicSystemSetting(Long id) {
        magicSystemSettingRepository.deleteById(id);
        log.info("删除魔法系统: {}", id);
    }
}
