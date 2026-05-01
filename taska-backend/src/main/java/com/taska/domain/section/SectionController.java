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

    @GetMapping
    public List<SectionDto> getAll(@RequestParam(required = false) UUID project_id) {
        return sectionService.findAll(project_id).stream().map(sectionMapper::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectionDto create(@Valid @RequestBody SectionRequest req) {
        return sectionMapper.toDto(sectionService.create(req));
    }

    @GetMapping("/{id}")
    public SectionDto getById(@PathVariable UUID id) {
        return sectionMapper.toDto(sectionService.findById(id));
    }

    @PutMapping("/{id}")
    public SectionDto update(@PathVariable UUID id, @RequestBody SectionRequest req) {
        return sectionMapper.toDto(sectionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        sectionService.delete(id);
    }
}
