// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { afterEach, describe, expect, test, vi } from "vitest";
import { cleanup, render, screen, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import EncounterCard from "./EncounterCard";

afterEach(cleanup);

/**
 * Unit tests for EncounterCard 
 */

function baseEncounter(overrides = {}) {
  return {
    id: 11,
    name: "Goblin Ambush",
    description: "A trap in the forest.",
    public: true,
    creatorUsername: "admin",
    ...overrides,
  };
}

function renderCard(props) {
  return render(
    <MemoryRouter>
      <EncounterCard {...props} />
    </MemoryRouter>
  );
}

describe("EncounterCard", () => {
  test("shows the encounter's name as a link to its details page, and its description", () => {
    renderCard({
      encounter: baseEncounter(),
      currentUsername: "user",
      isAdmin: false,
      onEdit: vi.fn(),
      onDelete: vi.fn(),
    });

    expect(screen.getByRole("link", { name: "Goblin Ambush" })).toHaveAttribute("href", "/encounters/11");
    expect(screen.getByText("A trap in the forest.")).toBeInTheDocument();
  });

  test("shows a Private badge only for a private encounter", () => {
    const { rerender } = renderCard({
      encounter: baseEncounter({ public: false }),
      currentUsername: "user",
      isAdmin: false,
      onEdit: vi.fn(),
      onDelete: vi.fn(),
    });
    expect(screen.getByText("Private")).toBeInTheDocument();

    rerender(
      <MemoryRouter>
        <EncounterCard
          encounter={baseEncounter({ public: true })}
          currentUsername="user"
          isAdmin={false}
          onEdit={vi.fn()}
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );
    expect(screen.queryByText("Private")).not.toBeInTheDocument();
  });

  test("shows Edit/Delete to the encounter's own creator, and calls the callbacks with it", () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const encounter = baseEncounter({ creatorUsername: "user" });
    renderCard({ encounter, currentUsername: "user", isAdmin: false, onEdit, onDelete });

    fireEvent.click(screen.getByText("Edit"));
    expect(onEdit).toHaveBeenCalledWith(encounter);

    fireEvent.click(screen.getByText("Delete"));
    expect(onDelete).toHaveBeenCalledWith(11);
  });

  test("hides Edit/Delete from a non-admin viewing someone else's encounter", () => {
    renderCard({
      encounter: baseEncounter({ creatorUsername: "someone-else" }),
      currentUsername: "user",
      isAdmin: false,
      onEdit: vi.fn(),
      onDelete: vi.fn(),
    });

    expect(screen.queryByText("Edit")).not.toBeInTheDocument();
  });

  test("shows Edit/Delete to an admin regardless of who created the encounter", () => {
    renderCard({
      encounter: baseEncounter({ creatorUsername: "someone-else" }),
      currentUsername: "user",
      isAdmin: true,
      onEdit: vi.fn(),
      onDelete: vi.fn(),
    });

    expect(screen.getByText("Edit")).toBeInTheDocument();
  });
});
