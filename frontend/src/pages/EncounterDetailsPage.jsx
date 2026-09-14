import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";
import MonsterRow from "../components/MonsterRow";
import CharacterRow from "../components/CharacterRow";

// The backend clamps search page size to 100 max, so this is the most monsters/
// characters the "add to encounter" dropdowns below can ever show at once.
const MAX_AVAILABLE_SIZE = 100;

function EncounterDetailsPage() {
  const { id } = useParams();
  const { auth } = useAuth();
  const [encounter, setEncounter] = useState(null);
  const [monsterList, setMonsterList] = useState([]);
  const [characterList, setCharacterList] = useState([]);
  const [availableMonsters, setAvailableMonsters] = useState([]);
  const [availableCharacters, setAvailableCharacters] = useState([]);
  const [roundPending, setRoundPending] = useState(false);

  const loadAll = async () => {
    const [enc, monsters, characters, myMonsters, myCharacters] = await Promise.all([
      api.get(`/encounters/${id}`, auth.token),
      api.get(`/encounters/${id}/monsters`, auth.token),
      api.get(`/encounters/${id}/characters`, auth.token),
      api.get(`/monsters?name=&type=&sortBy=name&direction=asc&page=0&size=${MAX_AVAILABLE_SIZE}`, auth.token),
      api.get(`/characters?name=&characterClass=&sortBy=name&direction=asc&page=0&size=${MAX_AVAILABLE_SIZE}`, auth.token),
    ]);
    setEncounter(enc);
    setMonsterList(monsters);
    setCharacterList(characters);
    setAvailableMonsters(myMonsters.items);
    setAvailableCharacters(myCharacters.items);
  };

  useEffect(() => {
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  /**
   * Api calls to add monsters/characters, remove monsters/characters
   */
  const addMonster = async (monsterId) => {
    await api.post(`/encounters/${id}/monsters/${monsterId}`, auth.token);
    loadAll();
  };

  const addCharacter = async (playerCharacterId) => {
    await api.post(`/encounters/${id}/characters/${playerCharacterId}`, auth.token);
    loadAll();
  };

  const removeMonster = async (monsterId) => {
    await api.del(`/encounters/${id}/monsters/${monsterId}`, auth.token);
    loadAll();
  };

  const removeCharacter = async (characterId) => {
    await api.del(`/encounters/${id}/characters/${characterId}`, auth.token);
    loadAll();
  };

  /**
   * Advances the encounter to the next round: increments the round counter
   * and resets everyone's reaction/legendary-action usage on the backend,
   * then reloads so the tracker reflects the reset.
   */
  const nextRound = async () => {
    setRoundPending(true);
    try {
      await api.post(`/encounters/${id}/next-round`, auth.token);
      await loadAll();
    } finally {
      setRoundPending(false);
    }
  };

  if (!encounter) {
    return null;
  }

  // Combine monsters and characters into one initiative-ordered turn order,
  // highest initiative first.
  const turnOrder = [
    ...monsterList.map((monster) => ({
      key: `monster-${monster.id}`,
      type: "monster",
      initiative: monster.initiative,
      monster,
    })),
    ...characterList.map((character) => ({
      key: `character-${character.id}`,
      type: "character",
      initiative: character.initiative,
      character,
    })),
  ].sort((a, b) => b.initiative - a.initiative);

  return (
    <div className="container mt-3">
      <h2>{encounter.name}</h2>
      <p>{encounter.description}</p>

      <div className="d-flex align-items-center gap-3 mb-3">
        <h4 className="mb-0">Round {encounter.currentRound}</h4>
        <button className="btn btn-primary btn-sm" disabled={roundPending} onClick={nextRound}>
          Next Round
        </button>
      </div>

      <h4>Turn Order</h4>
      {turnOrder.map((entry) =>
        entry.type === "monster" ? (
          <MonsterRow
            key={entry.key}
            monster={entry.monster}
            encounterId={id}
            token={auth.token}
            onUpdated={(updated) =>
              setMonsterList((prev) => prev.map((p) => (p.id === updated.id ? updated : p)))
            }
            onRemove={removeMonster}
          />
        ) : (
          <CharacterRow
            key={entry.key}
            character={entry.character}
            encounterId={id}
            token={auth.token}
            onUpdated={(updated) =>
              setCharacterList((prev) => prev.map((p) => (p.id === updated.id ? updated : p)))
            }
            onRemove={removeCharacter}
          />
        )
      )}
      {turnOrder.length === 0 && <p className="text-muted">No combatants yet. Add some below.</p>}

      <div className="row mt-3">
        <div className="col-sm-6">
          <label className="form-label" htmlFor="add-monster-select">Add a monster</label>
          <select
            id="add-monster-select"
            className="form-select mb-3"
            defaultValue=""
            onChange={(e) => e.target.value && addMonster(e.target.value)}
          >
            <option value="" disabled>Add a monster...</option>
            {availableMonsters.map((monster) => (
              <option key={monster.id} value={monster.id}>{monster.name}</option>
            ))}
          </select>
        </div>
        <div className="col-sm-6">
          <label className="form-label" htmlFor="add-character-select">Add a character</label>
          <select
            id="add-character-select"
            className="form-select mb-3"
            defaultValue=""
            onChange={(e) => e.target.value && addCharacter(e.target.value)}
          >
            <option value="" disabled>Add a character...</option>
            {availableCharacters.map((character) => (
              <option key={character.id} value={character.id}>{character.name}</option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
}

export default EncounterDetailsPage;
