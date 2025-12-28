package com.ai.novel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器 - 处理前端路由和HTML页面
 * 
 * 所有非API、非资源扩展名的请求都转发到index.html，让前端的Vue Router处理
 */
@Controller
public class IndexController {

    /**
     * 处理根路径
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * 处理debug.html等诊断页面
     */
    @GetMapping("/debug.html")
    public String debug() {
        return "forward:/debug.html";
    }

    /**
     * 处理前端路由（SPA），避免刷新页面404
     * 当用户访问 /novels/:id 等路由时，转发到index.html让前端处理
     */
    @GetMapping(value = {
        "/novels/**",
        "/chapters/**", 
        "/characters/**",
        "/scenes/**",
        "/outlines/**",
        "/create",
        "/my-novels"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}