import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ToastProvider } from "./context/ToastContext";
import { NavBar } from "./components/NavBar";
import { ToastContainer } from "./components/ToastContainer";
import { MyWeek } from "./views/MyWeek";
import { Reconcile } from "./views/Reconcile";
import { TeamDashboard } from "./views/TeamDashboard";
import "./styles.css";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <div className="app">
            <NavBar />
            <ToastContainer />
            <main className="main-content">
              <Routes>
                <Route path="/" element={<Navigate to="/my-week" replace />} />
                <Route path="/my-week" element={<MyWeek />} />
                <Route path="/reconcile" element={<Reconcile />} />
                <Route path="/team-dashboard" element={<TeamDashboard />} />
              </Routes>
            </main>
          </div>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
