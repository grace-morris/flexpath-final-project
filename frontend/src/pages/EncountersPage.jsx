import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";
import SearchSortBar from "../components/SearchSortBar";
import EncounterCard from "../components/EncounterCard";

const emptyForm = { name: "", description: "", public: false };
const PAGE_SIZE = 10;

function EncountersPage() {
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [encounters, setEncounters] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const [name, setName] = useState("");
  const [visibility, setVisibility] = useState("all");
  const [sortBy, setSortBy] = useState("name");
  const [direction, setDirection] = useState("asc");
  const [page, setPage] = useState(0);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);

  /**
   * Load the initial encounters
   */
  const loadEncounters = async () => {
    const query = new URLSearchParams({
      name,
      visibility,
      sortBy,
      direction,
      page,
      size: PAGE_SIZE,
    }).toString();
    const results = await api.get(`/encounters?${query}`, auth.token);
    setEncounters(results.items);
    setTotalCount(results.totalCount);
  };

  const lastFilters = useRef({ name, visibility, sortBy, direction });

  useEffect(() => {
    const prev = lastFilters.current;
    const filtersChanged =
      prev.name !== name ||
      prev.visibility !== visibility ||
      prev.sortBy !== sortBy ||
      prev.direction !== direction;

    if (filtersChanged) {
      lastFilters.current = { name, visibility, sortBy, direction };
      if (page !== 0) {
        setPage(0);
        return; // the resulting page 
      }
    }

    loadEncounters();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [name, visibility, sortBy, direction, page]);

  /**
   * Api call to submit form
   */
  const submitForm = async (e) => {
    e.preventDefault();
    if (editingId) {
      await api.put(`/encounters/${editingId}`, auth.token, form);
      setForm(emptyForm);
      setEditingId(null);
      loadEncounters();
    } else {
      await api.post("/encounters", auth.token, form);
      setForm(emptyForm);
      // send the user to their encounter list so they can jump right into it
      navigate("/");
    }
  };

  const startEdit = (encounter) => {
    setForm({
      name: encounter.name,
      description: encounter.description,
      public: encounter.public,
    });
    setEditingId(encounter.id);
  };

  const cancelEdit = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const deleteEncounter = async (id) => {
    await api.del(`/encounters/${id}`, auth.token);
    if (editingId === id) {
      cancelEdit();
    }
    loadEncounters();
  };

  const totalPages = Math.max(Math.ceil(totalCount / PAGE_SIZE), 1);

  return (
    <div className="container mt-3">
      <h2>Encounters</h2>

      <SearchSortBar
        name={name}
        onNameChange={setName}
        filterLabel="Visibility"
        filterValue={visibility}
        onFilterChange={setVisibility}
        filterOptions={[
          { value: "all", label: "Mine + public" },
          { value: "public", label: "Public only" },
          { value: "mine", label: "Mine only" },
        ]}
        sortOptions={[
          { value: "name", label: "name" },
          { value: "created_at", label: "date created" },
        ]}
        sortBy={sortBy}
        onSortByChange={setSortBy}
        direction={direction}
        onDirectionChange={setDirection}
      />

      {encounters.map((encounter) => (
        <EncounterCard
          key={encounter.id}
          encounter={encounter}
          currentUsername={auth.username}
          isAdmin={auth.isAdmin}
          onEdit={startEdit}
          onDelete={deleteEncounter}
        />
      ))}

      {encounters.length === 0 && <p className="text-muted">No encounters found.</p>}

      <nav className="d-flex justify-content-between align-items-center mb-4" aria-label="Encounters pagination">
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
        <h4>{editingId ? "Edit an Existing Encounter:" : "Create a New Encounter:"}</h4>

        <div className="mb-2">
          <label className="form-label" htmlFor="encounter-name">Name:</label>
          <input id="encounter-name" className="form-control" value={form.name}
                 onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </div>

        <div className="mb-2">
          <label className="form-label" htmlFor="encounter-description">Description:</label>
          <textarea id="encounter-description" className="form-control" value={form.description}
                    placeholder="Description" onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>

        <div className="form-check mb-3">
          <input id="encounter-public" className="form-check-input" type="checkbox" checked={form.public}
                 onChange={(e) => setForm({ ...form, public: e.target.checked })} />
          <label className="form-check-label" htmlFor="encounter-public">Public</label>
        </div>

        <button className="btn btn-primary" type="submit">{editingId ? "Save" : "Create"}</button>
        {editingId && (
          <button className="btn btn-link" type="button" onClick={cancelEdit}>
            Cancel
          </button>
        )}
      </form>
    </div>
  );
}

export default EncountersPage;