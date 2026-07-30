import { ReactNode, useMemo, useState } from 'react'
import { AuthContext, AuthContextValue } from './auth-context'

const ACCESS_TOKEN_KEY = 'pass-in:access-token'
const REFRESH_TOKEN_KEY = 'pass-in:refresh-token'

export function AuthProvider({ children }: { children: ReactNode }) {
    const [accessToken, setAccessToken] = useState(() => localStorage.getItem(ACCESS_TOKEN_KEY))
    const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem(REFRESH_TOKEN_KEY))

    const value = useMemo<AuthContextValue>(() => ({
        accessToken,
        refreshToken,
        isAuthenticated: Boolean(accessToken && refreshToken),
        saveTokens(tokens) {
            localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken)
            localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken)
            setAccessToken(tokens.accessToken)
            setRefreshToken(tokens.refreshToken)
        },
        logout() {
            localStorage.removeItem(ACCESS_TOKEN_KEY)
            localStorage.removeItem(REFRESH_TOKEN_KEY)
            setAccessToken(null)
            setRefreshToken(null)
        },
    }), [accessToken, refreshToken])

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}
