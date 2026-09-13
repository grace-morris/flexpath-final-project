import { useEffect, useState } from "react";
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

function CharactersPage() {
  const { auth } = useAuth();
  const [characters, setCharacters] = useState([]);
  const [name, setName] = useState("");
  const [characterClass, setCharacterClass] = useState("");
  const [sortBy, setSortBy] = useState("name");
  const [direction, setDirection] = useState("asc");
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);

  /**
   * Load the initial characters on screen
   */
  const loadCharacters = async () => {
    const query = new URLSearchParams({ name, characterClass, sortBy, direction }).toString();
    const results = await api.get(`/characters?${query}`, auth.token);
    setCharacters(results);
  };

  useEffect(() => {
    loadCharacters();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [name, characterClass, sortBy, direction]);

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

  return (
    <div className="container mt-3">
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