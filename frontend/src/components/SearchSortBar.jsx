/**
 * Reused by the Monsters, Characters, and Encounters pages — a search box,
 * an optional second filter (type/class), and a sort field + direction
 * control. onFilterChange is omitted entirely on the Encounters page,
 * which only searches by name.
 */
function SearchSortBar({
  name,
  onNameChange,
  filterLabel,
  filterValue,
  onFilterChange,
  sortOptions,
  sortBy,
  onSortByChange,
  direction,
  onDirectionChange,
}) {
  return (
    <div className="row g-2 mb-3">
      <div className="col-auto">
        <input
          className="form-control"
          placeholder="Search by name"
          value={name}
          onChange={(e) => onNameChange(e.target.value)}
        />
      </div>

      {onFilterChange && (
        <div className="col-auto">
          <input
            className="form-control"
            placeholder={filterLabel}
            value={filterValue}
            onChange={(e) => onFilterChange(e.target.value)}
          />
        </div>
      )}

      <div className="col-auto">
        <select className="form-select" value={sortBy} onChange={(e) => onSortByChange(e.target.value)}>
          {sortOptions.map((option) => (
            <option key={option.value} value={option.value}>
              Sort by {option.label}
            </option>
          ))}
        </select>
      </div>

      <div className="col-auto">
        <select className="form-select" value={direction} onChange={(e) => onDirectionChange(e.target.value)}>
          <option value="asc">Ascending</option>
          <option value="desc">Descending</option>
        </select>
      </div>
    </div>
  );
}

export default SearchSortBar;
