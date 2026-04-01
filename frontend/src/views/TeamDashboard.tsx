import React, { useState, useEffect, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { api } from "../api/client";
import { TaskCard } from "../components/TaskCard";
import type { TeamMemberSummary, WeeklyCommit, GoalNode, Week } from "../types";
import { UserRole } from "../types";

const ALIGNMENT_ICONS: Record<string, string> = {
  GREEN: "\u2705",
  YELLOW: "\u26A0\uFE0F",
  RED: "\uD83D\uDD34",
  NA: "\u2014",
};

const ALIGNMENT_TEXT: Record<string, string> = {
  GREEN: "All aligned",
  YELLOW: "Partially aligned",
  RED: "Unaligned",
  NA: "N/A",
};

export function TeamDashboard() {
  const { currentUser } = useAuth();
  const { showError, showSuccess } = useToast();
  const [currentWeek, setCurrentWeek] = useState<Week | null>(null);
  const [teamSummary, setTeamSummary] = useState<TeamMemberSummary[]>([]);
  const [goals, setGoals] = useState<GoalNode[]>([]);
  const [selectedMember, setSelectedMember] = useState<TeamMemberSummary | null>(null);
  const [memberTasks, setMemberTasks] = useState<WeeklyCommit[]>([]);
  const [loading, setLoading] = useState(true);

  // Add employee state
  const [showAddForm, setShowAddForm] = useState(false);
  const [inviteName, setInviteName] = useState("");
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviting, setInviting] = useState(false);

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

      const summary = await api.getTeamSummary(week.id, currentUser.id);
      setTeamSummary(summary);
    } catch (err) {
      showError("Failed to load dashboard. Please try again.");
    }
    setLoading(false);
  }, [currentUser]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  async function handleDrillDown(member: TeamMemberSummary) {
    if (!currentWeek) return;
    try {
      setSelectedMember(member);
      const tasks = await api.getMemberTasks(currentWeek.id, member.userId);
      setMemberTasks(tasks);
    } catch (err) {
      showError("Failed to load member tasks.");
      setSelectedMember(null);
    }
  }

  async function handleInvite(e: React.FormEvent) {
    e.preventDefault();
    if (!currentUser) return;
    setInviting(true);
    try {
      await api.inviteEmployee(inviteName, inviteEmail, currentUser.id);
      showSuccess(`${inviteName} has been added to your team`);
      setInviteName("");
      setInviteEmail("");
      setShowAddForm(false);
      loadData();
    } catch (err: any) {
      showError(err.message || "Failed to add employee");
    }
    setInviting(false);
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
    const thisWeekTasks = memberTasks.filter((t) => !t.carriedOverFrom);
    const carriedOverTasks = memberTasks.filter((t) => t.carriedOverFrom);

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
          <>
            <h3 className="section-subtitle">This Week</h3>
            {thisWeekTasks.length === 0 ? (
              <div className="empty-state">No new tasks this week.</div>
            ) : (
              thisWeekTasks.map((task) => (
                <TaskCard key={task.id} task={task} goals={goals} showActions={false} />
              ))
            )}
            {carriedOverTasks.length > 0 && (
              <>
                <h3 className="section-subtitle">Carried Over</h3>
                {carriedOverTasks.map((task) => (
                  <TaskCard key={task.id} task={task} goals={goals} showActions={false} />
                ))}
              </>
            )}
          </>
        )}
      </div>
    );
  }

  return (
    <div>
      <div className="section-header">
        <h1 className="section-title">Team Dashboard</h1>
        <button className="btn btn-primary" onClick={() => setShowAddForm(!showAddForm)}>
          + Add Employee
        </button>
      </div>
      {currentWeek && (
        <p style={{ fontSize: 13, color: "#666", marginBottom: 16 }}>
          {currentWeek.startDate} &mdash; {currentWeek.endDate}
        </p>
      )}

      {showAddForm && (
        <form className="invite-form" onSubmit={handleInvite}>
          <input
            className="form-input"
            type="text"
            placeholder="Employee name"
            value={inviteName}
            onChange={(e) => setInviteName(e.target.value)}
            required
          />
          <input
            className="form-input"
            type="email"
            placeholder="Employee email"
            value={inviteEmail}
            onChange={(e) => setInviteEmail(e.target.value)}
            required
          />
          <button className="btn btn-success" type="submit" disabled={inviting}>
            {inviting ? "Adding..." : "Add"}
          </button>
          <button className="btn btn-outline" type="button" onClick={() => setShowAddForm(false)}>
            Cancel
          </button>
        </form>
      )}

      {teamSummary.length === 0 ? (
        <div className="empty-state">No employees on your team yet. Add one above.</div>
      ) : (
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
      )}
    </div>
  );
}
