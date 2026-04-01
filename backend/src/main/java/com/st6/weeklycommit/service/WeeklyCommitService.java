package com.st6.weeklycommit.service;

import com.st6.weeklycommit.dto.CreateCommitRequest;
import com.st6.weeklycommit.dto.ReconcileRequest;
import com.st6.weeklycommit.dto.UpdateCommitRequest;
import com.st6.weeklycommit.entity.ReconciliationEntry;
import com.st6.weeklycommit.entity.WeeklyCommit;
import com.st6.weeklycommit.entity.Week;
import com.st6.weeklycommit.entity.enums.CommitStatus;
import java.time.LocalDate;
import com.st6.weeklycommit.repository.ReconciliationEntryRepository;
import com.st6.weeklycommit.repository.WeeklyCommitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class WeeklyCommitService {

    private final WeeklyCommitRepository commitRepository;
    private final ReconciliationEntryRepository reconciliationRepository;
    private final WeekService weekService;

    public WeeklyCommitService(WeeklyCommitRepository commitRepository,
                               ReconciliationEntryRepository reconciliationRepository,
                               WeekService weekService) {
        this.commitRepository = commitRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.weekService = weekService;
    }

    public WeeklyCommit create(CreateCommitRequest request) {
        WeeklyCommit commit = new WeeklyCommit();
        commit.setId(UUID.randomUUID());
        commit.setName(request.name());
        commit.setPriority(request.priority());
        commit.setOwnerId(request.ownerId());
        commit.setWeekId(request.weekId());
        commit.setStatus(CommitStatus.DRAFT);

        if (request.goalId() != null) {
            commit.setGoalId(request.goalId());
            commit.setCustomGoal(false);
        } else if (request.customGoalText() != null && !request.customGoalText().isBlank()) {
            commit.setCustomGoalText(request.customGoalText());
            commit.setCustomGoal(true);
        }

        return commitRepository.save(commit);
    }

    public WeeklyCommit update(UUID id, UpdateCommitRequest request) {
        WeeklyCommit commit = getById(id);
        if (commit.getStatus() != CommitStatus.DRAFT) {
            throw new IllegalStateException("Can only edit tasks in DRAFT status");
        }

        if (request.name() != null) commit.setName(request.name());
        if (request.priority() != null) commit.setPriority(request.priority());

        if (request.goalId() != null) {
            commit.setGoalId(request.goalId());
            commit.setCustomGoalText(null);
            commit.setCustomGoal(false);
        } else if (request.customGoalText() != null) {
            commit.setCustomGoalText(request.customGoalText());
            commit.setGoalId(null);
            commit.setCustomGoal(true);
        }

        return commitRepository.save(commit);
    }

    public void delete(UUID id) {
        WeeklyCommit commit = getById(id);
        if (commit.getStatus() != CommitStatus.DRAFT) {
            throw new IllegalStateException("Can only delete tasks in DRAFT status");
        }
        commitRepository.delete(commit);
    }

    public WeeklyCommit getById(UUID id) {
        return commitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commit not found: " + id));
    }

    public List<WeeklyCommit> getByOwnerAndWeek(UUID ownerId, UUID weekId) {
        return commitRepository.findByOwnerIdAndWeekId(ownerId, weekId);
    }

    public List<WeeklyCommit> getThisWeekTasks(UUID ownerId, UUID weekId) {
        return commitRepository.findByOwnerIdAndWeekIdAndCarriedOverFromIsNull(ownerId, weekId);
    }

    public List<WeeklyCommit> getCarriedOverTasks(UUID ownerId, UUID weekId) {
        return commitRepository.findByOwnerIdAndWeekIdAndCarriedOverFromIsNotNull(ownerId, weekId);
    }

    public List<WeeklyCommit> getByWeek(UUID weekId) {
        return commitRepository.findByWeekId(weekId);
    }

    @Transactional
    public void lockWeek(UUID ownerId, UUID weekId) {
        List<WeeklyCommit> drafts = commitRepository.findByOwnerIdAndWeekIdAndStatus(ownerId, weekId, CommitStatus.DRAFT);
        for (WeeklyCommit commit : drafts) {
            commit.setStatus(CommitStatus.LOCKED);
        }
        commitRepository.saveAll(drafts);
    }

    @Transactional
    public void openReconciliation(UUID ownerId, UUID weekId) {
        List<WeeklyCommit> locked = commitRepository.findByOwnerIdAndWeekIdAndStatus(ownerId, weekId, CommitStatus.LOCKED);
        for (WeeklyCommit commit : locked) {
            commit.setStatus(CommitStatus.RECONCILING);
        }
        commitRepository.saveAll(locked);
    }

    @Transactional
    public ReconciliationEntry reconcileTask(ReconcileRequest request) {
        WeeklyCommit commit = getById(request.commitId());
        if (commit.getStatus() != CommitStatus.RECONCILING) {
            throw new IllegalStateException("Task must be in RECONCILING status to reconcile");
        }

        if (!request.done() && (request.explanation() == null || request.explanation().isBlank())) {
            throw new IllegalArgumentException("Explanation is required when marking a task as not done");
        }

        ReconciliationEntry entry = new ReconciliationEntry();
        entry.setId(UUID.randomUUID());
        entry.setCommitId(commit.getId());
        entry.setDone(request.done());
        entry.setExplanation(request.explanation());
        reconciliationRepository.save(entry);

        if (request.done()) {
            commit.setStatus(CommitStatus.RECONCILED);
        } else {
            commit.setStatus(CommitStatus.CARRY_FORWARD);
            carryForward(commit);
        }
        commitRepository.save(commit);

        return entry;
    }

    private void carryForward(WeeklyCommit original) {
        Week originalWeek = weekService.getById(original.getWeekId());
        Week nextWeek = weekService.getNextWeekAfter(originalWeek);
        WeeklyCommit carried = new WeeklyCommit();
        carried.setId(UUID.randomUUID());
        carried.setName(original.getName());
        carried.setPriority(original.getPriority());
        carried.setGoalId(original.getGoalId());
        carried.setCustomGoalText(original.getCustomGoalText());
        carried.setCustomGoal(original.isCustomGoal());
        carried.setOwnerId(original.getOwnerId());
        carried.setWeekId(nextWeek.getId());
        carried.setCarriedOverFrom(original.getId());
        carried.setStatus(CommitStatus.DRAFT);
        commitRepository.save(carried);
    }

    public boolean hasUnreconciledPriorWeek(UUID ownerId) {
        Week currentWeek = weekService.getCurrentWeek();
        LocalDate priorMonday = currentWeek.getStartDate().minusDays(7);
        Week priorWeek;
        try {
            priorWeek = weekService.getOrCreateWeekFor(priorMonday);
        } catch (Exception e) {
            return false;
        }
        List<WeeklyCommit> priorTasks = commitRepository.findByOwnerIdAndWeekId(ownerId, priorWeek.getId());
        return priorTasks.stream().anyMatch(t ->
                t.getStatus() == CommitStatus.LOCKED || t.getStatus() == CommitStatus.RECONCILING);
    }
}
