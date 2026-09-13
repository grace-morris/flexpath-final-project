import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * check the token's exp claim to see if it's expired
 */
function getExpiryTime(token) {
  try {
    const payload = token.split(".")[1];
    const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    return typeof decoded.exp === "number" ? decoded.exp * 1000 : null;
  } catch {
    return null;
  }
}

/**
 * reroute to login
 */
function RequireAuth({ children }) {
  const { auth, setAuth } = useAuth();

  const [expired, setExpired] = useState(() => {
    if (!auth) return false;
    const expiresAt = getExpiryTime(auth.token);
    return expiresAt !== null && expiresAt <= Date.now();
  });

  //check regularly to see if authorization token has expired
   useEffect(() => {
    if (!auth || expired) {
      return;
    }
    const expiresAt = getExpiryTime(auth.token);
    if (expiresAt === null) {
      return;
    }
    const msUntilExpiry = expiresAt - Date.now();
    if (msUntilExpiry <= 0) {
      setExpired(true);
      return;
    }
    const timer = setTimeout(() => setExpired(true), msUntilExpiry);
    return () => clearTimeout(timer);
  }, [auth, expired]);

  useEffect(() => {
    if (expired) {
      setAuth(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expired]);

  if (!auth || expired) {
    return (
      <Navigate
        to="/login"
        replace
        state={expired ? { message: "Your session has expired. Please log in again." } : undefined}
      />
    );
  }

  return children;
}

export default RequireAuth;