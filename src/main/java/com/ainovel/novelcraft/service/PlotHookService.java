package com.ainovel.novelcraft.service;

import com.ainovel.novelcraft.entity.PlotHook;
import com.ainovel.novelcraft.repository.PlotHookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlotHookService {
    
    @Autowired
    private PlotHookRepository plotHookRepository;
    
    public List<PlotHook> getPlotHooksByNovelId(Long novelId) {
        return plotHookRepository.findByNovelId(novelId);
    }
    
    public PlotHook getPlotHookById(Long id) {
        return plotHookRepository.findById(id).orElse(null);
    }
    
    public PlotHook createPlotHook(PlotHook plotHook) {
        return plotHookRepository.save(plotHook);
    }
    
    public PlotHook updatePlotHook(PlotHook plotHook) {
        return plotHookRepository.save(plotHook);
    }
    
    public void deletePlotHook(Long id) {
        plotHookRepository.deleteById(id);
    }
}