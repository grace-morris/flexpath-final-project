import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const BASE_URL = "/api";

/**
 * Combined login/register screen and hands info back to AuthContext
 */
function LoginPage() {
  const { setAuth } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [mode, setMode] = useState("login"); // "login" | "register"
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");


  /**
   * see if the session is expired before or during engagement with the site
   */
  const [error, setError] = useState(() => {
    if (location.state?.message) {
      return location.state.message;
    }
    if (localStorage.getItem("sessionExpired") === "true") {
      localStorage.removeItem("sessionExpired");
      return "Your session expired. Please log in again.";
    }
    return "";
  });
  const [submitting, setSubmitting] = useState(false);

  const logIn = async (username, password) => {
    const loginResponse = await fetch("/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    if (!loginResponse.ok) {
      throw new Error("Incorrect username or password.");
    }

    const loginData = await loginResponse.json();
    const token = loginData.accessToken.token;

    const rolesResponse = await fetch(`${BASE_URL}/profile/roles`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const roles = rolesResponse.ok ? await rolesResponse.json() : [];
    const isAdmin = roles.includes("ADMIN");

    setAuth({ token, username, isAdmin });
    navigate("/encounters");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      if (mode === "register") {
        const registerResponse = await fetch(`${BASE_URL}/users`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ username, password }),
        });
        if (!registerResponse.ok) {
          const message = await registerResponse.text();
          throw new Error(message || "Could not create that account.");
        }
      }
      await logIn(username, password);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container mt-5" style={{ maxWidth: "400px" }}>
      <h2 className="mb-3">{mode === "login" ? "Log in" : "Create an account"}</h2>

      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={handleSubmit}>
        <input
          className="form-control mb-2"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoFocus
          required
        />
        <input
          className="form-control mb-3"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button className="btn btn-primary w-100" type="submit" disabled={submitting}>
          {submitting ? "Please wait..." : mode === "login" ? "Log in" : "Register"}
        </button>
      </form>

      <button
        className="btn btn-link mt-2"
        onClick={() => {
          setError("");
          setMode(mode === "login" ? "register" : "login");
        }}
      >
        {mode === "login" ? "Need an account? Register" : "Already have an account? Log in"}
      </button>
    </div>
  );
}

export default LoginPage;