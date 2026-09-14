import { useEffect, useState } from "react";
import { api } from "../api/apiClient";

/**
 * One row in the battle tracker's turn order for a monster
 *
 * @param monster the monster to display
 * @param encounterId the id of the encounter
 * @param token the access token
 * @param onUpdated called with the updated monster after a change
 * @param onRemove called with the monster's id to remove it from the encounter
 */
function MonsterRow({ monster, encounterId, token, onUpdated, onRemove }) {
  const [pending, setPending] = useState(false);
  const [initiativeInput, setInitiativeInput] = useState(monster.initiative);

  // Keep the input in sync if the monster's initiative changes
  useEffect(() => {
    setInitiativeInput(monster.initiative);
  }, [monster.initiative]);

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

  const commitInitiative = async () => {
    const initiative = parseInt(initiativeInput, 10) || 0;
    if (initiative === monster.initiative) {
      return;
    }
    setPending(true);
    try {
      const updated = await api.patch(
        `/encounters/${encounterId}/monsters/${monster.id}/initiative`,
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
        `/encounters/${encounterId}/monsters/${monster.id}/reaction`,
        token,
        { usedReaction: !monster.usedReaction }
      );
      onUpdated(updated);
    } finally {
      setPending(false);
    }
  };

  const useLegendaryAction = async () => {
    setPending(true);
    try {
      const updated = await api.patch(`/encounters/${encounterId}/monsters/${monster.id}/legendary-action`, token);
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
        aria-label={`${monster.displayName ?? monster.monsterName} initiative`}
        value={initiativeInput}
        disabled={pending}
        onChange={(e) => setInitiativeInput(e.target.value)}
        onBlur={commitInitiative}
      />
      <span className="participant-name">{monster.displayName ?? monster.monsterName}</span>
      <span className="participant-ac">AC {monster.armorClass}</span>
      <button disabled={pending} onClick={() => adjustHealth(-1)}>-1</button>
      <span className="participant-hp">
        {monster.currentHealth} / {monster.maxHealth} HP
      </span>
      <button disabled={pending} onClick={() => adjustHealth(1)}>+1</button>
      <button
        className={`btn btn-sm ${monster.usedReaction ? "btn-secondary" : "btn-outline-secondary"}`}
        disabled={pending}
        onClick={toggleReaction}
      >
        Reaction {monster.usedReaction ? "used" : "available"}
      </button>
      {monster.maxLegendaryActions > 0 && (
        <button
          className="btn btn-sm btn-outline-warning"
          disabled={pending || monster.legendaryActionsUsed >= monster.maxLegendaryActions}
          onClick={useLegendaryAction}
        >
          Legendary {monster.legendaryActionsUsed}/{monster.maxLegendaryActions}
        </button>
      )}
      <button className="btn btn-sm btn-outline-danger ms-auto" onClick={() => onRemove(monster.id)}>
        Remove
      </button>
    </div>
  );
}

export default MonsterRow;
