import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import RequireAuth from "./components/RequireAuth";
import LoginPage from "./pages/LoginPage";
import MonstersPage from "./pages/MonstersPage";
import CharactersPage from "./pages/CharactersPage";
import EncountersPage from "./pages/EncountersPage";
import EncounterDetailPage from "./pages/EncounterDetailsPage";

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/monsters"
            element={
              <RequireAuth>
                <MonstersPage />
              </RequireAuth>
            }
          />
          <Route
            path="/characters"
            element={
              <RequireAuth>
                <CharactersPage />
              </RequireAuth>
            }
          />
          <Route
            path="/encounters"
            element={
              <RequireAuth>
                <EncountersPage />
              </RequireAuth>
            }
          />
          <Route
            path="/encounters/:id"
            element={
              <RequireAuth>
                <EncounterDetailPage />
              </RequireAuth>
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;