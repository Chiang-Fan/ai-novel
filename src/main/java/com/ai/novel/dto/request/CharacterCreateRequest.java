package com.ai.novel.dto.request;

import com.ai.novel.entity.enums.CharacterImportance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色创建请求
 */
@Data
public class CharacterCreateRequest {
    
    @NotNull(message = "小说ID不能为空")
    private Long novelId;
    
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100字符")
    private String name;
    
    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;
    
    @Size(max = 1000, message = "性格描述不能超过1000字符")
    private String personality;
    
    @Size(max = 2000, message = "背景故事不能超过2000字符")
    private String backstory;
    
    @NotNull(message = "重要性级别不能为空")
    private CharacterImportance importance;
    
    private Integer age;
    
    @Size(max = 50, message = "性别长度不能超过50字符")
    private String gender;
    
    @Size(max = 1000, message = "外貌描述不能超过1000字符")
    private String appearance;
    
    @Size(max = 2000, message = "能力描述不能超过2000字符")
    private String abilities;
    
    @Size(max = 1000, message = "目标描述不能超过1000字符")
    private String goals;
    
    @Size(max = 1000, message = "当前状态不能超过1000字符")
    private String currentState;
}
