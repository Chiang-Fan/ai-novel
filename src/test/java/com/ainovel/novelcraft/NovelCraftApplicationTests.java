package com.ainovel.novelcraft;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class NovelCraftApplicationTests {

    @Test
    void contextLoads() {
        // 只测试Spring上下文是否能正常加载
    }
}