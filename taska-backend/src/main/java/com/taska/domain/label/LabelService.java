package com.taska.domain.label;

import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepo;

    /**
     * Returns all labels ordered by position ascending.
     *
     * @return list of all label entities
     */
    @Transactional(readOnly = true)
    public List<Label> findAll() {
        return labelRepo.findAllByOrderByPositionAsc();
    }

    /**
     * Returns the label with the given ID, or throws {@link com.taska.exception.ResourceNotFoundException}.
     *
     * @param id the label UUID
     * @return the matching label entity
     */
    @Transactional(readOnly = true)
    public Label findById(UUID id) {
        return getOrThrow(id);
    }

    /**
     * Creates and persists a new label. Defaults: color "charcoal", position 0, not a favourite.
     *
     * @param req the label creation payload
     * @return the persisted label entity
     */
    public Label create(LabelRequest req) {
        Label l = new Label();
        l.setName(req.name());
        l.setColor(req.color() != null ? req.color() : "charcoal");
        l.setPosition(req.order() != null ? req.order() : 0);
        l.setIsFavorite(req.isFavorite() != null ? req.isFavorite() : false);
        return labelRepo.save(l);
    }

    /**
     * Updates an existing label with non-null fields from the request.
     *
     * @param id  the label UUID to update
     * @param req the update payload
     * @return the updated label entity
     */
    public Label update(UUID id, LabelRequest req) {
        Label l = getOrThrow(id);
        if (req.name() != null) l.setName(req.name());
        if (req.color() != null) l.setColor(req.color());
        if (req.order() != null) l.setPosition(req.order());
        if (req.isFavorite() != null) l.setIsFavorite(req.isFavorite());
        return labelRepo.save(l);
    }

    /**
     * Deletes the label with the given ID.
     * Throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the label UUID to delete
     */
    public void delete(UUID id) {
        labelRepo.delete(getOrThrow(id));
    }

    /**
     * Loads a label by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the label UUID
     * @return the label entity
     */
    private Label getOrThrow(UUID id) {
        return labelRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));
    }
}
