import React from "react";
import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { UserRole } from "../types";

export function NavBar() {
  const { currentUser, logout } = useAuth();

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
        <span className="navbar-user-name">{currentUser?.name}</span>
        <button className="btn-logout" onClick={logout}>Sign Out</button>
      </div>
    </nav>
  );
}
