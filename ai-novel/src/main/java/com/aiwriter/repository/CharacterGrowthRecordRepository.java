package com.aiwriter.repository;

import com.aiwriter.entity.CharacterGrowthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CharacterGrowthRecordRepository extends JpaRepository<CharacterGrowthRecord, Long> {
    
    /**
     * 查询角色的所有成长记录
     */
    List<CharacterGrowthRecord> findByCharacterIdOrderByRecordTimeAsc(Long characterId);
    
    /**
     * 查询角色在指定时间范围内的成长记录
     */
    List<CharacterGrowthRecord> findByCharacterIdAndRecordTimeBetweenOrderByRecordTimeAsc(
        Long characterId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询指定章节的成长记录
     */
    List<CharacterGrowthRecord> findByChapterIdOrderByRecordTimeAsc(Long chapterId);
    
    /**
     * 查询角色最新的成长记录
     */
    CharacterGrowthRecord findFirstByCharacterIdOrderByRecordTimeDesc(Long characterId);
}
