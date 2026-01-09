package com.aiwriter.service;

import com.aiwriter.entity.PlotForeshadowing;
import com.aiwriter.repository.PlotForeshadowingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlotForeshadowingServiceTest {

    @Mock
    private PlotForeshadowingRepository plotForeshadowingRepository;

    @InjectMocks
    private PlotForeshadowingService plotForeshadowingService;

    private PlotForeshadowing sampleForeshadowing;

    @BeforeEach
    void setUp() {
        sampleForeshadowing = new PlotForeshadowing();
        sampleForeshadowing.setId(1L);
        sampleForeshadowing.setNovelId(100L);
        sampleForeshadowing.setTitle("Test Foreshadowing");
        sampleForeshadowing.setDescription("This is a test foreshadowing");
        sampleForeshadowing.setStatus("PENDING");
        sampleForeshadowing.setPriority(5);
        sampleForeshadowing.setPlantedInChapter(1);
    }

    @Test
    void testGetPlotForeshadowings() {
        // Arrange
        List<PlotForeshadowing> expectedList = Arrays.asList(sampleForeshadowing);
        when(plotForeshadowingRepository.findByNovelIdOrderByPlantedInChapterAsc(100L)).thenReturn(expectedList);

        // Act
        List<PlotForeshadowing> actualList = plotForeshadowingService.getPlotForeshadowings(100L);

        // Assert
        assertEquals(1, actualList.size());
        assertEquals("Test Foreshadowing", actualList.get(0).getTitle());
        verify(plotForeshadowingRepository).findByNovelIdOrderByPlantedInChapterAsc(100L);
    }

    @Test
    void testGetPlotForeshadowingsByChapter() {
        // Arrange
        List<PlotForeshadowing> expectedList = Arrays.asList(sampleForeshadowing);
        when(plotForeshadowingRepository.findByChapterId(10L)).thenReturn(expectedList);

        // Act
        List<PlotForeshadowing> actualList = plotForeshadowingService.getPlotForeshadowingsByChapter(10L);

        // Assert
        assertEquals(1, actualList.size());
        assertEquals("Test Foreshadowing", actualList.get(0).getTitle());
        verify(plotForeshadowingRepository).findByChapterId(10L);
    }

    @Test
    void testCreatePlotForeshadowing() {
        // Arrange
        when(plotForeshadowingRepository.save(any(PlotForeshadowing.class))).thenReturn(sampleForeshadowing);

        // Act
        PlotForeshadowing result = plotForeshadowingService.createPlotForeshadowing(sampleForeshadowing);

        // Assert
        assertNotNull(result);
        assertEquals("Test Foreshadowing", result.getTitle());
        verify(plotForeshadowingRepository).save(any(PlotForeshadowing.class));
    }

    @Test
    void testUpdatePlotForeshadowing() {
        // Arrange
        PlotForeshadowing updatedForeshadowing = new PlotForeshadowing();
        updatedForeshadowing.setTitle("Updated Foreshadowing");
        updatedForeshadowing.setDescription("Updated description");
        updatedForeshadowing.setStatus("TRIGGERED");
        
        when(plotForeshadowingRepository.findById(1L)).thenReturn(Optional.of(sampleForeshadowing));
        when(plotForeshadowingRepository.save(any(PlotForeshadowing.class))).thenReturn(updatedForeshadowing);

        // Act
        PlotForeshadowing result = plotForeshadowingService.updatePlotForeshadowing(1L, updatedForeshadowing);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Foreshadowing", result.getTitle());
        assertEquals("TRIGGERED", result.getStatus());
        verify(plotForeshadowingRepository).findById(1L);
        verify(plotForeshadowingRepository).save(any(PlotForeshadowing.class));
    }

    @Test
    void testUpdatePlotForeshadowing_NotFound() {
        // Arrange
        when(plotForeshadowingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            plotForeshadowingService.updatePlotForeshadowing(999L, sampleForeshadowing);
        });
        verify(plotForeshadowingRepository).findById(999L);
    }

    @Test
    void testDeletePlotForeshadowing() {
        // Act
        plotForeshadowingService.deletePlotForeshadowing(1L);

        // Assert
        verify(plotForeshadowingRepository).deleteById(1L);
    }

    @Test
    void testTriggerPlotForeshadowing() {
        // Arrange
        when(plotForeshadowingRepository.findById(1L)).thenReturn(Optional.of(sampleForeshadowing));
        when(plotForeshadowingRepository.save(any(PlotForeshadowing.class))).thenReturn(sampleForeshadowing);

        // Act
        PlotForeshadowing result = plotForeshadowingService.triggerPlotForeshadowing(1L, 5);

        // Assert
        assertNotNull(result);
        assertEquals("TRIGGERED", result.getStatus());
        assertEquals(Integer.valueOf(5), result.getTriggeredInChapter());
        verify(plotForeshadowingRepository).findById(1L);
        verify(plotForeshadowingRepository).save(any(PlotForeshadowing.class));
    }

    @Test
    void testResolvePlotForeshadowing() {
        // Arrange
        when(plotForeshadowingRepository.findById(1L)).thenReturn(Optional.of(sampleForeshadowing));
        when(plotForeshadowingRepository.save(any(PlotForeshadowing.class))).thenReturn(sampleForeshadowing);

        // Act
        PlotForeshadowing result = plotForeshadowingService.resolvePlotForeshadowing(1L, 10, "Resolved successfully");

        // Assert
        assertNotNull(result);
        assertEquals("RESOLVED", result.getStatus());
        assertEquals(Integer.valueOf(10), result.getResolvedInChapter());
        assertEquals("Resolved successfully", result.getResolutionNote());
        verify(plotForeshadowingRepository).findById(1L);
        verify(plotForeshadowingRepository).save(any(PlotForeshadowing.class));
    }
}