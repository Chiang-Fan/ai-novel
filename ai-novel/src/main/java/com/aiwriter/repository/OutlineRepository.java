package com.aiwriter.repository;

import com.aiwriter.entity.Outline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutlineRepository extends JpaRepository<Outline, Long> {
    List<Outline> findByNovelIdOrderBySequenceNumberAsc(Long novelId);
    List<Outline> findByNovelIdAndParentIdOrderBySequenceNumberAsc(Long novelId, Long parentId);
    List<Outline> findByNovelIdAndNodeTypeOrderBySequenceNumberAsc(Long novelId, String nodeType);
    List<Outline> findByNovelIdAndParentIdIsNullOrderBySequenceNumberAsc(Long novelId);
}