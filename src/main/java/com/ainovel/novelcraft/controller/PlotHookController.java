package com.ainovel.novelcraft.controller;

import com.ainovel.novelcraft.entity.PlotHook;
import com.ainovel.novelcraft.service.PlotHookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/novels/{novelId}/plot-hooks")
public class PlotHookController {
    
    @Autowired
    private PlotHookService plotHookService;
    
    @GetMapping
    public ResponseEntity<List<PlotHook>> getPlotHooksByNovelId(@PathVariable Long novelId) {
        List<PlotHook> plotHooks = plotHookService.getPlotHooksByNovelId(novelId);
        return ResponseEntity.ok(plotHooks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PlotHook> getPlotHookById(@PathVariable Long novelId, @PathVariable Long id) {
        PlotHook plotHook = plotHookService.getPlotHookById(id);
        if (plotHook != null && plotHook.getNovelId().equals(novelId)) {
            return ResponseEntity.ok(plotHook);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<PlotHook> createPlotHook(@PathVariable Long novelId, @RequestBody PlotHook plotHook) {
        plotHook.setNovelId(novelId);
        PlotHook createdPlotHook = plotHookService.createPlotHook(plotHook);
        return ResponseEntity.ok(createdPlotHook);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PlotHook> updatePlotHook(@PathVariable Long novelId, @PathVariable Long id, @RequestBody PlotHook plotHook) {
        PlotHook existingPlotHook = plotHookService.getPlotHookById(id);
        if (existingPlotHook != null && existingPlotHook.getNovelId().equals(novelId)) {
            plotHook.setId(id);
            plotHook.setNovelId(novelId);
            PlotHook updatedPlotHook = plotHookService.updatePlotHook(plotHook);
            return ResponseEntity.ok(updatedPlotHook);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<PlotHook> updatePlotHookStatus(@PathVariable Long novelId, @PathVariable Long id, @RequestBody PlotHook.Status status) {
        PlotHook plotHook = plotHookService.getPlotHookById(id);
        if (plotHook != null && plotHook.getNovelId().equals(novelId)) {
            plotHook.setStatus(status);
            PlotHook updatedPlotHook = plotHookService.updatePlotHook(plotHook);
            return ResponseEntity.ok(updatedPlotHook);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlotHook(@PathVariable Long novelId, @PathVariable Long id) {
        PlotHook plotHook = plotHookService.getPlotHookById(id);
        if (plotHook != null && plotHook.getNovelId().equals(novelId)) {
            plotHookService.deletePlotHook(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}