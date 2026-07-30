import { createContext } from 'react'
import { AuthTokens } from './api'

export type AuthContextValue = {
    accessToken: string | null
    refreshToken: string | null
    isAuthenticated: boolean
    saveTokens: (tokens: AuthTokens) => void
    logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
