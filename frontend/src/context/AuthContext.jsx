import { createContext, useContext, useState } from "react";

/**
 * holds the JWT/username/admin flag once logged in.
 * Kept separate from the monster/character/encounter feature so you just
 * need to call setAuth({ token, username, isAdmin })
 */
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuthState] = useState(() => {
    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");
    const isAdmin = localStorage.getItem("isAdmin") === "true";
    return token ? { token, username, isAdmin } : null;
  });

  const setAuth = (nextAuth) => {
    if (nextAuth) {
      localStorage.setItem("token", nextAuth.token);
      localStorage.setItem("username", nextAuth.username);
      localStorage.setItem("isAdmin", String(nextAuth.isAdmin));
    } else {
      localStorage.removeItem("token");
      localStorage.removeItem("username");
      localStorage.removeItem("isAdmin");
    }
    setAuthState(nextAuth);
  };

  return (
    <AuthContext.Provider value={{ auth, setAuth }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
