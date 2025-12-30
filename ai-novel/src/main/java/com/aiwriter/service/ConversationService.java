package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import com.aiwriter.service.ai.AiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI对话服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {
    
    private final ConversationSessionRepository sessionRepository;
    private final ConversationMessageRepository messageRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final CharacterRepository characterRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建新会话
     */
    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {
        // 验证小说存在
        Novel novel = novelRepository.findById(request.getNovelId())
            .orElseThrow(() -> new RuntimeException("小说不存在"));
        
        // 构建上下文快照
        Map<String, Object> contextSnapshot = buildContextSnapshot(request.getNovelId(), request.getChapterId());
        
        // 创建会话
        ConversationSession session = new ConversationSession();
        session.setNovelId(request.getNovelId());
        session.setChapterId(request.getChapterId());
        session.setSessionType(request.getSessionType());
        session.setStatus("ACTIVE");
        
        // 设置标题
        if (request.getSessionTitle() != null && !request.getSessionTitle().isEmpty()) {
            session.setSessionTitle(request.getSessionTitle());
        } else {
            session.setSessionTitle(generateSessionTitle(request.getSessionType(), novel, request.getChapterId()));
        }
        
        // 保存上下文快照
        try {
            session.setContextSnapshot(objectMapper.writeValueAsString(contextSnapshot));
        } catch (Exception e) {
            log.error("序列化上下文快照失败", e);
        }
        
        session = sessionRepository.save(session);
        
        // 如果有初始消息，发送它
        if (request.getInitialMessage() != null && !request.getInitialMessage().isEmpty()) {
            SendMessageRequest sendRequest = new SendMessageRequest();
            sendRequest.setSessionId(session.getId());
            sendRequest.setContent(request.getInitialMessage());
            sendRequest.setIncludeContext(true);
            
            try {
                sendMessage(sendRequest);
            } catch (Exception e) {
                log.error("发送初始消息失败", e);
            }
        }
        
        return convertToSessionResponse(session);
    }
    
    /**
     * 发送消息并获取AI回复
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 获取会话
        ConversationSession session = sessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        // 保存用户消息
        ConversationMessage userMessage = new ConversationMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole("USER");
        userMessage.setContent(request.getContent());
        userMessage.setMessageType("TEXT");
        userMessage = messageRepository.save(userMessage);
        
        // 构建系统提示词
        String systemPrompt = buildSystemPrompt(session, request.getIncludeContext());
        
        // 构建对话历史（最近10轮）
        List<ConversationMessage> recentMessages = messageRepository.findRecentMessagesBySessionId(session.getId(), 20);
        Collections.reverse(recentMessages); // 按时间顺序排列
        
        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        for (ConversationMessage msg : recentMessages) {
            messages.add(Map.of(
                "role", msg.getRole().toLowerCase(),
                "content", msg.getContent()
            ));
        }
        
        // 调用AI服务
        String aiResponse;
        try {
            aiResponse = aiService.chat(systemPrompt, request.getContent());
        } catch (Exception e) {
            log.error("AI服务调用失败", e);
            aiResponse = "抱歉，我遇到了一些问题，请稍后再试。错误信息：" + e.getMessage();
        }
        
        int responseTime = (int) (System.currentTimeMillis() - startTime);
        
        // 保存AI回复
        ConversationMessage assistantMessage = new ConversationMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole("ASSISTANT");
        assistantMessage.setContent(aiResponse);
        assistantMessage.setMessageType("TEXT");
        assistantMessage.setResponseTime(responseTime);
        assistantMessage = messageRepository.save(assistantMessage);
        
        // 更新会话消息计数
        session.setTotalMessages(session.getTotalMessages() + 2);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        
        return convertToMessageResponse(assistantMessage);
    }
    
    /**
     * 获取小说的会话列表
     */
    public Page<SessionResponse> getSessionsByNovel(Long novelId, Pageable pageable) {
        Page<ConversationSession> sessions = sessionRepository.findByNovelIdOrderByUpdatedAtDesc(novelId, pageable);
        
        List<SessionResponse> responses = sessions.getContent().stream()
            .map(this::convertToSessionResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, sessions.getTotalElements());
    }
    
    /**
     * 获取会话详情
     */
    public SessionDetailResponse getSessionDetail(Long sessionId) {
        ConversationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        List<ConversationMessage> messages = messageRepository.findBySessionIdOrderByCreatedAt(sessionId);
        
        SessionDetailResponse response = new SessionDetailResponse();
        // 复制基本信息
        response.setId(session.getId());
        response.setNovelId(session.getNovelId());
        response.setChapterId(session.getChapterId());
        response.setSessionTitle(session.getSessionTitle());
        response.setSessionType(session.getSessionType());
        response.setTotalMessages(session.getTotalMessages());
        response.setStatus(session.getStatus());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        
        // 设置所有消息
        response.setMessages(messages.stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList()));
        
        // 解析上下文快照
        if (session.getContextSnapshot() != null) {
            try {
                Map<String, Object> contextMap = objectMapper.readValue(
                    session.getContextSnapshot(),
                    new TypeReference<Map<String, Object>>() {}
                );
                response.setContextSnapshot(contextMap);
            } catch (Exception e) {
                log.error("解析上下文快照失败", e);
            }
        }
        
        return response;
    }
    
    /**
     * 归档会话
     */
    @Transactional
    public void archiveSession(Long sessionId) {
        ConversationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        session.setStatus("ARCHIVED");
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }
    
    /**
     * 删除会话
     */
    @Transactional
    public void deleteSession(Long sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new RuntimeException("会话不存在");
        }
        
        // 删除所有消息
        messageRepository.deleteBySessionId(sessionId);
        
        // 删除会话
        sessionRepository.deleteById(sessionId);
    }
    
    /**
     * 获取统计信息
     */
    public ConversationStatistics getStatistics(Long novelId) {
        ConversationStatistics stats = new ConversationStatistics();
        
        // 总会话数
        Long totalSessions = sessionRepository.countByNovelId(novelId);
        stats.setTotalSessions(totalSessions.intValue());
        
        // 活跃会话数
        Long activeSessions = sessionRepository.countByNovelIdAndStatus(novelId, "ACTIVE");
        stats.setActiveSessions(activeSessions.intValue());
        
        // 获取所有会话
        List<ConversationSession> sessions = sessionRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
        
        // 总消息数和平均值
        int totalMessages = sessions.stream()
            .mapToInt(s -> s.getTotalMessages() != null ? s.getTotalMessages() : 0)
            .sum();
        stats.setTotalMessages(totalMessages);
        
        if (totalSessions > 0) {
            stats.setAvgMessagesPerSession((double) totalMessages / totalSessions);
        } else {
            stats.setAvgMessagesPerSession(0.0);
        }
        
        // 会话类型分布
        Map<String, Integer> typeDistribution = new HashMap<>();
        for (ConversationSession session : sessions) {
            String type = session.getSessionType() != null ? session.getSessionType() : "GENERAL";
            typeDistribution.put(type, typeDistribution.getOrDefault(type, 0) + 1);
        }
        stats.setSessionTypeDistribution(typeDistribution);
        
        return stats;
    }
    
    /**
     * 构建上下文快照
     */
    private Map<String, Object> buildContextSnapshot(Long novelId, Long chapterId) {
        Map<String, Object> context = new HashMap<>();
        
        // 小说信息
        novelRepository.findById(novelId).ifPresent(novel -> {
            Map<String, Object> novelInfo = new HashMap<>();
            novelInfo.put("id", novel.getId());
            novelInfo.put("title", novel.getTitle());
            novelInfo.put("genre", novel.getGenre());
            // theme 字段不存在，使用 description 代替
            novelInfo.put("description", novel.getDescription());
            novelInfo.put("totalChapters", novel.getTotalChapters());
            novelInfo.put("totalWords", novel.getTotalWords());
            context.put("novel", novelInfo);
        });
        
        // 章节信息（如果指定）
        if (chapterId != null) {
            chapterRepository.findById(chapterId).ifPresent(chapter -> {
                Map<String, Object> chapterInfo = new HashMap<>();
                chapterInfo.put("id", chapter.getId());
                chapterInfo.put("number", chapter.getChapterNumber());
                chapterInfo.put("title", chapter.getTitle());
                chapterInfo.put("summary", chapter.getSummary());
                chapterInfo.put("wordCount", chapter.getWordCount());
                context.put("chapter", chapterInfo);
            });
        }
        
        // 角色信息
        List<com.aiwriter.entity.Character> characters = characterRepository.findByNovelIdOrderByRoleTypeAsc(novelId);
        if (!characters.isEmpty()) {
            List<Map<String, Object>> characterList = characters.stream()
                .limit(5) // 最多5个角色
                .map(ch -> {
                    Map<String, Object> charInfo = new HashMap<>();
                    charInfo.put("name", ch.getName());
                    charInfo.put("roleType", ch.getRoleType());
                    charInfo.put("personality", ch.getPersonality());
                    return charInfo;
                })
                .collect(Collectors.toList());
            context.put("characters", characterList);
        }
        
        return context;
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(ConversationSession session, Boolean includeContext) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一位专业的小说创作顾问，擅长：\n");
        prompt.append("1. 情节设计和结构分析\n");
        prompt.append("2. 人物塑造和心理刻画\n");
        prompt.append("3. 文风指导和语言润色\n");
        prompt.append("4. 创作技巧和经验分享\n\n");
        
        // 如果需要包含上下文
        if (includeContext != null && includeContext && session.getContextSnapshot() != null) {
            try {
                Map<String, Object> context = objectMapper.readValue(
                    session.getContextSnapshot(),
                    new TypeReference<Map<String, Object>>() {}
                );
                
                // 小说信息
                if (context.containsKey("novel")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> novel = (Map<String, Object>) context.get("novel");
                    prompt.append("当前小说信息：\n");
                    prompt.append("- 标题：").append(novel.get("title")).append("\n");
                    if (novel.get("genre") != null) {
                        prompt.append("- 类型：").append(novel.get("genre")).append("\n");
                    }
                    if (novel.get("description") != null) {
                        prompt.append("- 简介：").append(novel.get("description")).append("\n");
                    }
                    prompt.append("\n");
                }
                
                // 章节信息
                if (context.containsKey("chapter")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> chapter = (Map<String, Object>) context.get("chapter");
                    prompt.append("当前章节：第").append(chapter.get("number")).append("章");
                    if (chapter.get("title") != null) {
                        prompt.append(" - ").append(chapter.get("title"));
                    }
                    prompt.append("\n");
                    if (chapter.get("summary") != null) {
                        prompt.append("章节摘要：").append(chapter.get("summary")).append("\n");
                    }
                    prompt.append("\n");
                }
                
                // 角色信息
                if (context.containsKey("characters")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> characters = (List<Map<String, Object>>) context.get("characters");
                    if (!characters.isEmpty()) {
                        prompt.append("主要角色：\n");
                        for (Map<String, Object> ch : characters) {
                            prompt.append("- ").append(ch.get("name"));
                            if (ch.get("personality") != null) {
                                prompt.append("：").append(ch.get("personality"));
                            }
                            prompt.append("\n");
                        }
                        prompt.append("\n");
                    }
                }
                
            } catch (Exception e) {
                log.error("解析上下文快照失败", e);
            }
        }
        
        prompt.append("请提供专业、实用的创作建议。回答时：\n");
        prompt.append("1. 结合小说的具体情况\n");
        prompt.append("2. 提供可操作的建议\n");
        prompt.append("3. 必要时给出示例\n");
        prompt.append("4. 保持友好和鼓励的语气\n");
        
        return prompt.toString();
    }
    
    /**
     * 生成会话标题
     */
    private String generateSessionTitle(String sessionType, Novel novel, Long chapterId) {
        String typeLabel = switch (sessionType != null ? sessionType : "GENERAL") {
            case "WRITING_ADVICE" -> "创作咨询";
            case "PLOT_CONSULTATION" -> "情节讨论";
            case "STYLE_GUIDANCE" -> "文风建议";
            default -> "对话";
        };
        
        if (chapterId != null) {
            return typeLabel + " - " + novel.getTitle();
        } else {
            return typeLabel + " - " + novel.getTitle();
        }
    }
    
    /**
     * 转换为SessionResponse
     */
    private SessionResponse convertToSessionResponse(ConversationSession session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setNovelId(session.getNovelId());
        response.setChapterId(session.getChapterId());
        response.setSessionTitle(session.getSessionTitle());
        response.setSessionType(session.getSessionType());
        response.setTotalMessages(session.getTotalMessages());
        response.setStatus(session.getStatus());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        
        // 获取最近3条消息
        List<ConversationMessage> recentMessages = messageRepository.findRecentMessagesBySessionId(session.getId(), 3);
        Collections.reverse(recentMessages);
        response.setRecentMessages(recentMessages.stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList()));
        
        return response;
    }
    
    /**
     * 转换为MessageResponse
     */
    private MessageResponse convertToMessageResponse(ConversationMessage message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setRole(message.getRole());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setTokenCount(message.getTokenCount());
        response.setResponseTime(message.getResponseTime());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
