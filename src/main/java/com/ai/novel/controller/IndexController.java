package com.ai.novel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 首页控制器 - 处理前端路由
 */
@Controller
public class IndexController {

    /**
     * 处理根路径，返回前端首页
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * 处理前端路由，避免刷新页面404
     * 所有非API、非静态资源路径都转发到index.html，让前端路由处理
     */
    @GetMapping(value = {
        "/novels/**",
        "/chapters/**", 
        "/characters/**",
        "/scenes/**",
        "/outlines/**"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}