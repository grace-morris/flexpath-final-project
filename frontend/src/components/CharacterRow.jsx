import { useEffect, useState } from "react";
import { api } from "../api/apiClient";

/**
 * One row in the battle tracker's turn order for a player character in the
 * encounter: initiative, health adjustment, and a reaction toggle. Player
 * characters don't have legendary actions, so there's no counter for that here.
 *
 * @param character the character (EncounterPlayerCharacter) to display/change
 * @param encounterId the id of the encounter
 * @param token the access token
 * @param onUpdated called with the updated character after a change
 * @param onRemove called with the character's id to remove it from the encounter
 */
function CharacterRow({ character, encounterId, token, onUpdated, onRemove }) {
  const [pending, setPending] = useState(false);
  const [initiativeInput, setInitiativeInput] = useState(character.initiative);

  // Keep the input in sync if the character's initiative changes from outside
  // this row (e.g. a fresh load after adding another combatant).
  useEffect(() => {
    setInitiativeInput(character.initiative);
  }, [character.initiative]);

  const adjustHealth = async (whatChanged) => {
    setPending(true);
    try {
      const updated = await api.patch(
        `/encounters/${encounterId}/characters/${character.id}/health`,
        token,
        { currentHealth: character.currentHealth + whatChanged }
      );
      onUpdated(updated);
    } finally {
      setPending(false);
    }
  };

  const commitInitiative = async () => {
    const initiative = parseInt(initiativeInput, 10) || 0;
    if (initiative === character.initiative) {
      return;
    }
    setPending(true);
    try {
      const updated = await api.patch(
        `/encounters/${encounterId}/characters/${character.id}/initiative`,
        token,
        { initiative }
      );
      onUpdated(updated);
    } finally {
      setPending(false);
    }
  };

  const toggleReaction = async () => {
    setPending(true);
    try {
      const updated = await api.patch(
        `/encounters/${encounterId}/characters/${character.id}/reaction`,
        token,
        { usedReaction: !character.usedReaction }
      );
      onUpdated(updated);
    } finally {
      setPending(false);
    }
  };

  return (
    <div className="participant-row d-flex align-items-center gap-2 mb-1">
      <input
        className="form-control form-control-sm"
        style={{ width: "4.5rem" }}
        type="number"
        aria-label={`${character.playerCharacterName} initiative`}
        value={initiativeInput}
        disabled={pending}
        onChange={(e) => setInitiativeInput(e.target.value)}
        onBlur={commitInitiative}
      />
      <span className="participant-name">{character.playerCharacterName}</span>
      <span className="participant-ac">AC {character.armorClass}</span>
      <button disabled={pending} onClick={() => adjustHealth(-1)}>-1</button>
      <span className="participant-hp">
        {character.currentHealth} / {character.maxHealth} HP
      </span>
      <button disabled={pending} onClick={() => adjustHealth(1)}>+1</button>
      <button
        className={`btn btn-sm ${character.usedReaction ? "btn-secondary" : "btn-outline-secondary"}`}
        disabled={pending}
        onClick={toggleReaction}
      >
        Reaction {character.usedReaction ? "used" : "available"}
      </button>
      <button className="btn btn-sm btn-outline-danger ms-auto" onClick={() => onRemove(character.id)}>
        Remove
      </button>
    </div>
  );
}

export default CharacterRow;
