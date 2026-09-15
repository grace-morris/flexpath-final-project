// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent, waitFor } from "@testing-library/react";
import LoginPage from "./LoginPage";
import { useAuth } from "../context/AuthContext";
import { useNavigate, useLocation } from "react-router-dom";

// See RequireAuth.test.jsx for why this is needed.
afterEach(cleanup);

/**
 * Unit tests for LoginPage, the combined login/register form
 */

vi.mock("../context/AuthContext", () => ({
  useAuth: vi.fn(),
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: vi.fn(),
    useLocation: vi.fn(),
  };
});

function fakeResponse({ ok = true, json = null, text = "" } = {}) {
  return { ok, json: async () => json, text: async () => text };
}

describe("LoginPage", () => {
  let setAuth;
  let navigate;

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();

    setAuth = vi.fn();
    useAuth.mockReturnValue({ setAuth });

    navigate = vi.fn();
    useNavigate.mockReturnValue(navigate);
    useLocation.mockReturnValue({ state: null });

    global.fetch = vi.fn();
  });

  test("defaults to login mode, with a username and password field", () => {
    render(<LoginPage />);

    expect(screen.getByRole("heading", { name: "Log in" })).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Username")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Log in" })).toBeInTheDocument();
  });

  test("switches to register mode and back when the toggle link is clicked", () => {
    render(<LoginPage />);

    fireEvent.click(screen.getByText("Need an account? Register"));
    expect(screen.getByRole("heading", { name: "Create an account" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Register" })).toBeInTheDocument();

    fireEvent.click(screen.getByText("Already have an account? Log in"));
    expect(screen.getByRole("heading", { name: "Log in" })).toBeInTheDocument();
  });

  test("shows the message passed in router state (e.g. RequireAuth's session-expired redirect)", () => {
    useLocation.mockReturnValue({ state: { message: "Your session has expired. Please log in again." } });

    render(<LoginPage />);

    expect(screen.getByText("Your session has expired. Please log in again.")).toBeInTheDocument();
  });

  test("falls back to a sessionExpired flag in localStorage when there's no router state message, and clears the flag", () => {
    localStorage.setItem("sessionExpired", "true");

    render(<LoginPage />);

    expect(screen.getByText("Your session expired. Please log in again.")).toBeInTheDocument();
    expect(localStorage.getItem("sessionExpired")).toBeNull();
  });

  test("router state message takes priority over the localStorage flag when both are present", () => {
    localStorage.setItem("sessionExpired", "true");
    useLocation.mockReturnValue({ state: { message: "Router message wins" } });

    render(<LoginPage />);

    expect(screen.getByText("Router message wins")).toBeInTheDocument();
    expect(screen.queryByText("Your session expired. Please log in again.")).not.toBeInTheDocument();
  });

  test("logging in successfully stores the token/username/admin flag and navigates home", async () => {
    global.fetch.mockImplementation((url) => {
      if (url === "/auth/login") {
        return Promise.resolve(fakeResponse({ json: { accessToken: { token: "abc123" } } }));
      }
      if (url === "/api/profile/roles") {
        return Promise.resolve(fakeResponse({ json: ["ADMIN"] }));
      }
      return Promise.reject(new Error(`Unexpected fetch: ${url}`));
    });

    render(<LoginPage />);
    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "grace" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "hunter2" } });
    fireEvent.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() => expect(setAuth).toHaveBeenCalledWith({ token: "abc123", username: "grace", isAdmin: true }));
    expect(navigate).toHaveBeenCalledWith("/");
  });

  test("a non-admin user's roles response results in isAdmin: false", async () => {
    global.fetch.mockImplementation((url) => {
      if (url === "/auth/login") {
        return Promise.resolve(fakeResponse({ json: { accessToken: { token: "abc123" } } }));
      }
      if (url === "/api/profile/roles") {
        return Promise.resolve(fakeResponse({ json: [] }));
      }
      return Promise.reject(new Error(`Unexpected fetch: ${url}`));
    });

    render(<LoginPage />);
    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "grace" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "hunter2" } });
    fireEvent.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() =>
      expect(setAuth).toHaveBeenCalledWith({ token: "abc123", username: "grace", isAdmin: false })
    );
  });

  test("a roles request that fails is treated as no roles, rather than crashing the login", async () => {
    global.fetch.mockImplementation((url) => {
      if (url === "/auth/login") {
        return Promise.resolve(fakeResponse({ json: { accessToken: { token: "abc123" } } }));
      }
      if (url === "/api/profile/roles") {
        return Promise.resolve(fakeResponse({ ok: false }));
      }
      return Promise.reject(new Error(`Unexpected fetch: ${url}`));
    });

    render(<LoginPage />);
    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "grace" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "hunter2" } });
    fireEvent.click(screen.getByRole("button", { name: "Log in" }));

    await waitFor(() =>
      expect(setAuth).toHaveBeenCalledWith({ token: "abc123", username: "grace", isAdmin: false })
    );
  });

  test("an incorrect login shows an error and never calls setAuth", async () => {
    global.fetch.mockResolvedValue(fakeResponse({ ok: false }));

    render(<LoginPage />);
    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "grace" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "wrong" } });
    fireEvent.click(screen.getByRole("button", { name: "Log in" }));

    expect(await screen.findByText("Incorrect username or password.")).toBeInTheDocument();
    expect(setAuth).not.toHaveBeenCalled();
    expect(navigate).not.toHaveBeenCalled();
  });

  test("registering creates the account before logging in", async () => {
    global.fetch.mockImplementation((url, options) => {
      if (url === "/api/users" && options?.method === "POST") {
        return Promise.resolve(fakeResponse({ ok: true }));
      }
      if (url === "/auth/login") {
        return Promise.resolve(fakeResponse({ json: { accessToken: { token: "new-token" } } }));
      }
      if (url === "/api/profile/roles") {
        return Promise.resolve(fakeResponse({ json: [] }));
      }
      return Promise.reject(new Error(`Unexpected fetch: ${url}`));
    });

    render(<LoginPage />);
    fireEvent.click(screen.getByText("Need an account? Register"));
    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "newuser" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "hunter2" } });
    fireEvent.click(screen.getByRole("button", { name: "Register" }));

    await waitFor(() =>
      expect(setAuth).toHaveBeenCalledWith({ token: "new-token", username: "newuser", isAdmin: false })
    );
    expect(navigate).toHaveBeenCalledWith("/");
  });

  test("a failed registration shows the server's error message and never attempts to log in", async () => {
    global.fetch.mockImplementation((url) => {
      if (url === "/api/users") {
        return Promise.resolve(fakeResponse({ ok: false, text: "Username is already taken." }));
      }
      return Promise.reject(new Error(`Unexpected fetch: ${url} - login should not have been attempted`));
    });

    render(<LoginPage />);
    fireEvent.click(screen.getByText("Need an account? Register"));
    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "grace" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "hunter2" } });
    fireEvent.click(screen.getByRole("button", { name: "Register" }));

    expect(await screen.findByText("Username is already taken.")).toBeInTheDocument();
    expect(setAuth).not.toHaveBeenCalled();
  });

  test("disables the submit button and shows a waiting label while the request is in flight", async () => {
    let resolveLogin;
    global.fetch.mockImplementation((url) => {
      if (url === "/auth/login") {
        return new Promise((resolve) => {
          resolveLogin = () => resolve(fakeResponse({ json: { accessToken: { token: "abc123" } } }));
        });
      }
      if (url === "/api/profile/roles") {
        return Promise.resolve(fakeResponse({ json: [] }));
      }
      return Promise.reject(new Error(`Unexpected fetch: ${url}`));
    });

    render(<LoginPage />);
    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "grace" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "hunter2" } });
    fireEvent.click(screen.getByRole("button", { name: "Log in" }));

    const button = await screen.findByRole("button", { name: "Please wait..." });
    expect(button).toBeDisabled();

    resolveLogin();
    await waitFor(() => expect(setAuth).toHaveBeenCalled());
  });
});
