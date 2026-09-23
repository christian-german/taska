package com.taska.label.application;

import com.taska.label.model.Label;
import com.taska.label.model.LabelCreateParameters;
import com.taska.label.model.LabelUpdateParameters;
import com.taska.label.persistence.LabelRepository;
import com.taska.platform.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;

    /**
     * Returns all labels ordered by position ascending.
     *
     * @return list of all label entities
     */
    public List<Label> findAll() {
        return labelRepository.findAllByOrderByPositionAsc();
    }

    /**
     * Returns the label with the given ID, or throws {@link com.taska.platform.exception.ResourceNotFoundException}.
     *
     * @param labelId the label UUID
     * @return the matching label entity
     */
    public Label findById(UUID labelId) {
        return getOrThrow(labelId);
    }

    /**
     * Creates and persists a new label. Defaults: color "charcoal", position 0, not a favourite.
     *
     * @param labelCreateParameters application parameters for the new label
     * @return the persisted label entity
     */
    @Transactional
    public Label create(LabelCreateParameters labelCreateParameters) {
        Label label = new Label();
        label.setName(labelCreateParameters.name());
        label.setColor(labelCreateParameters.color());
        label.setPosition(labelCreateParameters.position());
        label.setIsFavorite(labelCreateParameters.favorite());
        return labelRepository.save(label);
    }

    /**
     * Replaces all mutable fields of an existing label.
     *
     * @param labelId the label UUID to update
     * @param labelUpdateParameters application parameters for the replacement
     * @return the updated label entity
     */
    @Transactional
    public Label replace(UUID labelId, LabelUpdateParameters labelUpdateParameters) {
        Label label = getOrThrow(labelId);
        label.setName(labelUpdateParameters.name());
        label.setColor(labelUpdateParameters.color());
        label.setPosition(labelUpdateParameters.position());
        label.setIsFavorite(labelUpdateParameters.favorite());
        return labelRepository.save(label);
    }

    /**
     * Deletes the label with the given ID. Throws {@link com.taska.platform.exception.ResourceNotFoundException} if not found.
     *
     * @param labelId the label UUID to delete
     */
    @Transactional
    public void delete(UUID labelId) {
        labelRepository.delete(getOrThrow(labelId));
    }

    /**
     * Loads a label by ID or throws {@link com.taska.platform.exception.ResourceNotFoundException} if not found.
     *
     * @param labelId the label UUID
     * @return the label entity
     */
    private Label getOrThrow(UUID labelId) {
        return labelRepository.findById(labelId).orElseThrow(() -> new ResourceNotFoundException("Label not found: " + labelId));
    }
}
