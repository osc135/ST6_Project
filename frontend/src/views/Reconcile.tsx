import React, { useState, useEffect, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { api } from "../api/client";
import type { WeeklyCommit, GoalNode, Week } from "../types";
import { CommitStatus, PRIORITY_LABELS } from "../types";

interface ReconcileState {
  [commitId: string]: {
    done: boolean | null;
    explanation: string;
  };
}

export function Reconcile() {
  const { currentUser } = useAuth();
  const { showError, showSuccess } = useToast();
  const [priorWeek, setPriorWeek] = useState<Week | null>(null);
  const [tasks, setTasks] = useState<WeeklyCommit[]>([]);
  const [goals, setGoals] = useState<GoalNode[]>([]);
  const [reconcileState, setReconcileState] = useState<ReconcileState>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const loadData = useCallback(async () => {
    if (!currentUser) return;
    setLoading(true);
    try {
      const [week, allGoals] = await Promise.all([
        api.getPriorWeek(),
        api.getHierarchy(),
      ]);
      setGoals(allGoals);

      if (!week) {
        setPriorWeek(null);
        setTasks([]);
        setLoading(false);
        return;
      }

      setPriorWeek(week);
      const allTasks = await api.getCommitsByOwnerAndWeek(currentUser.id, week.id);
      const reconcilable = allTasks.filter(
        (t) =>
          t.status === CommitStatus.RECONCILING ||
          t.status === CommitStatus.LOCKED
      );
      setTasks(reconcilable);

      const initial: ReconcileState = {};
      for (const task of reconcilable) {
        initial[task.id] = { done: null, explanation: "" };
      }
      setReconcileState(initial);
    } catch (err) {
      showError("Failed to load reconciliation data. Please try again.");
    }
    setLoading(false);
  }, [currentUser]);

  useEffect(() => {
    loadData();
    setSubmitted(false);
  }, [loadData]);

  async function handleOpenReconciliation() {
    if (!currentUser || !priorWeek) return;
    try {
      await api.openReconciliation(currentUser.id, priorWeek.id);
      showSuccess("Reconciliation opened.");
      loadData();
    } catch (err) {
      showError(err instanceof Error ? err.message : "Failed to open reconciliation.");
    }
  }

  function setTaskDone(commitId: string, done: boolean) {
    setReconcileState((prev) => ({
      ...prev,
      [commitId]: { ...prev[commitId], done, explanation: done ? "" : prev[commitId].explanation },
    }));
  }

  function setTaskExplanation(commitId: string, explanation: string) {
    setReconcileState((prev) => ({
      ...prev,
      [commitId]: { ...prev[commitId], explanation },
    }));
  }

  async function handleSubmit() {
    const reconcilingTasks = tasks.filter((t) => t.status === CommitStatus.RECONCILING);
    const allAddressed = reconcilingTasks.every((t) => reconcileState[t.id]?.done !== null);
    if (!allAddressed) return;

    const notDoneWithoutExplanation = reconcilingTasks.some(
      (t) => reconcileState[t.id]?.done === false && !reconcileState[t.id]?.explanation.trim()
    );
    if (notDoneWithoutExplanation) return;

    setSubmitting(true);
    try {
      for (const task of reconcilingTasks) {
        const state = reconcileState[task.id];
        await api.reconcile({
          commitId: task.id,
          done: state.done!,
          explanation: state.done ? undefined : state.explanation,
        });
      }
      setSubmitted(true);
      showSuccess("Reconciliation submitted!");
    } catch (err) {
      showError(err instanceof Error ? err.message : "Failed to submit reconciliation.");
    }
    setSubmitting(false);
  }

  if (!currentUser) return <div className="loading">Select a user to continue</div>;
  if (loading) return <div className="loading">Loading...</div>;

  if (submitted) {
    return (
      <div>
        <h1 className="section-title">Reconciliation</h1>
        <div className="empty-state">
          <p style={{ fontSize: 16, marginBottom: 8 }}>Reconciliation submitted!</p>
          <p>Tasks marked as not done have been carried forward to next week.</p>
        </div>
      </div>
    );
  }

  if (!priorWeek || tasks.length === 0) {
    return (
      <div>
        <h1 className="section-title">Reconciliation</h1>
        <div className="empty-state">No tasks to reconcile from last week.</div>
      </div>
    );
  }

  const hasLockedTasks = tasks.some((t) => t.status === CommitStatus.LOCKED);
  const hasReconcilingTasks = tasks.some((t) => t.status === CommitStatus.RECONCILING);

  return (
    <div>
      <h1 className="section-title" style={{ marginBottom: 8 }}>Reconciliation</h1>
      <p style={{ fontSize: 13, color: "#666", marginBottom: 16 }}>
        Week of {priorWeek.startDate} &mdash; {priorWeek.endDate}
      </p>

      {hasLockedTasks && !hasReconcilingTasks && (
        <div style={{ marginBottom: 20 }}>
          <p style={{ marginBottom: 8, fontSize: 14 }}>
            Your prior week tasks are locked. Open reconciliation to mark them as done or not done.
          </p>
          <button className="btn btn-primary" onClick={handleOpenReconciliation}>
            Open Reconciliation
          </button>
        </div>
      )}

      {tasks.map((task) => {
        const goalNode = task.goalId ? goals.find((g) => g.id === task.goalId) : null;
        const state = reconcileState[task.id];
        const priorityInfo = PRIORITY_LABELS[task.priority];

        return (
          <div key={task.id} className="reconcile-card">
            <div className="task-card-header">
              <span className="task-name">{task.name}</span>
              <span className={`priority-badge priority-${task.priority}`}>
                {priorityInfo.label}
              </span>
            </div>
            <div className={`task-goal ${task.customGoal ? "custom" : ""}`}>
              {goalNode ? goalNode.title : task.customGoalText ? `Custom: ${task.customGoalText}` : "No goal linked"}
            </div>

            {task.status === CommitStatus.RECONCILING && (
              <>
                <div className="reconcile-actions">
                  <button
                    className={`btn btn-success ${state?.done === true ? "selected" : ""}`}
                    onClick={() => setTaskDone(task.id, true)}
                  >
                    Done
                  </button>
                  <button
                    className={`btn btn-danger ${state?.done === false ? "selected" : ""}`}
                    onClick={() => setTaskDone(task.id, false)}
                  >
                    Not Done
                  </button>
                </div>

                {state?.done === false && (
                  <div className="explanation-input">
                    <textarea
                      className="form-textarea"
                      placeholder="Why wasn't this completed? (required)"
                      value={state.explanation}
                      onChange={(e) => setTaskExplanation(task.id, e.target.value)}
                      rows={2}
                    />
                  </div>
                )}
              </>
            )}
          </div>
        );
      })}

      {hasReconcilingTasks && (
        <div style={{ marginTop: 20 }}>
          <button
            className="btn btn-primary"
            onClick={handleSubmit}
            disabled={
              submitting ||
              tasks
                .filter((t) => t.status === CommitStatus.RECONCILING)
                .some((t) => reconcileState[t.id]?.done === null) ||
              tasks.some(
                (t) =>
                  reconcileState[t.id]?.done === false && !reconcileState[t.id]?.explanation.trim()
              )
            }
          >
            {submitting ? "Submitting..." : "Submit Reconciliation"}
          </button>
        </div>
      )}
    </div>
  );
}
