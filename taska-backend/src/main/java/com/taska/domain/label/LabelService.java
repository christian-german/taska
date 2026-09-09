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

    private final LabelRepository labelRepository;

    /**
     * Returns all labels ordered by position ascending.
     *
     * @return list of all label entities
     */
    @Transactional(readOnly = true)
    public List<Label> findAll() {
        return labelRepository.findAllByOrderByPositionAsc();
    }

    /**
     * Returns the label with the given ID, or throws {@link com.taska.exception.ResourceNotFoundException}.
     *
     * @param labelId the label UUID
     * @return the matching label entity
     */
    @Transactional(readOnly = true)
    public Label findById(UUID labelId) {
        return getOrThrow(labelId);
    }

    /**
     * Creates and persists a new label. Defaults: color "charcoal", position 0, not a favourite.
     *
     * @param labelRequest the label creation payload
     * @return the persisted label entity
     */
    public Label create(LabelRequest labelRequest) {
        Label label = new Label();
        label.setName(labelRequest.name());
        label.setColor(labelRequest.color() != null ? labelRequest.color() : "charcoal");
        label.setPosition(labelRequest.order() != null ? labelRequest.order() : 0);
        label.setIsFavorite(labelRequest.isFavorite() != null ? labelRequest.isFavorite() : false);
        return labelRepository.save(label);
    }

    /**
     * Updates an existing label with non-null fields from the request.
     *
     * @param labelId      the label UUID to update
     * @param labelRequest the update payload
     * @return the updated label entity
     */
    public Label update(UUID labelId, LabelRequest labelRequest) {
        Label label = getOrThrow(labelId);
        if (labelRequest.name() != null) {
            label.setName(labelRequest.name());
        }
        if (labelRequest.color() != null) {
            label.setColor(labelRequest.color());
        }
        if (labelRequest.order() != null) {
            label.setPosition(labelRequest.order());
        }
        if (labelRequest.isFavorite() != null) {
            label.setIsFavorite(labelRequest.isFavorite());
        }
        return labelRepository.save(label);
    }

    /**
     * Deletes the label with the given ID.
     * Throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param labelId the label UUID to delete
     */
    public void delete(UUID labelId) {
        labelRepository.delete(getOrThrow(labelId));
    }

    /**
     * Loads a label by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param labelId the label UUID
     * @return the label entity
     */
    private Label getOrThrow(UUID labelId) {
        return labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + labelId));
    }
}
