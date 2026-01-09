package com.aiwriter.repository;

import com.aiwriter.entity.PlotForeshadowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlotForeshadowingRepository extends JpaRepository<PlotForeshadowing, Long> {
    List<PlotForeshadowing> findByNovelIdAndStatus(Long novelId, String status);
    List<PlotForeshadowing> findByNovelIdOrderByCreatedAtDesc(Long novelId);
    List<PlotForeshadowing> findByNovelIdOrderByPlantedInChapterAsc(Long novelId);
    List<PlotForeshadowing> findByChapterId(Long chapterId);
}