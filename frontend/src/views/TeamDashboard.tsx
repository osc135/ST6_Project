import React, { useState, useEffect, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/client";
import { TaskCard } from "../components/TaskCard";
import type { TeamMemberSummary, WeeklyCommit, GoalNode, Week } from "../types";
import { UserRole } from "../types";

const ALIGNMENT_ICONS: Record<string, string> = {
  GREEN: "\u2705",
  YELLOW: "\u26A0\uFE0F",
  RED: "\uD83D\uDD34",
};

const ALIGNMENT_TEXT: Record<string, string> = {
  GREEN: "All aligned",
  YELLOW: "Partially aligned",
  RED: "Unaligned",
};

export function TeamDashboard() {
  const { currentUser } = useAuth();
  const [currentWeek, setCurrentWeek] = useState<Week | null>(null);
  const [teamSummary, setTeamSummary] = useState<TeamMemberSummary[]>([]);
  const [goals, setGoals] = useState<GoalNode[]>([]);
  const [selectedMember, setSelectedMember] = useState<TeamMemberSummary | null>(null);
  const [memberTasks, setMemberTasks] = useState<WeeklyCommit[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    if (!currentUser) return;
    setLoading(true);
    try {
      const [week, allGoals] = await Promise.all([
        api.getCurrentWeek(),
        api.getHierarchy(),
      ]);
      setCurrentWeek(week);
      setGoals(allGoals);

      const summary = await api.getTeamSummary(week.id);
      setTeamSummary(summary);
    } catch (err) {
      console.error("Failed to load dashboard:", err);
    }
    setLoading(false);
  }, [currentUser]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  async function handleDrillDown(member: TeamMemberSummary) {
    if (!currentWeek) return;
    setSelectedMember(member);
    const tasks = await api.getMemberTasks(currentWeek.id, member.userId);
    setMemberTasks(tasks);
  }

  if (!currentUser) return <div className="loading">Select a user to continue</div>;

  if (currentUser.role !== UserRole.MANAGER) {
    return (
      <div className="empty-state">
        <p>The Team Dashboard is only available to managers.</p>
      </div>
    );
  }

  if (loading) return <div className="loading">Loading...</div>;

  if (selectedMember) {
    return (
      <div>
        <div className="drill-down-header">
          <button className="back-btn" onClick={() => setSelectedMember(null)}>
            &larr; Back to Team
          </button>
          <h1 className="section-title">{selectedMember.name}'s Tasks</h1>
        </div>
        {memberTasks.length === 0 ? (
          <div className="empty-state">No tasks this week.</div>
        ) : (
          memberTasks.map((task) => (
            <TaskCard key={task.id} task={task} goals={goals} showActions={false} />
          ))
        )}
      </div>
    );
  }

  return (
    <div>
      <h1 className="section-title" style={{ marginBottom: 16 }}>Team Dashboard</h1>
      {currentWeek && (
        <p style={{ fontSize: 13, color: "#666", marginBottom: 16 }}>
          {currentWeek.startDate} &mdash; {currentWeek.endDate}
        </p>
      )}
      <table className="team-table">
        <thead>
          <tr>
            <th>Team Member</th>
            <th>Tasks This Week</th>
            <th>Alignment Status</th>
          </tr>
        </thead>
        <tbody>
          {teamSummary.map((member) => (
            <tr key={member.userId} onClick={() => handleDrillDown(member)}>
              <td>{member.name}</td>
              <td>{member.taskCount}</td>
              <td className={`alignment-${member.alignmentStatus}`}>
                {ALIGNMENT_ICONS[member.alignmentStatus]}{" "}
                {ALIGNMENT_TEXT[member.alignmentStatus]}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
