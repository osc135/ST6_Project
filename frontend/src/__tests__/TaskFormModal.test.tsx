import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import { TaskFormModal } from "../components/TaskFormModal";
import { Priority, CommitStatus, GoalLevel } from "../types";
import type { WeeklyCommit, GoalNode } from "../types";

const mockGoals: GoalNode[] = [
  { id: "outcome-1", title: "Increase revenue by 20%", level: GoalLevel.OUTCOME, parentId: null },
  { id: "outcome-2", title: "Improve customer retention", level: GoalLevel.OUTCOME, parentId: null },
  { id: "rally-1", title: "Win the market", level: GoalLevel.RALLY_CRY, parentId: null },
];

function makeTask(overrides: Partial<WeeklyCommit> = {}): WeeklyCommit {
  return {
    id: "task-1",
    name: "Build dashboard",
    priority: Priority.QUEEN,
    goalId: "outcome-1",
    customGoalText: null,
    customGoal: false,
    ownerId: "user-1",
    weekId: "week-1",
    carriedOverFrom: null,
    status: CommitStatus.DRAFT,
    ...overrides,
  };
}

describe("TaskFormModal", () => {
  it("renders 'Add Task' title when no existingTask", () => {
    render(
      <TaskFormModal goals={mockGoals} onSubmit={jest.fn()} onClose={jest.fn()} />
    );
    expect(screen.getByText("Add Task", { selector: "h2" })).toBeInTheDocument();
  });

  it("renders 'Edit Task' title when existingTask is provided", () => {
    render(
      <TaskFormModal
        goals={mockGoals}
        existingTask={makeTask()}
        onSubmit={jest.fn()}
        onClose={jest.fn()}
      />
    );
    expect(screen.getByText("Edit Task")).toBeInTheDocument();
  });

  it("pre-fills form fields when editing existing task", () => {
    const task = makeTask({ name: "Build dashboard", priority: Priority.QUEEN });
    render(
      <TaskFormModal
        goals={mockGoals}
        existingTask={task}
        onSubmit={jest.fn()}
        onClose={jest.fn()}
      />
    );
    const nameInput = screen.getByPlaceholderText("What are you working on?");
    expect(nameInput).toHaveValue("Build dashboard");

    const prioritySelect = screen.getByDisplayValue(/Queen/);
    expect(prioritySelect).toBeInTheDocument();
  });

  it("calls onSubmit with correct data when form submitted with org goal", () => {
    const onSubmit = jest.fn();
    const task = makeTask({ name: "Build dashboard", priority: Priority.QUEEN, goalId: "outcome-1" });
    render(
      <TaskFormModal
        goals={mockGoals}
        existingTask={task}
        onSubmit={onSubmit}
        onClose={jest.fn()}
      />
    );

    fireEvent.click(screen.getByText("Save Changes"));

    expect(onSubmit).toHaveBeenCalledWith({
      name: "Build dashboard",
      priority: Priority.QUEEN,
      goalId: "outcome-1",
      customGoalText: undefined,
    });
  });

  it("calls onSubmit with custom goal text when custom goal mode selected", () => {
    const onSubmit = jest.fn();
    render(
      <TaskFormModal goals={mockGoals} onSubmit={onSubmit} onClose={jest.fn()} />
    );

    // Fill in name
    const nameInput = screen.getByPlaceholderText("What are you working on?");
    fireEvent.change(nameInput, { target: { value: "Fix tech debt" } });

    // Switch to custom goal mode
    fireEvent.click(screen.getByLabelText("Custom Goal"));

    // Type custom goal text
    const goalInput = screen.getByPlaceholderText("Type custom goal...");
    fireEvent.change(goalInput, { target: { value: "Reduce build time" } });

    // Submit
    fireEvent.click(screen.getByText("Add Task", { selector: "button" }));

    expect(onSubmit).toHaveBeenCalledWith({
      name: "Fix tech debt",
      priority: Priority.KNIGHT,
      goalId: undefined,
      customGoalText: "Reduce build time",
    });
  });

  it("calls onClose when cancel button clicked", () => {
    const onClose = jest.fn();
    render(
      <TaskFormModal goals={mockGoals} onSubmit={jest.fn()} onClose={onClose} />
    );
    fireEvent.click(screen.getByText("Cancel"));
    expect(onClose).toHaveBeenCalled();
  });

  it("calls onClose when overlay clicked", () => {
    const onClose = jest.fn();
    const { container } = render(
      <TaskFormModal goals={mockGoals} onSubmit={jest.fn()} onClose={onClose} />
    );
    const overlay = container.querySelector(".modal-overlay")!;
    fireEvent.click(overlay);
    expect(onClose).toHaveBeenCalled();
  });

  it("does not submit when task name is empty", () => {
    const onSubmit = jest.fn();
    render(
      <TaskFormModal goals={mockGoals} onSubmit={onSubmit} onClose={jest.fn()} />
    );

    // Name is already empty, just submit
    fireEvent.click(screen.getByText("Add Task", { selector: "button" }));
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("priority selector shows all 4 chess piece options", () => {
    render(
      <TaskFormModal goals={mockGoals} onSubmit={jest.fn()} onClose={jest.fn()} />
    );
    const options = screen.getAllByRole("option");
    // Filter to only priority options (the select options in the priority dropdown)
    const priorityLabels = ["King", "Queen", "Knight", "Pawn"];
    for (const label of priorityLabels) {
      expect(options.some((opt) => opt.textContent?.includes(label))).toBe(true);
    }
  });

  it("goal dropdown filters outcomes as user types", () => {
    render(
      <TaskFormModal goals={mockGoals} onSubmit={jest.fn()} onClose={jest.fn()} />
    );

    const goalInput = screen.getByPlaceholderText("Search org goals...");

    // Focus to open dropdown
    fireEvent.focus(goalInput);
    // Both outcomes should be visible
    expect(screen.getByText("Increase revenue by 20%")).toBeInTheDocument();
    expect(screen.getByText("Improve customer retention")).toBeInTheDocument();

    // Type to filter
    fireEvent.change(goalInput, { target: { value: "revenue" } });
    expect(screen.getByText("Increase revenue by 20%")).toBeInTheDocument();
    expect(screen.queryByText("Improve customer retention")).not.toBeInTheDocument();
  });
});
