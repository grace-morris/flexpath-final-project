// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import EncountersPage from "./EncountersPage";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";
import { useNavigate } from "react-router-dom";

afterEach(cleanup);

/**
 * Unit tests for EncountersPage 
 */

vi.mock("../context/AuthContext", () => ({
  useAuth: vi.fn(),
}));

vi.mock("../api/apiClient", () => ({
  api: { get: vi.fn(), post: vi.fn(), put: vi.fn(), del: vi.fn() },
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: vi.fn(),
  };
});

function mockEncounters(items, totalCount = items.length) {
  api.get.mockResolvedValue({ items, totalCount });
}

function baseEncounter(overrides = {}) {
  return {
    id: 1,
    name: "A",
    description: "",
    public: true,
    creatorUsername: "grace",
    ...overrides,
  };
}

describe("EncountersPage", () => {
  let navigate;

  beforeEach(() => {
    vi.clearAllMocks();
    useAuth.mockReturnValue({ auth: { token: "tok", username: "grace", isAdmin: false } });
    navigate = vi.fn();
    useNavigate.mockReturnValue(navigate);
    api.post.mockResolvedValue({});
    api.put.mockResolvedValue({});
    api.del.mockResolvedValue(null);
  });

  function renderPage() {
    return render(
      <MemoryRouter>
        <EncountersPage />
      </MemoryRouter>
    );
  }

  test("loads and renders a card per encounter returned by the API", async () => {
    mockEncounters([baseEncounter({ name: "Goblin Ambush", description: "A trap" })]);

    renderPage();

    expect(await screen.findByText("Goblin Ambush")).toBeInTheDocument();
    expect(api.get.mock.calls[0][1]).toBe("tok");
  });

  test("shows 'No encounters found.' when the search returns nothing", async () => {
    mockEncounters([]);

    renderPage();

    expect(await screen.findByText("No encounters found.")).toBeInTheDocument();
  });

  test("has no second filter control - just search, visibility, and sort", async () => {
    mockEncounters([]);
    renderPage();
    await screen.findByText("No encounters found.");

    // Only 3 <select>s exist (visibility, sort by, direction) - a second
    // filter like Monsters' Type or Characters' Class would add a 4th.
    expect(screen.getAllByRole("combobox")).toHaveLength(3);
    // The only text-ish inputs are the search box, the create-form's name
    // field, and its description textarea - no extra filter input either.
    expect(screen.getAllByRole("textbox")).toHaveLength(3);
  });

  test("creating a new encounter posts the form and navigates home, without reloading this page's own list", async () => {
    mockEncounters([]);
    renderPage();
    await screen.findByText("No encounters found.");

    fireEvent.change(screen.getByLabelText("Name:"), { target: { value: "New Fight" } });
    fireEvent.click(screen.getByRole("button", { name: "Create" }));

    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith("/encounters", "tok", expect.objectContaining({ name: "New Fight" }))
    );
    expect(navigate).toHaveBeenCalledWith("/");
    expect(api.get).toHaveBeenCalledTimes(1); // only the initial load - no reload on create
  });

  test("clicking Edit populates the form and switches it into edit mode", async () => {
    mockEncounters([baseEncounter({ id: 7, name: "Boss Fight", description: "The final battle" })]);
    renderPage();
    await screen.findByText("Boss Fight");

    fireEvent.click(screen.getByText("Edit"));

    expect(screen.getByText("Edit an Existing Encounter:")).toBeInTheDocument();
    expect(screen.getByLabelText("Name:")).toHaveValue("Boss Fight");
    expect(screen.getByRole("button", { name: "Save" })).toBeInTheDocument();
    expect(screen.getByText("Cancel")).toBeInTheDocument();
  });

  test("saving an edit PUTs to that encounter's id, reloads the list, and does not navigate away", async () => {
    mockEncounters([baseEncounter({ id: 7, name: "Boss Fight", description: "The final battle" })]);
    renderPage();
    await screen.findByText("Boss Fight");
    fireEvent.click(screen.getByText("Edit"));

    fireEvent.change(screen.getByLabelText("Name:"), { target: { value: "Boss Rematch" } });
    fireEvent.click(screen.getByRole("button", { name: "Save" }));

    await waitFor(() =>
      expect(api.put).toHaveBeenCalledWith("/encounters/7", "tok", expect.objectContaining({ name: "Boss Rematch" }))
    );
    expect(await screen.findByText("Create a New Encounter:")).toBeInTheDocument();
    expect(navigate).not.toHaveBeenCalled();
  });

  test("Cancel backs out of editing without saving anything", async () => {
    mockEncounters([baseEncounter({ id: 7, name: "Boss Fight", description: "The final battle" })]);
    renderPage();
    await screen.findByText("Boss Fight");
    fireEvent.click(screen.getByText("Edit"));
    fireEvent.change(screen.getByLabelText("Name:"), { target: { value: "Changed my mind" } });

    fireEvent.click(screen.getByText("Cancel"));

    expect(screen.getByText("Create a New Encounter:")).toBeInTheDocument();
    expect(screen.getByLabelText("Name:")).toHaveValue("");
    expect(api.put).not.toHaveBeenCalled();
  });

  test("deleting an encounter calls the API then reloads", async () => {
    mockEncounters([baseEncounter({ id: 3, name: "Doomed" })]);
    renderPage();
    await screen.findByText("Doomed");
    mockEncounters([]);

    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => expect(api.del).toHaveBeenCalledWith("/encounters/3", "tok"));
    expect(await screen.findByText("No encounters found.")).toBeInTheDocument();
  });

  test("deleting the encounter currently being edited also cancels the edit", async () => {
    mockEncounters([baseEncounter({ id: 3, name: "Doomed" })]);
    renderPage();
    await screen.findByText("Doomed");
    fireEvent.click(screen.getByText("Edit"));
    expect(screen.getByText("Edit an Existing Encounter:")).toBeInTheDocument();

    mockEncounters([]);
    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => expect(api.del).toHaveBeenCalledWith("/encounters/3", "tok"));
    expect(await screen.findByText("Create a New Encounter:")).toBeInTheDocument();
  });

  test("Previous is disabled on the first page and Next requests the next page", async () => {
    mockEncounters([baseEncounter({ id: 1, name: "A" })], 15);
    renderPage();
    await screen.findByText("A");

    expect(screen.getByText("Previous")).toBeDisabled();

    mockEncounters([baseEncounter({ id: 2, name: "B" })], 15);
    fireEvent.click(screen.getByText("Next"));

    await screen.findByText("B");
    expect(screen.getByText("Next")).toBeDisabled();
  });
});
