package com.st6.weeklycommit.controller;

import com.st6.weeklycommit.dto.TeamMemberSummary;
import com.st6.weeklycommit.entity.User;
import com.st6.weeklycommit.entity.WeeklyCommit;
import com.st6.weeklycommit.entity.enums.UserRole;
import com.st6.weeklycommit.repository.UserRepository;
import com.st6.weeklycommit.service.DashboardService;
import com.st6.weeklycommit.service.WeeklyCommitService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final WeeklyCommitService commitService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService,
                               WeeklyCommitService commitService,
                               UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.commitService = commitService;
        this.userRepository = userRepository;
    }

    private UUID requireManager(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id header is required");
        }
        UUID callerId = UUID.fromString(userIdHeader);
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (caller.getRole() != UserRole.MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only managers can access the team dashboard");
        }
        return callerId;
    }

    @GetMapping("/team/{weekId}")
    public List<TeamMemberSummary> getTeamSummary(
            @PathVariable UUID weekId,
            @RequestParam UUID managerId,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID callerId = requireManager(userIdHeader);
        if (!callerId.equals(managerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own team");
        }
        return dashboardService.getTeamSummary(weekId, managerId);
    }

    @GetMapping("/team/{weekId}/member/{userId}")
    public List<WeeklyCommit> getMemberTasks(@PathVariable UUID weekId, @PathVariable UUID userId,
                                              @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        requireManager(userIdHeader);
        return commitService.getByOwnerAndWeek(userId, weekId);
    }
}
