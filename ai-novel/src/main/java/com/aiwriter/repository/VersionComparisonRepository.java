package com.aiwriter.repository;

import com.aiwriter.entity.VersionComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VersionComparisonRepository extends JpaRepository<VersionComparison, Long> {
    
    // 查找已缓存的对比结果
    Optional<VersionComparison> findByVersionIdFromAndVersionIdTo(Long versionIdFrom, Long versionIdTo);
}
