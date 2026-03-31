import type {
  User,
  Week,
  GoalNode,
  WeeklyCommit,
  ReconciliationEntry,
  TeamMemberSummary,
  CreateCommitRequest,
  UpdateCommitRequest,
  ReconcileRequest,
} from "../types";

const BASE_URL = "http://localhost:8080/api";

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ error: res.statusText }));
    throw new Error(error.error || res.statusText);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text);
}

export const api = {
  // Users
  getUsers: () => request<User[]>("/users"),
  getUser: (id: string) => request<User>(`/users/${id}`),
  getUserByEmail: (email: string) => request<User>(`/users/email/${email}`),

  // Weeks
  getCurrentWeek: () => request<Week>("/weeks/current"),
  getPriorWeek: () => request<Week | null>("/weeks/prior"),
  getWeek: (id: string) => request<Week>(`/weeks/${id}`),

  // Goals
  getGoals: () => request<GoalNode[]>("/goals"),
  getOutcomes: () => request<GoalNode[]>("/goals/outcomes"),
  getHierarchy: () => request<GoalNode[]>("/goals/hierarchy"),

  // Commits
  createCommit: (data: CreateCommitRequest) =>
    request<WeeklyCommit>("/commits", { method: "POST", body: JSON.stringify(data) }),
  updateCommit: (id: string, data: UpdateCommitRequest) =>
    request<WeeklyCommit>(`/commits/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  deleteCommit: (id: string) =>
    request<void>(`/commits/${id}`, { method: "DELETE" }),
  getCommit: (id: string) => request<WeeklyCommit>(`/commits/${id}`),
  getCommitsByOwnerAndWeek: (ownerId: string, weekId: string) =>
    request<WeeklyCommit[]>(`/commits/week/${weekId}/owner/${ownerId}`),
  getThisWeekTasks: (ownerId: string, weekId: string) =>
    request<WeeklyCommit[]>(`/commits/week/${weekId}/owner/${ownerId}/this-week`),
  getCarriedOverTasks: (ownerId: string, weekId: string) =>
    request<WeeklyCommit[]>(`/commits/week/${weekId}/owner/${ownerId}/carried-over`),
  lockWeek: (ownerId: string, weekId: string) =>
    request<void>(`/commits/week/${weekId}/owner/${ownerId}/lock`, { method: "POST" }),
  openReconciliation: (ownerId: string, weekId: string) =>
    request<void>(`/commits/week/${weekId}/owner/${ownerId}/open-reconciliation`, { method: "POST" }),
  reconcile: (data: ReconcileRequest) =>
    request<ReconciliationEntry>("/commits/reconcile", { method: "POST", body: JSON.stringify(data) }),
  hasUnreconciled: (ownerId: string) =>
    request<boolean>(`/commits/owner/${ownerId}/has-unreconciled`),

  // Dashboard
  getTeamSummary: (weekId: string) =>
    request<TeamMemberSummary[]>(`/dashboard/team/${weekId}`),
  getMemberTasks: (weekId: string, userId: string) =>
    request<WeeklyCommit[]>(`/dashboard/team/${weekId}/member/${userId}`),
};
