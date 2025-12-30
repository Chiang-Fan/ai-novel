package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import com.aiwriter.dto.*;
import com.aiwriter.entity.*;
import com.aiwriter.service.WorldSettingService;
import com.aiwriter.service.WorldSettingValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 世界观设定控制器
 */
@RestController
@RequestMapping("/api/world-settings")
@RequiredArgsConstructor
public class WorldSettingController {

    private final WorldSettingService worldSettingService;
    private final WorldSettingValidationService validationService;

    /**
     * 创建世界观
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WorldSetting>> createWorldSetting(
            @Valid @RequestBody WorldSettingRequest request) {
        WorldSetting worldSetting = worldSettingService.createWorldSetting(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(worldSetting, "世界观创建成功"));
    }

    /**
     * 获取世界观详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorldSetting>> getWorldSetting(@PathVariable Long id) {
        WorldSetting worldSetting = worldSettingService.getWorldSetting(id);
        return ResponseEntity.ok(ApiResponse.success(worldSetting));
    }

    /**
     * 获取小说的所有世界观
     */
    @GetMapping("/novel/{novelId}")
    public ResponseEntity<ApiResponse<List<WorldSetting>>> getNovelWorldSettings(@PathVariable Long novelId) {
        List<WorldSetting> worldSettings = worldSettingService.getNovelWorldSettings(novelId);
        return ResponseEntity.ok(ApiResponse.success(worldSettings));
    }

    /**
     * 发布世界观
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<WorldSetting>> publishWorldSetting(@PathVariable Long id) {
        WorldSetting worldSetting = worldSettingService.publishWorldSetting(id);
        return ResponseEntity.ok(ApiResponse.success(worldSetting, "世界观发布成功"));
    }

    // ========== 地理位置接口 ==========

    /**
     * 创建地理位置
     */
    @PostMapping("/geography")
    public ResponseEntity<ApiResponse<GeographySetting>> createGeography(
            @Valid @RequestBody GeographySettingRequest request) {
        GeographySetting geography = worldSettingService.createGeographySetting(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(geography, "地理位置创建成功"));
    }

    /**
     * 获取世界观的所有地理位置
     */
    @GetMapping("/{worldSettingId}/geography")
    public ResponseEntity<ApiResponse<List<GeographySetting>>> getGeographies(@PathVariable Long worldSettingId) {
        List<GeographySetting> geographies = worldSettingService.getGeographySettings(worldSettingId);
        return ResponseEntity.ok(ApiResponse.success(geographies));
    }

    // ========== 时代背景接口 ==========

    /**
     * 创建时代背景
     */
    @PostMapping("/time-period")
    public ResponseEntity<ApiResponse<TimePeriodSetting>> createTimePeriod(
            @Valid @RequestBody TimePeriodSettingRequest request) {
        TimePeriodSetting timePeriod = worldSettingService.createTimePeriodSetting(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(timePeriod, "时代背景创建成功"));
    }

    /**
     * 获取世界观的所有时代背景
     */
    @GetMapping("/{worldSettingId}/time-periods")
    public ResponseEntity<ApiResponse<List<TimePeriodSetting>>> getTimePeriods(@PathVariable Long worldSettingId) {
        List<TimePeriodSetting> timePeriods = worldSettingService.getTimePeriodSettings(worldSettingId);
        return ResponseEntity.ok(ApiResponse.success(timePeriods));
    }

    // ========== 种族系统接口 ==========

    /**
     * 创建种族设定
     */
    @PostMapping("/race")
    public ResponseEntity<ApiResponse<RaceSetting>> createRace(
            @Valid @RequestBody RaceSettingRequest request) {
        RaceSetting race = worldSettingService.createRaceSetting(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(race, "种族创建成功"));
    }

    /**
     * 获取世界观的所有种族
     */
    @GetMapping("/{worldSettingId}/races")
    public ResponseEntity<ApiResponse<List<RaceSetting>>> getRaces(@PathVariable Long worldSettingId) {
        List<RaceSetting> races = worldSettingService.getRaceSettings(worldSettingId);
        return ResponseEntity.ok(ApiResponse.success(races));
    }

    // ========== 魔法系统接口 ==========

    /**
     * 创建魔法系统
     */
    @PostMapping("/magic-system")
    public ResponseEntity<ApiResponse<MagicSystemSetting>> createMagicSystem(
            @Valid @RequestBody MagicSystemSettingRequest request) {
        MagicSystemSetting magicSystem = worldSettingService.createMagicSystemSetting(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(magicSystem, "魔法系统创建成功"));
    }

    /**
     * 获取世界观的所有魔法系统
     */
    @GetMapping("/{worldSettingId}/magic-systems")
    public ResponseEntity<ApiResponse<List<MagicSystemSetting>>> getMagicSystems(@PathVariable Long worldSettingId) {
        List<MagicSystemSetting> magicSystems = worldSettingService.getMagicSystemSettings(worldSettingId);
        return ResponseEntity.ok(ApiResponse.success(magicSystems));
    }

    /**
     * 更新魔法系统
     */
    @PutMapping("/magic-system/{id}")
    public ResponseEntity<ApiResponse<MagicSystemSetting>> updateMagicSystem(
            @PathVariable Long id,
            @Valid @RequestBody MagicSystemSettingRequest request) {
        MagicSystemSetting magicSystem = worldSettingService.updateMagicSystemSetting(id, request);
        return ResponseEntity.ok(ApiResponse.success(magicSystem, "魔法系统更新成功"));
    }

    /**
     * 删除魔法系统
     */
    @DeleteMapping("/magic-system/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMagicSystem(@PathVariable Long id) {
        worldSettingService.deleteMagicSystemSetting(id);
        return ResponseEntity.ok(ApiResponse.success(null, "魔法系统删除成功"));
    }

    /**
     * 验证世界观的完整性与一致性
     */
    @GetMapping("/{worldSettingId}/validate")
    public ResponseEntity<ApiResponse<WorldSettingValidationService.ValidationResult>> validateWorldSetting(
            @PathVariable Long worldSettingId) {
        WorldSettingValidationService.ValidationResult result = validationService.validateWorldSettingCompleteness(worldSettingId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
