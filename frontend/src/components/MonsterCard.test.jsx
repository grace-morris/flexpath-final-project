// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent } from "@testing-library/react";
import MonsterCard from "./MonsterCard";

afterEach(cleanup);

/**
 * Unit tests for MonsterCard 
 */

function baseMonster(overrides = {}) {
  return {
    id: 3,
    name: "Owlbear",
    monsterType: "Monstrosity",
    challengeRating: 3,
    armorClass: 13,
    health: 59,
    legendaryActions: 0,
    public: true,
    creatorUsername: "admin",
    ...overrides,
  };
}

describe("MonsterCard", () => {
  test("shows the monster's name, type, CR, AC, and HP", () => {
    render(
      <MonsterCard monster={baseMonster()} currentUsername="user" isAdmin={false} onEdit={vi.fn()} onDelete={vi.fn()} />
    );

    expect(screen.getByText("Owlbear")).toBeInTheDocument();
    expect(screen.getByText(/Monstrosity.*CR 3.*AC 13.*59 HP/)).toBeInTheDocument();
  });

  test("mentions legendary actions only when the monster has any", () => {
    const { rerender } = render(
      <MonsterCard monster={baseMonster()} currentUsername="user" isAdmin={false} onEdit={vi.fn()} onDelete={vi.fn()} />
    );
    expect(screen.queryByText(/Legendary Actions/)).not.toBeInTheDocument();

    rerender(
      <MonsterCard
        monster={baseMonster({ legendaryActions: 2 })}
        currentUsername="user"
        isAdmin={false}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );
    expect(screen.getByText(/2 Legendary Actions/)).toBeInTheDocument();
  });

  test("shows a Private badge for a private monster, not a public one", () => {
    const { rerender } = render(
      <MonsterCard
        monster={baseMonster({ public: false })}
        currentUsername="user"
        isAdmin={false}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );
    expect(screen.getByText("Private")).toBeInTheDocument();

    rerender(
      <MonsterCard
        monster={baseMonster({ public: true })}
        currentUsername="user"
        isAdmin={false}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );
    expect(screen.queryByText("Private")).not.toBeInTheDocument();
  });

  test("shows Edit/Delete to the monster's own creator, and calls the callbacks with it", () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const monster = baseMonster({ creatorUsername: "user" });
    render(<MonsterCard monster={monster} currentUsername="user" isAdmin={false} onEdit={onEdit} onDelete={onDelete} />);

    fireEvent.click(screen.getByText("Edit"));
    expect(onEdit).toHaveBeenCalledWith(monster);

    fireEvent.click(screen.getByText("Delete"));
    expect(onDelete).toHaveBeenCalledWith(3);
  });

  test("shows Edit/Delete to an admin even for someone else's monster", () => {
    render(
      <MonsterCard
        monster={baseMonster({ creatorUsername: "someone-else" })}
        currentUsername="user"
        isAdmin={true}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );

    expect(screen.getByText("Edit")).toBeInTheDocument();
  });

  test("hides Edit/Delete from a non-admin viewing someone else's monster", () => {
    render(
      <MonsterCard
        monster={baseMonster({ creatorUsername: "someone-else" })}
        currentUsername="user"
        isAdmin={false}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );

    expect(screen.queryByText("Edit")).not.toBeInTheDocument();
    expect(screen.queryByText("Delete")).not.toBeInTheDocument();
  });
});
