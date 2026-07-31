import { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useAuth } from '../lib/use-auth'

export function ProtectedRoute({ children }: { children: ReactNode }) {
    const location = useLocation()
    const { isAuthenticated } = useAuth()

    if (!isAuthenticated)
        return <Navigate to="/login" replace state={{ from: location.pathname }} />

    return children
}
