package com.ai.novel.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 小说未找到
     */
    public static ResourceNotFoundException novel(Long id) {
        return new ResourceNotFoundException("小说不存在: " + id);
    }
    
    /**
     * 章节未找到
     */
    public static ResourceNotFoundException chapter(Long id) {
        return new ResourceNotFoundException("章节不存在: " + id);
    }
    
    /**
     * 角色未找到
     */
    public static ResourceNotFoundException character(Long id) {
        return new ResourceNotFoundException("角色不存在: " + id);
    }
    
    /**
     * 场景未找到
     */
    public static ResourceNotFoundException scene(Long id) {
        return new ResourceNotFoundException("场景不存在: " + id);
    }
}
