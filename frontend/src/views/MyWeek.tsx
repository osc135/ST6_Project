import React, { useState, useEffect, useCallback } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/client";
import { TaskCard } from "../components/TaskCard";
import { TaskFormModal } from "../components/TaskFormModal";
import type { WeeklyCommit, GoalNode, Week } from "../types";

export function MyWeek() {
  const { currentUser } = useAuth();
  const [currentWeek, setCurrentWeek] = useState<Week | null>(null);
  const [thisWeekTasks, setThisWeekTasks] = useState<WeeklyCommit[]>([]);
  const [carriedOverTasks, setCarriedOverTasks] = useState<WeeklyCommit[]>([]);
  const [goals, setGoals] = useState<GoalNode[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [editingTask, setEditingTask] = useState<WeeklyCommit | null>(null);
  const [hasUnreconciled, setHasUnreconciled] = useState(false);
  const [nudgeDismissed, setNudgeDismissed] = useState(false);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    if (!currentUser) return;
    setLoading(true);
    try {
      const [week, allGoals, unreconciled] = await Promise.all([
        api.getCurrentWeek(),
        api.getHierarchy(),
        api.hasUnreconciled(currentUser.id),
      ]);
      setCurrentWeek(week);
      setGoals(allGoals);
      setHasUnreconciled(unreconciled);

      const [thisWeek, carried] = await Promise.all([
        api.getThisWeekTasks(currentUser.id, week.id),
        api.getCarriedOverTasks(currentUser.id, week.id),
      ]);
      setThisWeekTasks(thisWeek);
      setCarriedOverTasks(carried);
    } catch (err) {
      console.error("Failed to load data:", err);
    }
    setLoading(false);
  }, [currentUser]);

  useEffect(() => {
    loadData();
    setNudgeDismissed(false);
  }, [loadData]);

  async function handleAddTask(data: {
    name: string;
    priority: string;
    goalId?: string;
    customGoalText?: string;
  }) {
    if (!currentUser || !currentWeek) return;
    await api.createCommit({
      name: data.name,
      priority: data.priority as WeeklyCommit["priority"],
      goalId: data.goalId,
      customGoalText: data.customGoalText,
      ownerId: currentUser.id,
      weekId: currentWeek.id,
    });
    setShowModal(false);
    loadData();
  }

  async function handleEditTask(data: {
    name: string;
    priority: string;
    goalId?: string;
    customGoalText?: string;
  }) {
    if (!editingTask) return;
    await api.updateCommit(editingTask.id, {
      name: data.name,
      priority: data.priority as WeeklyCommit["priority"],
      goalId: data.goalId,
      customGoalText: data.customGoalText,
    });
    setEditingTask(null);
    loadData();
  }

  async function handleDelete(taskId: string) {
    await api.deleteCommit(taskId);
    loadData();
  }

  if (!currentUser) return <div className="loading">Select a user to continue</div>;
  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div>
      {hasUnreconciled && !nudgeDismissed && (
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
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          + Add Task
        </button>
      </div>

      {currentWeek && (
        <p style={{ fontSize: 13, color: "#666", marginBottom: 16 }}>
          {currentWeek.startDate} &mdash; {currentWeek.endDate}
        </p>
      )}

      <h3 className="section-subtitle">This Week</h3>
      {thisWeekTasks.length === 0 ? (
        <div className="empty-state">No tasks yet. Add one to get started.</div>
      ) : (
        thisWeekTasks.map((task) => (
          <TaskCard
            key={task.id}
            task={task}
            goals={goals}
            onEdit={(t) => setEditingTask(t)}
            onDelete={handleDelete}
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
