package com.st6.weeklycommit.controller;

import com.st6.weeklycommit.dto.TeamMemberSummary;
import com.st6.weeklycommit.entity.WeeklyCommit;
import com.st6.weeklycommit.service.DashboardService;
import com.st6.weeklycommit.service.WeeklyCommitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final WeeklyCommitService commitService;

    public DashboardController(DashboardService dashboardService, WeeklyCommitService commitService) {
        this.dashboardService = dashboardService;
        this.commitService = commitService;
    }

    @GetMapping("/team/{weekId}")
    public List<TeamMemberSummary> getTeamSummary(@PathVariable UUID weekId) {
        return dashboardService.getTeamSummary(weekId);
    }

    @GetMapping("/team/{weekId}/member/{userId}")
    public List<WeeklyCommit> getMemberTasks(@PathVariable UUID weekId, @PathVariable UUID userId) {
        return commitService.getByOwnerAndWeek(userId, weekId);
    }
}
