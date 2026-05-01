package com.taska.domain.label;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/labels")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public List<LabelResponse> getAll() {
        return labelService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelResponse create(@Valid @RequestBody LabelRequest req) {
        return labelService.create(req);
    }

    @GetMapping("/{id}")
    public LabelResponse getById(@PathVariable UUID id) {
        return labelService.findById(id);
    }

    @PutMapping("/{id}")
    public LabelResponse update(@PathVariable UUID id, @RequestBody LabelRequest req) {
        return labelService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        labelService.delete(id);
    }
}
