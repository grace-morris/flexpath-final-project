// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent, waitFor } from "@testing-library/react";
import MonsterRow from "./MonsterRow";
import { api } from "../api/apiClient";

afterEach(cleanup);

/**
 * Unit tests for MonsterRow - one row of the battle tracker's intiative 
 */

vi.mock("../api/apiClient", () => ({
  api: { patch: vi.fn() },
}));

function baseMonster(overrides = {}) {
  return {
    id: 1,
    monsterName: "Goblin",
    displayName: null,
    initiative: 10,
    armorClass: 13,
    currentHealth: 7,
    maxHealth: 10,
    usedReaction: false,
    maxLegendaryActions: 0,
    legendaryActionsUsed: 0,
    ...overrides,
  };
}

describe("MonsterRow", () => {
  let onUpdated;
  let onRemove;

  beforeEach(() => {
    vi.clearAllMocks();
    onUpdated = vi.fn();
    onRemove = vi.fn();
  });

  test("shows the monster's name, AC, and health", () => {
    render(
      <MonsterRow monster={baseMonster()} encounterId={42} token="tok" onUpdated={onUpdated} onRemove={onRemove} />
    );

    expect(screen.getByText("Goblin")).toBeInTheDocument();
    expect(screen.getByText("AC 13")).toBeInTheDocument();
    expect(screen.getByText("7 / 10 HP")).toBeInTheDocument();
  });

  test("prefers displayName over monsterName when both are present (duplicate-monster numbering)", () => {
    render(
      <MonsterRow
        monster={baseMonster({ displayName: "Goblin 1" })}
        encounterId={42}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );

    expect(screen.getByText("Goblin 1")).toBeInTheDocument();
    expect(screen.queryByText("Goblin")).not.toBeInTheDocument();
  });

  test("clicking -1 patches currentHealth down by one and reports the update", async () => {
    api.patch.mockResolvedValue({ ...baseMonster(), currentHealth: 6 });

    render(
      <MonsterRow monster={baseMonster()} encounterId={42} token="tok" onUpdated={onUpdated} onRemove={onRemove} />
    );
    fireEvent.click(screen.getByText("-1"));

    await waitFor(() =>
      expect(api.patch).toHaveBeenCalledWith("/encounters/42/monsters/1/health", "tok", { currentHealth: 6 })
    );
    expect(onUpdated).toHaveBeenCalledWith({ ...baseMonster(), currentHealth: 6 });
  });

  test("clicking +1 patches currentHealth up by one", async () => {
    api.patch.mockResolvedValue({ ...baseMonster(), currentHealth: 8 });

    render(
      <MonsterRow monster={baseMonster()} encounterId={42} token="tok" onUpdated={onUpdated} onRemove={onRemove} />
    );
    fireEvent.click(screen.getByText("+1"));

    await waitFor(() =>
      expect(api.patch).toHaveBeenCalledWith("/encounters/42/monsters/1/health", "tok", { currentHealth: 8 })
    );
  });

  test("changing and blurring the initiative field commits the new value", async () => {
    api.patch.mockResolvedValue({ ...baseMonster(), initiative: 15 });

    render(
      <MonsterRow monster={baseMonster()} encounterId={42} token="tok" onUpdated={onUpdated} onRemove={onRemove} />
    );
    const input = screen.getByLabelText("Goblin initiative");
    fireEvent.change(input, { target: { value: "15" } });
    fireEvent.blur(input);

    await waitFor(() =>
      expect(api.patch).toHaveBeenCalledWith("/encounters/42/monsters/1/initiative", "tok", { initiative: 15 })
    );
  });

  test("blurring the initiative field without changing its value does not call the API", () => {
    render(
      <MonsterRow monster={baseMonster()} encounterId={42} token="tok" onUpdated={onUpdated} onRemove={onRemove} />
    );
    const input = screen.getByLabelText("Goblin initiative");
    fireEvent.blur(input);

    expect(api.patch).not.toHaveBeenCalled();
  });

  test("an unparseable initiative value commits as 0 rather than crashing", async () => {
    api.patch.mockResolvedValue({ ...baseMonster(), initiative: 0 });

    render(
      <MonsterRow monster={baseMonster()} encounterId={42} token="tok" onUpdated={onUpdated} onRemove={onRemove} />
    );
    const input = screen.getByLabelText("Goblin initiative");
    fireEvent.change(input, { target: { value: "not a number" } });
    fireEvent.blur(input);

    await waitFor(() =>
      expect(api.patch).toHaveBeenCalledWith("/encounters/42/monsters/1/initiative", "tok", { initiative: 0 })
    );
  });

  test("toggling the reaction button flips usedReaction and relabels itself", async () => {
    api.patch.mockResolvedValue({ ...baseMonster(), usedReaction: true });

    render(
      <MonsterRow monster={baseMonster()} encounterId={42} token="tok" onUpdated={onUpdated} onRemove={onRemove} />
    );
    fireEvent.click(screen.getByText("Reaction available"));

    await waitFor(() =>
      expect(api.patch).toHaveBeenCalledWith("/encounters/42/monsters/1/reaction", "tok", { usedReaction: true })
    );
  });

  test("shows a legendary action button only for monsters that have any", () => {
    const { rerender } = render(
      <MonsterRow monster={baseMonster()} encounterId={42} token="tok" onUpdated={onUpdated} onRemove={onRemove} />
    );
    expect(screen.queryByText(/Legendary/)).not.toBeInTheDocument();

    rerender(
      <MonsterRow
        monster={baseMonster({ maxLegendaryActions: 3, legendaryActionsUsed: 1 })}
        encounterId={42}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );
    expect(screen.getByText("Legendary 1/3")).toBeInTheDocument();
  });

  test("disables the legendary action button once all uses are spent", () => {
    render(
      <MonsterRow
        monster={baseMonster({ maxLegendaryActions: 3, legendaryActionsUsed: 3 })}
        encounterId={42}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );

    expect(screen.getByText("Legendary 3/3")).toBeDisabled();
  });

  test("clicking the legendary action button patches the legendary-action endpoint", async () => {
    api.patch.mockResolvedValue(baseMonster({ maxLegendaryActions: 3, legendaryActionsUsed: 2 }));

    render(
      <MonsterRow
        monster={baseMonster({ maxLegendaryActions: 3, legendaryActionsUsed: 1 })}
        encounterId={42}
        token="tok"
        onUpdated={onUpdated}
        onRemove={onRemove}
      />
    );
    fireEvent.click(screen.getByText("Legendary 1/3"));

    await waitFor(() => expect(api.patch).toHaveBeenCalledWith("/encounters/42/monsters/1/legendary-action", "tok"));
  });

  test("clicking Remove calls onRemove with the monster's id, without touching the API", () => {
    render(
      <MonsterRow monster={baseMonster()} encounterId={42} token="tok" onUpdated={onUpdated} onRemove={onRemove} />
    );
    fireEvent.click(screen.getByText("Remove"));

    expect(onRemove).toHaveBeenCalledWith(1);
    expect(api.patch).not.toHaveBeenCalled();
  });
});
