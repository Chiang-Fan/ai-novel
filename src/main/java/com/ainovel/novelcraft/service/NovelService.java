package com.ainovel.novelcraft.service;

import com.ainovel.novelcraft.entity.Novel;
import com.ainovel.novelcraft.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NovelService {
    
    @Autowired
    private NovelRepository novelRepository;
    
    public List<Novel> getAllNovels() {
        return novelRepository.findAll();
    }
    
    public Optional<Novel> getNovelById(Long id) {
        return novelRepository.findById(id);
    }
    
    public Novel createNovel(String title, String outline) {
        Novel novel = new Novel();
        novel.setTitle(title);
        novel.setOutline(outline);
        return novelRepository.save(novel);
    }
    
    public Novel updateNovel(Novel novel) {
        return novelRepository.save(novel);
    }
    
    public void deleteNovel(Long id) {
        novelRepository.deleteById(id);
    }
}