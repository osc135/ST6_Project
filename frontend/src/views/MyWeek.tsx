import React, { useState, useEffect, useCallback } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { api } from "../api/client";
import { TaskCard } from "../components/TaskCard";
import { TaskFormModal } from "../components/TaskFormModal";
import type { WeeklyCommit, GoalNode, Week } from "../types";
import { CommitStatus } from "../types";

function shiftDate(dateStr: string, days: number): string {
  const d = new Date(dateStr + "T00:00:00");
  d.setDate(d.getDate() + days);
  return d.toISOString().split("T")[0];
}

export function MyWeek() {
  const { currentUser } = useAuth();
  const { showError, showSuccess } = useToast();
  const [viewedWeek, setViewedWeek] = useState<Week | null>(null);
  const [currentWeekId, setCurrentWeekId] = useState<string | null>(null);
  const [thisWeekTasks, setThisWeekTasks] = useState<WeeklyCommit[]>([]);
  const [carriedOverTasks, setCarriedOverTasks] = useState<WeeklyCommit[]>([]);
  const [goals, setGoals] = useState<GoalNode[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [editingTask, setEditingTask] = useState<WeeklyCommit | null>(null);
  const [hasUnreconciled, setHasUnreconciled] = useState(false);
  const [nudgeDismissed, setNudgeDismissed] = useState(false);
  const [loading, setLoading] = useState(true);

  const isCurrentWeek = viewedWeek != null && viewedWeek.id === currentWeekId;

  const loadWeekData = useCallback(async (week: Week) => {
    if (!currentUser) return;
    try {
      const [thisWeek, carried] = await Promise.all([
        api.getThisWeekTasks(currentUser.id, week.id),
        api.getCarriedOverTasks(currentUser.id, week.id),
      ]);
      setThisWeekTasks(thisWeek);
      setCarriedOverTasks(carried);
    } catch (err) {
      showError("Failed to load tasks.");
    }
  }, [currentUser, showError]);

  const loadInitial = useCallback(async () => {
    if (!currentUser) return;
    setLoading(true);
    try {
      const [week, allGoals, unreconciled] = await Promise.all([
        api.getCurrentWeek(),
        api.getHierarchy(),
        api.hasUnreconciled(currentUser.id),
      ]);
      setViewedWeek(week);
      setCurrentWeekId(week.id);
      setGoals(allGoals);
      setHasUnreconciled(unreconciled);
      await loadWeekData(week);
    } catch (err) {
      showError("Failed to load weekly data. Please try again.");
    }
    setLoading(false);
  }, [currentUser, loadWeekData, showError]);

  useEffect(() => {
    loadInitial();
    setNudgeDismissed(false);
  }, [loadInitial]);

  async function navigateWeek(direction: "prev" | "next") {
    if (!viewedWeek) return;
    const targetDate = direction === "prev"
      ? shiftDate(viewedWeek.startDate, -7)
      : shiftDate(viewedWeek.endDate, 3);
    setLoading(true);
    try {
      const week = await api.getWeekByDate(targetDate);
      setViewedWeek(week);
      await loadWeekData(week);
    } catch (err) {
      showError("Failed to load week.");
    }
    setLoading(false);
  }

  function goToCurrentWeek() {
    loadInitial();
  }

  async function refreshCurrentWeek() {
    if (!viewedWeek) return;
    await loadWeekData(viewedWeek);
  }

  async function handleAddTask(data: {
    name: string;
    priority: string;
    goalId?: string;
    customGoalText?: string;
  }) {
    if (!currentUser || !viewedWeek) return;
    try {
      await api.createCommit({
        name: data.name,
        priority: data.priority as WeeklyCommit["priority"],
        goalId: data.goalId,
        customGoalText: data.customGoalText,
        ownerId: currentUser.id,
        weekId: viewedWeek.id,
      });
      setShowModal(false);
      showSuccess("Task added.");
      refreshCurrentWeek();
    } catch (err) {
      showError(err instanceof Error ? err.message : "Failed to add task.");
    }
  }

  async function handleEditTask(data: {
    name: string;
    priority: string;
    goalId?: string;
    customGoalText?: string;
  }) {
    if (!editingTask) return;
    try {
      await api.updateCommit(editingTask.id, {
        name: data.name,
        priority: data.priority as WeeklyCommit["priority"],
        goalId: data.goalId,
        customGoalText: data.customGoalText,
      });
      setEditingTask(null);
      showSuccess("Task updated.");
      refreshCurrentWeek();
    } catch (err) {
      showError(err instanceof Error ? err.message : "Failed to update task.");
    }
  }

  async function handleDelete(taskId: string) {
    try {
      await api.deleteCommit(taskId);
      showSuccess("Task deleted.");
      refreshCurrentWeek();
    } catch (err) {
      showError(err instanceof Error ? err.message : "Failed to delete task.");
    }
  }

  async function handleLockWeek() {
    if (!currentUser || !viewedWeek) return;
    try {
      await api.lockWeek(currentUser.id, viewedWeek.id);
      showSuccess("Week locked.");
      refreshCurrentWeek();
    } catch (err) {
      showError(err instanceof Error ? err.message : "Failed to lock week.");
    }
  }

  const allTasks = [...thisWeekTasks, ...carriedOverTasks];
  const hasDraftTasks = allTasks.some((t) => t.status === CommitStatus.DRAFT);
  const allLocked = allTasks.length > 0 && allTasks.every((t) => t.status !== CommitStatus.DRAFT);

  if (!currentUser) return <div className="loading">Select a user to continue</div>;
  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div>
      {isCurrentWeek && hasUnreconciled && !nudgeDismissed && (
        <div className="nudge-banner">
          <span>
            You haven't reconciled last week yet.{" "}
            <Link to="/reconcile">Do it here &rarr;</Link>
          </span>
          <button onClick={() => setNudgeDismissed(true)}>&times;</button>
        </div>
      )}

      <div className="section-header">
        <h1 className="section-title">My Week</h1>
        <div style={{ display: "flex", gap: 8 }}>
          {isCurrentWeek && hasDraftTasks && (
            <button className="btn btn-outline" onClick={handleLockWeek}>
              Lock Week
            </button>
          )}
          {isCurrentWeek && !allLocked && (
            <button className="btn btn-primary" onClick={() => setShowModal(true)}>
              + Add Task
            </button>
          )}
        </div>
      </div>

      {viewedWeek && (
        <div className="week-nav">
          <button className="week-nav-btn" onClick={() => navigateWeek("prev")}>
            &larr; Previous
          </button>
          <div className="week-nav-label">
            <span>{viewedWeek.startDate} &mdash; {viewedWeek.endDate}</span>
            {!isCurrentWeek && (
              <button className="week-nav-today" onClick={goToCurrentWeek}>
                Today
              </button>
            )}
          </div>
          <button className="week-nav-btn" onClick={() => navigateWeek("next")}>
            Next &rarr;
          </button>
        </div>
      )}

      {!isCurrentWeek && (
        <div className="week-nav-past-banner">
          You are viewing a different week. Tasks are read-only.
        </div>
      )}

      {isCurrentWeek && allLocked && allTasks.length > 0 && (
        <div style={{ background: "#dbeafe", border: "1px solid #93c5fd", borderRadius: 8, padding: "10px 14px", marginBottom: 16, fontSize: 14, color: "#1e40af" }}>
          Your week is locked. Head to Reconcile at the end of the week to review your tasks.
        </div>
      )}

      <h3 className="section-subtitle">This Week</h3>
      {thisWeekTasks.length === 0 ? (
        <div className="empty-state">
          {isCurrentWeek ? "No tasks yet. Add one to get started." : "No tasks this week."}
        </div>
      ) : (
        thisWeekTasks.map((task) => (
          <TaskCard
            key={task.id}
            task={task}
            goals={goals}
            onEdit={(t) => setEditingTask(t)}
            onDelete={handleDelete}
            showActions={isCurrentWeek}
          />
        ))
      )}

      {carriedOverTasks.length > 0 && (
        <>
          <h3 className="section-subtitle">Carried Over</h3>
          {carriedOverTasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              goals={goals}
              onEdit={(t) => setEditingTask(t)}
              onDelete={handleDelete}
              showActions={isCurrentWeek}
            />
          ))}
        </>
      )}

      {showModal && (
        <TaskFormModal
          goals={goals}
          onSubmit={handleAddTask}
          onClose={() => setShowModal(false)}
        />
      )}

      {editingTask && (
        <TaskFormModal
          goals={goals}
          existingTask={editingTask}
          onSubmit={handleEditTask}
          onClose={() => setEditingTask(null)}
        />
      )}
    </div>
  );
}
