package com.ainovel.novelcraft.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "writing_styles")
@Data
public class WritingStyle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "novel_id", nullable = false, unique = true)
    private Long novelId;
    
    @Column(name = "avg_sentence_length")
    private Integer avgSentenceLength;
    
    @Column(name = "frequent_verbs", columnDefinition = "JSON")
    private String frequentVerbs; // JSON array string
    
    @Column(name = "banned_words", columnDefinition = "JSON")
    private String bannedWords; // JSON array string
    
    @Column(name = "emotional_distance")
    private String emotionalDistance; // immersive/distant
    
    @Column(name = "moral_stance")
    private String moralStance; // neutral/critical/compassionate
    
    @Column(name = "humor_style")
    private String humorStyle; // none/ironic/absurd
    
    @Column(name = "description_profile", columnDefinition = "JSON")
    private String descriptionProfile; // JSON object string
    
    @Column(name = "author_rules", columnDefinition = "JSON")
    private String authorRules; // JSON array string
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}