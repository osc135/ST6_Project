import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { NavBar } from "./components/NavBar";
import { MyWeek } from "./views/MyWeek";
import { Reconcile } from "./views/Reconcile";
import { TeamDashboard } from "./views/TeamDashboard";
import "./styles.css";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <div className="app">
          <NavBar />
          <main className="main-content">
            <Routes>
              <Route path="/" element={<Navigate to="/my-week" replace />} />
              <Route path="/my-week" element={<MyWeek />} />
              <Route path="/reconcile" element={<Reconcile />} />
              <Route path="/team-dashboard" element={<TeamDashboard />} />
            </Routes>
          </main>
        </div>
      </AuthProvider>
    </BrowserRouter>
  );
}
