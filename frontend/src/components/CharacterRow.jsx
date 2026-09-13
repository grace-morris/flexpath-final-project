import { useState } from "react";
import { api } from "../api/apiClient";

/**
 * 
 * @param character the character to change
 * @param encounterId the id of the encounter
 * @param token the access token
 * @param onUpdated change after updating
 * @param onRemove change after removal
 * @returns component for the row
 */
function CharacterRow({ character, encounterId, token, onUpdated, onRemove }) {
  const [pending, setPending] = useState(false);

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

  return (
    <div className="participant-row d-flex align-items-center gap-2 mb-1">
      <span className="participant-name">{character.playerCharacterName}</span>
      <span className="participant-ac">AC {character.armorClass}</span>
      <button disabled={pending} onClick={() => adjustHealth(-1)}>-1</button>
      <span className="participant-hp">
        {character.currentHealth} / {character.maxHealth} HP
      </span>
      <button disabled={pending} onClick={() => adjustHealth(1)}>+1</button>
      <button className="btn btn-sm btn-outline-danger ms-auto" onClick={() => onRemove(character.id)}>
        Remove
      </button>
    </div>
  );
}

export default CharacterRow;
