// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import CharactersPage from "./CharactersPage";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";

afterEach(cleanup);

/**
 * Unit tests for CharactersPage 
 */

vi.mock("../context/AuthContext", () => ({
  useAuth: vi.fn(),
}));

vi.mock("../api/apiClient", () => ({
  api: { get: vi.fn(), post: vi.fn(), put: vi.fn(), del: vi.fn() },
}));

function mockCharacters(items, totalCount = items.length) {
  api.get.mockResolvedValue({ items, totalCount });
}

function baseCharacter(overrides = {}) {
  return {
    id: 1,
    name: "A",
    level: 1,
    characterClass: "Fighter",
    armorClass: 10,
    health: 5,
    description: "",
    public: true,
    creatorUsername: "grace",
    ...overrides,
  };
}

function fillRequiredFields() {
  fireEvent.change(screen.getByLabelText("Name:"), { target: { value: "Aragorn" } });
  fireEvent.change(screen.getByLabelText("Class:"), { target: { value: "Ranger" } });
}

describe("CharactersPage", () => {
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
        <CharactersPage />
      </MemoryRouter>
    );
  }

  test("loads and renders a card per character returned by the API", async () => {
    mockCharacters([baseCharacter({ name: "Aragorn" })]);

    renderPage();

    expect(await screen.findByText("Aragorn")).toBeInTheDocument();
    expect(api.get.mock.calls[0][1]).toBe("tok");
  });

  test("shows 'No characters found.' when the search returns nothing", async () => {
    mockCharacters([]);

    renderPage();

    expect(await screen.findByText("No characters found.")).toBeInTheDocument();
  });

  test("changing the class filter reloads with the new filter, back on page 1, with no wasted extra request", async () => {
    mockCharacters([baseCharacter({ name: "Z", characterClass: "Bard" })], 25);

    renderPage();
    await screen.findByText("Z");
    fireEvent.click(screen.getByText("Next"));
    await waitFor(() => expect(screen.getByText(/Page 2 of/)).toBeInTheDocument());

    fireEvent.change(screen.getByPlaceholderText("Class"), { target: { value: "Ranger" } });

    await waitFor(() => expect(api.get).toHaveBeenCalledTimes(3));
    const lastCall = api.get.mock.calls.at(-1);
    expect(lastCall[0]).toContain("characterClass=Ranger");
    expect(lastCall[0]).toContain("page=0");
  });

  test("submitting the form with no character being edited creates a new character and reloads the list", async () => {
    mockCharacters([]);
    renderPage();
    await screen.findByText("No characters found.");

    fillRequiredFields();
    fireEvent.click(screen.getByRole("button", { name: "Create" }));

    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith(
        "/characters",
        "tok",
        expect.objectContaining({ name: "Aragorn", characterClass: "Ranger" })
      )
    );
    expect(api.get).toHaveBeenCalledTimes(2);
  });

  test("clicking Edit populates the form and switches it into edit mode", async () => {
    mockCharacters([baseCharacter({ id: 7, name: "Legolas", characterClass: "Ranger" })]);
    renderPage();
    await screen.findByText("Legolas");

    fireEvent.click(screen.getByText("Edit"));

    expect(screen.getByText("Edit an Existing Character:")).toBeInTheDocument();
    expect(screen.getByLabelText("Name:")).toHaveValue("Legolas");
    expect(screen.getByRole("button", { name: "Save" })).toBeInTheDocument();
  });

  test("saving an edit PUTs to that character's id and returns the form to create mode", async () => {
    mockCharacters([baseCharacter({ id: 7, name: "Legolas", characterClass: "Ranger" })]);
    renderPage();
    await screen.findByText("Legolas");
    fireEvent.click(screen.getByText("Edit"));

    fireEvent.change(screen.getByLabelText("Name:"), { target: { value: "Legolas Greenleaf" } });
    fireEvent.click(screen.getByRole("button", { name: "Save" }));

    await waitFor(() =>
      expect(api.put).toHaveBeenCalledWith(
        "/characters/7",
        "tok",
        expect.objectContaining({ name: "Legolas Greenleaf" })
      )
    );
    expect(await screen.findByText("Create a New Character:")).toBeInTheDocument();
  });

  test("deleting a character calls the API then reloads", async () => {
    mockCharacters([baseCharacter({ id: 3, name: "Doomed" })]);
    renderPage();
    await screen.findByText("Doomed");
    mockCharacters([]);

    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => expect(api.del).toHaveBeenCalledWith("/characters/3", "tok"));
    expect(await screen.findByText("No characters found.")).toBeInTheDocument();
  });

  test("Previous is disabled on the first page and Next requests the next page", async () => {
    mockCharacters([baseCharacter({ id: 1, name: "A" })], 15);
    renderPage();
    await screen.findByText("A");

    expect(screen.getByText("Previous")).toBeDisabled();

    mockCharacters([baseCharacter({ id: 2, name: "B" })], 15);
    fireEvent.click(screen.getByText("Next"));

    await screen.findByText("B");
    expect(screen.getByText("Next")).toBeDisabled();
  });
});
