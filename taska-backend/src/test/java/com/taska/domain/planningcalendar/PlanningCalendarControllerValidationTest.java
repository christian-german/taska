package com.taska.domain.planningcalendar;

import com.taska.domain.planningcalendar.controller.PlanningCalendarController;
import com.taska.domain.planningcalendar.controller.PlanningCalendarMapper;
import com.taska.domain.planningcalendar.service.PlanningCalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlanningCalendarControllerValidationTest {

    private PlanningCalendarService planningCalendarService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        planningCalendarService = mock(PlanningCalendarService.class);
        PlanningCalendarMapper planningCalendarMapper = new PlanningCalendarMapper() {};
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PlanningCalendarController(
                        planningCalendarService,
                        planningCalendarMapper))
                .build();
    }

    @Test
    void createRejectsAnInvalidNestedRuleBeforeInvokingTheService() throws Exception {
        mockMvc.perform(post("/planning-calendars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Work",
                                  "rules":[{"dayOfWeek":8,"startMinute":720,"endMinute":540}]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(planningCalendarService);
    }

    @Test
    void updateRejectsOverlappingRulesBeforeInvokingTheService() throws Exception {
        mockMvc.perform(put("/planning-calendars/{planningCalendarId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Work",
                                  "rules":[
                                    {"dayOfWeek":1,"startMinute":540,"endMinute":720},
                                    {"dayOfWeek":1,"startMinute":600,"endMinute":780}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(planningCalendarService);
    }
}
