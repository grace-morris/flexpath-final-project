function CharacterCard({ character, currentUsername, isAdmin, onEdit, onDelete }) {
  const canModify = isAdmin || character.creatorUsername === currentUsername;

  return (
    <div className="card mb-2">
      <div className="card-body d-flex justify-content-between align-items-center">
        <div>
          <h5 className="card-title mb-0">{character.name}</h5>
          <p className="card-text mb-0">
            Level {character.level} {character.characterClass} · AC {character.armorClass} · {character.health} HP
          </p>
          {!character.public && <span className="badge bg-secondary">Private</span>}
        </div>
        {canModify && (
          <div>
            <button className="btn btn-sm btn-outline-primary me-2" onClick={() => onEdit(character)}>
              Edit
            </button>
            <button className="btn btn-sm btn-outline-danger" onClick={() => onDelete(character.id)}>
              Delete
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default CharacterCard;
