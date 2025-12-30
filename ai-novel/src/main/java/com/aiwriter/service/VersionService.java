package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.Chapter;
import com.aiwriter.entity.ChapterVersion;
import com.aiwriter.entity.VersionComparison;
import com.aiwriter.repository.ChapterRepository;
import com.aiwriter.repository.ChapterVersionRepository;
import com.aiwriter.repository.VersionComparisonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 版本管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VersionService {
    
    private final ChapterVersionRepository versionRepository;
    private final VersionComparisonRepository comparisonRepository;
    private final ChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建新版本
     */
    @Transactional
    public VersionResponse createVersion(CreateVersionRequest request) {
        // 验证章节存在
        Chapter chapter = chapterRepository.findById(request.getChapterId())
            .orElseThrow(() -> new RuntimeException("章节不存在"));
        
        // 获取下一个版本号
        Integer maxVersion = versionRepository.findMaxVersionNumberByChapterId(request.getChapterId());
        int newVersionNumber = (maxVersion == null ? 0 : maxVersion) + 1;
        
        // 创建新版本
        ChapterVersion version = new ChapterVersion();
        version.setChapterId(request.getChapterId());
        version.setVersionNumber(newVersionNumber);
        version.setContent(request.getContent());
        version.setWordCount(request.getContent().length());
        version.setVersionTag(request.getVersionTag());
        version.setVersionNote(request.getVersionNote());
        version.setCreatedType(request.getCreatedType() != null ? request.getCreatedType() : "MANUAL");
        version.setCreatedBy("system");
        version.setIsCurrent(true);
        version.setIsDeleted(false);
        
        // 取消之前的当前版本
        versionRepository.findByChapterIdAndIsCurrentTrueAndIsDeletedFalse(request.getChapterId())
            .ifPresent(oldCurrent -> {
                oldCurrent.setIsCurrent(false);
                versionRepository.save(oldCurrent);
            });
        
        version = versionRepository.save(version);
        
        return convertToResponse(version);
    }
    
    /**
     * 获取版本列表
     */
    public Page<VersionResponse> getVersionList(Long chapterId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ChapterVersion> versions = versionRepository
            .findByChapterIdAndIsDeletedFalseOrderByVersionNumberDesc(chapterId, pageRequest);
        
        return versions.map(this::convertToResponse);
    }
    
    /**
     * 获取版本详情
     */
    public VersionDetailResponse getVersionDetail(Long versionId) {
        ChapterVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new RuntimeException("版本不存在"));
        
        return convertToDetailResponse(version);
    }
    
    /**
     * 对比两个版本
     */
    public CompareVersionResponse compareVersions(CompareVersionRequest request) {
        // 查找版本
        ChapterVersion versionFrom = versionRepository.findById(request.getVersionIdFrom())
            .orElseThrow(() -> new RuntimeException("源版本不存在"));
        ChapterVersion versionTo = versionRepository.findById(request.getVersionIdTo())
            .orElseThrow(() -> new RuntimeException("目标版本不存在"));
        
        // 检查缓存
        Optional<VersionComparison> cached = comparisonRepository
            .findByVersionIdFromAndVersionIdTo(request.getVersionIdFrom(), request.getVersionIdTo());
        
        if (cached.isPresent()) {
            log.info("使用缓存的对比结果");
            return buildCompareResponse(versionFrom, versionTo, cached.get());
        }
        
        // 计算差异
        List<CompareVersionResponse.DiffBlock> diffs = computeDiff(
            versionFrom.getContent(), 
            versionTo.getContent()
        );
        
        // 计算统计
        CompareVersionResponse.DiffStatistics statistics = computeStatistics(diffs, versionFrom, versionTo);
        
        // 缓存结果
        cacheComparison(request.getVersionIdFrom(), request.getVersionIdTo(), diffs, statistics);
        
        // 构建响应
        CompareVersionResponse response = new CompareVersionResponse();
        response.setVersionFrom(buildVersionInfo(versionFrom));
        response.setVersionTo(buildVersionInfo(versionTo));
        response.setStatistics(statistics);
        response.setDiffs(diffs);
        
        return response;
    }
    
    /**
     * 回滚到指定版本
     */
    @Transactional
    public VersionResponse rollbackToVersion(Long versionId) {
        ChapterVersion targetVersion = versionRepository.findById(versionId)
            .orElseThrow(() -> new RuntimeException("版本不存在"));
        
        // 创建新版本（基于目标版本的内容）
        CreateVersionRequest request = new CreateVersionRequest();
        request.setChapterId(targetVersion.getChapterId());
        request.setContent(targetVersion.getContent());
        request.setVersionTag("回滚自版本 " + targetVersion.getVersionNumber());
        request.setVersionNote("回滚到版本 " + targetVersion.getVersionNumber());
        request.setCreatedType("ROLLBACK");
        
        return createVersion(request);
    }
    
    /**
     * 删除版本（软删除）
     */
    @Transactional
    public void deleteVersion(Long versionId) {
        ChapterVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new RuntimeException("版本不存在"));
        
        if (version.getIsCurrent()) {
            throw new RuntimeException("不能删除当前版本");
        }
        
        version.setIsDeleted(true);
        versionRepository.save(version);
    }
    
    /**
     * 更新版本标签
     */
    @Transactional
    public VersionResponse updateVersionTag(Long versionId, String tag, String note) {
        ChapterVersion version = versionRepository.findById(versionId)
            .orElseThrow(() -> new RuntimeException("版本不存在"));
        
        version.setVersionTag(tag);
        version.setVersionNote(note);
        version = versionRepository.save(version);
        
        return convertToResponse(version);
    }
    
    /**
     * 获取版本统计
     */
    public VersionStatistics getVersionStatistics(Long chapterId) {
        List<ChapterVersion> versions = versionRepository
            .findByChapterIdOrderByVersionNumberDesc(chapterId);
        
        VersionStatistics stats = new VersionStatistics();
        stats.setChapterId(chapterId);
        stats.setTotalVersions(versions.size());
        
        // 当前版本号
        versions.stream()
            .filter(v -> v.getIsCurrent() && !v.getIsDeleted())
            .findFirst()
            .ifPresent(v -> stats.setCurrentVersionNumber(v.getVersionNumber()));
        
        // 计算编辑统计
        stats.setTotalEdits(versions.size());
        
        // 简化统计（实际应该对比相邻版本）
        if (versions.size() > 1) {
            int totalAdded = 0;
            int totalDeleted = 0;
            
            for (int i = 0; i < versions.size() - 1; i++) {
                int diff = versions.get(i).getWordCount() - versions.get(i + 1).getWordCount();
                if (diff > 0) {
                    totalAdded += diff;
                } else {
                    totalDeleted += Math.abs(diff);
                }
            }
            
            stats.setTotalWordsAdded(totalAdded);
            stats.setTotalWordsDeleted(totalDeleted);
            stats.setAverageEditSize((double) (totalAdded + totalDeleted) / versions.size());
        }
        
        return stats;
    }
    
    /**
     * 计算文本差异（简化版 diff 算法）
     */
    private List<CompareVersionResponse.DiffBlock> computeDiff(String text1, String text2) {
        List<CompareVersionResponse.DiffBlock> diffs = new ArrayList<>();
        
        // 按行分割
        String[] lines1 = text1.split("\n");
        String[] lines2 = text2.split("\n");
        
        // 使用最长公共子序列（LCS）算法
        int[][] dp = new int[lines1.length + 1][lines2.length + 1];
        
        // 填充 DP 表
        for (int i = 1; i <= lines1.length; i++) {
            for (int j = 1; j <= lines2.length; j++) {
                if (lines1[i - 1].equals(lines2[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        // 回溯构建差异
        int i = lines1.length, j = lines2.length;
        int lineNum = 0;
        
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && lines1[i - 1].equals(lines2[j - 1])) {
                CompareVersionResponse.DiffBlock block = new CompareVersionResponse.DiffBlock();
                block.setType("EQUAL");
                block.setContent(lines1[i - 1]);
                block.setLineNumber(lineNum++);
                diffs.add(0, block);
                i--;
                j--;
            } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
                CompareVersionResponse.DiffBlock block = new CompareVersionResponse.DiffBlock();
                block.setType("INSERT");
                block.setContent(lines2[j - 1]);
                block.setLineNumber(lineNum++);
                diffs.add(0, block);
                j--;
            } else if (i > 0) {
                CompareVersionResponse.DiffBlock block = new CompareVersionResponse.DiffBlock();
                block.setType("DELETE");
                block.setContent(lines1[i - 1]);
                block.setLineNumber(lineNum++);
                diffs.add(0, block);
                i--;
            }
        }
        
        return diffs;
    }
    
    /**
     * 计算差异统计
     */
    private CompareVersionResponse.DiffStatistics computeStatistics(
        List<CompareVersionResponse.DiffBlock> diffs,
        ChapterVersion versionFrom,
        ChapterVersion versionTo
    ) {
        CompareVersionResponse.DiffStatistics stats = new CompareVersionResponse.DiffStatistics();
        
        int added = 0, deleted = 0, unchanged = 0;
        
        for (CompareVersionResponse.DiffBlock diff : diffs) {
            switch (diff.getType()) {
                case "INSERT" -> added++;
                case "DELETE" -> deleted++;
                case "EQUAL" -> unchanged++;
            }
        }
        
        stats.setAddedCount(added);
        stats.setDeletedCount(deleted);
        stats.setUnchangedCount(unchanged);
        stats.setModifiedCount(Math.min(added, deleted));
        stats.setTotalChanges(added + deleted);
        stats.setWordDifference(versionTo.getWordCount() - versionFrom.getWordCount());
        
        return stats;
    }
    
    /**
     * 缓存对比结果
     */
    private void cacheComparison(
        Long versionIdFrom, 
        Long versionIdTo, 
        List<CompareVersionResponse.DiffBlock> diffs,
        CompareVersionResponse.DiffStatistics statistics
    ) {
        try {
            VersionComparison comparison = new VersionComparison();
            comparison.setVersionIdFrom(versionIdFrom);
            comparison.setVersionIdTo(versionIdTo);
            comparison.setDiffResult(objectMapper.writeValueAsString(diffs));
            comparison.setAddedCount(statistics.getAddedCount());
            comparison.setDeletedCount(statistics.getDeletedCount());
            comparison.setModifiedCount(statistics.getModifiedCount());
            
            comparisonRepository.save(comparison);
        } catch (Exception e) {
            log.error("缓存对比结果失败", e);
        }
    }
    
    /**
     * 从缓存构建响应
     */
    private CompareVersionResponse buildCompareResponse(
        ChapterVersion versionFrom,
        ChapterVersion versionTo,
        VersionComparison cached
    ) {
        CompareVersionResponse response = new CompareVersionResponse();
        response.setVersionFrom(buildVersionInfo(versionFrom));
        response.setVersionTo(buildVersionInfo(versionTo));
        
        // 统计
        CompareVersionResponse.DiffStatistics stats = new CompareVersionResponse.DiffStatistics();
        stats.setAddedCount(cached.getAddedCount());
        stats.setDeletedCount(cached.getDeletedCount());
        stats.setModifiedCount(cached.getModifiedCount());
        stats.setTotalChanges(cached.getAddedCount() + cached.getDeletedCount());
        stats.setWordDifference(versionTo.getWordCount() - versionFrom.getWordCount());
        response.setStatistics(stats);
        
        // 差异
        try {
            List<CompareVersionResponse.DiffBlock> diffs = objectMapper.readValue(
                cached.getDiffResult(),
                objectMapper.getTypeFactory().constructCollectionType(
                    List.class, CompareVersionResponse.DiffBlock.class
                )
            );
            response.setDiffs(diffs);
        } catch (Exception e) {
            log.error("解析缓存的差异结果失败", e);
            response.setDiffs(new ArrayList<>());
        }
        
        return response;
    }
    
    /**
     * 构建版本信息
     */
    private CompareVersionResponse.VersionInfo buildVersionInfo(ChapterVersion version) {
        CompareVersionResponse.VersionInfo info = new CompareVersionResponse.VersionInfo();
        info.setId(version.getId());
        info.setVersionNumber(version.getVersionNumber());
        info.setVersionTag(version.getVersionTag());
        info.setWordCount(version.getWordCount());
        return info;
    }
    
    /**
     * 转换为响应对象
     */
    private VersionResponse convertToResponse(ChapterVersion version) {
        VersionResponse response = new VersionResponse();
        response.setId(version.getId());
        response.setChapterId(version.getChapterId());
        response.setVersionNumber(version.getVersionNumber());
        response.setWordCount(version.getWordCount());
        response.setVersionTag(version.getVersionTag());
        response.setVersionNote(version.getVersionNote());
        response.setCreatedBy(version.getCreatedBy());
        response.setCreatedType(version.getCreatedType());
        response.setIsCurrent(version.getIsCurrent());
        response.setCreatedAt(version.getCreatedAt());
        return response;
    }
    
    /**
     * 转换为详情响应对象
     */
    private VersionDetailResponse convertToDetailResponse(ChapterVersion version) {
        VersionDetailResponse response = new VersionDetailResponse();
        response.setId(version.getId());
        response.setChapterId(version.getChapterId());
        response.setVersionNumber(version.getVersionNumber());
        response.setWordCount(version.getWordCount());
        response.setVersionTag(version.getVersionTag());
        response.setVersionNote(version.getVersionNote());
        response.setCreatedBy(version.getCreatedBy());
        response.setCreatedType(version.getCreatedType());
        response.setIsCurrent(version.getIsCurrent());
        response.setCreatedAt(version.getCreatedAt());
        response.setContent(version.getContent());
        return response;
    }
}
