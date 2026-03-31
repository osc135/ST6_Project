import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { TaskCard } from "../components/TaskCard";
import { Priority, CommitStatus, GoalLevel } from "../types";
import type { WeeklyCommit, GoalNode } from "../types";

const mockGoals: GoalNode[] = [
  { id: "goal-1", title: "Payment integration shipped", level: GoalLevel.OUTCOME, parentId: null },
];

function makeTask(overrides: Partial<WeeklyCommit> = {}): WeeklyCommit {
  return {
    id: "task-1",
    name: "Wire up Stripe",
    priority: Priority.KING,
    goalId: "goal-1",
    customGoalText: null,
    customGoal: false,
    ownerId: "user-1",
    weekId: "week-1",
    carriedOverFrom: null,
    status: CommitStatus.DRAFT,
    ...overrides,
  };
}

describe("TaskCard", () => {
  it("renders task name and priority", () => {
    render(<TaskCard task={makeTask()} goals={mockGoals} />);
    expect(screen.getByText("Wire up Stripe")).toBeInTheDocument();
    expect(screen.getByText("King")).toBeInTheDocument();
  });

  it("renders org goal title when linked", () => {
    render(<TaskCard task={makeTask()} goals={mockGoals} />);
    expect(screen.getByText("Payment integration shipped")).toBeInTheDocument();
  });

  it("renders custom goal text when custom goal", () => {
    const task = makeTask({ goalId: null, customGoalText: "Tech debt", customGoal: true });
    render(<TaskCard task={task} goals={mockGoals} />);
    expect(screen.getByText("Custom: Tech debt")).toBeInTheDocument();
  });

  it("renders 'No goal linked' when no goal", () => {
    const task = makeTask({ goalId: null, customGoalText: null, customGoal: false });
    render(<TaskCard task={task} goals={mockGoals} />);
    expect(screen.getByText("No goal linked")).toBeInTheDocument();
  });

  it("shows edit and delete buttons in DRAFT status", () => {
    render(<TaskCard task={makeTask()} goals={mockGoals} onEdit={jest.fn()} onDelete={jest.fn()} />);
    expect(screen.getByText("Edit")).toBeInTheDocument();
    expect(screen.getByText("Delete")).toBeInTheDocument();
  });

  it("hides edit and delete buttons when not DRAFT", () => {
    const task = makeTask({ status: CommitStatus.LOCKED });
    render(<TaskCard task={task} goals={mockGoals} onEdit={jest.fn()} onDelete={jest.fn()} />);
    expect(screen.queryByText("Edit")).toBeNull();
    expect(screen.queryByText("Delete")).toBeNull();
  });

  it("calls onEdit when edit button clicked", () => {
    const onEdit = jest.fn();
    const task = makeTask();
    render(<TaskCard task={task} goals={mockGoals} onEdit={onEdit} />);
    fireEvent.click(screen.getByText("Edit"));
    expect(onEdit).toHaveBeenCalledWith(task);
  });

  it("calls onDelete when delete button clicked", () => {
    const onDelete = jest.fn();
    render(<TaskCard task={makeTask()} goals={mockGoals} onDelete={onDelete} />);
    fireEvent.click(screen.getByText("Delete"));
    expect(onDelete).toHaveBeenCalledWith("task-1");
  });

  it("shows carried over indicator", () => {
    const task = makeTask({ carriedOverFrom: "old-task-1" });
    render(<TaskCard task={task} goals={mockGoals} />);
    expect(screen.getByText("Carried over from previous week")).toBeInTheDocument();
  });

  it("hides actions when showActions is false", () => {
    render(<TaskCard task={makeTask()} goals={mockGoals} showActions={false} onEdit={jest.fn()} onDelete={jest.fn()} />);
    expect(screen.queryByText("Edit")).toBeNull();
    expect(screen.queryByText("Delete")).toBeNull();
  });
});
