import React, { useEffect, useState } from "react";
import type { User } from "../types";
import { UserRole } from "../types";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

const SEEDED_EMAILS = ["alice@st6.com", "bob@st6.com", "carol@st6.com", "dan@st6.com"];

type View = "select" | "signin" | "signup";

export function LoginPage() {
  const { login } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState<View>("select");
  const [error, setError] = useState("");

  // Sign in state
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  // Sign up state
  const [signupName, setSignupName] = useState("");
  const [signupEmail, setSignupEmail] = useState("");
  const [signupPassword, setSignupPassword] = useState("");
  const [signupRole, setSignupRole] = useState("EMPLOYEE");

  useEffect(() => {
    api.getUsers().then((fetched) => {
      setUsers(fetched);
      setLoading(false);
    });
  }, []);

  const seededUsers = users.filter((u) => SEEDED_EMAILS.includes(u.email));

  const resetForms = () => {
    setError("");
    setEmail("");
    setPassword("");
    setSignupName("");
    setSignupEmail("");
    setSignupPassword("");
    setSignupRole("EMPLOYEE");
  };

  const switchView = (v: View) => {
    resetForms();
    setView(v);
  };

  const handleSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      const user = await api.login(email, password);
      login(user);
    } catch (err: any) {
      setError(err.message || "Invalid email or password");
    }
  };

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      const user = await api.register(signupName, signupEmail, signupPassword, signupRole);
      login(user);
    } catch (err: any) {
      setError(err.message || "Registration failed");
    }
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="login-page">
      <div className="login-container">
        <h1 className="login-title">Weekly Commits</h1>

        {view === "select" && (
          <>
            <p className="login-subtitle">Quick sign in as a demo user</p>
            <div className="login-user-grid">
              {seededUsers.map((user) => (
                <button
                  key={user.id}
                  className="login-user-card"
                  onClick={() => login(user)}
                >
                  <div className="login-avatar">
                    {user.name.split(" ").map((n) => n[0]).join("")}
                  </div>
                  <div className="login-user-info">
                    <div className="login-user-name">{user.name}</div>
                    <div className="login-user-email">{user.email}</div>
                  </div>
                  <span className={`login-role-badge role-${user.role}`}>
                    {user.role === UserRole.MANAGER ? "Manager" : "Employee"}
                  </span>
                </button>
              ))}
            </div>
            <div className="login-divider">
              <span>or</span>
            </div>
            <div className="login-actions">
              <button className="btn btn-primary login-action-btn" onClick={() => switchView("signin")}>
                Sign In
              </button>
              <button className="btn btn-outline login-action-btn" onClick={() => switchView("signup")}>
                Create Account
              </button>
            </div>
          </>
        )}

        {view === "signin" && (
          <>
            <p className="login-subtitle">Sign in to your account</p>
            <form className="login-form" onSubmit={handleSignIn}>
              {error && <div className="login-error">{error}</div>}
              <div className="form-group">
                <label className="form-label">Email</label>
                <input
                  className="form-input"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Password</label>
                <input
                  className="form-input"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter password"
                  required
                />
              </div>
              <button className="btn btn-primary login-action-btn" type="submit">Sign In</button>
            </form>
            <button className="login-back-link" onClick={() => switchView("select")}>
              Back to user select
            </button>
          </>
        )}

        {view === "signup" && (
          <>
            <p className="login-subtitle">Create an account</p>
            <form className="login-form" onSubmit={handleSignUp}>
              {error && <div className="login-error">{error}</div>}
              <div className="form-group">
                <label className="form-label">Name</label>
                <input
                  className="form-input"
                  type="text"
                  value={signupName}
                  onChange={(e) => setSignupName(e.target.value)}
                  placeholder="Your full name"
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Email</label>
                <input
                  className="form-input"
                  type="email"
                  value={signupEmail}
                  onChange={(e) => setSignupEmail(e.target.value)}
                  placeholder="you@example.com"
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Password</label>
                <input
                  className="form-input"
                  type="password"
                  value={signupPassword}
                  onChange={(e) => setSignupPassword(e.target.value)}
                  placeholder="At least 4 characters"
                  minLength={4}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Role</label>
                <select
                  className="form-select"
                  value={signupRole}
                  onChange={(e) => setSignupRole(e.target.value)}
                >
                  <option value="EMPLOYEE">Employee</option>
                  <option value="MANAGER">Manager</option>
                </select>
              </div>
              <button className="btn btn-primary login-action-btn" type="submit">Create Account</button>
            </form>
            <button className="login-back-link" onClick={() => switchView("select")}>
              Back to user select
            </button>
          </>
        )}
      </div>
    </div>
  );
}
