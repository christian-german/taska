package com.taska.label;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taska.label.adapter.http.LabelController;
import com.taska.label.adapter.http.LabelMapper;
import com.taska.label.adapter.http.LabelMapperImpl;
import com.taska.label.application.LabelService;
import com.taska.label.model.Label;
import com.taska.label.model.LabelCreateParameters;
import com.taska.label.model.LabelUpdateParameters;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

class LabelControllerTest {

    private LabelService labelService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        labelService = mock(LabelService.class);
        LabelMapper labelMapper = new LabelMapperImpl();
        mockMvc = MockMvcBuilders.standaloneSetup(new LabelController(labelService, labelMapper))
                .setMessageConverters(new JacksonJsonHttpMessageConverter(applicationJsonMapper()))
                .build();
    }

    /**
     * Mirrors spring.jackson.deserialization.fail-on-unknown-properties from the application.
     */
    private static JsonMapper applicationJsonMapper() {
        return JsonMapper.builder().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
    }

    @Test
    void create_mapsOmittedOptionalPropertiesToDocumentedDefaults() throws Exception {
        LabelCreateParameters expectedParameters = new LabelCreateParameters("Work", "charcoal", 0, false);
        Label createdLabel = label("Work", "charcoal", 0, false);
        when(labelService.create(expectedParameters)).thenReturn(createdLabel);

        mockMvc.perform(post("/labels").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Work"}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Work"))
                .andExpect(jsonPath("$.color").value("charcoal"))
                .andExpect(jsonPath("$.order").value(0))
                .andExpect(jsonPath("$.isFavorite").value(false));

        verify(labelService).create(expectedParameters);
    }

    @Test
    void update_mapsACompleteReplacementRequest() throws Exception {
        UUID labelId = UUID.randomUUID();
        LabelUpdateParameters expectedParameters = new LabelUpdateParameters("Personal", "green", 2, true);
        Label updatedLabel = label("Personal", "green", 2, true);
        when(labelService.replace(labelId, expectedParameters)).thenReturn(updatedLabel);

        mockMvc.perform(put("/labels/{labelId}", labelId).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "name":"Personal",
                  "color":"green",
                  "order":2,
                  "isFavorite":true
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Personal"))
                .andExpect(jsonPath("$.color").value("green"))
                .andExpect(jsonPath("$.order").value(2))
                .andExpect(jsonPath("$.isFavorite").value(true));

        verify(labelService).replace(labelId, expectedParameters);
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "color", "order", "isFavorite"})
    void update_rejectsMissingReplacementProperties(String omittedProperty) throws Exception {
        String requestBody = switch (omittedProperty) {
            case "name" -> """
                    {"color":"blue","order":1,"isFavorite":false}
                    """;
            case "color" -> """
                    {"name":"Work","order":1,"isFavorite":false}
                    """;
            case "order" -> """
                    {"name":"Work","color":"blue","isFavorite":false}
                    """;
            case "isFavorite" -> """
                    {"name":"Work","color":"blue","order":1}
                    """;
            default -> throw new IllegalArgumentException("Unexpected property: " + omittedProperty);
        };

        mockMvc.perform(put("/labels/{labelId}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(labelService);
    }

    @Test
    void update_rejectsNullForANonNullableReplacementProperty() throws Exception {
        mockMvc.perform(put("/labels/{labelId}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "name":"Work",
                  "color":null,
                  "order":1,
                  "isFavorite":false
                }
                """)).andExpect(status().isBadRequest());

        verifyNoInteractions(labelService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"post", "put"})
    void requests_rejectUnknownProperties(String method) throws Exception {
        String requestBody = """
                {
                  "name":"Work",
                  "color":"blue",
                  "order":1,
                  "isFavorite":false,
                  "unexpected":"value"
                }
                """;

        var request = method.equals("post") ? post("/labels") : put("/labels/{labelId}", UUID.randomUUID());

        mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isBadRequest());

        verifyNoInteractions(labelService);
    }

    private Label label(String name, String color, int position, boolean favorite) {
        Label label = new Label();
        label.setId(UUID.randomUUID());
        label.setName(name);
        label.setColor(color);
        label.setPosition(position);
        label.setIsFavorite(favorite);
        return label;
    }
}
