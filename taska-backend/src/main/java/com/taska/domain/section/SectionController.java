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

    @GetMapping
    public List<SectionDto> getAll(@RequestParam(required = false) UUID project_id) {
        return sectionService.findAll(project_id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectionDto create(@Valid @RequestBody SectionRequest req) {
        return sectionService.create(req);
    }

    @GetMapping("/{id}")
    public SectionDto getById(@PathVariable UUID id) {
        return sectionService.findById(id);
    }

    @PutMapping("/{id}")
    public SectionDto update(@PathVariable UUID id, @RequestBody SectionRequest req) {
        return sectionService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        sectionService.delete(id);
    }
}
