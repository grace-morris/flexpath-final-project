// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent, waitFor } from "@testing-library/react";
import CharacterRow from "./CharacterRow";
import { api } from "../api/apiClient";

afterEach(cleanup);

/**
 * Unit tests for CharacterRow 
 */

vi.mock("../api/apiClient", () => ({
  api: { patch: vi.fn() },
}));

function baseCharacter(overrides = {}) {
  return {
    id: 5,
    playerCharacterName: "Aragorn",
    initiative: 18,
    armorClass: 16,
    currentHealth: 30,
    maxHealth: 40,
    usedReaction: false,
    ...overrides,
  };
}

describe("CharacterRow", () => {
  let onUpdated;
  let onRemove;

  beforeEach(() => {
    vi.clearAllMocks();
    onUpdated = vi.fn();
    onRemove = vi.fn();
  });

  test("shows the character's name, AC, and health", () => {
    render(
      <CharacterRow
        character={baseCharacter()}
        encounterId={7}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );

    expect(screen.getByText("Aragorn")).toBeInTheDocument();
    expect(screen.getByText("AC 16")).toBeInTheDocument();
    expect(screen.getByText("30 / 40 HP")).toBeInTheDocument();
  });

  test("clicking -1/+1 patches currentHealth accordingly", async () => {
    api.patch.mockResolvedValue({ ...baseCharacter(), currentHealth: 29 });

    render(
      <CharacterRow
        character={baseCharacter()}
        encounterId={7}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );
    fireEvent.click(screen.getByText("-1"));

    await waitFor(() =>
      expect(api.patch).toHaveBeenCalledWith("/encounters/7/characters/5/health", "tok", { currentHealth: 29 })
    );
    expect(onUpdated).toHaveBeenCalledWith({ ...baseCharacter(), currentHealth: 29 });
  });

  test("changing and blurring the initiative field commits the new value", async () => {
    api.patch.mockResolvedValue({ ...baseCharacter(), initiative: 12 });

    render(
      <CharacterRow
        character={baseCharacter()}
        encounterId={7}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );
    const input = screen.getByLabelText("Aragorn initiative");
    fireEvent.change(input, { target: { value: "12" } });
    fireEvent.blur(input);

    await waitFor(() =>
      expect(api.patch).toHaveBeenCalledWith("/encounters/7/characters/5/initiative", "tok", { initiative: 12 })
    );
  });

  test("blurring the initiative field without changing its value does not call the API", () => {
    render(
      <CharacterRow
        character={baseCharacter()}
        encounterId={7}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );
    fireEvent.blur(screen.getByLabelText("Aragorn initiative"));

    expect(api.patch).not.toHaveBeenCalled();
  });

  test("toggling the reaction button flips usedReaction", async () => {
    api.patch.mockResolvedValue({ ...baseCharacter(), usedReaction: true });

    render(
      <CharacterRow
        character={baseCharacter()}
        encounterId={7}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );
    fireEvent.click(screen.getByText("Reaction available"));

    await waitFor(() =>
      expect(api.patch).toHaveBeenCalledWith("/encounters/7/characters/5/reaction", "tok", { usedReaction: true })
    );
  });

  test("there is no legendary action control - player characters don't have any", () => {
    render(
      <CharacterRow
        character={baseCharacter()}
        encounterId={7}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );

    expect(screen.queryByText(/Legendary/)).not.toBeInTheDocument();
  });

  test("clicking Remove calls onRemove with the character's id", () => {
    render(
      <CharacterRow
        character={baseCharacter()}
        encounterId={7}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );
    fireEvent.click(screen.getByText("Remove"));

    expect(onRemove).toHaveBeenCalledWith(5);
  });
});
