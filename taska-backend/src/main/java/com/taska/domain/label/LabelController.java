package com.taska.domain.label;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/labels")
public class LabelController {

    private final LabelService labelService;

    @GetMapping
    public List<LabelDto> getAll() {
        return labelService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDto create(@Valid @RequestBody LabelRequest req) {
        return labelService.create(req);
    }

    @GetMapping("/{id}")
    public LabelDto getById(@PathVariable UUID id) {
        return labelService.findById(id);
    }

    @PutMapping("/{id}")
    public LabelDto update(@PathVariable UUID id, @RequestBody LabelRequest req) {
        return labelService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        labelService.delete(id);
    }
}
