import React, { createContext, useContext, useState, useCallback, useRef } from "react";

export interface Toast {
  id: number;
  message: string;
  type: "error" | "success";
}

interface ToastContextValue {
  toasts: Toast[];
  showError: (message: string) => void;
  showSuccess: (message: string) => void;
  dismiss: (id: number) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error("useToast must be used within ToastProvider");
  return ctx;
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const nextId = useRef(0);

  const dismiss = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    (message: string, type: "error" | "success") => {
      const id = nextId.current++;
      setToasts((prev) => [...prev, { id, message, type }]);
      setTimeout(() => dismiss(id), type === "error" ? 6000 : 3000);
    },
    [dismiss]
  );

  const showError = useCallback((msg: string) => addToast(msg, "error"), [addToast]);
  const showSuccess = useCallback((msg: string) => addToast(msg, "success"), [addToast]);

  return (
    <ToastContext.Provider value={{ toasts, showError, showSuccess, dismiss }}>
      {children}
    </ToastContext.Provider>
  );
}
