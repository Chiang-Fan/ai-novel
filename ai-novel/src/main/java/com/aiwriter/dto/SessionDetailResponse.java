package com.aiwriter.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.Map;

/**
 * 会话详情响应（包含所有消息）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SessionDetailResponse extends SessionResponse {
    
    /**
     * 所有消息列表
     */
    private List<MessageResponse> messages;
    
    /**
     * 上下文快照
     */
    private Map<String, Object> contextSnapshot;
}
