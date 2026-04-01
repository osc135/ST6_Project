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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/commits")
public class CommitController {

    private final WeeklyCommitService commitService;

    public CommitController(WeeklyCommitService commitService) {
        this.commitService = commitService;
    }

    private UUID requireUserId(String header) {
        if (header == null || header.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id header is required");
        }
        return UUID.fromString(header);
    }

    private void requireOwnership(UUID callerId, UUID ownerId) {
        if (!callerId.equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own tasks");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WeeklyCommit create(@Valid @RequestBody CreateCommitRequest request,
                               @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID callerId = requireUserId(userIdHeader);
        requireOwnership(callerId, request.ownerId());
        return commitService.create(request);
    }

    @PutMapping("/{id}")
    public WeeklyCommit update(@PathVariable UUID id, @RequestBody UpdateCommitRequest request,
                               @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID callerId = requireUserId(userIdHeader);
        WeeklyCommit existing = commitService.getById(id);
        requireOwnership(callerId, existing.getOwnerId());
        return commitService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
                       @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID callerId = requireUserId(userIdHeader);
        WeeklyCommit existing = commitService.getById(id);
        requireOwnership(callerId, existing.getOwnerId());
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
    public void lockWeek(@PathVariable UUID ownerId, @PathVariable UUID weekId,
                         @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID callerId = requireUserId(userIdHeader);
        requireOwnership(callerId, ownerId);
        commitService.lockWeek(ownerId, weekId);
    }

    @PostMapping("/week/{weekId}/owner/{ownerId}/open-reconciliation")
    public void openReconciliation(@PathVariable UUID ownerId, @PathVariable UUID weekId,
                                   @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID callerId = requireUserId(userIdHeader);
        requireOwnership(callerId, ownerId);
        commitService.openReconciliation(ownerId, weekId);
    }

    @PostMapping("/reconcile")
    public ReconciliationEntry reconcile(@Valid @RequestBody ReconcileRequest request,
                                         @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID callerId = requireUserId(userIdHeader);
        WeeklyCommit existing = commitService.getById(request.commitId());
        requireOwnership(callerId, existing.getOwnerId());
        return commitService.reconcileTask(request);
    }

    @GetMapping("/owner/{ownerId}/has-unreconciled")
    public boolean hasUnreconciled(@PathVariable UUID ownerId) {
        return commitService.hasUnreconciledPriorWeek(ownerId);
    }
}
