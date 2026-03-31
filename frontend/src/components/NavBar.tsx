import React from "react";
import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { UserRole } from "../types";

export function NavBar() {
  const { currentUser, users, switchUser } = useAuth();

  return (
    <nav className="navbar">
      <div className="navbar-brand">Weekly Commits</div>
      <div className="navbar-links">
        <NavLink to="/my-week" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
          My Week
        </NavLink>
        <NavLink to="/reconcile" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
          Reconcile
        </NavLink>
        {currentUser?.role === UserRole.MANAGER && (
          <NavLink to="/team-dashboard" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
            Team Dashboard
          </NavLink>
        )}
      </div>
      <div className="navbar-user">
        <select
          value={currentUser?.id ?? ""}
          onChange={(e) => switchUser(e.target.value)}
          className="user-select"
        >
          {users.map((user) => (
            <option key={user.id} value={user.id}>
              {user.name} ({user.role})
            </option>
          ))}
        </select>
      </div>
    </nav>
  );
}
