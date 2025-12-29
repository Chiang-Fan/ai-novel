package com.aiwriter.controller;

import com.aiwriter.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查Controller
 */
@Tag(name = "系统管理", description = "系统健康检查和状态查询")
@RestController
@RequestMapping("/api")
public class HealthController {
    
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "AI Novel Writer");
        health.put("version", "2.1.0");
        return ApiResponse.success(health);
    }
    
    @Operation(summary = "获取系统信息", description = "获取系统版本和配置信息")
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "AI智能小说创作系统");
        info.put("version", "2.1.0-java");
        info.put("description", "基于Spring Boot 3和Vue 3的全栈小说创作平台");
        info.put("features", new String[]{
            "AI智能续写",
            "场景管理",
            "大纲规划",
            "角色管理",
            "智能推荐",
            "编辑历史"
        });
        return ApiResponse.success(info);
    }
}
