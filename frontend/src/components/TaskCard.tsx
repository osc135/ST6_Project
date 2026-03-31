import React from "react";
import type { WeeklyCommit, GoalNode } from "../types";
import { PRIORITY_LABELS, CommitStatus } from "../types";

interface TaskCardProps {
  task: WeeklyCommit;
  goals: GoalNode[];
  onEdit?: (task: WeeklyCommit) => void;
  onDelete?: (taskId: string) => void;
  showActions?: boolean;
}

export function TaskCard({ task, goals, onEdit, onDelete, showActions = true }: TaskCardProps) {
  const goalNode = task.goalId ? goals.find((g) => g.id === task.goalId) : null;
  const priorityInfo = PRIORITY_LABELS[task.priority];
  const canEdit = task.status === CommitStatus.DRAFT;

  return (
    <div className="task-card">
      <div className="task-card-header">
        <span className="task-name">{task.name}</span>
        <div className="task-meta">
          <span className={`priority-badge priority-${task.priority}`}>
            {priorityInfo.label}
          </span>
          <span className={`status-badge status-${task.status}`}>
            {task.status.replace("_", " ")}
          </span>
        </div>
      </div>
      <div className={`task-goal ${task.customGoal ? "custom" : ""} ${!goalNode && !task.customGoalText ? "unaligned" : ""}`}>
        {goalNode
          ? goalNode.title
          : task.customGoalText
            ? `Custom: ${task.customGoalText}`
            : "No goal linked"}
      </div>
      {task.carriedOverFrom && (
        <div style={{ fontSize: 12, color: "#7c3aed", marginTop: 4 }}>
          Carried over from previous week
        </div>
      )}
      {showActions && canEdit && (
        <div className="task-card-actions">
          {onEdit && (
            <button className="btn btn-outline btn-sm" onClick={() => onEdit(task)}>
              Edit
            </button>
          )}
          {onDelete && (
            <button className="btn btn-danger btn-sm" onClick={() => onDelete(task.id)}>
              Delete
            </button>
          )}
        </div>
      )}
    </div>
  );
}
