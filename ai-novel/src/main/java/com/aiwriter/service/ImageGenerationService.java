package com.aiwriter.service;

import com.aiwriter.dto.*;
import com.aiwriter.entity.*;
import com.aiwriter.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI图片生成Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationService {
    
    private final GeneratedImageRepository imageRepository;
    private final ImageGenerationHistoryRepository historyRepository;
    private final ImageTemplateRepository templateRepository;
    private final CharacterRepository characterRepository;
    private final SceneRepository sceneRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 生成图片
     */
    @Transactional
    public List<GeneratedImageResponse> generateImage(ImageGenerationRequest request) {
        log.info("生成图片请求: type={}, style={}, batchSize={}", 
                request.getImageType(), request.getStyle(), request.getBatchSize());
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 构建完整提示词
        String fullPrompt = buildPrompt(request);
        
        // 模拟AI生成（实际应调用AI服务）
        List<GeneratedImage> images = new ArrayList<>();
        int successCount = 0;
        
        for (int i = 0; i < request.getBatchSize(); i++) {
            try {
                GeneratedImage image = new GeneratedImage();
                image.setNovelId(request.getNovelId());
                image.setChapterId(request.getChapterId());
                image.setCharacterId(request.getCharacterId());
                image.setSceneId(request.getSceneId());
                image.setImageType(request.getImageType());
                image.setPrompt(fullPrompt);
                image.setNegativePrompt(request.getNegativePrompt());
                image.setStyle(request.getStyle() != null ? request.getStyle() : "REALISTIC");
                image.setWidth(request.getWidth() != null ? request.getWidth() : 1024);
                image.setHeight(request.getHeight() != null ? request.getHeight() : 1024);
                
                // 模拟生成结果
                image.setImageUrl("https://example.com/generated_" + UUID.randomUUID() + ".png");
                image.setLocalPath("/images/" + UUID.randomUUID() + ".png");
                image.setFileSize((long) (Math.random() * 5000000 + 1000000)); // 1-6MB
                image.setAiModel("Stable Diffusion XL");
                image.setQualityScore((int) (Math.random() * 20 + 80)); // 80-100
                
                // 生成参数
                Map<String, Object> params = new HashMap<>();
                params.put("steps", 30);
                params.put("cfg_scale", 7.5);
                params.put("sampler", "DPM++ 2M Karras");
                image.setGenerationParams(objectMapper.writeValueAsString(params));
                
                // 标签
                List<String> tags = generateTags(request);
                image.setTags(objectMapper.writeValueAsString(tags));
                
                images.add(imageRepository.save(image));
                successCount++;
            } catch (Exception e) {
                log.error("生成图片失败", e);
            }
        }
        
        // 记录历史
        long duration = (System.currentTimeMillis() - startTime) / 1000;
        saveHistory(request, images.size(), successCount, duration);
        
        // 更新模板使用次数
        if (request.getTemplateId() != null) {
            updateTemplateUsage(request.getTemplateId());
        }
        
        return images.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 构建提示词
     */
    private String buildPrompt(ImageGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        // 使用模板
        if (request.getTemplateId() != null) {
            Optional<ImageTemplate> templateOpt = templateRepository.findById(request.getTemplateId());
            if (templateOpt.isPresent()) {
                prompt.append(templateOpt.get().getPromptTemplate()).append(", ");
            }
        }
        
        // 添加自定义提示词
        if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
            prompt.append(request.getPrompt()).append(", ");
        }
        
        // 根据类型添加内容
        if ("CHARACTER".equals(request.getImageType()) && request.getCharacterId() != null) {
            Optional<com.aiwriter.entity.Character> charOpt = characterRepository.findById(request.getCharacterId());
            charOpt.ifPresent(character -> {
                prompt.append("character named ").append(character.getName()).append(", ");
                if (character.getAppearance() != null) {
                    prompt.append(character.getAppearance()).append(", ");
                }
            });
        } else if ("SCENE".equals(request.getImageType()) && request.getSceneId() != null) {
            Optional<Scene> sceneOpt = sceneRepository.findById(request.getSceneId());
            sceneOpt.ifPresent(scene -> {
                prompt.append(scene.getName()).append(", ");
                if (scene.getDescription() != null) {
                    prompt.append(scene.getDescription()).append(", ");
                }
            });
        }
        
        // 添加风格
        if (request.getStyle() != null) {
            prompt.append(getStylePrompt(request.getStyle())).append(", ");
        }
        
        // 添加质量标签
        prompt.append("masterpiece, best quality, highly detailed, 8k");
        
        return prompt.toString();
    }
    
    /**
     * 获取风格提示词
     */
    private String getStylePrompt(String style) {
        return switch (style) {
            case "ANIME" -> "anime style, cel shading";
            case "COMIC" -> "comic book style, bold lines";
            case "WATERCOLOR" -> "watercolor painting, soft colors";
            case "OIL_PAINTING" -> "oil painting, classical art";
            case "SKETCH" -> "pencil sketch, hand drawn";
            default -> "photorealistic, realistic";
        };
    }
    
    /**
     * 生成标签
     */
    private List<String> generateTags(ImageGenerationRequest request) {
        List<String> tags = new ArrayList<>();
        tags.add(request.getImageType().toLowerCase());
        if (request.getStyle() != null) {
            tags.add(request.getStyle().toLowerCase());
        }
        return tags;
    }
    
    /**
     * 保存历史记录
     */
    private void saveHistory(ImageGenerationRequest request, int generated, int success, long duration) {
        ImageGenerationHistory history = new ImageGenerationHistory();
        history.setNovelId(request.getNovelId());
        history.setPrompt(request.getPrompt());
        history.setStyle(request.getStyle());
        history.setBatchSize(request.getBatchSize());
        history.setGeneratedCount(generated);
        history.setSuccessCount(success);
        history.setDurationSeconds((int) duration);
        historyRepository.save(history);
    }
    
    /**
     * 更新模板使用次数
     */
    private void updateTemplateUsage(Long templateId) {
        templateRepository.findById(templateId).ifPresent(template -> {
            template.setUsageCount(template.getUsageCount() + 1);
            templateRepository.save(template);
        });
    }
    
    /**
     * 获取图片列表
     */
    public List<GeneratedImageResponse> getImages(Long novelId, String imageType) {
        List<GeneratedImage> images;
        if (imageType != null && !imageType.isEmpty()) {
            images = imageRepository.findByNovelIdAndImageTypeOrderByCreatedAtDesc(novelId, imageType);
        } else {
            images = imageRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
        }
        return images.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 采用图片
     */
    @Transactional
    public void adoptImage(Long imageId) {
        GeneratedImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("图片不存在"));
        
        image.setIsAdopted(true);
        image.setAdoptedAt(LocalDateTime.now());
        imageRepository.save(image);
    }
    
    /**
     * 获取模板列表
     */
    public List<ImageTemplateResponse> getTemplates(String category) {
        List<ImageTemplate> templates;
        if (category != null && !category.isEmpty()) {
            templates = templateRepository.findByCategoryOrderByUsageCountDesc(category);
        } else {
            templates = templateRepository.findByIsSystemTrueOrderByUsageCountDesc();
        }
        return templates.stream()
                .map(this::toTemplateResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为响应DTO
     */
    private GeneratedImageResponse toResponse(GeneratedImage image) {
        List<String> tags = new ArrayList<>();
        try {
            if (image.getTags() != null) {
                tags = objectMapper.readValue(image.getTags(), new TypeReference<>() {});
            }
        } catch (Exception e) {
            log.warn("解析标签失败", e);
        }
        
        return GeneratedImageResponse.builder()
                .id(image.getId())
                .novelId(image.getNovelId())
                .chapterId(image.getChapterId())
                .characterId(image.getCharacterId())
                .sceneId(image.getSceneId())
                .imageType(image.getImageType())
                .prompt(image.getPrompt())
                .negativePrompt(image.getNegativePrompt())
                .style(image.getStyle())
                .imageUrl(image.getImageUrl())
                .localPath(image.getLocalPath())
                .width(image.getWidth())
                .height(image.getHeight())
                .fileSize(image.getFileSize())
                .aiModel(image.getAiModel())
                .qualityScore(image.getQualityScore())
                .isAdopted(image.getIsAdopted())
                .adoptedAt(image.getAdoptedAt())
                .tags(tags)
                .createdAt(image.getCreatedAt())
                .build();
    }
    
    /**
     * 转换为模板响应DTO
     */
    private ImageTemplateResponse toTemplateResponse(ImageTemplate template) {
        return ImageTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .category(template.getCategory())
                .description(template.getDescription())
                .promptTemplate(template.getPromptTemplate())
                .negativePromptTemplate(template.getNegativePromptTemplate())
                .defaultStyle(template.getDefaultStyle())
                .recommendedSize(template.getRecommendedSize())
                .previewUrl(template.getPreviewUrl())
                .usageCount(template.getUsageCount())
                .isSystem(template.getIsSystem())
                .createdAt(template.getCreatedAt())
                .build();
    }
}
