package com.aiwriter.service;

import com.aiwriter.dto.NovelCreateRequest;
import com.aiwriter.dto.NovelUpdateRequest;
import com.aiwriter.entity.Novel;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 小说服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NovelService {
    
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    
    /**
     * 创建小说
     */
    @Transactional
    public Novel createNovel(NovelCreateRequest request) {
        Novel novel = new Novel();
        novel.setTitle(request.getTitle());
        novel.setDescription(request.getDescription());
        novel.setGenre(request.getGenre());
        novel.setTargetAudience(request.getTargetAudience());
        novel.setWritingStyle(request.getWritingStyle());
        novel.setStatus("planning"); // 默认状态
        novel.setTotalChapters(0);
        novel.setTotalWords(0);
        
        Novel saved = novelRepository.save(novel);
        // log.info("创建小说成功: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }
    
    /**
     * 获取小说详情
     */
    public Novel getNovel(Long id) {
        return novelRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("小说不存在: " + id));
    }
    
    /**
     * 获取所有小说
     */
    public List<Novel> getAllNovels() {
        return novelRepository.findAll();
    }
    
    /**
     * 更新小说
     */
    @Transactional
    public Novel updateNovel(Long id, NovelUpdateRequest request) {
        Novel novel = getNovel(id);
        
        if (request.getTitle() != null) {
            novel.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            novel.setDescription(request.getDescription());
        }
        if (request.getGenre() != null) {
            novel.setGenre(request.getGenre());
        }
        if (request.getTargetAudience() != null) {
            novel.setTargetAudience(request.getTargetAudience());
        }
        if (request.getWritingStyle() != null) {
            novel.setWritingStyle(request.getWritingStyle());
        }
        if (request.getStatus() != null) {
            novel.setStatus(request.getStatus());
        }
        
        Novel updated = novelRepository.save(novel);
        // log.info("更新小说成功: id={}", id);
        return updated;
    }
    
    /**
     * 删除小说
     */
    @Transactional
    public void deleteNovel(Long id) {
        Novel novel = getNovel(id);
        novelRepository.delete(novel);
        // log.info("删除小说成功: id={}, title={}", id, novel.getTitle());
    }
    
    /**
     * 更新小说统计信息
     */
    @Transactional
    public void updateNovelStats(Long novelId) {
        Novel novel = getNovel(novelId);
        
        // 统计章节数
        long chapterCount = chapterRepository.countByNovelId(novelId);
        novel.setTotalChapters((int) chapterCount);
        
        // 统计总字数
        Integer totalWords = chapterRepository.sumWordCountByNovelId(novelId)
            .orElse(0);
        novel.setTotalWords(totalWords);
        
        novelRepository.save(novel);
        // TODO: 添加日志记录
        // log.debug("更新小说统计: novelId={}, chapters={}, words={}", novelId, chapterCount, totalWords);
    }
    
    /**
     * 根据状态查询小说
     */
    public List<Novel> getNovelsByStatus(String status) {
        return novelRepository.findByStatus(status);
    }
    
    /**
     * 根据类型查询小说
     */
    public List<Novel> getNovelsByGenre(String genre) {
        return novelRepository.findByGenre(genre);
    }
    
    /**
     * 搜索小说
     */
    public List<Novel> searchNovels(String keyword) {
        return novelRepository.findByTitleContaining(keyword);
    }
}
