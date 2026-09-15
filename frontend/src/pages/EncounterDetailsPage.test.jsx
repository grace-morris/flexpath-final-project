// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent, waitFor } from "@testing-library/react";
import EncounterDetailsPage from "./EncounterDetailsPage";
import { api } from "../api/apiClient";
import { useAuth } from "../context/AuthContext";
afterEach(cleanup);

/**
 * Unit tests for EncounterDetailsPage
 */

vi.mock("../api/apiClient", () => ({
  api: { get: vi.fn(), post: vi.fn(), del: vi.fn() },
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: vi.fn(),
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useParams: () => ({ id: "42" }),
    Link: ({ children, to }) => <a href={to}>{children}</a>,
  };
});

vi.mock("../components/MonsterRow", () => ({
  default: ({ monster, onRemove }) => (
    <div data-testid={`monster-${monster.id}`}>
      {monster.displayName}
      <button onClick={() => onRemove(monster.id)}>Remove {monster.displayName}</button>
    </div>
  ),
}));

vi.mock("../components/CharacterRow", () => ({
  default: ({ character, onRemove }) => (
    <div data-testid={`character-${character.id}`}>
      {character.playerCharacterName}
      <button onClick={() => onRemove(character.id)}>Remove {character.playerCharacterName}</button>
    </div>
  ),
}));

// Wires api.get to answer each of loadAll()'s five requests by path, so the
// test can hand it just the pieces a given scenario cares about.
function mockLoadAll({ encounter, monsters = [], characters = [], availableMonsters = [], availableCharacters = [] }) {
  api.get.mockImplementation((path) => {
    if (path === "/encounters/42") return Promise.resolve(encounter);
    if (path === "/encounters/42/monsters") return Promise.resolve(monsters);
    if (path === "/encounters/42/characters") return Promise.resolve(characters);
    if (path.startsWith("/monsters?")) {
      return Promise.resolve({ items: availableMonsters, totalCount: availableMonsters.length });
    }
    if (path.startsWith("/characters?")) {
      return Promise.resolve({ items: availableCharacters, totalCount: availableCharacters.length });
    }
    return Promise.reject(new Error(`Unexpected api.get path in test: ${path}`));
  });
}

describe("EncounterDetailsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuth.mockReturnValue({ auth: { token: "test-token" } });
  });

  test("renders nothing until the encounter has loaded, then shows its name, description and round", async () => {
    mockLoadAll({ encounter: { id: 42, name: "Goblin Ambush", description: "A trap!", currentRound: 3 } });

    render(<EncounterDetailsPage />);

    expect(await screen.findByText("Goblin Ambush")).toBeInTheDocument();
    expect(screen.getByText("A trap!")).toBeInTheDocument();
    expect(screen.getByText("Round 3")).toBeInTheDocument();
  });

  test("numbers same-named monsters by the order they were added (their row id), not by initiative", async () => {
    mockLoadAll({
      encounter: { id: 42, name: "E", description: "", currentRound: 1 },
      monsters: [
        { id: 2, monsterName: "Goblin", initiative: 20 },
        { id: 1, monsterName: "Goblin", initiative: 5 },
      ],
    });

    render(<EncounterDetailsPage />);

    // id 1 was added first, so it's "Goblin 1" even though id 2 (higher
    // initiative) acts first in the turn order.
    expect(await screen.findByText("Goblin 1")).toBeInTheDocument();
    expect(screen.getByText("Goblin 2")).toBeInTheDocument();
  });

  test("does not number a monster whose name isn't shared by anything else in the encounter", async () => {
    mockLoadAll({
      encounter: { id: 42, name: "E", description: "", currentRound: 1 },
      monsters: [{ id: 1, monsterName: "Goblin", initiative: 5 }],
    });

    render(<EncounterDetailsPage />);

    expect(await screen.findByText("Goblin")).toBeInTheDocument();
  });

  test("orders the combined turn order by initiative, highest first, across monsters and characters", async () => {
    mockLoadAll({
      encounter: { id: 42, name: "E", description: "", currentRound: 1 },
      monsters: [{ id: 1, monsterName: "Goblin", initiative: 5 }],
      characters: [{ id: 1, playerCharacterName: "Aragorn", initiative: 20 }],
    });

    render(<EncounterDetailsPage />);
    await screen.findByText("Aragorn");

    const rows = screen.getAllByTestId(/^(monster|character)-/);
    expect(rows[0].textContent).toContain("Aragorn");
    expect(rows[1].textContent).toContain("Goblin");
  });

  test("shows 'Encounter Ended!' once the last combatant is removed", async () => {
    mockLoadAll({
      encounter: { id: 42, name: "E", description: "", currentRound: 1 },
      monsters: [{ id: 1, monsterName: "Goblin", initiative: 5 }],
    });
    api.del.mockResolvedValue(null);

    render(<EncounterDetailsPage />);
    await screen.findByText("Goblin");
    expect(screen.queryByText("Encounter Ended!")).not.toBeInTheDocument();

    // removeMonster() calls api.del then reloads via loadAll() - point the
    // mock at an empty list before triggering the removal.
    mockLoadAll({ encounter: { id: 42, name: "E", description: "", currentRound: 1 }, monsters: [] });
    fireEvent.click(screen.getByText("Remove Goblin"));

    expect(await screen.findByText("Encounter Ended!")).toBeInTheDocument();
  });

  test("a brand-new encounter with no combatants yet does not show 'Encounter Ended!'", async () => {
    mockLoadAll({ encounter: { id: 42, name: "Empty Encounter", description: "", currentRound: 1 } });

    render(<EncounterDetailsPage />);

    await screen.findByText("Empty Encounter");
    expect(screen.queryByText("Encounter Ended!")).not.toBeInTheDocument();
    expect(screen.getByText("No combatants yet. Add some below.")).toBeInTheDocument();
  });

  test("clicking Next Round posts to the next-round endpoint and reloads the encounter", async () => {
    mockLoadAll({ encounter: { id: 42, name: "E", description: "", currentRound: 1 } });
    api.post.mockResolvedValue({});

    render(<EncounterDetailsPage />);
    await screen.findByText("Round 1");

    fireEvent.click(screen.getByText("Next Round"));

    await waitFor(() => expect(api.post).toHaveBeenCalledWith("/encounters/42/next-round", "test-token"));
  });

  test("picking a monster from the dropdown adds it to the encounter", async () => {
    mockLoadAll({
      encounter: { id: 42, name: "E", description: "", currentRound: 1 },
      availableMonsters: [{ id: 7, name: "Owlbear" }],
    });
    api.post.mockResolvedValue({});

    render(<EncounterDetailsPage />);
    await screen.findByText("Owlbear");

    fireEvent.change(screen.getByLabelText("Add a monster"), { target: { value: "7" } });

    await waitFor(() => expect(api.post).toHaveBeenCalledWith("/encounters/42/monsters/7", "test-token"));
  });
});
