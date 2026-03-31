package com.st6.weeklycommit.entity;

import com.st6.weeklycommit.entity.enums.CommitStatus;
import com.st6.weeklycommit.entity.enums.Priority;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "weekly_commits")
public class WeeklyCommit {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "custom_goal_text")
    private String customGoalText;

    @Column(name = "is_custom_goal", nullable = false)
    private boolean isCustomGoal;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "week_id", nullable = false)
    private UUID weekId;

    @Column(name = "carried_over_from")
    private UUID carriedOverFrom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommitStatus status = CommitStatus.DRAFT;

    public WeeklyCommit() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public UUID getGoalId() { return goalId; }
    public void setGoalId(UUID goalId) { this.goalId = goalId; }

    public String getCustomGoalText() { return customGoalText; }
    public void setCustomGoalText(String customGoalText) { this.customGoalText = customGoalText; }

    public boolean isCustomGoal() { return isCustomGoal; }
    public void setCustomGoal(boolean customGoal) { isCustomGoal = customGoal; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public UUID getWeekId() { return weekId; }
    public void setWeekId(UUID weekId) { this.weekId = weekId; }

    public UUID getCarriedOverFrom() { return carriedOverFrom; }
    public void setCarriedOverFrom(UUID carriedOverFrom) { this.carriedOverFrom = carriedOverFrom; }

    public CommitStatus getStatus() { return status; }
    public void setStatus(CommitStatus status) { this.status = status; }
}
