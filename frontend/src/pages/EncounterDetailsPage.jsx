import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";
import MonsterRow from "../components/MonsterRow";
import CharacterRow from "../components/CharacterRow";

function EncounterDetailsPage() {
  const { id } = useParams();
  const { auth } = useAuth();
  const [encounter, setEncounter] = useState(null);
  const [monsterList, setMonsterList] = useState([]);
  const [characterList, setCharacterList] = useState([]);
  const [availableMonsters, setAvailableMonsters] = useState([]);
  const [availableCharacters, setAvailableCharacters] = useState([]);

  const loadAll = async () => {
    const [enc, monsters, characters, myMonsters, myCharacters] = await Promise.all([
      api.get(`/encounters/${id}`, auth.token),
      api.get(`/encounters/${id}/monsters`, auth.token),
      api.get(`/encounters/${id}/characters`, auth.token),
      api.get(`/monsters?name=&type=&sortBy=name&direction=asc`, auth.token),
      api.get(`/characters?name=&characterClass=&sortBy=name&direction=asc`, auth.token),
    ]);
    setEncounter(enc);
    setMonsterList(monsters);
    setCharacterList(characters);
    setAvailableMonsters(myMonsters);
    setAvailableCharacters(myCharacters);
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

  if (!encounter) {
    return null;
  }

  return (
    <div className="container mt-3">
      <h2>{encounter.name}</h2>
      <p>{encounter.description}</p>

      <h4>Monsters</h4>
      {monsterList.map((monster) => (
        <MonsterRow
          key={monster.id}
          monster={monster}
          encounterId={id}
          token={auth.token}
          onUpdated={(updated) =>
            setMonsterList((prev) => prev.map((p) => (p.id === updated.id ? updated : p)))
          }
          onRemove={removeMonster}
        />
      ))}
      <select
        className="form-select mb-3"
        defaultValue=""
        onChange={(e) => e.target.value && addMonster(e.target.value)}
      >
        <option value="" disabled>Add a monster...</option>
        {availableMonsters.map((monster) => (
          <option key={monster.id} value={monster.id}>{monster.name}</option>
        ))}
      </select>

      <h4>Player Characters</h4>
      {characterList.map((character) => (
        <CharacterRow
          key={character.id}
          character={character}
          encounterId={id}
          token={auth.token}
          onUpdated={(updated) =>
            setCharacterList((prev) => prev.map((p) => (p.id === updated.id ? updated : p)))
          }
          onRemove={removeCharacter}
        />
      ))}
      <select
        className="form-select"
        defaultValue=""
        onChange={(e) => e.target.value && addCharacter(e.target.value)}
      >
        <option value="" disabled>Add a character...</option>
        {availableCharacters.map((character) => (
          <option key={character.id} value={character.id}>{character.name}</option>
        ))}
      </select>
    </div>
  );
}

export default EncounterDetailsPage;
