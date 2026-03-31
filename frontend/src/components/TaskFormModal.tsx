import React, { useState, useEffect } from "react";
import type { GoalNode, WeeklyCommit } from "../types";
import { Priority, PRIORITY_LABELS } from "../types";

interface TaskFormModalProps {
  goals: GoalNode[];
  existingTask?: WeeklyCommit | null;
  onSubmit: (data: {
    name: string;
    priority: Priority;
    goalId?: string;
    customGoalText?: string;
  }) => void;
  onClose: () => void;
}

export function TaskFormModal({ goals, existingTask, onSubmit, onClose }: TaskFormModalProps) {
  const [name, setName] = useState(existingTask?.name ?? "");
  const [priority, setPriority] = useState<Priority>(existingTask?.priority ?? Priority.KNIGHT);
  const [goalMode, setGoalMode] = useState<"org" | "custom">(
    existingTask?.customGoal ? "custom" : "org"
  );
  const [goalId, setGoalId] = useState(existingTask?.goalId ?? "");
  const [customGoalText, setCustomGoalText] = useState(existingTask?.customGoalText ?? "");
  const [goalSearch, setGoalSearch] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);

  const outcomes = goals.filter((g) => g.level === "OUTCOME");
  const filteredOutcomes = outcomes.filter((g) =>
    g.title.toLowerCase().includes(goalSearch.toLowerCase())
  );

  const selectedGoal = outcomes.find((g) => g.id === goalId);

  useEffect(() => {
    if (selectedGoal) {
      setGoalSearch(selectedGoal.title);
    }
  }, [selectedGoal]);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim()) return;
    onSubmit({
      name: name.trim(),
      priority,
      goalId: goalMode === "org" && goalId ? goalId : undefined,
      customGoalText: goalMode === "custom" && customGoalText.trim() ? customGoalText.trim() : undefined,
    });
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">{existingTask ? "Edit Task" : "Add Task"}</h2>
          <button className="btn btn-outline btn-sm" onClick={onClose}>
            &times;
          </button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Task Name</label>
            <input
              className="form-input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="What are you working on?"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Company Goal</label>
            <div style={{ position: "relative" }}>
              <input
                className="form-input"
                value={goalMode === "org" ? goalSearch : customGoalText}
                onChange={(e) => {
                  if (goalMode === "org") {
                    setGoalSearch(e.target.value);
                    setGoalId("");
                    setShowDropdown(true);
                  } else {
                    setCustomGoalText(e.target.value);
                  }
                }}
                onFocus={() => goalMode === "org" && setShowDropdown(true)}
                placeholder={goalMode === "org" ? "Search org goals..." : "Type custom goal..."}
              />
              {goalMode === "org" && showDropdown && filteredOutcomes.length > 0 && (
                <div
                  style={{
                    position: "absolute",
                    top: "100%",
                    left: 0,
                    right: 0,
                    background: "#fff",
                    border: "1px solid #d1d5db",
                    borderRadius: 6,
                    maxHeight: 200,
                    overflowY: "auto",
                    zIndex: 10,
                  }}
                >
                  {filteredOutcomes.map((g) => (
                    <div
                      key={g.id}
                      style={{
                        padding: "8px 12px",
                        cursor: "pointer",
                        fontSize: 14,
                        borderBottom: "1px solid #f3f4f6",
                      }}
                      onMouseDown={() => {
                        setGoalId(g.id);
                        setGoalSearch(g.title);
                        setShowDropdown(false);
                      }}
                    >
                      {g.title}
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div style={{ marginTop: 6, display: "flex", gap: 12, fontSize: 13 }}>
              <label>
                <input
                  type="radio"
                  checked={goalMode === "org"}
                  onChange={() => setGoalMode("org")}
                />{" "}
                Org Goal
              </label>
              <label>
                <input
                  type="radio"
                  checked={goalMode === "custom"}
                  onChange={() => setGoalMode("custom")}
                />{" "}
                Custom Goal
              </label>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Priority</label>
            <select
              className="form-select"
              value={priority}
              onChange={(e) => setPriority(e.target.value as Priority)}
            >
              {Object.entries(PRIORITY_LABELS).map(([key, val]) => (
                <option key={key} value={key}>
                  {val.label} — {val.meaning}
                </option>
              ))}
            </select>
          </div>

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {existingTask ? "Save Changes" : "Add Task"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
