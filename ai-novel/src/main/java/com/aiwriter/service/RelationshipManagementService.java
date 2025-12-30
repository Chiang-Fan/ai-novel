package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.Character;
import com.aiwriter.entity.CharacterRelationship;
import com.aiwriter.entity.RelationshipHistory;
import com.aiwriter.repository.CharacterRelationshipRepository;
import com.aiwriter.repository.CharacterRepository;
import com.aiwriter.repository.RelationshipHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色关系管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RelationshipManagementService {
    
    private final CharacterRelationshipRepository relationshipRepository;
    private final RelationshipHistoryRepository historyRepository;
    private final CharacterRepository characterRepository;
    
    /**
     * 创建关系
     */
    @Transactional
    public RelationshipResponse createRelationship(RelationshipRequest request) {
        log.info("创建角色关系: {} -> {}", request.getCharacterId(), request.getRelatedCharacterId());
        
        // 检查角色是否存在
        Character character = characterRepository.findById(request.getCharacterId())
            .orElseThrow(() -> new RuntimeException("角色不存在: " + request.getCharacterId()));
        Character relatedCharacter = characterRepository.findById(request.getRelatedCharacterId())
            .orElseThrow(() -> new RuntimeException("相关角色不存在: " + request.getRelatedCharacterId()));
        
        // 检查关系是否已存在
        Optional<CharacterRelationship> existing = relationshipRepository
            .findByCharacterIdAndRelatedCharacterId(request.getCharacterId(), request.getRelatedCharacterId());
        if (existing.isPresent()) {
            throw new RuntimeException("关系已存在");
        }
        
        // 创建关系
        CharacterRelationship relationship = new CharacterRelationship();
        relationship.setCharacterId(request.getCharacterId());
        relationship.setRelatedCharacterId(request.getRelatedCharacterId());
        relationship.setRelationshipType(request.getRelationshipType());
        relationship.setDescription(request.getDescription());
        relationship.setStrength(request.getStrength() != null ? request.getStrength() : 50);
        
        relationship = relationshipRepository.save(relationship);
        
        // 记录历史
        RelationshipHistory history = new RelationshipHistory();
        history.setRelationshipId(relationship.getId());
        history.setChangeType("CREATE");
        history.setNewStrength(relationship.getStrength());
        history.setNewType(relationship.getRelationshipType());
        history.setEventDesc("创建关系");
        historyRepository.save(history);
        
        return toResponse(relationship, character, relatedCharacter);
    }
    
    /**
     * 更新关系
     */
    @Transactional
    public RelationshipResponse updateRelationship(Long id, RelationshipRequest request) {
        log.info("更新角色关系: {}", id);
        
        CharacterRelationship relationship = relationshipRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("关系不存在: " + id));
        
        // 记录变化
        boolean changed = false;
        RelationshipHistory history = new RelationshipHistory();
        history.setRelationshipId(id);
        
        // 检查强度变化
        if (request.getStrength() != null && !request.getStrength().equals(relationship.getStrength())) {
            history.setOldStrength(relationship.getStrength());
            history.setNewStrength(request.getStrength());
            history.setChangeType("STRENGTH_CHANGE");
            relationship.setStrength(request.getStrength());
            changed = true;
        }
        
        // 检查类型变化
        if (request.getRelationshipType() != null && !request.getRelationshipType().equals(relationship.getRelationshipType())) {
            history.setOldType(relationship.getRelationshipType());
            history.setNewType(request.getRelationshipType());
            if (!changed) {
                history.setChangeType("TYPE_CHANGE");
            } else {
                history.setChangeType("MULTIPLE_CHANGE");
            }
            relationship.setRelationshipType(request.getRelationshipType());
            changed = true;
        }
        
        // 更新描述
        if (request.getDescription() != null) {
            relationship.setDescription(request.getDescription());
        }
        
        relationship = relationshipRepository.save(relationship);
        
        // 保存历史记录
        if (changed) {
            history.setEventDesc("更新关系");
            historyRepository.save(history);
        }
        
        Character character = characterRepository.findById(relationship.getCharacterId()).orElse(null);
        Character relatedCharacter = characterRepository.findById(relationship.getRelatedCharacterId()).orElse(null);
        
        return toResponse(relationship, character, relatedCharacter);
    }
    
    /**
     * 删除关系
     */
    @Transactional
    public void deleteRelationship(Long id) {
        log.info("删除角色关系: {}", id);
        
        CharacterRelationship relationship = relationshipRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("关系不存在: " + id));
        
        // 记录删除历史
        RelationshipHistory history = new RelationshipHistory();
        history.setRelationshipId(id);
        history.setChangeType("DELETE");
        history.setOldStrength(relationship.getStrength());
        history.setOldType(relationship.getRelationshipType());
        history.setEventDesc("删除关系");
        historyRepository.save(history);
        
        relationshipRepository.delete(relationship);
    }
    
    /**
     * 获取关系详情
     */
    public RelationshipResponse getRelationship(Long id) {
        CharacterRelationship relationship = relationshipRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("关系不存在: " + id));
        
        Character character = characterRepository.findById(relationship.getCharacterId()).orElse(null);
        Character relatedCharacter = characterRepository.findById(relationship.getRelatedCharacterId()).orElse(null);
        
        return toResponse(relationship, character, relatedCharacter);
    }
    
    /**
     * 获取角色的所有关系
     */
    public List<RelationshipResponse> getCharacterRelationships(Long characterId) {
        List<CharacterRelationship> relationships = relationshipRepository
            .findAllRelationshipsByCharacterId(characterId);
        
        return relationships.stream()
            .map(rel -> {
                Character character = characterRepository.findById(rel.getCharacterId()).orElse(null);
                Character relatedCharacter = characterRepository.findById(rel.getRelatedCharacterId()).orElse(null);
                return toResponse(rel, character, relatedCharacter);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 获取小说的关系图谱
     */
    public RelationshipGraphResponse getRelationshipGraph(Long novelId) {
        log.info("获取小说关系图谱: {}", novelId);
        
        // 获取所有角色
        List<Character> characters = characterRepository.findByNovelIdOrderByRoleTypeAsc(novelId);
        
        // 获取所有关系
        List<CharacterRelationship> relationships = relationshipRepository.findByNovelId(novelId);
        
        // 构建节点
        List<RelationshipGraphNode> nodes = characters.stream()
            .map(c -> {
                RelationshipGraphNode node = new RelationshipGraphNode();
                node.setId(c.getId());
                node.setName(c.getName());
                node.setRoleType(c.getRoleType());
                node.setImportance(calculateImportance(c));
                node.setCategory(c.getRoleType());
                return node;
            })
            .collect(Collectors.toList());
        
        // 构建边
        List<RelationshipGraphEdge> edges = relationships.stream()
            .map(r -> {
                RelationshipGraphEdge edge = new RelationshipGraphEdge();
                edge.setId(r.getId());
                edge.setSource(r.getCharacterId());
                edge.setTarget(r.getRelatedCharacterId());
                edge.setType(r.getRelationshipType());
                edge.setStrength(r.getStrength());
                edge.setDescription(r.getDescription());
                return edge;
            })
            .collect(Collectors.toList());
        
        RelationshipGraphResponse response = new RelationshipGraphResponse();
        response.setNodes(nodes);
        response.setEdges(edges);
        
        return response;
    }
    
    /**
     * 添加关系事件
     */
    @Transactional
    public RelationshipHistoryResponse addRelationshipEvent(RelationshipHistoryRequest request) {
        log.info("添加关系事件: {}", request.getRelationshipId());
        
        // 检查关系是否存在
        CharacterRelationship relationship = relationshipRepository.findById(request.getRelationshipId())
            .orElseThrow(() -> new RuntimeException("关系不存在: " + request.getRelationshipId()));
        
        RelationshipHistory history = new RelationshipHistory();
        history.setRelationshipId(request.getRelationshipId());
        history.setChangeType(request.getChangeType() != null ? request.getChangeType() : "EVENT");
        history.setEventDesc(request.getEventDesc());
        history.setRelatedChapterId(request.getRelatedChapterId());
        
        // 如果有强度变化，更新关系
        if (request.getNewStrength() != null) {
            history.setOldStrength(relationship.getStrength());
            history.setNewStrength(request.getNewStrength());
            relationship.setStrength(request.getNewStrength());
            relationshipRepository.save(relationship);
        }
        
        // 如果有类型变化，更新关系
        if (request.getNewType() != null) {
            history.setOldType(relationship.getRelationshipType());
            history.setNewType(request.getNewType());
            relationship.setRelationshipType(request.getNewType());
            relationshipRepository.save(relationship);
        }
        
        history = historyRepository.save(history);
        
        return toHistoryResponse(history);
    }
    
    /**
     * 获取关系历史
     */
    public List<RelationshipHistoryResponse> getRelationshipHistory(Long relationshipId) {
        List<RelationshipHistory> histories = historyRepository
            .findByRelationshipIdOrderByChangeTimeDesc(relationshipId);
        
        return histories.stream()
            .map(this::toHistoryResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 删除关系历史记录
     */
    @Transactional
    public void deleteRelationshipHistory(Long historyId) {
        log.info("删除关系历史记录: {}", historyId);
        historyRepository.deleteById(historyId);
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 计算角色重要性
     */
    private Integer calculateImportance(Character character) {
        // 基础分数
        int score = 50;
        
        // 根据角色类型加分
        String roleType = character.getRoleType();
        if (roleType != null) {
            switch (roleType) {
                case "PROTAGONIST":
                    score += 50;
                    break;
                case "ANTAGONIST":
                    score += 40;
                    break;
                case "SUPPORTING":
                    score += 20;
                    break;
                case "MINOR":
                    score += 10;
                    break;
            }
        }
        
        // 根据重要性等级加分（1-10，数字越大越重要）
        Integer importanceLevel = character.getImportanceLevel();
        if (importanceLevel != null) {
            score += importanceLevel * 2;  // 最多加20分
        }
        
        // 全局主角额外加分
        if (Boolean.TRUE.equals(character.getIsGlobalProtagonist())) {
            score += 20;
        }
        
        return Math.min(score, 100);
    }
    
    /**
     * 转换为响应DTO
     */
    private RelationshipResponse toResponse(CharacterRelationship relationship, 
                                           Character character, 
                                           Character relatedCharacter) {
        RelationshipResponse response = new RelationshipResponse();
        response.setId(relationship.getId());
        response.setCharacterId(relationship.getCharacterId());
        response.setCharacterName(character != null ? character.getName() : "未知");
        response.setRelatedCharacterId(relationship.getRelatedCharacterId());
        response.setRelatedCharacterName(relatedCharacter != null ? relatedCharacter.getName() : "未知");
        response.setRelationshipType(relationship.getRelationshipType());
        response.setDescription(relationship.getDescription());
        response.setStrength(relationship.getStrength());
        response.setCreatedAt(relationship.getCreatedAt());
        response.setUpdatedAt(relationship.getUpdatedAt());
        return response;
    }
    
    /**
     * 转换为历史响应DTO
     */
    private RelationshipHistoryResponse toHistoryResponse(RelationshipHistory history) {
        RelationshipHistoryResponse response = new RelationshipHistoryResponse();
        response.setId(history.getId());
        response.setRelationshipId(history.getRelationshipId());
        response.setChangeType(history.getChangeType());
        response.setOldStrength(history.getOldStrength());
        response.setNewStrength(history.getNewStrength());
        response.setOldType(history.getOldType());
        response.setNewType(history.getNewType());
        response.setEventDesc(history.getEventDesc());
        response.setRelatedChapterId(history.getRelatedChapterId());
        response.setChangeTime(history.getChangeTime());
        return response;
    }
}
