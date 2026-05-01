package com.taska.domain.section;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SectionRepository extends JpaRepository<Section, UUID> {
    List<Section> findByProjectIdOrderByPositionAsc(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
