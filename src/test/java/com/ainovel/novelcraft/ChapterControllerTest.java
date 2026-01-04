package com.ainovel.novelcraft;

import com.ainovel.novelcraft.dto.GenerateChapterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ChapterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private com.ainovel.novelcraft.service.NovelChapterGenerationService novelChapterGenerationService;

    @Test
    void testGenerateChapterWithDirection() throws Exception {
        GenerateChapterRequest request = new GenerateChapterRequest();
        request.setChapterNumber(1);
        request.setDirection("主角在雨夜发现信件，情绪崩溃但强忍泪水");
        request.setBannedElements(new String[]{"战斗", "回忆童年"});
        request.setMood("忧伤");
        request.setOverrideHighStakes(false);

        mockMvc.perform(post("/api/v1/novels/1/chapters/directed")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("chapterNumber", "1"))
                .andExpect(status().is4xxClientError()); // 由于小说不存在，预期返回4xx错误
    }
}