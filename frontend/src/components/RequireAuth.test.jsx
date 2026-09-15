// @vitest-environment jsdom
import { beforeEach, describe, expect, test, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import RequireAuth from "./RequireAuth";
import { useAuth } from "../context/AuthContext";

/**
 * Unit tests for RequireAuth - the route guard that decides whether a
 * protected page renders at all. Two collaborators are mocked so the test
 * only exercises RequireAuth's own logic:
 *  - useAuth() is mocked directly, since RequireAuth only ever reads
 *    `auth`/`setAuth` off of it and never needs a real AuthProvider.
 *  - react-router-dom's <Navigate> is mocked to a plain div carrying its
 *    props as data attributes, so a redirect can be asserted on without
 *    needing a real Router/Routes tree.
 *
 * Requires `vitest`, `jsdom` and `@testing-library/react` as devDependencies
 * (jsdom is new - @testing-library/react and @testing-library/dom are
 * already in package.json). The `@vitest-environment jsdom` comment on the
 * first line switches just this file to a DOM environment without needing
 * a project-wide vite.config.js change, since RequireAuth renders real DOM
 * (via @testing-library/react) while apiClient.test.js does not.
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
    // Not valid base64/JSON, so getExpiryTime's try/catch swallows the error
    // and returns null - RequireAuth should fail open here, not throw.
    useAuth.mockReturnValue({ auth: { token: "not-a-real.jwt-token.at-all" }, setAuth: vi.fn() });

    render(
      <RequireAuth>
        <div>Protected Content</div>
      </RequireAuth>
    );

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
  });
});
