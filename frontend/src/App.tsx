import { Navigate, Route, Routes } from "react-router-dom"
import { Header } from "./components/header"
import { EventAttendeesPage } from "./pages/event-attendees-page"
import { EventsPage } from "./pages/events-page"

function App() {

  return (
    <div className="max-w-[1216px] mx-auto py-5 flex flex-col gap-5">
      <Header />
      <Routes>
        <Route path="/" element={<Navigate to="/events" replace />} />
        <Route path="/events" element={<EventsPage />} />
        <Route path="/events/:eventId" element={<EventAttendeesPage />} />
        <Route path="/events/:eventId/attendees" element={<EventAttendeesPage />} />
        <Route path="*" element={<Navigate to="/events" replace />} />
      </Routes>
    </div>
  )
}

export default App
