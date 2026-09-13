import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
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
          <Route path="/monsters" element={<MonstersPage />} />
          <Route path="/characters" element={<CharactersPage />} />
          <Route path="/encounters" element={<EncountersPage />} />
          <Route path="/encounters/:id" element={<EncounterDetailPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
