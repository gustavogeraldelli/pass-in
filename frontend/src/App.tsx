import { Navigate, Route, Routes } from 'react-router'
import { Header } from './components/header'
import { ProtectedRoute } from './components/protected-route'
import { AttendeeBadgePage } from './pages/attendee-badge-page'
import { CheckInPage } from './pages/check-in-page'
import { EventAttendeesPage } from './pages/event-attendees-page'
import { EventPage } from './pages/event-page'
import { EventsPage } from './pages/events-page'
import { LoginPage } from './pages/login-page'
import { RegisterPage } from './pages/register-page'

function App() {

  return (
    <div className="max-w-[1216px] mx-auto py-5 flex flex-col gap-5">
      <Header />
      <Routes>
        <Route path="/" element={<Navigate to="/events" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/events"
          element={(
            <ProtectedRoute>
              <EventsPage />
            </ProtectedRoute>
          )}
        />
        <Route path="/events/:eventId" element={<EventPage />} />
        <Route
          path="/events/:eventId/attendees"
          element={(
            <ProtectedRoute>
              <EventAttendeesPage />
            </ProtectedRoute>
          )}
        />
        <Route path="/attendees/:attendeeId/badge" element={<AttendeeBadgePage />} />
        <Route path="/check-ins/:token" element={<CheckInPage />} />
        <Route path="*" element={<Navigate to="/events" replace />} />
      </Routes>
    </div>
  )
}

export default App
