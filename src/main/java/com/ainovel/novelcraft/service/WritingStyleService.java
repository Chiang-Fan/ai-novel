package com.ainovel.novelcraft.service;

import com.ainovel.novelcraft.entity.WritingStyle;
import com.ainovel.novelcraft.repository.WritingStyleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WritingStyleService {
    
    @Autowired
    private WritingStyleRepository writingStyleRepository;
    
    public WritingStyle getWritingStyleByNovelId(Long novelId) {
        return writingStyleRepository.findByNovelId(novelId);
    }
    
    public WritingStyle createWritingStyle(WritingStyle writingStyle) {
        return writingStyleRepository.save(writingStyle);
    }
    
    public WritingStyle updateWritingStyle(WritingStyle writingStyle) {
        return writingStyleRepository.save(writingStyle);
    }
    
    public void deleteWritingStyle(Long id) {
        writingStyleRepository.deleteById(id);
    }
}