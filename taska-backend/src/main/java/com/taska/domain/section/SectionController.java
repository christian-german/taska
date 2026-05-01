package com.taska.domain.section;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public List<SectionResponse> getAll(@RequestParam(required = false) UUID project_id) {
        return sectionService.findAll(project_id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponse create(@Valid @RequestBody SectionRequest req) {
        return sectionService.create(req);
    }

    @GetMapping("/{id}")
    public SectionResponse getById(@PathVariable UUID id) {
        return sectionService.findById(id);
    }

    @PutMapping("/{id}")
    public SectionResponse update(@PathVariable UUID id, @RequestBody SectionRequest req) {
        return sectionService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        sectionService.delete(id);
    }
}
