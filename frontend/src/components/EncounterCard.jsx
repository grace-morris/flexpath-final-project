import { Link } from "react-router-dom";

function EncounterCard({ encounter, currentUsername, isAdmin, onEdit, onDelete }) {
  const canModify = isAdmin || encounter.creatorUsername === currentUsername;

  return (
    <div className="card mb-2">
      <div className="card-body d-flex justify-content-between align-items-center">
        <div>
          <h5 className="card-title mb-0">
            <Link to={`/encounters/${encounter.id}`}>{encounter.name}</Link>
          </h5>
          <p className="card-text mb-0">{encounter.description}</p>
          {!encounter.public && <span className="badge bg-secondary">Private</span>}
        </div>
        {canModify && (
          <div>
            <button className="btn btn-sm btn-outline-primary me-2" onClick={() => onEdit(encounter)}>
              Edit
            </button>
            <button className="btn btn-sm btn-outline-danger" onClick={() => onDelete(encounter.id)}>
              Delete
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default EncounterCard;