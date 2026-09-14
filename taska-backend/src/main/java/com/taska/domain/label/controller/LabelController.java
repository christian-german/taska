package com.taska.domain.label.controller;

import com.taska.domain.label.service.LabelCreateParameters;
import com.taska.domain.label.service.LabelService;
import com.taska.domain.label.service.LabelUpdateParameters;
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
     * @param labelCreateRequest validated label creation payload
     * @return the created label DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDto create(@Valid @RequestBody LabelCreateRequest labelCreateRequest) {
        LabelCreateParameters labelCreateParameters = labelMapper.toParameters(labelCreateRequest);
        return labelMapper.toDto(labelService.create(labelCreateParameters));
    }

    /**
     * Returns a single label by its UUID.
     *
     * @param labelId the label UUID
     * @return the label DTO, or 404 if not found
     */
    @GetMapping("/{labelId}")
    public LabelDto getById(@PathVariable UUID labelId) {
        return labelMapper.toDto(labelService.findById(labelId));
    }

    /**
     * Replaces all mutable fields of an existing label.
     *
     * @param labelId the label UUID
     * @param labelUpdateRequest the complete replacement payload
     * @return the updated label DTO
     */
    @PutMapping("/{labelId}")
    public LabelDto update(
            @PathVariable UUID labelId,
            @Valid @RequestBody LabelUpdateRequest labelUpdateRequest) {
        LabelUpdateParameters labelUpdateParameters = labelMapper.toParameters(labelUpdateRequest);
        return labelMapper.toDto(labelService.update(labelId, labelUpdateParameters));
    }

    /**
     * Deletes the label with the given ID. Returns HTTP 204 on success.
     *
     * @param labelId the label UUID to delete
     */
    @DeleteMapping("/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID labelId) {
        labelService.delete(labelId);
    }
}
