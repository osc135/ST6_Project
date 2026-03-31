import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import type { User } from "../types";
import { api } from "../api/client";

interface AuthContextValue {
  currentUser: User | null;
  users: User[];
  switchUser: (userId: string) => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextValue>({
  currentUser: null,
  users: [],
  switchUser: () => {},
  loading: true,
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [users, setUsers] = useState<User[]>([]);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getUsers().then((fetched) => {
      setUsers(fetched);
      if (fetched.length > 0) {
        setCurrentUser(fetched[0]);
      }
      setLoading(false);
    });
  }, []);

  const switchUser = useCallback(
    (userId: string) => {
      const user = users.find((u) => u.id === userId);
      if (user) setCurrentUser(user);
    },
    [users]
  );

  return (
    <AuthContext.Provider value={{ currentUser, users, switchUser, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
