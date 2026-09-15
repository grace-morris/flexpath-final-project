// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent } from "@testing-library/react";
import CharacterCard from "./CharacterCard";

afterEach(cleanup);

/**
 * Unit tests for CharacterCard - see MonsterCard.test.jsx for the sibling
 * component this mirrors; same canModify logic, no legendary-actions line.
 */

function baseCharacter(overrides = {}) {
  return {
    id: 9,
    name: "Aragorn",
    level: 5,
    characterClass: "Ranger",
    armorClass: 16,
    health: 44,
    public: true,
    creatorUsername: "admin",
    ...overrides,
  };
}

describe("CharacterCard", () => {
  test("shows the character's name, level, class, AC, and HP", () => {
    render(
      <CharacterCard
        character={baseCharacter()}
        currentUsername="user"
        isAdmin={false}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );

    expect(screen.getByText("Aragorn")).toBeInTheDocument();
    expect(screen.getByText(/Level 5 Ranger.*AC 16.*44 HP/)).toBeInTheDocument();
  });

  test("shows a Private badge only for a private character", () => {
    const { rerender } = render(
      <CharacterCard
        character={baseCharacter({ public: false })}
        currentUsername="user"
        isAdmin={false}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );
    expect(screen.getByText("Private")).toBeInTheDocument();

    rerender(
      <CharacterCard
        character={baseCharacter({ public: true })}
        currentUsername="user"
        isAdmin={false}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );
    expect(screen.queryByText("Private")).not.toBeInTheDocument();
  });

  test("shows Edit/Delete to the character's own creator, and calls the callbacks with it", () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const character = baseCharacter({ creatorUsername: "user" });
    render(
      <CharacterCard character={character} currentUsername="user" isAdmin={false} onEdit={onEdit} onDelete={onDelete} />
    );

    fireEvent.click(screen.getByText("Edit"));
    expect(onEdit).toHaveBeenCalledWith(character);

    fireEvent.click(screen.getByText("Delete"));
    expect(onDelete).toHaveBeenCalledWith(9);
  });

  test("hides Edit/Delete from a non-admin viewing someone else's character", () => {
    render(
      <CharacterCard
        character={baseCharacter({ creatorUsername: "someone-else" })}
        currentUsername="user"
        isAdmin={false}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );

    expect(screen.queryByText("Edit")).not.toBeInTheDocument();
  });

  test("shows Edit/Delete to an admin regardless of who created the character", () => {
    render(
      <CharacterCard
        character={baseCharacter({ creatorUsername: "someone-else" })}
        currentUsername="user"
        isAdmin={true}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
      />
    );

    expect(screen.getByText("Edit")).toBeInTheDocument();
  });
});
