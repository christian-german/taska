package com.taska.domain.section;

import com.taska.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SectionService {

    private final SectionRepository sectionRepo;

    public SectionService(SectionRepository sectionRepo) {
        this.sectionRepo = sectionRepo;
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> findAll(UUID projectId) {
        if (projectId != null) {
            return sectionRepo.findByProjectIdOrderByPositionAsc(projectId).stream().map(this::toResponse).toList();
        }
        return sectionRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SectionResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public SectionResponse create(SectionRequest req) {
        Section s = new Section();
        s.setName(req.name());
        s.setProjectId(req.projectId());
        s.setPosition(req.order() != null ? req.order() : 0);
        return toResponse(sectionRepo.save(s));
    }

    public SectionResponse update(UUID id, SectionRequest req) {
        Section s = getOrThrow(id);
        if (req.name() != null) s.setName(req.name());
        if (req.order() != null) s.setPosition(req.order());
        return toResponse(sectionRepo.save(s));
    }

    public void delete(UUID id) {
        sectionRepo.delete(getOrThrow(id));
    }

    private Section getOrThrow(UUID id) {
        return sectionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + id));
    }

    public SectionResponse toResponse(Section s) {
        return new SectionResponse(s.getId(), s.getName(), s.getProjectId(), s.getPosition(), s.getCreatedAt());
    }
}
