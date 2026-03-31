export enum UserRole {
  EMPLOYEE = "EMPLOYEE",
  MANAGER = "MANAGER",
}

export enum GoalLevel {
  RALLY_CRY = "RALLY_CRY",
  DEFINING_OBJECTIVE = "DEFINING_OBJECTIVE",
  OUTCOME = "OUTCOME",
}

export enum Priority {
  KING = "KING",
  QUEEN = "QUEEN",
  KNIGHT = "KNIGHT",
  PAWN = "PAWN",
}

export enum CommitStatus {
  DRAFT = "DRAFT",
  LOCKED = "LOCKED",
  RECONCILING = "RECONCILING",
  RECONCILED = "RECONCILED",
  CARRY_FORWARD = "CARRY_FORWARD",
}

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
}

export interface Week {
  id: string;
  startDate: string;
  endDate: string;
}

export interface GoalNode {
  id: string;
  title: string;
  level: GoalLevel;
  parentId: string | null;
}

export interface WeeklyCommit {
  id: string;
  name: string;
  priority: Priority;
  goalId: string | null;
  customGoalText: string | null;
  customGoal: boolean;
  ownerId: string;
  weekId: string;
  carriedOverFrom: string | null;
  status: CommitStatus;
}

export interface ReconciliationEntry {
  id: string;
  commitId: string;
  done: boolean;
  explanation: string | null;
}

export interface TeamMemberSummary {
  userId: string;
  name: string;
  taskCount: number;
  alignmentStatus: "GREEN" | "YELLOW" | "RED";
}

export interface CreateCommitRequest {
  name: string;
  priority: Priority;
  goalId?: string;
  customGoalText?: string;
  ownerId: string;
  weekId: string;
}

export interface UpdateCommitRequest {
  name?: string;
  priority?: Priority;
  goalId?: string;
  customGoalText?: string;
}

export interface ReconcileRequest {
  commitId: string;
  done: boolean;
  explanation?: string;
}

export const PRIORITY_LABELS: Record<Priority, { label: string; meaning: string }> = {
  [Priority.KING]: { label: "King", meaning: "Critical" },
  [Priority.QUEEN]: { label: "Queen", meaning: "High" },
  [Priority.KNIGHT]: { label: "Knight", meaning: "Medium" },
  [Priority.PAWN]: { label: "Pawn", meaning: "Low" },
};
