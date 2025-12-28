package com.ai.novel.service;

import com.ai.novel.dto.request.NovelCreateRequest;
import com.ai.novel.dto.request.NovelUpdateRequest;
import com.ai.novel.entity.Novel;
import com.ai.novel.entity.enums.NovelStatus;
import com.ai.novel.exception.BusinessException;
import com.ai.novel.exception.ResourceNotFoundException;
import com.ai.novel.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 小说管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NovelService {
    
    private final NovelRepository novelRepository;
    
    /**
     * 创建小说
     */
    @Transactional
    public Novel createNovel(NovelCreateRequest request) {
        log.info("创建小说: {}", request.getTitle());
        
        Novel novel = new Novel();
        novel.setTitle(request.getTitle());
        novel.setDescription(request.getDescription());
        novel.setAuthor(request.getAuthor());
        novel.setType(request.getType());
        
        // 将String类型的writingStyle转换为Map (如果需要的话可以解析JSON)
        if (request.getWritingStyle() != null && !request.getWritingStyle().isEmpty()) {
            // 简单存储为description字段
            Map<String, Object> styleMap = new HashMap<>();
            styleMap.put("description", request.getWritingStyle());
            novel.setWritingStyle(styleMap);
        }
        
        novel.setTargetWordCount(request.getTargetWordCount());
        novel.setCurrentWordCount(0);
        novel.setChapterCount(0);
        novel.setStatus(NovelStatus.DRAFT);
        
        Novel saved = novelRepository.save(novel);
        log.info("小说创建成功, ID: {}", saved.getId());
        return saved;
    }
    
    /**
     * 更新小说
     */
    @Transactional
    public Novel updateNovel(Long id, NovelUpdateRequest request) {
        log.info("更新小说: {}", id);
        
        Novel novel = getNovelById(id);
        
        if (request.getTitle() != null) {
            novel.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            novel.setDescription(request.getDescription());
        }
        if (request.getAuthor() != null) {
            novel.setAuthor(request.getAuthor());
        }
        if (request.getType() != null) {
            novel.setType(request.getType());
        }
        if (request.getWritingStyle() != null) {
            // 将String类型的writingStyle转换为Map
            Map<String, Object> styleMap = new HashMap<>();
            styleMap.put("description", request.getWritingStyle());
            novel.setWritingStyle(styleMap);
        }
        if (request.getTargetWordCount() != null) {
            novel.setTargetWordCount(request.getTargetWordCount());
        }
        if (request.getStatus() != null) {
            novel.setStatus(request.getStatus());
        }
        
        Novel updated = novelRepository.save(novel);
        log.info("小说更新成功, ID: {}", updated.getId());
        return updated;
    }
    
    /**
     * 获取小说详情
     */
    @Transactional(readOnly = true)
    public Novel getNovelById(Long id) {
        return novelRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.novel(id));
    }
    
    /**
     * 分页查询小说列表
     */
    @Transactional(readOnly = true)
    public Page<Novel> listNovels(Pageable pageable) {
        return novelRepository.findAll(pageable);
    }
    
    /**
     * 按状态查询小说
     */
    @Transactional(readOnly = true)
    public Page<Novel> listNovelsByStatus(NovelStatus status, Pageable pageable) {
        return novelRepository.findByStatus(status, pageable);
    }
    
    /**
     * 删除小说
     */
    @Transactional
    public void deleteNovel(Long id) {
        log.info("删除小说: {}", id);
        
        if (!novelRepository.existsById(id)) {
            throw ResourceNotFoundException.novel(id);
        }
        
        novelRepository.deleteById(id);
        log.info("小说删除成功, ID: {}", id);
    }
    
    /**
     * 更新小说字数统计
     */
    @Transactional
    public void updateWordCount(Long novelId, int wordCountDelta) {
        Novel novel = getNovelById(novelId);
        novel.setCurrentWordCount(novel.getCurrentWordCount() + wordCountDelta);
        novelRepository.save(novel);
    }
    
    /**
     * 更新章节计数
     */
    @Transactional
    public void updateChapterCount(Long novelId, int chapterCountDelta) {
        Novel novel = getNovelById(novelId);
        novel.setChapterCount(novel.getChapterCount() + chapterCountDelta);
        novelRepository.save(novel);
    }
}
