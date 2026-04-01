package com.st6.weeklycommit.service;

import com.st6.weeklycommit.dto.TeamMemberSummary;
import com.st6.weeklycommit.entity.User;
import com.st6.weeklycommit.entity.WeeklyCommit;
import com.st6.weeklycommit.entity.enums.CommitStatus;
import com.st6.weeklycommit.entity.enums.Priority;
import com.st6.weeklycommit.entity.enums.UserRole;
import com.st6.weeklycommit.repository.UserRepository;
import com.st6.weeklycommit.repository.WeeklyCommitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WeeklyCommitRepository commitRepository;

    @InjectMocks
    private DashboardService service;

    private final UUID managerId = UUID.randomUUID();

    @Test
    void getTeamSummary_allAligned_returnsGreen() {
        UUID weekId = UUID.randomUUID();
        User user = new User(UUID.randomUUID(), "Carol", "carol@st6.com", "", UserRole.EMPLOYEE);
        WeeklyCommit task = makeTask(user.getId(), weekId, UUID.randomUUID(), null, false);

        when(userRepository.findByManagerId(managerId)).thenReturn(List.of(user));
        when(commitRepository.findByOwnerIdAndWeekId(user.getId(), weekId)).thenReturn(List.of(task));

        List<TeamMemberSummary> result = service.getTeamSummary(weekId, managerId);

        assertEquals(1, result.size());
        assertEquals("GREEN", result.get(0).alignmentStatus());
        assertEquals(1, result.get(0).taskCount());
    }

    @Test
    void getTeamSummary_customGoal_returnsYellow() {
        UUID weekId = UUID.randomUUID();
        User user = new User(UUID.randomUUID(), "Bob", "bob@st6.com", "", UserRole.EMPLOYEE);
        WeeklyCommit aligned = makeTask(user.getId(), weekId, UUID.randomUUID(), null, false);
        WeeklyCommit custom = makeTask(user.getId(), weekId, null, "Tech debt", true);

        when(userRepository.findByManagerId(managerId)).thenReturn(List.of(user));
        when(commitRepository.findByOwnerIdAndWeekId(user.getId(), weekId))
                .thenReturn(List.of(aligned, custom));

        List<TeamMemberSummary> result = service.getTeamSummary(weekId, managerId);

        assertEquals("YELLOW", result.get(0).alignmentStatus());
    }

    @Test
    void getTeamSummary_noGoalAtAll_returnsRed() {
        UUID weekId = UUID.randomUUID();
        User user = new User(UUID.randomUUID(), "Dan", "dan@st6.com", "", UserRole.EMPLOYEE);
        WeeklyCommit noGoal = makeTask(user.getId(), weekId, null, null, false);

        when(userRepository.findByManagerId(managerId)).thenReturn(List.of(user));
        when(commitRepository.findByOwnerIdAndWeekId(user.getId(), weekId)).thenReturn(List.of(noGoal));

        List<TeamMemberSummary> result = service.getTeamSummary(weekId, managerId);

        assertEquals("RED", result.get(0).alignmentStatus());
    }

    @Test
    void getTeamSummary_noTasks_returnsNA() {
        UUID weekId = UUID.randomUUID();
        User user = new User(UUID.randomUUID(), "New Employee", "new@st6.com", "", UserRole.EMPLOYEE);

        when(userRepository.findByManagerId(managerId)).thenReturn(List.of(user));
        when(commitRepository.findByOwnerIdAndWeekId(user.getId(), weekId)).thenReturn(List.of());

        List<TeamMemberSummary> result = service.getTeamSummary(weekId, managerId);

        assertEquals("NA", result.get(0).alignmentStatus());
        assertEquals(0, result.get(0).taskCount());
    }

    private WeeklyCommit makeTask(UUID ownerId, UUID weekId, UUID goalId, String customGoalText, boolean isCustom) {
        WeeklyCommit task = new WeeklyCommit();
        task.setId(UUID.randomUUID());
        task.setName("Test task");
        task.setPriority(Priority.KNIGHT);
        task.setOwnerId(ownerId);
        task.setWeekId(weekId);
        task.setGoalId(goalId);
        task.setCustomGoalText(customGoalText);
        task.setCustomGoal(isCustom);
        task.setStatus(CommitStatus.DRAFT);
        return task;
    }
}
