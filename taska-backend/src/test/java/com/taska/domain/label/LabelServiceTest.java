package com.taska.domain.label;

import com.taska.domain.label.repository.LabelRepository;
import com.taska.domain.label.service.LabelCreateParameters;
import com.taska.domain.label.service.LabelService;
import com.taska.domain.label.service.LabelUpdateParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private LabelService labelService;

    @Test
    void create_appliesEveryApplicationParameter() {
        LabelCreateParameters labelCreateParameters =
                new LabelCreateParameters("Work", "blue", 3, true);
        when(labelRepository.save(org.mockito.ArgumentMatchers.any(Label.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Label createdLabel = labelService.create(labelCreateParameters);

        assertThat(createdLabel.getName()).isEqualTo("Work");
        assertThat(createdLabel.getColor()).isEqualTo("blue");
        assertThat(createdLabel.getPosition()).isEqualTo(3);
        assertThat(createdLabel.getIsFavorite()).isTrue();
        verify(labelRepository).save(createdLabel);
    }

    @Test
    void update_replacesEveryMutableProperty() {
        UUID labelId = UUID.randomUUID();
        Label existingLabel = label("Old", "charcoal", 1, false);
        LabelUpdateParameters labelUpdateParameters =
                new LabelUpdateParameters("Updated", "green", 4, true);
        when(labelRepository.findById(labelId)).thenReturn(Optional.of(existingLabel));
        when(labelRepository.save(existingLabel)).thenReturn(existingLabel);

        Label updatedLabel = labelService.update(labelId, labelUpdateParameters);

        assertThat(updatedLabel.getName()).isEqualTo("Updated");
        assertThat(updatedLabel.getColor()).isEqualTo("green");
        assertThat(updatedLabel.getPosition()).isEqualTo(4);
        assertThat(updatedLabel.getIsFavorite()).isTrue();
        verify(labelRepository).save(existingLabel);
    }

    private Label label(String name, String color, int position, boolean favorite) {
        Label label = new Label();
        label.setName(name);
        label.setColor(color);
        label.setPosition(position);
        label.setIsFavorite(favorite);
        return label;
    }
}
