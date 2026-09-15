// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import MonstersPage from "./MonstersPage";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";

afterEach(cleanup);

/**
 * Unit tests for MonstersPage, the search/filter/sort
 */

vi.mock("../context/AuthContext", () => ({
  useAuth: vi.fn(),
}));

vi.mock("../api/apiClient", () => ({
  api: { get: vi.fn(), post: vi.fn(), put: vi.fn(), del: vi.fn() },
}));

function mockMonsters(items, totalCount = items.length) {
  api.get.mockResolvedValue({ items, totalCount });
}

function baseMonster(overrides = {}) {
  return {
    id: 1,
    name: "A",
    monsterType: "Beast",
    challengeRating: 1,
    armorClass: 10,
    health: 5,
    legendaryActions: 0,
    description: "",
    public: true,
    creatorUsername: "grace",
    ...overrides,
  };
}

function fillRequiredFields() {
  fireEvent.change(screen.getByLabelText("Name:"), { target: { value: "Owlbear" } });
  fireEvent.change(screen.getByLabelText("Type:"), { target: { value: "Monstrosity" } });
}

describe("MonstersPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuth.mockReturnValue({ auth: { token: "tok", username: "grace", isAdmin: false } });
    api.post.mockResolvedValue({});
    api.put.mockResolvedValue({});
    api.del.mockResolvedValue(null);
  });

  function renderPage() {
    return render(
      <MemoryRouter>
        <MonstersPage />
      </MemoryRouter>
    );
  }

  test("loads and renders a card per monster returned by the API", async () => {
    mockMonsters([baseMonster({ name: "Owlbear" })]);

    renderPage();

    expect(await screen.findByText("Owlbear")).toBeInTheDocument();
    expect(api.get.mock.calls[0][1]).toBe("tok");
  });

  test("shows 'No monsters found.' when the search returns nothing", async () => {
    mockMonsters([]);

    renderPage();

    expect(await screen.findByText("No monsters found.")).toBeInTheDocument();
  });

  test("changing the name search reloads with the new filter, back on page 1, with no wasted extra request", async () => {
    mockMonsters([baseMonster({ name: "Zzz" })], 25);

    renderPage();
    await screen.findByText("Zzz");
    fireEvent.click(screen.getByText("Next"));
    await waitFor(() => expect(screen.getByText(/Page 2 of/)).toBeInTheDocument());

    fireEvent.change(screen.getByPlaceholderText("Search by name"), { target: { value: "Owl" } });

    // Two calls so far (initial load, Next) plus exactly one more for the
    // filter change - not two - is the point of the ref-tracked
    // "did the filters actually change" check in MonstersPage.
    await waitFor(() => expect(api.get).toHaveBeenCalledTimes(3));
    const lastCall = api.get.mock.calls.at(-1);
    expect(lastCall[0]).toContain("name=Owl");
    expect(lastCall[0]).toContain("page=0");
  });

  test("submitting the form with no monster being edited creates a new monster and reloads the list", async () => {
    mockMonsters([]);
    renderPage();
    await screen.findByText("No monsters found.");

    fillRequiredFields();
    fireEvent.click(screen.getByRole("button", { name: "Create" }));

    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith(
        "/monsters",
        "tok",
        expect.objectContaining({ name: "Owlbear", monsterType: "Monstrosity" })
      )
    );
    expect(api.get).toHaveBeenCalledTimes(2); // initial load + reload after create
  });

  test("clicking Edit populates the form and switches it into edit mode", async () => {
    mockMonsters([baseMonster({ id: 7, name: "Goblin Boss" })]);
    renderPage();
    await screen.findByText("Goblin Boss");

    fireEvent.click(screen.getByText("Edit"));

    expect(screen.getByText("Edit the Existing Monster:")).toBeInTheDocument();
    expect(screen.getByLabelText("Name:")).toHaveValue("Goblin Boss");
    expect(screen.getByRole("button", { name: "Save" })).toBeInTheDocument();
  });

  test("saving an edit PUTs to that monster's id and returns the form to create mode", async () => {
    mockMonsters([baseMonster({ id: 7, name: "Goblin Boss" })]);
    renderPage();
    await screen.findByText("Goblin Boss");
    fireEvent.click(screen.getByText("Edit"));

    fireEvent.change(screen.getByLabelText("Name:"), { target: { value: "Goblin Warlord" } });
    fireEvent.click(screen.getByRole("button", { name: "Save" }));

    await waitFor(() =>
      expect(api.put).toHaveBeenCalledWith("/monsters/7", "tok", expect.objectContaining({ name: "Goblin Warlord" }))
    );
    expect(await screen.findByText("Create a New Monster:")).toBeInTheDocument();
  });

  test("deleting a monster calls the API then reloads", async () => {
    mockMonsters([baseMonster({ id: 3, name: "Doomed" })]);
    renderPage();
    await screen.findByText("Doomed");
    mockMonsters([]);

    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => expect(api.del).toHaveBeenCalledWith("/monsters/3", "tok"));
    expect(await screen.findByText("No monsters found.")).toBeInTheDocument();
  });

  test("Previous is disabled on the first page and Next requests the next page", async () => {
    mockMonsters([baseMonster({ id: 1, name: "A" })], 15);
    renderPage();
    await screen.findByText("A");

    expect(screen.getByText("Previous")).toBeDisabled();

    mockMonsters([baseMonster({ id: 2, name: "B" })], 15);
    fireEvent.click(screen.getByText("Next"));

    await screen.findByText("B");
    expect(screen.getByText("Next")).toBeDisabled();
  });
});
