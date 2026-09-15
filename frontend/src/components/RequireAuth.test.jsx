// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import RequireAuth from "./RequireAuth";
import { useAuth } from "../context/AuthContext";
afterEach(cleanup);

/**
 * Unit tests for RequireAuth - the route guard that decides whether a protected page renders at all
 */

vi.mock("../context/AuthContext", () => ({
  useAuth: vi.fn(),
}));

vi.mock("react-router-dom", () => ({
  Navigate: ({ to, state }) => <div data-testid="navigate" data-to={to} data-message={state?.message} />,
}));

// Builds a fake (unsigned) JWT with only the exp claim that matters here.
function makeToken(expSecondsFromNow) {
  const payload = { exp: Math.floor(Date.now() / 1000) + expSecondsFromNow };
  const base64Payload = btoa(JSON.stringify(payload));
  return `header.${base64Payload}.signature`;
}

describe("RequireAuth", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test("redirects to /login when there is no logged-in user at all", () => {
    useAuth.mockReturnValue({ auth: null, setAuth: vi.fn() });

    render(
      <RequireAuth>
        <div>Protected Content</div>
      </RequireAuth>
    );

    expect(screen.getByTestId("navigate")).toHaveAttribute("data-to", "/login");
    expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
  });

  test("renders the protected children when the token has not expired yet", () => {
    useAuth.mockReturnValue({ auth: { token: makeToken(3600) }, setAuth: vi.fn() });

    render(
      <RequireAuth>
        <div>Protected Content</div>
      </RequireAuth>
    );

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
    expect(screen.queryByTestId("navigate")).not.toBeInTheDocument();
  });

  test("redirects to /login with a session-expired message when the token's exp claim has already passed", () => {
    useAuth.mockReturnValue({ auth: { token: makeToken(-3600) }, setAuth: vi.fn() });

    render(
      <RequireAuth>
        <div>Protected Content</div>
      </RequireAuth>
    );

    const navigate = screen.getByTestId("navigate");
    expect(navigate).toHaveAttribute("data-to", "/login");
    expect(navigate.dataset.message).toMatch(/session has expired/i);
    expect(screen.queryByText("Protected Content")).not.toBeInTheDocument();
  });

  test("calls setAuth(null) once it notices the token is already expired", () => {
    const setAuth = vi.fn();
    useAuth.mockReturnValue({ auth: { token: makeToken(-3600) }, setAuth });

    render(
      <RequireAuth>
        <div>Protected Content</div>
      </RequireAuth>
    );

    expect(setAuth).toHaveBeenCalledWith(null);
  });

  test("treats an unparseable token as not-expired rather than crashing (getExpiryTime returns null)", () => {
    useAuth.mockReturnValue({ auth: { token: "not-a-real.jwt-token.at-all" }, setAuth: vi.fn() });

    render(
      <RequireAuth>
        <div>Protected Content</div>
      </RequireAuth>
    );

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
  });
});
