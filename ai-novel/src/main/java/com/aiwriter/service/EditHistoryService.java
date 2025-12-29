package com.aiwriter.service;

import com.aiwriter.entity.EditHistory;
import com.aiwriter.repository.EditHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EditHistoryService {
    
    private final EditHistoryRepository editHistoryRepository;
    
    @Transactional(readOnly = true)
    public List<EditHistory> getHistoryByChapter(Long chapterId) {
        return editHistoryRepository.findByChapterIdOrderByCreatedAtDesc(chapterId);
    }
    
    @Transactional
    public EditHistory recordEdit(Long chapterId, String operationType, 
                                   String contentBefore, String contentAfter, 
                                   String reason) {
        EditHistory history = new EditHistory();
        history.setChapterId(chapterId);
        history.setOperationType(operationType);
        history.setContentBefore(contentBefore);
        history.setContentAfter(contentAfter);
        history.setEditReason(reason);
        
        // 计算字数变化
        int beforeCount = contentBefore != null ? contentBefore.length() : 0;
        int afterCount = contentAfter != null ? contentAfter.length() : 0;
        history.setWordCountDiff(afterCount - beforeCount);
        
        // 生成变更摘要
        String summary = String.format("%s操作，字数变化: %+d", 
                operationType, history.getWordCountDiff());
        history.setChangeSummary(summary);
        
        EditHistory saved = editHistoryRepository.save(history);
        log.info("记录编辑历史成功: 章节 {}, 操作 {}", chapterId, operationType);
        return saved;
    }
}
