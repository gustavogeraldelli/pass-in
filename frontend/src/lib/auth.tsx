import { ReactNode, useCallback, useMemo, useState } from 'react'
import { ApiRequestError, AuthTokens, refreshOrganizer } from './api'
import { AuthContext, AuthContextValue } from './auth-context'

const ACCESS_TOKEN_KEY = 'pass-in:access-token'
const REFRESH_TOKEN_KEY = 'pass-in:refresh-token'

export function AuthProvider({ children }: { children: ReactNode }) {
    const [accessToken, setAccessToken] = useState(() => localStorage.getItem(ACCESS_TOKEN_KEY))
    const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem(REFRESH_TOKEN_KEY))

    const saveTokens = useCallback((tokens: AuthTokens) => {
        localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken)
        localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken)
        setAccessToken(tokens.accessToken)
        setRefreshToken(tokens.refreshToken)
    }, [])

    const logout = useCallback(() => {
        localStorage.removeItem(ACCESS_TOKEN_KEY)
        localStorage.removeItem(REFRESH_TOKEN_KEY)
        setAccessToken(null)
        setRefreshToken(null)
    }, [])

    const authenticatedRequest = useCallback(async <T,>(request: (accessToken: string) => Promise<T>) => {
        if (!accessToken || !refreshToken)
            throw new Error('Session expired. Sign in again.')

        try {
            return await request(accessToken)
        }
        catch (error) {
            if (!(error instanceof ApiRequestError) || error.status !== 401)
                throw error

            try {
                const tokens = await refreshOrganizer(refreshToken)
                saveTokens(tokens)
                return await request(tokens.accessToken)
            }
            catch (refreshError) {
                logout()
                throw refreshError
            }
        }
    }, [accessToken, logout, refreshToken, saveTokens])

    const value = useMemo<AuthContextValue>(() => ({
        accessToken,
        refreshToken,
        isAuthenticated: Boolean(accessToken && refreshToken),
        saveTokens,
        authenticatedRequest,
        logout,
    }), [accessToken, authenticatedRequest, logout, refreshToken, saveTokens])

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}
