package com.taska.domain.section;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sections")
public class SectionController {

    private final SectionService sectionService;
    private final SectionMapper sectionMapper;

    /**
     * Returns all sections, optionally filtered by project.
     *
     * @param project_id optional project UUID to scope the result
     * @return list of section DTOs
     */
    @GetMapping
    public List<SectionDto> getAll(@RequestParam(required = false) UUID project_id) {
        return sectionService.findAll(project_id).stream().map(sectionMapper::toDto).toList();
    }

    /**
     * Creates a new section. Returns HTTP 201 with the created section DTO.
     *
     * @param req validated section creation payload
     * @return the created section DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectionDto create(@Valid @RequestBody SectionRequest req) {
        return sectionMapper.toDto(sectionService.create(req));
    }

    /**
     * Returns a single section by its UUID.
     *
     * @param id the section UUID
     * @return the section DTO, or 404 if not found
     */
    @GetMapping("/{id}")
    public SectionDto getById(@PathVariable UUID id) {
        return sectionMapper.toDto(sectionService.findById(id));
    }

    /**
     * Updates an existing section with non-null fields from the request.
     *
     * @param id  the section UUID
     * @param req the update payload
     * @return the updated section DTO
     */
    @PutMapping("/{id}")
    public SectionDto update(@PathVariable UUID id, @RequestBody SectionRequest req) {
        return sectionMapper.toDto(sectionService.update(id, req));
    }

    /**
     * Deletes the section with the given ID. Returns HTTP 204 on success.
     *
     * @param id the section UUID to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        sectionService.delete(id);
    }
}
