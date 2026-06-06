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
    private final LabelMapper labelMapper;

    /**
     * Returns all labels ordered by position.
     *
     * @return list of all label DTOs
     */
    @GetMapping
    public List<LabelDto> getAll() {
        return labelService.findAll().stream().map(labelMapper::toDto).toList();
    }

    /**
     * Creates a new label. Returns HTTP 201 with the created label DTO.
     *
     * @param req validated label creation payload
     * @return the created label DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDto create(@Valid @RequestBody LabelRequest req) {
        return labelMapper.toDto(labelService.create(req));
    }

    /**
     * Returns a single label by its UUID.
     *
     * @param id the label UUID
     * @return the label DTO, or 404 if not found
     */
    @GetMapping("/{id}")
    public LabelDto getById(@PathVariable UUID id) {
        return labelMapper.toDto(labelService.findById(id));
    }

    /**
     * Updates an existing label with non-null fields from the request.
     *
     * @param id  the label UUID
     * @param req the update payload
     * @return the updated label DTO
     */
    @PutMapping("/{id}")
    public LabelDto update(@PathVariable UUID id, @RequestBody LabelRequest req) {
        return labelMapper.toDto(labelService.update(id, req));
    }

    /**
     * Deletes the label with the given ID. Returns HTTP 204 on success.
     *
     * @param id the label UUID to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        labelService.delete(id);
    }
}
