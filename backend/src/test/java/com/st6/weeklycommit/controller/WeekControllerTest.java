package com.st6.weeklycommit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WeekControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String CURRENT_WEEK_ID = "a0000000-0000-0000-0000-000000000001";
    private static final String PRIOR_WEEK_ID = "a0000000-0000-0000-0000-000000000002";

    @Test
    void getCurrentWeek_returnsCurrentWeek() throws Exception {
        mockMvc.perform(get("/api/weeks/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate", is("2026-03-30")))
                .andExpect(jsonPath("$.endDate", is("2026-04-03")));
    }

    @Test
    void getPriorWeek_returnsPriorWeek() throws Exception {
        mockMvc.perform(get("/api/weeks/prior"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate", is("2026-03-23")))
                .andExpect(jsonPath("$.endDate", is("2026-03-27")));
    }

    @Test
    void getByDate_currentWeekDate_returnsCurrentWeek() throws Exception {
        mockMvc.perform(get("/api/weeks/by-date").param("date", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(CURRENT_WEEK_ID)));
    }

    @Test
    void getByDate_priorWeekDate_returnsPriorWeek() throws Exception {
        mockMvc.perform(get("/api/weeks/by-date").param("date", "2026-03-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(PRIOR_WEEK_ID)));
    }

    @Test
    void getById_existingWeek_returnsWeek() throws Exception {
        mockMvc.perform(get("/api/weeks/" + CURRENT_WEEK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(CURRENT_WEEK_ID)))
                .andExpect(jsonPath("$.startDate", is("2026-03-30")));
    }

    @Test
    void getById_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/weeks/00000000-0000-0000-0000-000000000099"))
                .andExpect(status().isNotFound());
    }
}
