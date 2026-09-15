// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import Navbar from "./Navbar";
import { useAuth } from "../context/AuthContext";

afterEach(cleanup);

/**
 * Unit tests for Navbar. useAuth is mocked 
 */

vi.mock("../context/AuthContext", () => ({
  useAuth: vi.fn(),
}));

describe("Navbar", () => {
  test("shows a Log in link, not a logout button, when no one is logged in", () => {
    useAuth.mockReturnValue({ auth: null, setAuth: vi.fn() });

    render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );

    expect(screen.getByRole("link", { name: "Log in" })).toHaveAttribute("href", "/login");
    expect(screen.queryByText(/Log out/)).not.toBeInTheDocument();
  });

  test("shows a Log out button with the username when someone is logged in", () => {
    useAuth.mockReturnValue({ auth: { username: "grace", token: "tok" }, setAuth: vi.fn() });

    render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );

    expect(screen.getByText("Log out (grace)")).toBeInTheDocument();
    expect(screen.queryByRole("link", { name: "Log in" })).not.toBeInTheDocument();
  });

  test("clicking Log out clears auth via setAuth(null)", () => {
    const setAuth = vi.fn();
    useAuth.mockReturnValue({ auth: { username: "grace", token: "tok" }, setAuth });

    render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );
    fireEvent.click(screen.getByText("Log out (grace)"));

    expect(setAuth).toHaveBeenCalledWith(null);
  });

  test("links to the main sections of the app are always present", () => {
    useAuth.mockReturnValue({ auth: null, setAuth: vi.fn() });

    render(
      <MemoryRouter>
        <Navbar />
      </MemoryRouter>
    );

    expect(screen.getByRole("link", { name: "Monsters" })).toHaveAttribute("href", "/monsters");
    expect(screen.getByRole("link", { name: "Characters" })).toHaveAttribute("href", "/characters");
    expect(screen.getByRole("link", { name: "Encounters" })).toHaveAttribute("href", "/encounters");
    expect(screen.getByRole("link", { name: "D&D Battle Organizer" })).toHaveAttribute("href", "/");
  });
});
