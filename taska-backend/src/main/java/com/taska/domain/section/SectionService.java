package com.taska.domain.section;

import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepo;

    @Transactional(readOnly = true)
    public List<Section> findAll(UUID projectId) {
        if (projectId != null) return sectionRepo.findByProjectIdOrderByPositionAsc(projectId);
        return sectionRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Section findById(UUID id) {
        return getOrThrow(id);
    }

    public Section create(SectionRequest req) {
        Section s = new Section();
        s.setName(req.name());
        s.setProjectId(req.projectId());
        s.setPosition(req.order() != null ? req.order() : 0);
        return sectionRepo.save(s);
    }

    public Section update(UUID id, SectionRequest req) {
        Section s = getOrThrow(id);
        if (req.name() != null) s.setName(req.name());
        if (req.order() != null) s.setPosition(req.order());
        return sectionRepo.save(s);
    }

    public void delete(UUID id) {
        sectionRepo.delete(getOrThrow(id));
    }

    Section getOrThrow(UUID id) {
        return sectionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + id));
    }
}
