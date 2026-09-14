import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { api } from "../api/apiClient";
import EncounterCard from "../components/EncounterCard";

const PAGE_SIZE = 10;

/**
 * Landing page: shows only the encounters this user created or edited
 */
function HomePage() {
  const { auth } = useAuth();
  const navigate = useNavigate();
  const [encounters, setEncounters] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const [page, setPage] = useState(0);

  const loadEncounters = async () => {
    const query = new URLSearchParams({
      name: "",
      visibility: "mine",
      sortBy: "created_at",
      direction: "desc",
      page,
      size: PAGE_SIZE,
    }).toString();
    const results = await api.get(`/encounters?${query}`, auth.token);
    setEncounters(results.items);
    setTotalCount(results.totalCount);
  };

  useEffect(() => {
    loadEncounters();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const deleteEncounter = async (id) => {
    await api.del(`/encounters/${id}`, auth.token);
    loadEncounters();
  };

  const totalPages = Math.max(Math.ceil(totalCount / PAGE_SIZE), 1);

  return (
    <div className="container mt-3">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Your Encounters</h2>
        <Link className="btn btn-primary" to="/encounters">
          Create New Encounter
        </Link>
      </div>

      {encounters.map((encounter) => (
        <EncounterCard
          key={encounter.id}
          encounter={encounter}
          currentUsername={auth.username}
          isAdmin={auth.isAdmin}
          onEdit={() => navigate("/encounters")}
          onDelete={deleteEncounter}
        />
      ))}

      {encounters.length === 0 && (
        <p className="text-muted">
          You haven&apos;t created any encounters yet. Head to <Link to="/encounters">Encounters</Link> to make one.
        </p>
      )}

      {totalCount > PAGE_SIZE && (
        <nav className="d-flex justify-content-between align-items-center mb-4" aria-label="Home encounters pagination">
          <button
            className="btn btn-outline-secondary btn-sm"
            type="button"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(p - 1, 0))}
          >
            Previous
          </button>
          <span>
            Page {page + 1} of {totalPages} ({totalCount} total)
          </span>
          <button
            className="btn btn-outline-secondary btn-sm"
            type="button"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </button>
        </nav>
      )}
    </div>
  );
}

export default HomePage;
