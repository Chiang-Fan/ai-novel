package com.ainovel.novelcraft.repository;

import com.ainovel.novelcraft.entity.PlotHook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlotHookRepository extends JpaRepository<PlotHook, Long> {
    List<PlotHook> findByNovelIdAndStatus(Long novelId, PlotHook.Status status);
    List<PlotHook> findByNovelId(Long novelId);
}