function MonsterCard({ monster, currentUsername, isAdmin, onEdit, onDelete }) {
  const canModify = isAdmin || monster.creatorUsername === currentUsername;

  return (
    <div className="card mb-2">
      <div className="card-body d-flex justify-content-between align-items-center">
        <div>
          <h5 className="card-title mb-0">{monster.name}</h5>
          <p className="card-text mb-0">
            {monster.monsterType} · CR {monster.challengeRating} · AC {monster.armorClass} · {monster.health} HP
          </p>
          {!monster.public && <span className="badge bg-secondary">Private</span>}
        </div>
        {canModify && (
          <div>
            <button className="btn btn-sm btn-outline-primary me-2" onClick={() => onEdit(monster)}>
              Edit
            </button>
            <button className="btn btn-sm btn-outline-danger" onClick={() => onDelete(monster.id)}>
              Delete
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default MonsterCard;
