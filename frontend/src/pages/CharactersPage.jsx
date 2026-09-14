import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";
import SearchSortBar from "../components/SearchSortBar";
import CharacterCard from "../components/CharacterCard";

const emptyForm = {
  name: "",
  characterClass: "",
  level: 1,
  armorClass: 10,
  health: 10,
  description: "",
  public: false,
};
const PAGE_SIZE = 10;

function CharactersPage() {
  const { auth } = useAuth();
  const [characters, setCharacters] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const [name, setName] = useState("");
  const [characterClass, setCharacterClass] = useState("");
  const [sortBy, setSortBy] = useState("name");
  const [direction, setDirection] = useState("asc");
  const [page, setPage] = useState(0);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);

  /**
   * Load the current page of characters matching the search/filter/sort state
   */
  const loadCharacters = async () => {
    const query = new URLSearchParams({ name, characterClass, sortBy, direction, page, size: PAGE_SIZE }).toString();
    const results = await api.get(`/characters?${query}`, auth.token);
    setCharacters(results.items);
    setTotalCount(results.totalCount);
  };

  // Tracks the last-loaded filter/sort values so a change to any of them can
  // reset to page 0 without also firing an extra, wasted load at the old page.
  const lastFilters = useRef({ name, characterClass, sortBy, direction });

  useEffect(() => {
    const prev = lastFilters.current;
    const filtersChanged =
      prev.name !== name ||
      prev.characterClass !== characterClass ||
      prev.sortBy !== sortBy ||
      prev.direction !== direction;

    if (filtersChanged) {
      lastFilters.current = { name, characterClass, sortBy, direction };
      if (page !== 0) {
        setPage(0);
        return; // the resulting page change re-triggers this effect to load
      }
    }

    loadCharacters();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [name, characterClass, sortBy, direction, page]);

  const submitForm = async (e) => {
    e.preventDefault();
    if (editingId) {
      await api.put(`/characters/${editingId}`, auth.token, form);
    } else {
      await api.post("/characters", auth.token, form);
    }
    setForm(emptyForm);
    setEditingId(null);
    loadCharacters();
  };

  const startEdit = (character) => {
    setForm(character);
    setEditingId(character.id);
  };

  const deleteCharacter = async (id) => {
    await api.del(`/characters/${id}`, auth.token);
    loadCharacters();
  };

  const totalPages = Math.max(Math.ceil(totalCount / PAGE_SIZE), 1);

  return (
    <div className="container mt-3">
      <Link className="btn btn-link ps-0 mb-2" to="/">
        ← Back to Home
      </Link>

      <h2>Player Characters</h2>

      <SearchSortBar
        name={name}
        onNameChange={setName}
        filterLabel="Class"
        filterValue={characterClass}
        onFilterChange={setCharacterClass}
        sortOptions={[
          { value: "name", label: "name" },
          { value: "level", label: "level" },
          { value: "armor_class", label: "armor class" },
          { value: "health", label: "health" },
        ]}
        sortBy={sortBy}
        onSortByChange={setSortBy}
        direction={direction}
        onDirectionChange={setDirection}
      />

      {characters.map((character) => (
        <CharacterCard
          key={character.id}
          character={character}
          currentUsername={auth.username}
          isAdmin={auth.isAdmin}
          onEdit={startEdit}
          onDelete={deleteCharacter}
        />
      ))}

      {characters.length === 0 && <p className="text-muted">No characters found.</p>}

      <nav className="d-flex justify-content-between align-items-center mb-4" aria-label="Characters pagination">
        <button
          className="btn btn-outline-secondary btn-sm"
          type="button"
          disabled={page === 0}
          onClick={() => setPage((p) => Math.max(p - 1, 0))}
        >
          Previous
        </button>
        <span>
          Page {page + 1} of {totalPages} ({totalCount} total)
        </span>
        <button
          className="btn btn-outline-secondary btn-sm"
          type="button"
          disabled={page + 1 >= totalPages}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </nav>

      <form className="mt-4" onSubmit={submitForm}>
        <h4>{editingId ? "Edit an Existing Character:" : "Create a New Character:"}</h4>

        <div className="mb-2">
          <label className="form-label" htmlFor="character-name">Name:</label>
          <input id="character-name" className="form-control" value={form.name}
                 placeholder="Aragorn" onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="character-class">Class:</label>
          <input id="character-class" className="form-control" placeholder="e.g. Cleric" value={form.characterClass}
                 onChange={(e) => setForm({ ...form, characterClass: e.target.value })} required />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="character-level">Level:</label>
          <input id="character-level" className="form-control" type="number"
                 value={form.level} onChange={(e) => setForm({ ...form, level: parseInt(e.target.value) })} />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="character-ac">Armor Class:</label>
          <input id="character-ac" className="form-control" type="number"
                 value={form.armorClass}
                 onChange={(e) => setForm({ ...form, armorClass: parseInt(e.target.value) })} />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="character-hp">Max Health: </label>
          <input id="character-hp" className="form-control" type="number"
                 value={form.health}
                 onChange={(e) => setForm({ ...form, health: parseInt(e.target.value) })} />
        </div>

        <div className="mb-2">
          <textarea id="character-description" className="form-control" value={form.description}
                    placeholder="Description" onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>

        <div className="form-check mb-3">
          <input id="character-public" className="form-check-input" type="checkbox" checked={form.public}
                 onChange={(e) => setForm({ ...form, public: e.target.checked })} />
          <label className="form-check-label" htmlFor="character-public">Public</label>
        </div>

        <button className="btn btn-primary" type="submit">{editingId ? "Save" : "Create"}</button>
      </form>
    </div>
  );
}

export default CharactersPage;