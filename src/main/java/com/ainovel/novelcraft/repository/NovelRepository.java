package com.ainovel.novelcraft.repository;

import com.ainovel.novelcraft.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {
}