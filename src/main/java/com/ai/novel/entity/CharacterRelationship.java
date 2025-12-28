package com.ai.novel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 角色关系实体
 */
@Entity
@Table(name = "character_relationships", 
       uniqueConstraints = @UniqueConstraint(name = "uk_relationship", 
                                             columnNames = {"source_character_id", "target_character_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CharacterRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @ToString.Exclude
    private Novel novel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_character_id", nullable = false)
    @JsonIgnoreProperties({"relationshipsAsSource", "relationshipsAsTarget"})
    @ToString.Exclude
    private Character sourceCharacter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_character_id", nullable = false)
    @JsonIgnoreProperties({"relationshipsAsSource", "relationshipsAsTarget"})
    @ToString.Exclude
    private Character targetCharacter;

    @Column(name = "relationship_type", nullable = false, length = 50)
    private String relationshipType;

    @Lob
    @Column(name = "relationship_desc", columnDefinition = "TEXT")
    private String relationshipDesc;

    @Column(name = "intimacy_level")
    @Builder.Default
    private Integer intimacyLevel = 50;

    @Column(name = "established_chapter")
    private Integer establishedChapter;

    @Column(name = "last_interaction_chapter")
    private Integer lastInteractionChapter;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "relationship_history", columnDefinition = "JSON")
    private List<Map<String, Object>> relationshipHistory;

    @Column(name = "is_active", columnDefinition = "TINYINT DEFAULT 1")
    @Builder.Default
    private Integer isActive = 1;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
