package com.st6.weeklycommit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.st6.weeklycommit.dto.CreateCommitRequest;
import com.st6.weeklycommit.dto.ReconcileRequest;
import com.st6.weeklycommit.dto.UpdateCommitRequest;
import com.st6.weeklycommit.entity.WeeklyCommit;
import com.st6.weeklycommit.entity.enums.CommitStatus;
import com.st6.weeklycommit.entity.enums.Priority;
import com.st6.weeklycommit.repository.WeeklyCommitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WeeklyCommitRepository commitRepository;

    // Seeded IDs from data.sql
    private static final String ALICE_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String BOB_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901";
    private static final String CURRENT_WEEK_ID = "a0000000-0000-0000-0000-000000000001";
    private static final String PRIOR_WEEK_ID = "a0000000-0000-0000-0000-000000000002";
    private static final String OUTCOME_GOAL_ID = "30000000-0000-0000-0000-000000000001";

    // Seeded commit IDs (current week, DRAFT status)
    private static final String ALICE_DRAFT_COMMIT_ID = "c0000000-0000-0000-0000-000000000011";
    private static final String BOB_DRAFT_COMMIT_ID = "c0000000-0000-0000-0000-000000000013";

    // Seeded commit ID (prior week, LOCKED status - Bob's unreconciled task)
    private static final String BOB_LOCKED_COMMIT_ID = "c0000000-0000-0000-0000-000000000005";

    // -------------------------------------------------------
    // POST /api/commits - Create
    // -------------------------------------------------------

    @Test
    void createCommit_withOrgGoal_returnsCreatedWithGoalId() throws Exception {
        CreateCommitRequest request = new CreateCommitRequest(
                "Implement SSO login page",
                Priority.KING,
                UUID.fromString(OUTCOME_GOAL_ID),
                null,
                UUID.fromString(ALICE_ID),
                UUID.fromString(CURRENT_WEEK_ID)
        );

        mockMvc.perform(post("/api/commits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ALICE_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Implement SSO login page"))
                .andExpect(jsonPath("$.priority").value("KING"))
                .andExpect(jsonPath("$.goalId").value(OUTCOME_GOAL_ID))
                .andExpect(jsonPath("$.customGoal").value(false))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void createCommit_withCustomGoal_returnsCreatedWithCustomGoalText() throws Exception {
        CreateCommitRequest request = new CreateCommitRequest(
                "Organize team offsite",
                Priority.PAWN,
                null,
                "Team building activity",
                UUID.fromString(BOB_ID),
                UUID.fromString(CURRENT_WEEK_ID)
        );

        mockMvc.perform(post("/api/commits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", BOB_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Organize team offsite"))
                .andExpect(jsonPath("$.customGoalText").value("Team building activity"))
                .andExpect(jsonPath("$.customGoal").value(true))
                .andExpect(jsonPath("$.goalId").isEmpty());
    }

    @Test
    void createCommit_withoutUserIdHeader_returnsUnauthorized() throws Exception {
        CreateCommitRequest request = new CreateCommitRequest(
                "Sneaky task", Priority.PAWN, null, null,
                UUID.fromString(ALICE_ID), UUID.fromString(CURRENT_WEEK_ID)
        );

        mockMvc.perform(post("/api/commits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCommit_forAnotherUser_returnsForbidden() throws Exception {
        CreateCommitRequest request = new CreateCommitRequest(
                "Task for Alice", Priority.PAWN, null, null,
                UUID.fromString(ALICE_ID), UUID.fromString(CURRENT_WEEK_ID)
        );

        mockMvc.perform(post("/api/commits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", BOB_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------
    // PUT /api/commits/{id} - Update
    // -------------------------------------------------------

    @Test
    void updateCommit_inDraftStatus_succeeds() throws Exception {
        UpdateCommitRequest request = new UpdateCommitRequest(
                "Updated task name",
                Priority.QUEEN,
                null,
                null
        );

        mockMvc.perform(put("/api/commits/" + ALICE_DRAFT_COMMIT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ALICE_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated task name"))
                .andExpect(jsonPath("$.priority").value("QUEEN"));
    }

    @Test
    void updateCommit_inLockedStatus_returnsBadRequest() throws Exception {
        UpdateCommitRequest request = new UpdateCommitRequest(
                "Trying to update locked task",
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/commits/" + BOB_LOCKED_COMMIT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", BOB_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("DRAFT")));
    }

    @Test
    void updateCommit_byWrongUser_returnsForbidden() throws Exception {
        UpdateCommitRequest request = new UpdateCommitRequest("Hacked", null, null, null);

        mockMvc.perform(put("/api/commits/" + ALICE_DRAFT_COMMIT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", BOB_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------
    // DELETE /api/commits/{id}
    // -------------------------------------------------------

    @Test
    void deleteCommit_inDraftStatus_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/commits/" + ALICE_DRAFT_COMMIT_ID)
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isNoContent());

        // Verify actually deleted
        assertFalse(commitRepository.findById(UUID.fromString(ALICE_DRAFT_COMMIT_ID)).isPresent());
    }

    @Test
    void deleteCommit_inLockedStatus_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/commits/" + BOB_LOCKED_COMMIT_ID)
                        .header("X-User-Id", BOB_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("DRAFT")));
    }

    @Test
    void deleteCommit_byWrongUser_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/commits/" + ALICE_DRAFT_COMMIT_ID)
                        .header("X-User-Id", BOB_ID))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------
    // GET /api/commits/week/{weekId}/owner/{ownerId}
    // -------------------------------------------------------

    @Test
    void getByOwnerAndWeek_returnsCorrectTasks() throws Exception {
        mockMvc.perform(get("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + BOB_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].ownerId", everyItem(is(BOB_ID))));
    }

    @Test
    void getByOwnerAndWeek_noTasks_returnsEmptyList() throws Exception {
        String nonExistentUserId = "00000000-0000-0000-0000-000000000099";

        mockMvc.perform(get("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + nonExistentUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------------------------------------------
    // POST lock - transitions DRAFT to LOCKED
    // -------------------------------------------------------

    @Test
    void lockWeek_transitionsDraftToLocked() throws Exception {
        // Alice has 3 DRAFT tasks in the current week
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/lock")
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk());

        // Verify all Alice's current-week tasks are now LOCKED
        commitRepository.findByOwnerIdAndWeekId(
                UUID.fromString(ALICE_ID), UUID.fromString(CURRENT_WEEK_ID)
        ).forEach(commit -> assertEquals(CommitStatus.LOCKED, commit.getStatus()));
    }

    @Test
    void lockWeek_byWrongUser_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/lock")
                        .header("X-User-Id", BOB_ID))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------
    // POST open-reconciliation - transitions LOCKED to RECONCILING
    // -------------------------------------------------------

    @Test
    void openReconciliation_transitionsLockedToReconciling() throws Exception {
        // First lock Alice's tasks
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/lock")
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk());

        // Then open reconciliation
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/open-reconciliation")
                        .header("X-User-Id", ALICE_ID))
                .andExpect(status().isOk());

        // Verify all tasks are now RECONCILING
        commitRepository.findByOwnerIdAndWeekId(
                UUID.fromString(ALICE_ID), UUID.fromString(CURRENT_WEEK_ID)
        ).forEach(commit -> assertEquals(CommitStatus.RECONCILING, commit.getStatus()));
    }

    // -------------------------------------------------------
    // POST /api/commits/reconcile - mark done (RECONCILED)
    // -------------------------------------------------------

    @Test
    void reconcile_markDone_setsStatusToReconciled() throws Exception {
        // Lock then open reconciliation for Alice
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/lock")
                        .header("X-User-Id", ALICE_ID));
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/open-reconciliation")
                        .header("X-User-Id", ALICE_ID));

        ReconcileRequest request = new ReconcileRequest(
                UUID.fromString(ALICE_DRAFT_COMMIT_ID),
                true,
                null
        );

        mockMvc.perform(post("/api/commits/reconcile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ALICE_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(true))
                .andExpect(jsonPath("$.commitId").value(ALICE_DRAFT_COMMIT_ID));

        // Verify commit status is now RECONCILED
        WeeklyCommit commit = commitRepository.findById(UUID.fromString(ALICE_DRAFT_COMMIT_ID)).orElseThrow();
        assertEquals(CommitStatus.RECONCILED, commit.getStatus());
    }

    // -------------------------------------------------------
    // POST /api/commits/reconcile - mark not done (CARRY_FORWARD + new task)
    // -------------------------------------------------------

    @Test
    void reconcile_markNotDone_setsCarryForwardAndCreatesNewTask() throws Exception {
        // Lock then open reconciliation for Bob
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + BOB_ID + "/lock")
                        .header("X-User-Id", BOB_ID));
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + BOB_ID + "/open-reconciliation")
                        .header("X-User-Id", BOB_ID));

        ReconcileRequest request = new ReconcileRequest(
                UUID.fromString(BOB_DRAFT_COMMIT_ID),
                false,
                "Blocked by third-party API issue"
        );

        mockMvc.perform(post("/api/commits/reconcile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", BOB_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.explanation").value("Blocked by third-party API issue"));

        // Verify original commit is CARRY_FORWARD
        WeeklyCommit original = commitRepository.findById(UUID.fromString(BOB_DRAFT_COMMIT_ID)).orElseThrow();
        assertEquals(CommitStatus.CARRY_FORWARD, original.getStatus());

        // Verify a new carried-forward task was created
        boolean hasCarriedOverTask = commitRepository.findAll().stream()
                .anyMatch(c -> UUID.fromString(BOB_DRAFT_COMMIT_ID).equals(c.getCarriedOverFrom()));
        assertTrue(hasCarriedOverTask, "A new task should be created with carriedOverFrom pointing to the original");
    }

    @Test
    void reconcile_byWrongUser_returnsForbidden() throws Exception {
        // Lock then open reconciliation for Alice
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/lock")
                        .header("X-User-Id", ALICE_ID));
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/open-reconciliation")
                        .header("X-User-Id", ALICE_ID));

        ReconcileRequest request = new ReconcileRequest(
                UUID.fromString(ALICE_DRAFT_COMMIT_ID), true, null
        );

        // Bob tries to reconcile Alice's task
        mockMvc.perform(post("/api/commits/reconcile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", BOB_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------
    // POST /api/commits/reconcile - reject without explanation when not done
    // -------------------------------------------------------

    @Test
    void reconcile_notDoneWithoutExplanation_returnsBadRequest() throws Exception {
        // Lock then open reconciliation for Alice
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/lock")
                        .header("X-User-Id", ALICE_ID));
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/open-reconciliation")
                        .header("X-User-Id", ALICE_ID));

        ReconcileRequest request = new ReconcileRequest(
                UUID.fromString(ALICE_DRAFT_COMMIT_ID),
                false,
                null
        );

        mockMvc.perform(post("/api/commits/reconcile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ALICE_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Explanation is required")));
    }

    @Test
    void reconcile_notDoneWithBlankExplanation_returnsBadRequest() throws Exception {
        // Lock then open reconciliation for Alice
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/lock")
                        .header("X-User-Id", ALICE_ID));
        mockMvc.perform(post("/api/commits/week/" + CURRENT_WEEK_ID + "/owner/" + ALICE_ID + "/open-reconciliation")
                        .header("X-User-Id", ALICE_ID));

        ReconcileRequest request = new ReconcileRequest(
                UUID.fromString(ALICE_DRAFT_COMMIT_ID),
                false,
                "   "
        );

        mockMvc.perform(post("/api/commits/reconcile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ALICE_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Explanation is required")));
    }

    // -------------------------------------------------------
    // Reconcile task not in RECONCILING status is rejected
    // -------------------------------------------------------

    @Test
    void reconcile_taskNotInReconcilingStatus_returnsBadRequest() throws Exception {
        // Try to reconcile a DRAFT task directly (without lock + open-reconciliation)
        ReconcileRequest request = new ReconcileRequest(
                UUID.fromString(ALICE_DRAFT_COMMIT_ID),
                true,
                null
        );

        mockMvc.perform(post("/api/commits/reconcile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", ALICE_ID)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("RECONCILING")));
    }
}
