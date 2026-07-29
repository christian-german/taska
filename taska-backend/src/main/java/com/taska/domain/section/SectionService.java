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

    /**
     * Returns sections optionally scoped to a project. When {@code projectId} is provided,
     * results are ordered by position; otherwise all sections are returned unordered.
     *
     * @param projectId optional project UUID to scope the result
     * @return list of section entities
     */
    @Transactional(readOnly = true)
    public List<Section> findAll(UUID projectId) {
        if (projectId != null) return sectionRepo.findByProjectIdOrderByPositionAsc(projectId);
        return sectionRepo.findAll();
    }

    /**
     * Returns the section with the given ID, or throws {@link com.taska.exception.ResourceNotFoundException}.
     *
     * @param id the section UUID
     * @return the matching section entity
     */
    @Transactional(readOnly = true)
    public Section findById(UUID id) {
        return getOrThrow(id);
    }

    /**
     * Creates and persists a new section from the given request. Defaults position to 0.
     *
     * @param req the section creation payload
     * @return the persisted section entity
     */
    public Section create(SectionRequest req) {
        Section s = new Section();
        s.setName(req.name());
        s.setProjectId(req.projectId());
        s.setPosition(req.order() != null ? req.order() : 0);
        return sectionRepo.save(s);
    }

    /**
     * Updates a section's name and/or position with non-null fields from the request.
     *
     * @param id  the section UUID to update
     * @param req the update payload
     * @return the updated section entity
     */
    public Section update(UUID id, SectionRequest req) {
        Section s = getOrThrow(id);
        if (req.name() != null) s.setName(req.name());
        if (req.order() != null) s.setPosition(req.order());
        return sectionRepo.save(s);
    }

    /**
     * Deletes the section with the given ID.
     * Throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the section UUID to delete
     */
    public void delete(UUID id) {
        sectionRepo.delete(getOrThrow(id));
    }

    /**
     * Loads a section by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the section UUID
     * @return the section entity
     */
    Section getOrThrow(UUID id) {
        return sectionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + id));
    }
}
