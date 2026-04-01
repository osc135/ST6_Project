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
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CURRENT_WEEK_ID = "a0000000-0000-0000-0000-000000000001";
    private static final String ALICE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String BOB_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901";
    private static final String CAROL_ID = "c3d4e5f6-a7b8-9012-cdef-123456789012";
    private static final String DAN_ID = "d4e5f6a7-b8c9-0123-defa-234567890123";

    // -------------------------------------------------------
    // GET /api/dashboard/team/{weekId} - Team summary
    // -------------------------------------------------------

    @Test
    void getTeamSummary_returnsAllMembers() throws Exception {
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID)
                        .param("managerId", ALICE_ID)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getTeamSummary_carolHasGreenAlignment() throws Exception {
        // Carol has all 3 tasks linked to org goals (no custom, no unaligned)
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID)
                        .param("managerId", ALICE_ID)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userId == '" + CAROL_ID + "')].alignmentStatus",
                        contains("GREEN")))
                .andExpect(jsonPath("$[?(@.userId == '" + CAROL_ID + "')].taskCount",
                        contains(3)));
    }

    @Test
    void getTeamSummary_onlyReturnsDirectReports() throws Exception {
        // Alice is the manager — she should not appear in her own team summary
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID)
                        .param("managerId", ALICE_ID)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userId == '" + ALICE_ID + "')]").isEmpty());
    }

    @Test
    void getTeamSummary_danHasRedAlignment() throws Exception {
        // Dan has 1 aligned, 1 custom, 1 with NO goal at all => RED
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID)
                        .param("managerId", ALICE_ID)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userId == '" + DAN_ID + "')].alignmentStatus",
                        contains("RED")));
    }

    @Test
    void getTeamSummary_bobHasYellowAlignment() throws Exception {
        // Bob has 2 aligned + 1 custom goal => YELLOW
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID)
                        .param("managerId", ALICE_ID)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.userId == '" + BOB_ID + "')].alignmentStatus",
                        contains("YELLOW")));
    }

    @Test
    void getTeamSummary_byNonManager_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID)
                        .param("managerId", BOB_ID)
                        .header("X-User-Id", BOB_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTeamSummary_withoutHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID)
                        .param("managerId", ALICE_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTeamSummary_managerViewingAnotherManagersTeam_returnsForbidden() throws Exception {
        // Alice trying to view with a different managerId
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID)
                        .param("managerId", BOB_ID)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------
    // GET /api/dashboard/team/{weekId}/member/{userId} - Member tasks
    // -------------------------------------------------------

    @Test
    void getMemberTasks_returnsTasksForUser() throws Exception {
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID + "/member/" + CAROL_ID)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].ownerId", everyItem(is(CAROL_ID))));
    }

    @Test
    void getMemberTasks_aliceHasThreeCurrentWeekTasks() throws Exception {
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID + "/member/" + ALICE_ID)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getMemberTasks_nonExistentUser_returnsEmptyList() throws Exception {
        String nonExistentUserId = "00000000-0000-0000-0000-000000000099";
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID + "/member/" + nonExistentUserId)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getMemberTasks_byNonManager_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/team/" + CURRENT_WEEK_ID + "/member/" + CAROL_ID)
                        .header("X-User-Id", BOB_ID))
                .andExpect(status().isForbidden());
    }
}
