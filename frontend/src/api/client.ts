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

let currentUserId: string | null = null;

export function setCurrentUserId(userId: string | null) {
  currentUserId = userId;
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (currentUserId) {
    headers["X-User-Id"] = currentUserId;
  }
  const res = await fetch(`${BASE_URL}${path}`, {
    headers,
    ...options,
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ error: res.statusText }));
    throw new Error(error.message || error.error || res.statusText);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text);
}

export const api = {
  // Auth
  login: (email: string, password: string) =>
    request<User>("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) }),
  register: (name: string, email: string, password: string, role: string) =>
    request<User>("/auth/register", { method: "POST", body: JSON.stringify({ name, email, password, role }) }),
  inviteEmployee: (name: string, email: string, managerId: string) =>
    request<User>("/auth/invite", { method: "POST", body: JSON.stringify({ name, email, managerId }) }),

  // Users
  getUsers: () => request<User[]>("/users"),
  getUser: (id: string) => request<User>(`/users/${id}`),
  getUserByEmail: (email: string) => request<User>(`/users/email/${email}`),

  // Weeks
  getCurrentWeek: () => request<Week>("/weeks/current"),
  getPriorWeek: () => request<Week | null>("/weeks/prior"),
  getWeek: (id: string) => request<Week>(`/weeks/${id}`),
  getWeekByDate: (date: string) => request<Week>(`/weeks/by-date?date=${date}`),

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
  getTeamSummary: (weekId: string, managerId: string) =>
    request<TeamMemberSummary[]>(`/dashboard/team/${weekId}?managerId=${managerId}`),
  getMemberTasks: (weekId: string, userId: string) =>
    request<WeeklyCommit[]>(`/dashboard/team/${weekId}/member/${userId}`),
};
