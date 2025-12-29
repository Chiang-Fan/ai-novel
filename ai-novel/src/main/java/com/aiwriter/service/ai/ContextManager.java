package com.aiwriter.service.ai;

import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.PlotThread;
import com.aiwriter.entity.Scene;
import com.aiwriter.entity.WorldSetting;
import com.aiwriter.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 上下文管理器
 * 负责收集和构建AI续写所需的上下文信息
 */
@Service
@RequiredArgsConstructor
public class ContextManager {
    
    private final ChapterRepository chapterRepository;
    private final CharacterRepository characterRepository;
    private final PlotThreadRepository plotThreadRepository;
    private final WorldSettingRepository worldSettingRepository;
    private final SceneRepository sceneRepository;
    
    /**
     * 构建完整的续写上下文
     * 
     * @param novelId 小说ID
     * @param sceneId 场景ID（可选）
     * @param limitChapters 最近章节数量限制
     * @return 格式化的上下文字符串
     */
    public String buildContext(Long novelId, Long sceneId, int limitChapters) {
        StringBuilder context = new StringBuilder();
        
        // 1. 收集最近的章节内容
        List<Chapter> recentChapters = chapterRepository
            .findTopNByNovelId(novelId, limitChapters);
        
        if (!recentChapters.isEmpty()) {
            context.append("=== 最近章节内容 ===\n\n");
            for (Chapter chapter : recentChapters) {
                context.append(String.format("【第%d章 %s】\n", 
                    chapter.getChapterNumber(), chapter.getTitle()));
                context.append(chapter.getContent()).append("\n\n");
            }
        }
        
        // 2. 收集角色信息
        List<com.aiwriter.entity.Character> characters = characterRepository.findMainCharacters(novelId);
        if (!characters.isEmpty()) {
            context.append("=== 主要角色 ===\n\n");
            for (com.aiwriter.entity.Character character : characters) {
                context.append(formatCharacter(character)).append("\n");
            }
        }
        
        // 3. 收集待展开的伏笔（暂时简化，不调用有问题的getter）
        // List<PlotThread> activeThreads = plotThreadRepository
        //     .findByNovelIdAndStatusIn(novelId, List.of("planted", "developing"));
        // if (!activeThreads.isEmpty()) {
        //     context.append("=== 待展开的伏笔 ===\n\n");
        //     context.append("（功能开发中）\n\n");
        // }
        
        return context.toString();
    }
    
    /**
     * 格式化角色信息
     */
    private String formatCharacter(com.aiwriter.entity.Character character) {
        return String.format("""
            - %s（%s）
              性格：%s
              背景：%s
              外貌：%s
              能力：%s
            """,
            character.getName(),
            character.getRoleType() != null ? getRoleTypeName(character.getRoleType()) : "未设定",
            character.getPersonality() != null ? character.getPersonality() : "未设定",
            character.getBackground() != null ? character.getBackground() : "未设定",
            character.getAppearance() != null ? character.getAppearance() : "未设定",
            character.getAbilities() != null ? character.getAbilities() : "未设定"
        );
    }
    
    private String getRoleTypeName(String roleType) {
        return switch (roleType) {
            case "PROTAGONIST" -> "主角";
            case "ANTAGONIST" -> "反派";
            case "SUPPORTING" -> "配角";
            case "MINOR" -> "次要角色";
            default -> roleType;
        };
    }
}
