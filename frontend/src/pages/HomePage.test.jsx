// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import HomePage from "./HomePage";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";
import { useNavigate } from "react-router-dom";

afterEach(cleanup);

/**
 * Unit tests for HomePage, "Your Encounters"
 */

vi.mock("../context/AuthContext", () => ({
  useAuth: vi.fn(),
}));

vi.mock("../api/apiClient", () => ({
  api: { get: vi.fn(), del: vi.fn() },
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: vi.fn(),
  };
});

function parseQuery(pathWithQuery) {
  const [, query] = pathWithQuery.split("?");
  return Object.fromEntries(new URLSearchParams(query));
}

function mockEncounters(items, totalCount = items.length) {
  api.get.mockResolvedValue({ items, totalCount });
}

describe("HomePage", () => {
  let navigate;

  beforeEach(() => {
    vi.clearAllMocks();
    useAuth.mockReturnValue({ auth: { token: "tok", username: "grace", isAdmin: false } });
    navigate = vi.fn();
    useNavigate.mockReturnValue(navigate);
  });

  function renderPage() {
    return render(
      <MemoryRouter>
        <HomePage />
      </MemoryRouter>
    );
  }

  test("loads only the current user's own encounters, newest first", async () => {
    mockEncounters([]);

    renderPage();

    await waitFor(() => expect(api.get).toHaveBeenCalled());
    const [path, token] = api.get.mock.calls[0];
    expect(token).toBe("tok");
    expect(path.startsWith("/encounters?")).toBe(true);
    expect(parseQuery(path)).toMatchObject({
      visibility: "mine",
      sortBy: "created_at",
      direction: "desc",
      page: "0",
      size: "10",
    });
  });

  test("renders a card for each loaded encounter", async () => {
    mockEncounters([{ id: 1, name: "Goblin Ambush", description: "", public: true, creatorUsername: "grace" }]);

    renderPage();

    expect(await screen.findByText("Goblin Ambush")).toBeInTheDocument();
  });

  test("shows an empty-state message with a link to Encounters when there are none yet", async () => {
    mockEncounters([]);

    renderPage();

    expect(await screen.findByText(/haven't created any encounters yet/)).toBeInTheDocument();
  });

  test("deleting an encounter calls the API then reloads the list", async () => {
    mockEncounters([{ id: 1, name: "Goblin Ambush", description: "", public: true, creatorUsername: "grace" }]);
    api.del.mockResolvedValue(null);

    renderPage();
    await screen.findByText("Goblin Ambush");
    mockEncounters([]);

    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => expect(api.del).toHaveBeenCalledWith("/encounters/1", "tok"));
    expect(await screen.findByText(/haven't created any encounters yet/)).toBeInTheDocument();
  });

  test("editing an encounter navigates to the Encounters page, where editing actually happens", async () => {
    mockEncounters([{ id: 1, name: "Goblin Ambush", description: "", public: true, creatorUsername: "grace" }]);

    renderPage();
    await screen.findByText("Goblin Ambush");
    fireEvent.click(screen.getByText("Edit"));

    expect(navigate).toHaveBeenCalledWith("/encounters");
  });

  test("pagination controls only appear once there are more encounters than fit on one page", async () => {
    mockEncounters([{ id: 1, name: "Only One", description: "", public: true, creatorUsername: "grace" }], 1);

    renderPage();

    await screen.findByText("Only One");
    expect(screen.queryByLabelText("Home encounters pagination")).not.toBeInTheDocument();
  });

  test("Next/Previous page requests the next/previous page from the API", async () => {
    mockEncounters(
      [{ id: 1, name: "Page 1 Encounter", description: "", public: true, creatorUsername: "grace" }],
      15
    );

    renderPage();
    await screen.findByText("Page 1 Encounter");
    expect(screen.getByText("Page 1 of 2 (15 total)")).toBeInTheDocument();
    expect(screen.getByText("Previous")).toBeDisabled();

    mockEncounters([{ id: 2, name: "Page 2 Encounter", description: "", public: true, creatorUsername: "grace" }], 15);
    fireEvent.click(screen.getByText("Next"));

    await screen.findByText("Page 2 Encounter");
    const [path] = api.get.mock.calls.at(-1);
    expect(parseQuery(path).page).toBe("1");
    expect(screen.getByText("Next")).toBeDisabled();
  });
});
