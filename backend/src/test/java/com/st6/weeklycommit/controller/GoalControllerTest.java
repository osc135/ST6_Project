package com.st6.weeklycommit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------
    // GET /api/goals - returns all goals
    // -------------------------------------------------------

    @Test
    void getAll_returnsAllGoalNodes() throws Exception {
        // data.sql seeds 2 rally cries + 4 defining objectives + 9 outcomes = 15 total
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)));
    }

    @Test
    void getAll_containsRallyCries() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level == 'RALLY_CRY')]", hasSize(2)));
    }

    @Test
    void getAll_containsDefiningObjectives() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level == 'DEFINING_OBJECTIVE')]", hasSize(4)));
    }

    // -------------------------------------------------------
    // GET /api/goals/outcomes - returns only OUTCOME level goals
    // -------------------------------------------------------

    @Test
    void getOutcomes_returnsOnlyOutcomeLevelGoals() throws Exception {
        // 9 outcomes seeded in data.sql
        mockMvc.perform(get("/api/goals/outcomes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)))
                .andExpect(jsonPath("$[*].level", everyItem(is("OUTCOME"))));
    }

    @Test
    void getOutcomes_eachOutcomeHasParent() throws Exception {
        mockMvc.perform(get("/api/goals/outcomes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].parentId", everyItem(notNullValue())));
    }

    // -------------------------------------------------------
    // GET /api/goals/hierarchy - returns full hierarchy
    // -------------------------------------------------------

    @Test
    void getHierarchy_returnsAllGoals() throws Exception {
        mockMvc.perform(get("/api/goals/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)));
    }

    @Test
    void getHierarchy_rallyCriesHaveNoParent() throws Exception {
        mockMvc.perform(get("/api/goals/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level == 'RALLY_CRY')].parentId",
                        everyItem(nullValue())));
    }

    @Test
    void getHierarchy_definingObjectivesHaveRallyCryParents() throws Exception {
        mockMvc.perform(get("/api/goals/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level == 'DEFINING_OBJECTIVE')].parentId",
                        everyItem(notNullValue())));
    }
}
