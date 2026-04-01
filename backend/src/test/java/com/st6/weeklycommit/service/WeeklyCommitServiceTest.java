package com.st6.weeklycommit.service;

import com.st6.weeklycommit.dto.CreateCommitRequest;
import com.st6.weeklycommit.dto.ReconcileRequest;
import com.st6.weeklycommit.dto.UpdateCommitRequest;
import com.st6.weeklycommit.entity.ReconciliationEntry;
import com.st6.weeklycommit.entity.Week;
import com.st6.weeklycommit.entity.WeeklyCommit;
import com.st6.weeklycommit.entity.enums.CommitStatus;
import com.st6.weeklycommit.entity.enums.Priority;
import com.st6.weeklycommit.repository.ReconciliationEntryRepository;
import com.st6.weeklycommit.repository.WeeklyCommitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyCommitServiceTest {

    @Mock
    private WeeklyCommitRepository commitRepository;

    @Mock
    private ReconciliationEntryRepository reconciliationRepository;

    @Mock
    private WeekService weekService;

    @InjectMocks
    private WeeklyCommitService service;

    private UUID ownerId;
    private UUID weekId;
    private UUID goalId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        weekId = UUID.randomUUID();
        goalId = UUID.randomUUID();
    }

    @Test
    void create_withOrgGoal_setsGoalIdAndCustomGoalFalse() {
        CreateCommitRequest request = new CreateCommitRequest(
                "Wire up Stripe", Priority.KING, goalId, null, ownerId, weekId
        );
        when(commitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        WeeklyCommit result = service.create(request);

        assertEquals("Wire up Stripe", result.getName());
        assertEquals(Priority.KING, result.getPriority());
        assertEquals(goalId, result.getGoalId());
        assertFalse(result.isCustomGoal());
        assertNull(result.getCustomGoalText());
        assertEquals(CommitStatus.DRAFT, result.getStatus());
    }

    @Test
    void create_withCustomGoal_setsCustomGoalTrue() {
        CreateCommitRequest request = new CreateCommitRequest(
                "Fix CI", Priority.PAWN, null, "Tech debt", ownerId, weekId
        );
        when(commitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        WeeklyCommit result = service.create(request);

        assertNull(result.getGoalId());
        assertTrue(result.isCustomGoal());
        assertEquals("Tech debt", result.getCustomGoalText());
    }

    @Test
    void update_inDraftStatus_updatesFields() {
        WeeklyCommit existing = makeDraftCommit();
        when(commitRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(commitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateCommitRequest request = new UpdateCommitRequest("Updated name", Priority.QUEEN, null, null);
        WeeklyCommit result = service.update(existing.getId(), request);

        assertEquals("Updated name", result.getName());
        assertEquals(Priority.QUEEN, result.getPriority());
    }

    @Test
    void update_notInDraftStatus_throwsException() {
        WeeklyCommit locked = makeDraftCommit();
        locked.setStatus(CommitStatus.LOCKED);
        when(commitRepository.findById(locked.getId())).thenReturn(Optional.of(locked));

        assertThrows(IllegalStateException.class, () ->
                service.update(locked.getId(), new UpdateCommitRequest("New", null, null, null))
        );
    }

    @Test
    void delete_inDraftStatus_succeeds() {
        WeeklyCommit draft = makeDraftCommit();
        when(commitRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        service.delete(draft.getId());

        verify(commitRepository).delete(draft);
    }

    @Test
    void delete_notInDraftStatus_throwsException() {
        WeeklyCommit locked = makeDraftCommit();
        locked.setStatus(CommitStatus.LOCKED);
        when(commitRepository.findById(locked.getId())).thenReturn(Optional.of(locked));

        assertThrows(IllegalStateException.class, () -> service.delete(locked.getId()));
    }

    @Test
    void lockWeek_transitionsDraftsToLocked() {
        WeeklyCommit draft1 = makeDraftCommit();
        WeeklyCommit draft2 = makeDraftCommit();
        when(commitRepository.findByOwnerIdAndWeekIdAndStatus(ownerId, weekId, CommitStatus.DRAFT))
                .thenReturn(List.of(draft1, draft2));

        service.lockWeek(ownerId, weekId);

        assertEquals(CommitStatus.LOCKED, draft1.getStatus());
        assertEquals(CommitStatus.LOCKED, draft2.getStatus());
        verify(commitRepository).saveAll(List.of(draft1, draft2));
    }

    @Test
    void openReconciliation_transitionsLockedToReconciling() {
        WeeklyCommit locked1 = makeDraftCommit();
        locked1.setStatus(CommitStatus.LOCKED);
        when(commitRepository.findByOwnerIdAndWeekIdAndStatus(ownerId, weekId, CommitStatus.LOCKED))
                .thenReturn(List.of(locked1));

        service.openReconciliation(ownerId, weekId);

        assertEquals(CommitStatus.RECONCILING, locked1.getStatus());
        verify(commitRepository).saveAll(List.of(locked1));
    }

    @Test
    void reconcileTask_markedDone_transitionsToReconciled() {
        WeeklyCommit task = makeDraftCommit();
        task.setStatus(CommitStatus.RECONCILING);
        when(commitRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(reconciliationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(commitRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ReconcileRequest request = new ReconcileRequest(task.getId(), true, null);
        ReconciliationEntry entry = service.reconcileTask(request);

        assertTrue(entry.isDone());
        assertEquals(CommitStatus.RECONCILED, task.getStatus());
    }

    @Test
    void reconcileTask_markedNotDone_transitionsToCarryForwardAndCreatesNewTask() {
        WeeklyCommit task = makeDraftCommit();
        task.setStatus(CommitStatus.RECONCILING);
        Week taskWeek = new Week(weekId, LocalDate.now(), LocalDate.now().plusDays(4));
        Week nextWeek = new Week(UUID.randomUUID(), LocalDate.now().plusDays(7), LocalDate.now().plusDays(11));

        when(commitRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(reconciliationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(commitRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(weekService.getById(weekId)).thenReturn(taskWeek);
        when(weekService.getNextWeekAfter(taskWeek)).thenReturn(nextWeek);

        ReconcileRequest request = new ReconcileRequest(task.getId(), false, "Blocked by dependency");
        ReconciliationEntry entry = service.reconcileTask(request);

        assertFalse(entry.isDone());
        assertEquals("Blocked by dependency", entry.getExplanation());
        assertEquals(CommitStatus.CARRY_FORWARD, task.getStatus());

        // Verify a new carried-forward task was saved
        ArgumentCaptor<WeeklyCommit> captor = ArgumentCaptor.forClass(WeeklyCommit.class);
        verify(commitRepository, times(2)).save(captor.capture());
        WeeklyCommit carriedTask = captor.getAllValues().stream()
                .filter(c -> c.getCarriedOverFrom() != null)
                .findFirst()
                .orElseThrow();
        assertEquals(task.getId(), carriedTask.getCarriedOverFrom());
        assertEquals(nextWeek.getId(), carriedTask.getWeekId());
        assertEquals(CommitStatus.DRAFT, carriedTask.getStatus());
        assertEquals(task.getName(), carriedTask.getName());
    }

    @Test
    void reconcileTask_notDoneWithoutExplanation_throwsException() {
        WeeklyCommit task = makeDraftCommit();
        task.setStatus(CommitStatus.RECONCILING);
        when(commitRepository.findById(task.getId())).thenReturn(Optional.of(task));

        ReconcileRequest request = new ReconcileRequest(task.getId(), false, null);
        assertThrows(IllegalArgumentException.class, () -> service.reconcileTask(request));
    }

    @Test
    void reconcileTask_notInReconcilingStatus_throwsException() {
        WeeklyCommit task = makeDraftCommit();
        task.setStatus(CommitStatus.LOCKED);
        when(commitRepository.findById(task.getId())).thenReturn(Optional.of(task));

        ReconcileRequest request = new ReconcileRequest(task.getId(), true, null);
        assertThrows(IllegalStateException.class, () -> service.reconcileTask(request));
    }

    private WeeklyCommit makeDraftCommit() {
        WeeklyCommit commit = new WeeklyCommit();
        commit.setId(UUID.randomUUID());
        commit.setName("Test task");
        commit.setPriority(Priority.KNIGHT);
        commit.setOwnerId(ownerId);
        commit.setWeekId(weekId);
        commit.setStatus(CommitStatus.DRAFT);
        return commit;
    }
}
