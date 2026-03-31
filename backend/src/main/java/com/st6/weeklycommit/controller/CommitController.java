package com.st6.weeklycommit.controller;

import com.st6.weeklycommit.dto.CreateCommitRequest;
import com.st6.weeklycommit.dto.ReconcileRequest;
import com.st6.weeklycommit.dto.UpdateCommitRequest;
import com.st6.weeklycommit.entity.ReconciliationEntry;
import com.st6.weeklycommit.entity.WeeklyCommit;
import com.st6.weeklycommit.service.WeeklyCommitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/commits")
public class CommitController {

    private final WeeklyCommitService commitService;

    public CommitController(WeeklyCommitService commitService) {
        this.commitService = commitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WeeklyCommit create(@Valid @RequestBody CreateCommitRequest request) {
        return commitService.create(request);
    }

    @PutMapping("/{id}")
    public WeeklyCommit update(@PathVariable UUID id, @RequestBody UpdateCommitRequest request) {
        return commitService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        commitService.delete(id);
    }

    @GetMapping("/{id}")
    public WeeklyCommit getById(@PathVariable UUID id) {
        return commitService.getById(id);
    }

    @GetMapping("/week/{weekId}/owner/{ownerId}")
    public List<WeeklyCommit> getByOwnerAndWeek(@PathVariable UUID ownerId, @PathVariable UUID weekId) {
        return commitService.getByOwnerAndWeek(ownerId, weekId);
    }

    @GetMapping("/week/{weekId}/owner/{ownerId}/this-week")
    public List<WeeklyCommit> getThisWeekTasks(@PathVariable UUID ownerId, @PathVariable UUID weekId) {
        return commitService.getThisWeekTasks(ownerId, weekId);
    }

    @GetMapping("/week/{weekId}/owner/{ownerId}/carried-over")
    public List<WeeklyCommit> getCarriedOver(@PathVariable UUID ownerId, @PathVariable UUID weekId) {
        return commitService.getCarriedOverTasks(ownerId, weekId);
    }

    @PostMapping("/week/{weekId}/owner/{ownerId}/lock")
    public void lockWeek(@PathVariable UUID ownerId, @PathVariable UUID weekId) {
        commitService.lockWeek(ownerId, weekId);
    }

    @PostMapping("/week/{weekId}/owner/{ownerId}/open-reconciliation")
    public void openReconciliation(@PathVariable UUID ownerId, @PathVariable UUID weekId) {
        commitService.openReconciliation(ownerId, weekId);
    }

    @PostMapping("/reconcile")
    public ReconciliationEntry reconcile(@Valid @RequestBody ReconcileRequest request) {
        return commitService.reconcileTask(request);
    }

    @GetMapping("/owner/{ownerId}/has-unreconciled")
    public boolean hasUnreconciled(@PathVariable UUID ownerId) {
        return commitService.hasUnreconciledPriorWeek(ownerId);
    }
}
