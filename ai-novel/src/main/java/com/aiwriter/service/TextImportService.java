package com.aiwriter.service;

import com.aiwriter.entity.ImportedChapter;
import com.aiwriter.entity.TextImport;
import com.aiwriter.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 文本导入服务
 * 处理文本文件上传、解析和章节识别
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TextImportService {

    private final TextImportRepository textImportRepository;
    private final ImportedChapterRepository importedChapterRepository;
    private final NovelWritingStyleService writingStyleService;
    private final ChapterRepository chapterRepository;
    private final CharacterRepository characterRepository;
    private final PlotHookRepository plotHookRepository;

    /**
     * 上传并解析文本文件
     */
    @Transactional
    public TextImport uploadAndParseText(Long novelId, MultipartFile file) throws IOException {
        log.info("开始处理文本文件: {}", file.getOriginalFilename());

        TextImport textImport = new TextImport();
        textImport.setNovelId(novelId);
        textImport.setFileName(file.getOriginalFilename());
        textImport.setFileType(getFileType(file.getOriginalFilename()));

        try {
            // 读取文件内容
            String rawContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            textImport.setRawContent(rawContent);
            textImport.setStatus("parsing");

            // 清理内容
            String cleanedContent = cleanContent(rawContent);
            textImport.setCleanedContent(cleanedContent);
            textImport.setTotalWords(cleanedContent.length());

            // 保存导入记录
            TextImport saved = textImportRepository.save(textImport);

            // 自动分章
            autoChapterize(saved);

            // 更新状态
            saved.setStatus("parsed");
            textImportRepository.save(saved);

            log.info("文本文件解析成功: {} (章节数: {})", file.getOriginalFilename(), saved.getChapterCount());
            return saved;
        } catch (Exception e) {
            log.error("文本文件解析失败", e);
            textImport.setStatus("failed");
            textImport.setErrorMessage(e.getMessage());
            return textImportRepository.save(textImport);
        }
    }

    /**
     * 自动分章
     */
    @Transactional
    public void autoChapterize(TextImport textImport) {
        log.info("开始自动分章: {}", textImport.getId());

        String content = textImport.getCleanedContent();
        List<String[]> chapters = detectChapters(content);

        int chapterNumber = 1;
        for (String[] chapterData : chapters) {
            ImportedChapter importedChapter = new ImportedChapter();
            importedChapter.setTextImportId(textImport.getId());
            importedChapter.setNovelId(textImport.getNovelId());
            importedChapter.setTitle(chapterData[0]);
            importedChapter.setContent(chapterData[1]);
            importedChapter.setChapterNumber(chapterNumber);
            importedChapter.setWordCount(chapterData[1].length());
            importedChapter.setParagraphCount(countParagraphs(chapterData[1]));

            importedChapterRepository.save(importedChapter);
            chapterNumber++;
        }

        textImport.setChapterCount(chapters.size());
        textImportRepository.save(textImport);
    }

    /**
     * 检测章节
     */
    private List<String[]> detectChapters(String content) {
        List<String[]> chapters = new ArrayList<>();

        // 多种章节标识符匹配
        Pattern chapterPattern = Pattern.compile(
                "(?:第|Chapter|CHAPTER|Chapter\\s*)([\\u4e00-\\u9fff0-9一二三四五六七八九十百千万亿]+)(?:章|节|回|卷)",
                Pattern.CASE_INSENSITIVE
        );

        String[] lines = content.split("\\n");
        StringBuilder currentChapter = new StringBuilder();
        String currentTitle = "未命名章节";
        int chaptersCount = 0;

        for (String line : lines) {
            var matcher = chapterPattern.matcher(line);

            if (matcher.find()) {
                // 保存前一个章节
                if (chaptersCount > 0 && currentChapter.length() > 0) {
                    chapters.add(new String[]{currentTitle, currentChapter.toString()});
                }

                // 开始新章节
                currentTitle = line.trim();
                currentChapter = new StringBuilder();
                chaptersCount++;
            } else {
                currentChapter.append(line).append("\n");
            }
        }

        // 保存最后一个章节
        if (currentChapter.length() > 0) {
            chapters.add(new String[]{currentTitle, currentChapter.toString()});
        }

        // 如果没有检测到章节，按字数分割
        if (chapters.isEmpty()) {
            chapters.addAll(chapterizeBySplitLength(content));
        }

        log.info("检测到 {} 个章节", chapters.size());
        return chapters;
    }

    /**
     * 按字数分割为章节
     */
    private List<String[]> chapterizeBySplitLength(String content) {
        List<String[]> chapters = new ArrayList<>();
        int wordsPerChapter = 3000; // 每章约3000字

        int totalWords = content.length();
        int numChapters = Math.max(1, totalWords / wordsPerChapter);

        for (int i = 0; i < numChapters; i++) {
            int startIdx = i * wordsPerChapter;
            int endIdx = Math.min((i + 1) * wordsPerChapter, totalWords);

            String chapterContent = content.substring(startIdx, endIdx);
            String chapterTitle = "第" + (i + 1) + "章";

            chapters.add(new String[]{chapterTitle, chapterContent});
        }

        return chapters;
    }

    /**
     * 提取关键要素
     */
    @Transactional
    public void extractKeyElements(Long textImportId) {
        log.info("开始提取关键要素: {}", textImportId);

        TextImport textImport = textImportRepository.findById(textImportId)
                .orElseThrow(() -> new RuntimeException("导入记录不存在"));

        String content = textImport.getCleanedContent();

        // 提取地点
        Set<String> locations = extractLocations(content);
        textImport.setExtractedLocations(String.join(",", locations));

        // 提取人物
        Set<String> characters = extractCharacters(content);
        textImport.setExtractedCharacters(String.join(",", characters));

        // 提取事件
        Set<String> events = extractEvents(content);
        textImport.setExtractedEvents(String.join(",", events));

        textImportRepository.save(textImport);
        log.info("关键要素提取完成: 地点={}, 人物={}, 事件={}", locations.size(), characters.size(), events.size());
    }

    /**
     * 提取地点名称
     */
    private Set<String> extractLocations(String content) {
        Set<String> locations = new HashSet<>();

        // 简单的地点识别模式（可扩展为NLP处理）
        Pattern locationPattern = Pattern.compile("([\\u4e00-\\u9fff]{2,4}(?:城|镇|村|山|湖|海|国|大陆|岛|区|街|路|堂|殿|阁|塔|庙|寺))");
        var matcher = locationPattern.matcher(content);

        while (matcher.find() && locations.size() < 50) {
            locations.add(matcher.group());
        }

        return locations;
    }

    /**
     * 提取人物名称
     */
    private Set<String> extractCharacters(String content) {
        Set<String> characters = new HashSet<>();

        // 简单的人名识别模式
        Pattern characterPattern = Pattern.compile("([\\u4e00-\\u9fff]{2,4})(?:说|曰|道|喊|叫|问|答)");
        var matcher = characterPattern.matcher(content);

        while (matcher.find() && characters.size() < 50) {
            characters.add(matcher.group(1));
        }

        return characters;
    }

    /**
     * 提取事件关键词
     */
    private Set<String> extractEvents(String content) {
        Set<String> events = new HashSet<>();

        // 事件关键词模式
        Pattern eventPattern = Pattern.compile("(?:开始|发生|突然|出现|发现|遭遇|经历|发动|展开|陷入)([\\u4e00-\\u9fff]{3,10})[，。]");
        var matcher = eventPattern.matcher(content);

        while (matcher.find() && events.size() < 30) {
            events.add(matcher.group(1));
        }

        return events;
    }

    /**
     * 确认导入（Qwen-Project.md 增强版）
     * 将导入的章节转换为正式章节，并触发AI深度分析
     */
    @Transactional
    public void confirmImport(Long textImportId) {
        TextImport textImport = textImportRepository.findById(textImportId)
                .orElseThrow(() -> new RuntimeException("导入记录不存在"));

        textImport.setConfirmed(true);
        textImportRepository.save(textImport);
        
        // 将导入章节转换为正式章节
        convertImportedChaptersToFormal(textImport);
        
        // 触发AI深度分析（异步）
        performDeepAnalysis(textImport.getNovelId());
        
        log.info("导入已确认: {}", textImportId);
    }
    
    /**
     * 将导入章节转换为正式章节
     */
    private void convertImportedChaptersToFormal(TextImport textImport) {
        List<ImportedChapter> importedChapters = importedChapterRepository
                .findByTextImportIdOrderByChapterNumber(textImport.getId());
        
        for (ImportedChapter imported : importedChapters) {
            com.aiwriter.entity.Chapter chapter = new com.aiwriter.entity.Chapter();
            chapter.setNovelId(imported.getNovelId());
            chapter.setChapterNumber(imported.getChapterNumber());
            chapter.setTitle(imported.getTitle());
            chapter.setContent(imported.getContent());
            chapter.setWordCount(imported.getWordCount());
            chapter.setStatus("published");
            chapter.setIsAiGenerated(false); // 标记为导入内容
            
            chapterRepository.save(chapter);
        }
        
        log.info("已转换 {} 个章节为正式章节", importedChapters.size());
    }
    
    /**
     * 执行深度AI分析（Qwen-Project.md 核心功能）
     * 1. 批量分析章节提取四维风格画像
     * 2. 提取角色弧光和核心信念
     * 3. 识别伏笔和循环意象
     */
    private void performDeepAnalysis(Long novelId) {
        log.info("开始深度AI分析: novelId={}", novelId);
        
        try {
            // 1. 分析写作风格（调用NovelWritingStyleService）
            writingStyleService.analyzeBatchChapters(novelId);
            
            // 2. TODO: 提取角色弧光（后续实现）
            // characterArcService.extractCharacterArcs(novelId);
            
            // 3. TODO: 检测伏笔（后续实现）
            // plotHookDetectionService.detectPlotHooks(novelId);
            
            log.info("深度AI分析完成: novelId={}", novelId);
        } catch (Exception e) {
            log.error("深度AI分析失败: novelId={}", novelId, e);
        }
    }

    /**
     * 获取导入的章节列表
     */
    public List<ImportedChapter> getImportedChapters(Long textImportId) {
        return importedChapterRepository.findByTextImportIdOrderByChapterNumber(textImportId);
    }

    /**
     * 清理文本内容
     */
    private String cleanContent(String content) {
        // 移除多余的空行
        content = content.replaceAll("\\n\\s*\\n", "\n");
        // 移除特殊符号（但保留中文和常用标点）
        content = content.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        return content.trim();
    }

    /**
     * 计算段落数
     */
    private Integer countParagraphs(String content) {
        return content.split("\\n\\n").length;
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null) return "unknown";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return "unknown";
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}
