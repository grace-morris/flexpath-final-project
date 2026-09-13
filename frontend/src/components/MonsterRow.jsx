import { useState } from "react";
import { api } from "../api/apiClient";

/**
 * monster card in a row
 * @param monster the monster to change
 * @param encounterId the id of the encounter
 * @param token the access token
 * @param onUpdated change after updating
 * @param onRemove change after removal
 * @returns component for the row
 */
function MonsterRow({ monster, encounterId, token, onUpdated, onRemove }) {
  const [pending, setPending] = useState(false);

  const adjustHealth = async (whatChanged) => {
    setPending(true);
    try {
      const updated = await api.patch(
        `/encounters/${encounterId}/monsters/${monster.id}/health`,
        token,
        { currentHealth: monster.currentHealth + whatChanged }
      );
      onUpdated(updated);
    } finally {
      setPending(false);
    }
  };

  return (
    <div className="participant-row d-flex align-items-center gap-2 mb-1">
      <span className="participant-name">{monster.monsterName}</span>
      <span className="participant-ac">AC {monster.armorClass}</span>
      <button disabled={pending} onClick={() => adjustHealth(-1)}>-1</button>
      <span className="participant-hp">
        {monster.currentHealth} / {monster.maxHealth} HP
      </span>
      <button disabled={pending} onClick={() => adjustHealth(1)}>+1</button>
      <button className="btn btn-sm btn-outline-danger ms-auto" onClick={() => onRemove(monster.id)}>
        Remove
      </button>
    </div>
  );
}

export default MonsterRow;
