import { BrowserRouter, Routes, Route } from "react-router-dom";
import TourDetailsPage from "./pages/TourDetailsPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/tours/:tourId" element={<TourDetailsPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
