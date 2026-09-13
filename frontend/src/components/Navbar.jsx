import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Navbar() {
  const { auth, setAuth } = useAuth();

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
      <div className="container-fluid">
        <Link className="navbar-brand" to="/">D&D Battle Organizer</Link>
        <div className="navbar-nav">
          <Link className="nav-link" to="/monsters">Monsters</Link>
          <Link className="nav-link" to="/characters">Characters</Link>
          <Link className="nav-link" to="/encounters">Encounters</Link>
        </div>
        {auth && (
          <button className="btn btn-outline-light ms-auto" onClick={() => setAuth(null)}>
            Log out ({auth.username})
          </button>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
