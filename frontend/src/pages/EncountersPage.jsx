import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";
import SearchSortBar from "../components/SearchSortBar";
import EncounterCard from "../components/EncounterCard";

const emptyForm = { name: "", description: "", public: false };

function EncountersPage() {
  const { auth } = useAuth();
  const [encounters, setEncounters] = useState([]);
  const [name, setName] = useState("");
  const [sortBy, setSortBy] = useState("name");
  const [direction, setDirection] = useState("asc");
  const [form, setForm] = useState(emptyForm);

  /**
   * Load the initial encounters
   */
  const loadEncounters = async () => {
    const query = new URLSearchParams({ name, sortBy, direction }).toString();
    const results = await api.get(`/encounters?${query}`, auth.token);
    setEncounters(results);
  };

  useEffect(() => {
    loadEncounters();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [name, sortBy, direction]);

  /**
   * Api call to submit the search
   */
  const submitForm = async (e) => {
    e.preventDefault();
    await api.post("/encounters", auth.token, form);
    setForm(emptyForm);
    loadEncounters();
  };

  const deleteEncounter = async (id) => {
    await api.del(`/encounters/${id}`, auth.token);
    loadEncounters();
  };

  return (
    <div className="container mt-3">
      <h2>Encounters</h2>

      <SearchSortBar
        name={name}
        onNameChange={setName}
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
          onDelete={deleteEncounter}
        />
      ))}

      <form className="mt-4" onSubmit={submitForm}>
        <h4>Create a New Encounter:</h4>

        <div className="mb-2">
          <label className="form-label" htmlFor="encounter-name">Name:</label>
          <input id="encounter-name" className="form-control" value={form.name}
                 onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </div>

        <div className="mb-2">
          <textarea id="encounter-description" className="form-control" value={form.description}
                    placeholder="Description" onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>

        <div className="form-check mb-3">
          <input id="encounter-public" className="form-check-input" type="checkbox" checked={form.public}
                 onChange={(e) => setForm({ ...form, public: e.target.checked })} />
          <label className="form-check-label" htmlFor="encounter-public">Public</label>
        </div>

        <button className="btn btn-primary" type="submit">Create</button>
      </form>
    </div>
  );
}

export default EncountersPage;