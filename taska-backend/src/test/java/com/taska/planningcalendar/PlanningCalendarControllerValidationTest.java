package com.taska.planningcalendar;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taska.planningcalendar.adapter.http.PlanningCalendarController;
import com.taska.planningcalendar.adapter.http.PlanningCalendarMapper;
import com.taska.planningcalendar.adapter.http.PlanningCalendarMapperImpl;
import com.taska.planningcalendar.application.PlanningCalendarService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PlanningCalendarControllerValidationTest {

    private PlanningCalendarService planningCalendarService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        planningCalendarService = mock(PlanningCalendarService.class);
        PlanningCalendarMapper planningCalendarMapper = new PlanningCalendarMapperImpl();
        mockMvc = MockMvcBuilders.standaloneSetup(new PlanningCalendarController(planningCalendarService, planningCalendarMapper)).build();
    }

    @Test
    void createRejectsAnInvalidNestedRuleBeforeInvokingTheService() throws Exception {
        mockMvc.perform(post("/planning-calendars").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "name":"Work",
                  "rules":[{"dayOfWeek":8,"startMinute":720,"endMinute":540}]
                }
                """)).andExpect(status().isBadRequest());

        verifyNoInteractions(planningCalendarService);
    }

    @Test
    void updateRejectsOverlappingRulesBeforeInvokingTheService() throws Exception {
        mockMvc.perform(put("/planning-calendars/{planningCalendarId}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "name":"Work",
                  "rules":[
                    {"dayOfWeek":1,"startMinute":540,"endMinute":720},
                    {"dayOfWeek":1,"startMinute":600,"endMinute":780}
                  ]
                }
                """)).andExpect(status().isBadRequest());

        verifyNoInteractions(planningCalendarService);
    }
}
