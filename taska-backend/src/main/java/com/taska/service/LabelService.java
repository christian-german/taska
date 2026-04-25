package com.taska.service;

import com.taska.dto.LabelRequest;
import com.taska.dto.LabelResponse;
import com.taska.exception.ResourceNotFoundException;
import com.taska.model.Label;
import com.taska.repository.LabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LabelService {

    private final LabelRepository labelRepo;

    public LabelService(LabelRepository labelRepo) {
        this.labelRepo = labelRepo;
    }

    @Transactional(readOnly = true)
    public List<LabelResponse> findAll() {
        return labelRepo.findAllByOrderByPositionAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LabelResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public LabelResponse create(LabelRequest req) {
        Label l = new Label();
        l.setName(req.name());
        l.setColor(req.color() != null ? req.color() : "charcoal");
        l.setPosition(req.order() != null ? req.order() : 0);
        l.setIsFavorite(req.isFavorite() != null ? req.isFavorite() : false);
        return toResponse(labelRepo.save(l));
    }

    public LabelResponse update(UUID id, LabelRequest req) {
        Label l = getOrThrow(id);
        if (req.name() != null) l.setName(req.name());
        if (req.color() != null) l.setColor(req.color());
        if (req.order() != null) l.setPosition(req.order());
        if (req.isFavorite() != null) l.setIsFavorite(req.isFavorite());
        return toResponse(labelRepo.save(l));
    }

    public void delete(UUID id) {
        labelRepo.delete(getOrThrow(id));
    }

    private Label getOrThrow(UUID id) {
        return labelRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));
    }

    public LabelResponse toResponse(Label l) {
        return new LabelResponse(l.getId(), l.getName(), l.getColor(), l.getPosition(), l.getIsFavorite());
    }
}
