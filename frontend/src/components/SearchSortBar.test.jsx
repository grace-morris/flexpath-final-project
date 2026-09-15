// @vitest-environment jsdom
import "@testing-library/jest-dom/vitest";
import { describe, expect, test, vi, afterEach } from "vitest";
import { cleanup, render, screen, fireEvent } from "@testing-library/react";
import SearchSortBar from "./SearchSortBar";

afterEach(cleanup);

/**
 * Unit tests for SearchSortBar, the sort control and search
 */

const sortOptions = [
  { value: "name", label: "Name" },
  { value: "challengeRating", label: "Challenge Rating" },
];

function baseProps(overrides = {}) {
  return {
    name: "",
    onNameChange: vi.fn(),
    sortOptions,
    sortBy: "name",
    onSortByChange: vi.fn(),
    direction: "asc",
    onDirectionChange: vi.fn(),
    ...overrides,
  };
}

describe("SearchSortBar", () => {
  test("typing in the search box calls onNameChange with the new value", () => {
    const props = baseProps();
    render(<SearchSortBar {...props} />);

    fireEvent.change(screen.getByPlaceholderText("Search by name"), { target: { value: "goblin" } });

    expect(props.onNameChange).toHaveBeenCalledWith("goblin");
  });

  test("with no filter or visibility callbacks, only the search box's text input renders", () => {
    render(<SearchSortBar {...baseProps()} />);

    expect(screen.queryByLabelText("Visibility")).not.toBeInTheDocument();
    expect(screen.getAllByRole("textbox")).toHaveLength(1);
  });

  test("a filter with filterOptions renders as a select, not a text input", () => {
    const filterOptions = [
      { value: "Dragon", label: "Dragon" },
      { value: "Beast", label: "Beast" },
    ];
    const props = baseProps({
      filterLabel: "Type",
      filterValue: "Dragon",
      onFilterChange: vi.fn(),
      filterOptions,
    });
    render(<SearchSortBar {...props} />);

    const select = screen.getByLabelText("Type");
    expect(select.tagName).toBe("SELECT");
    fireEvent.change(select, { target: { value: "Beast" } });

    expect(props.onFilterChange).toHaveBeenCalledWith("Beast");
  });

  test("a filter with no filterOptions renders as a free-text input", () => {
    const props = baseProps({ filterLabel: "Class", filterValue: "", onFilterChange: vi.fn() });
    render(<SearchSortBar {...props} />);

    const input = screen.getByPlaceholderText("Class");
    fireEvent.change(input, { target: { value: "Wizard" } });

    expect(props.onFilterChange).toHaveBeenCalledWith("Wizard");
  });

  test("the visibility filter, when present, offers all/public/mine and calls onVisibilityChange", () => {
    const props = baseProps({ visibility: "all", onVisibilityChange: vi.fn() });
    render(<SearchSortBar {...props} />);

    const select = screen.getByLabelText("Visibility");
    expect(screen.getByText("Mine + public")).toBeInTheDocument();
    expect(screen.getByText("Public only")).toBeInTheDocument();
    expect(screen.getByText("Mine only")).toBeInTheDocument();

    fireEvent.change(select, { target: { value: "mine" } });
    expect(props.onVisibilityChange).toHaveBeenCalledWith("mine");
  });

  test("sort options render as 'Sort by <label>' and changing the select calls onSortByChange", () => {
    const props = baseProps();
    render(<SearchSortBar {...props} />);

    expect(screen.getByText("Sort by Name")).toBeInTheDocument();
    expect(screen.getByText("Sort by Challenge Rating")).toBeInTheDocument();

    fireEvent.change(screen.getByDisplayValue("Sort by Name"), { target: { value: "challengeRating" } });
    expect(props.onSortByChange).toHaveBeenCalledWith("challengeRating");
  });

  test("changing the direction select calls onDirectionChange", () => {
    const props = baseProps();
    render(<SearchSortBar {...props} />);

    fireEvent.change(screen.getByDisplayValue("Ascending"), { target: { value: "desc" } });

    expect(props.onDirectionChange).toHaveBeenCalledWith("desc");
  });
});
