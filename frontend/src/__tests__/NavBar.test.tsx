import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import { BrowserRouter } from "react-router-dom";
import { NavBar } from "../components/NavBar";
import { UserRole } from "../types";
import type { User } from "../types";

const mockSwitchUser = jest.fn();

const managerUser: User = {
  id: "user-1",
  name: "Alice Manager",
  email: "alice@example.com",
  role: UserRole.MANAGER,
};

const employeeUser: User = {
  id: "user-2",
  name: "Bob Employee",
  email: "bob@example.com",
  role: UserRole.EMPLOYEE,
};

const allUsers: User[] = [managerUser, employeeUser];

jest.mock("../context/AuthContext", () => ({
  useAuth: jest.fn(),
}));

import { useAuth } from "../context/AuthContext";
const mockUseAuth = useAuth as jest.Mock;

function renderNavBar() {
  return render(
    <BrowserRouter>
      <NavBar />
    </BrowserRouter>
  );
}

describe("NavBar", () => {
  beforeEach(() => {
    mockSwitchUser.mockClear();
  });

  it("renders 'Weekly Commits' brand text", () => {
    mockUseAuth.mockReturnValue({
      currentUser: managerUser,
      users: allUsers,
      switchUser: mockSwitchUser,
      loading: false,
    });
    renderNavBar();
    expect(screen.getByText("Weekly Commits")).toBeInTheDocument();
  });

  it("shows My Week and Reconcile links for all users", () => {
    mockUseAuth.mockReturnValue({
      currentUser: employeeUser,
      users: allUsers,
      switchUser: mockSwitchUser,
      loading: false,
    });
    renderNavBar();
    expect(screen.getByText("My Week")).toBeInTheDocument();
    expect(screen.getByText("Reconcile")).toBeInTheDocument();
  });

  it("shows Team Dashboard link for MANAGER role users", () => {
    mockUseAuth.mockReturnValue({
      currentUser: managerUser,
      users: allUsers,
      switchUser: mockSwitchUser,
      loading: false,
    });
    renderNavBar();
    expect(screen.getByText("Team Dashboard")).toBeInTheDocument();
  });

  it("hides Team Dashboard link for EMPLOYEE role users", () => {
    mockUseAuth.mockReturnValue({
      currentUser: employeeUser,
      users: allUsers,
      switchUser: mockSwitchUser,
      loading: false,
    });
    renderNavBar();
    expect(screen.queryByText("Team Dashboard")).not.toBeInTheDocument();
  });

  it("user select dropdown shows all users", () => {
    mockUseAuth.mockReturnValue({
      currentUser: managerUser,
      users: allUsers,
      switchUser: mockSwitchUser,
      loading: false,
    });
    renderNavBar();
    expect(screen.getByText("Alice Manager (MANAGER)")).toBeInTheDocument();
    expect(screen.getByText("Bob Employee (EMPLOYEE)")).toBeInTheDocument();
  });
});
