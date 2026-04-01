package com.st6.weeklycommit.service;

import com.st6.weeklycommit.dto.TeamMemberSummary;
import com.st6.weeklycommit.entity.User;
import com.st6.weeklycommit.entity.WeeklyCommit;
import com.st6.weeklycommit.repository.UserRepository;
import com.st6.weeklycommit.repository.WeeklyCommitRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final WeeklyCommitRepository commitRepository;

    public DashboardService(UserRepository userRepository, WeeklyCommitRepository commitRepository) {
        this.userRepository = userRepository;
        this.commitRepository = commitRepository;
    }

    public List<TeamMemberSummary> getTeamSummary(UUID weekId, UUID managerId) {
        List<User> employees = userRepository.findByManagerId(managerId);
        List<TeamMemberSummary> summaries = new ArrayList<>();

        for (User employee : employees) {
            List<WeeklyCommit> tasks = commitRepository.findByOwnerIdAndWeekId(employee.getId(), weekId);
            String alignment = calculateAlignment(tasks);
            summaries.add(new TeamMemberSummary(
                    employee.getId(),
                    employee.getName(),
                    tasks.size(),
                    alignment
            ));
        }
        return summaries;
    }

    private String calculateAlignment(List<WeeklyCommit> tasks) {
        if (tasks.isEmpty()) return "NA";

        boolean hasUnaligned = false;
        boolean hasCustom = false;

        for (WeeklyCommit task : tasks) {
            if (task.getGoalId() == null && (task.getCustomGoalText() == null || task.getCustomGoalText().isBlank())) {
                hasUnaligned = true;
            } else if (task.isCustomGoal()) {
                hasCustom = true;
            }
        }

        if (hasUnaligned) return "RED";
        if (hasCustom) return "YELLOW";
        return "GREEN";
    }
}
