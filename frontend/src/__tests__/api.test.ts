import { api } from "../api/client";
import { Priority } from "../types";

const BASE_URL = "http://localhost:8080/api";

const mockFetch = jest.fn();
global.fetch = mockFetch;

function mockResponse(body: unknown, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    statusText: status === 200 ? "OK" : "Internal Server Error",
    json: jest.fn().mockResolvedValue(body),
    text: jest.fn().mockResolvedValue(JSON.stringify(body)),
  };
}

describe("api client", () => {
  beforeEach(() => {
    mockFetch.mockReset();
  });

  it("getUsers calls correct URL", async () => {
    mockFetch.mockResolvedValue(mockResponse([{ id: "u1", name: "Alice" }]));
    const result = await api.getUsers();
    expect(mockFetch).toHaveBeenCalledWith(
      `${BASE_URL}/users`,
      expect.objectContaining({ headers: { "Content-Type": "application/json" } })
    );
    expect(result).toEqual([{ id: "u1", name: "Alice" }]);
  });

  it("getCurrentWeek calls correct URL", async () => {
    const week = { id: "w1", startDate: "2026-03-23", endDate: "2026-03-29" };
    mockFetch.mockResolvedValue(mockResponse(week));
    const result = await api.getCurrentWeek();
    expect(mockFetch).toHaveBeenCalledWith(
      `${BASE_URL}/weeks/current`,
      expect.objectContaining({ headers: { "Content-Type": "application/json" } })
    );
    expect(result).toEqual(week);
  });

  it("createCommit sends POST with correct body", async () => {
    const commit = {
      name: "New task",
      priority: Priority.KING,
      ownerId: "u1",
      weekId: "w1",
    };
    const response = { id: "c1", ...commit };
    mockFetch.mockResolvedValue(mockResponse(response));

    await api.createCommit(commit);

    expect(mockFetch).toHaveBeenCalledWith(
      `${BASE_URL}/commits`,
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify(commit),
        headers: { "Content-Type": "application/json" },
      })
    );
  });

  it("updateCommit sends PUT with correct body", async () => {
    const update = { name: "Updated task", priority: Priority.QUEEN };
    const response = { id: "c1", ...update };
    mockFetch.mockResolvedValue(mockResponse(response));

    await api.updateCommit("c1", update);

    expect(mockFetch).toHaveBeenCalledWith(
      `${BASE_URL}/commits/c1`,
      expect.objectContaining({
        method: "PUT",
        body: JSON.stringify(update),
        headers: { "Content-Type": "application/json" },
      })
    );
  });

  it("deleteCommit sends DELETE and handles 204", async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      status: 204,
      statusText: "No Content",
      json: jest.fn().mockRejectedValue(new Error("no body")),
      text: jest.fn().mockResolvedValue(""),
    });

    const result = await api.deleteCommit("c1");

    expect(mockFetch).toHaveBeenCalledWith(
      `${BASE_URL}/commits/c1`,
      expect.objectContaining({
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
      })
    );
    expect(result).toBeUndefined();
  });

  it("request throws on non-ok response with error message", async () => {
    mockFetch.mockResolvedValue({
      ok: false,
      status: 400,
      statusText: "Bad Request",
      json: jest.fn().mockResolvedValue({ error: "Validation failed" }),
    });

    await expect(api.getUsers()).rejects.toThrow("Validation failed");
  });

  it("reconcile sends correct payload", async () => {
    const payload = { commitId: "c1", done: true, explanation: "Completed on time" };
    const response = { id: "r1", ...payload };
    mockFetch.mockResolvedValue(mockResponse(response));

    await api.reconcile(payload);

    expect(mockFetch).toHaveBeenCalledWith(
      `${BASE_URL}/commits/reconcile`,
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify(payload),
        headers: { "Content-Type": "application/json" },
      })
    );
  });
});
