import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";
import SearchSortBar from "../components/SearchSortBar";
import MonsterCard from "../components/MonsterCard";

const emptyForm = {
  name: "",
  monsterType: "",
  challengeRating: 0,
  armorClass: 10,
  health: 10,
  description: "",
  public: false,
  legendaryActions: 0,
};
const PAGE_SIZE = 10;

function MonstersPage() {
  const { auth } = useAuth();
  const [monsters, setMonsters] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const [name, setName] = useState("");
  const [type, setType] = useState("");
  const [sortBy, setSortBy] = useState("name");
  const [direction, setDirection] = useState("asc");
  const [page, setPage] = useState(0);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);

  const loadMonsters = async () => {
    const query = new URLSearchParams({ name, type, sortBy, direction, page, size: PAGE_SIZE }).toString();
    const results = await api.get(`/monsters?${query}`, auth.token);
    setMonsters(results.items);
    setTotalCount(results.totalCount);
  };

  // Tracks the last-loaded filter/sort values
  const lastFilters = useRef({ name, type, sortBy, direction });

  useEffect(() => {
    const prev = lastFilters.current;
    const filtersChanged =
      prev.name !== name || prev.type !== type || prev.sortBy !== sortBy || prev.direction !== direction;

    if (filtersChanged) {
      lastFilters.current = { name, type, sortBy, direction };
      if (page !== 0) {
        setPage(0);
        return; 
      }
    }

    loadMonsters();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [name, type, sortBy, direction, page]);

  const submitForm = async (e) => {
    e.preventDefault();
    if (editingId) {
      await api.put(`/monsters/${editingId}`, auth.token, form);
    } else {
      await api.post("/monsters", auth.token, form);
    }
    setForm(emptyForm);
    setEditingId(null);
    loadMonsters();
  };

  const startEdit = (monster) => {
    setForm(monster);
    setEditingId(monster.id);
  };

  const deleteMonster = async (id) => {
    await api.del(`/monsters/${id}`, auth.token);
    loadMonsters();
  };

  const totalPages = Math.max(Math.ceil(totalCount / PAGE_SIZE), 1);

  return (
    <div className="container mt-3">
      <Link className="btn btn-link ps-0 mb-2" to="/">
        ← Back to Home
      </Link>

      <h2>Bestiary</h2>

      <SearchSortBar
        name={name}
        onNameChange={setName}
        filterLabel="Type"
        filterValue={type}
        onFilterChange={setType}
        sortOptions={[
          { value: "name", label: "name" },
          { value: "challenge_rating", label: "challenge rating" },
          { value: "armor_class", label: "armor class" },
          { value: "health", label: "health" },
        ]}
        sortBy={sortBy}
        onSortByChange={setSortBy}
        direction={direction}
        onDirectionChange={setDirection}
      />

      {monsters.map((monster) => (
        <MonsterCard
          key={monster.id}
          monster={monster}
          currentUsername={auth.username}
          isAdmin={auth.isAdmin}
          onEdit={startEdit}
          onDelete={deleteMonster}
        />
      ))}

      {monsters.length === 0 && <p className="text-muted">No monsters found.</p>}

      <nav className="d-flex justify-content-between align-items-center mb-4" aria-label="Monsters pagination">
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
        <h4>{editingId ? "Edit the Existing Monster: " : "Create a New Monster: "}</h4>

        <div className="mb-2">
          <label className="form-label" htmlFor="monster-name">Name:</label>
          <input id="monster-name" className="form-control" value={form.name} placeholder="Phil"
                 onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="monster-type">Type:</label>
          <input id="monster-type" className="form-control" placeholder="e.g. Undead" value={form.monsterType}
                 onChange={(e) => setForm({ ...form, monsterType: e.target.value })} required />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="monster-cr">Challenge Rating:</label>
          <input id="monster-cr" className="form-control" type="number" step="0.25"
                 value={form.challengeRating}
                 onChange={(e) => setForm({ ...form, challengeRating: parseFloat(e.target.value) })} />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="monster-ac">Armor Class:</label>
          <input id="monster-ac" className="form-control" type="number"
                 value={form.armorClass}
                 onChange={(e) => setForm({ ...form, armorClass: parseInt(e.target.value) })} />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="monster-hp">Max Health</label>
          <input id="monster-hp" className="form-control" type="number"
                 value={form.health}
                 onChange={(e) => setForm({ ...form, health: parseInt(e.target.value) })} />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="monster-legendary-actions">Legendary Actions</label>
          <input id="monster-legendary-actions" className="form-control" type="number" min="0"
                 value={form.legendaryActions}
                 onChange={(e) => setForm({ ...form, legendaryActions: parseInt(e.target.value) })} />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="monster-description">Description</label>
          <textarea id="monster-description" className="form-control" value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>

        <div className="form-check mb-3">
          <input id="monster-public" className="form-check-input" type="checkbox" checked={form.public}
                 onChange={(e) => setForm({ ...form, public: e.target.checked })} />
          <label className="form-check-label" htmlFor="monster-public">Public</label>
        </div>

        <button className="btn btn-primary" type="submit">{editingId ? "Save" : "Create"}</button>
      </form>
    </div>
  );
}

export default MonstersPage;