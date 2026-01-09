package com.aiwriter.service;

import com.aiwriter.dto.NovelCreationRequest;
import com.aiwriter.dto.NovelCreationResponse;
import com.aiwriter.service.ai.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmartNovelCreationServiceTest {

    @Mock
    private AiService aiService;
    
    @Mock
    private NovelService novelService;
    
    @Mock
    private OutlineGenerationService outlineGenerationService;
    
    @Mock
    private CharacterService characterService;
    
    @Mock
    private WorldSettingService worldSettingService;
    
    @InjectMocks
    private SmartNovelCreationService smartNovelCreationService;
    
    private String sessionId;
    
    @BeforeEach
    void setUp() {
        sessionId = "test-session-id";
    }
    
    @Test
    void testStartCreation() {
        // Arrange
        NovelCreationRequest request = new NovelCreationRequest();
        request.setInitialIdea("一个关于魔法学院的故事");
        request.setGenre("奇幻");
        
        when(aiService.chat(anyString(), anyString())).thenReturn(
            "- 魔法学院类奇幻\n- 龙与魔法类奇幻\n- 现代魔法类奇幻"
        );
        
        // Act
        NovelCreationResponse response = smartNovelCreationService.startCreation(request, sessionId);
        
        // Assert
        assertNotNull(response);
        assertEquals("GENRE", response.getCurrentStage());
        assertFalse(response.getIsComplete());
        assertNotNull(response.getQuestion());
        assertFalse(response.getSuggestions().isEmpty());
    }
    
    @Test
    void testProcessReply() {
        // Arrange
        NovelCreationRequest startRequest = new NovelCreationRequest();
        startRequest.setInitialIdea("一个关于魔法学院的故事");
        
        when(aiService.chat(anyString(), anyString())).thenReturn(
            "- 魔法学院类奇幻\n- 龙与魔法类奇幻\n- 现代魔法类奇幻"
        );
        
        // Start the session first
        smartNovelCreationService.startCreation(startRequest, sessionId);
        
        NovelCreationRequest replyRequest = new NovelCreationRequest();
        replyRequest.setInitialIdea("魔法学院类奇幻");
        replyRequest.setGenre("奇幻");
        
        // Act
        NovelCreationResponse response = smartNovelCreationService.processReply(replyRequest, sessionId);
        
        // Assert
        assertNotNull(response);
        assertEquals("OUTLINE", response.getCurrentStage());
        assertFalse(response.getIsComplete());
        assertNotNull(response.getQuestion());
    }
    
    @Test
    void testGetSessionStatus() {
        // Arrange
        NovelCreationRequest request = new NovelCreationRequest();
        request.setInitialIdea("一个关于魔法学院的故事");
        
        when(aiService.chat(anyString(), anyString())).thenReturn(
            "- 魔法学院类奇幻\n- 龙与魔法类奇幻\n- 现代魔法类奇幻"
        );
        
        // Start the session first
        smartNovelCreationService.startCreation(request, sessionId);
        
        // Act
        NovelCreationResponse response = smartNovelCreationService.getSessionStatus(sessionId);
        
        // Assert
        assertNotNull(response);
        assertEquals("GENRE", response.getCurrentStage());
        assertFalse(response.getIsComplete());
        assertNotNull(response.getContext());
    }
}