package com.ai.novel.service;

import com.ai.novel.dto.request.SceneCreateRequest;
import com.ai.novel.entity.Novel;
import com.ai.novel.entity.Scene;
import com.ai.novel.entity.enums.SceneStatus;
import com.ai.novel.exception.ResourceNotFoundException;
import com.ai.novel.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 场景管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneService {
    
    private final SceneRepository sceneRepository;
    private final NovelService novelService;
    
    /**
     * 创建场景
     */
    @Transactional
    public Scene createScene(SceneCreateRequest request) {
        log.info("创建场景: {}", request.getName());
        
        // 验证小说存在
        Novel novel = novelService.getNovelById(request.getNovelId());
        
        Scene scene = new Scene();
        scene.setNovel(novel);
        scene.setName(request.getName());
        scene.setDescription(request.getDescription());
        scene.setStartChapter(request.getStartChapter());
        scene.setEndChapter(request.getEndChapter());
        scene.setAtmosphere(request.getAtmosphere());
        
        // 将String类型的keyEvents转换为List
        if (request.getKeyEvents() != null && !request.getKeyEvents().isEmpty()) {
            scene.setKeyEvents(Arrays.asList(request.getKeyEvents().split(",")));
        }
        
        scene.setSceneGoals(request.getSceneGoals());
        scene.setStatus(SceneStatus.PLANNING);
        
        Scene saved = sceneRepository.save(scene);
        log.info("场景创建成功, ID: {}", saved.getId());
        return saved;
    }
    
    /**
     * 更新场景
     */
    @Transactional
    public Scene updateScene(Long id, SceneCreateRequest request) {
        log.info("更新场景: {}", id);
        
        Scene scene = getSceneById(id);
        
        if (request.getName() != null) {
            scene.setName(request.getName());
        }
        if (request.getDescription() != null) {
            scene.setDescription(request.getDescription());
        }
        if (request.getStartChapter() != null) {
            scene.setStartChapter(request.getStartChapter());
        }
        if (request.getEndChapter() != null) {
            scene.setEndChapter(request.getEndChapter());
        }
        if (request.getAtmosphere() != null) {
            scene.setAtmosphere(request.getAtmosphere());
        }
        if (request.getKeyEvents() != null) {
            // 将String类型的keyEvents转换为List
            scene.setKeyEvents(Arrays.asList(request.getKeyEvents().split(",")));
        }
        if (request.getSceneGoals() != null) {
            scene.setSceneGoals(request.getSceneGoals());
        }
        
        return sceneRepository.save(scene);
    }
    
    /**
     * 获取场景详情
     */
    @Transactional(readOnly = true)
    public Scene getSceneById(Long id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.scene(id));
    }
    
    /**
     * 查询小说的所有场景
     */
    @Transactional(readOnly = true)
    public List<Scene> listScenesByNovel(Long novelId) {
        novelService.getNovelById(novelId); // 验证小说存在
        return sceneRepository.findByNovelId(novelId);
    }
    
    /**
     * 按状态查询场景
     */
    @Transactional(readOnly = true)
    public List<Scene> listScenesByStatus(Long novelId, SceneStatus status) {
        novelService.getNovelById(novelId); // 验证小说存在
        return sceneRepository.findByNovelIdAndStatus(novelId, status);
    }
    
    /**
     * 删除场景
     */
    @Transactional
    public void deleteScene(Long id) {
        log.info("删除场景: {}", id);
        
        if (!sceneRepository.existsById(id)) {
            throw ResourceNotFoundException.scene(id);
        }
        
        sceneRepository.deleteById(id);
        log.info("场景删除成功, ID: {}", id);
    }
    
    /**
     * 更新场景状态
     */
    @Transactional
    public Scene updateSceneStatus(Long id, SceneStatus status) {
        Scene scene = getSceneById(id);
        scene.setStatus(status);
        return sceneRepository.save(scene);
    }
}
