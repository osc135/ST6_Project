import React from "react";
import { useToast } from "../context/ToastContext";

export function ToastContainer() {
  const { toasts, dismiss } = useToast();

  if (toasts.length === 0) return null;

  return (
    <div className="toast-container">
      {toasts.map((toast) => (
        <div key={toast.id} className={`toast toast-${toast.type}`}>
          <span>{toast.message}</span>
          <button className="toast-close" onClick={() => dismiss(toast.id)}>
            &times;
          </button>
        </div>
      ))}
    </div>
  );
}
