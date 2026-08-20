import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { AuthContext } from './AuthContext'
import type { AuthStatus, CurrentUser, LoginRequest, RegisterRequest } from './authTypes'
import { clearLocalSession, loginSession, registerUser, restoreSession } from './authService'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [status, setStatus] = useState<AuthStatus>('loading')
  const [error, setError] = useState<Error | null>(null)
  const [bootstrapAttempt, setBootstrapAttempt] = useState(0)

  useEffect(() => {
    let active = true
    setStatus('loading')
    setError(null)

    restoreSession()
      .then((restoredUser) => {
        if (!active) return
        setUser(restoredUser)
        setStatus(restoredUser ? 'authenticated' : 'unauthenticated')
      })
      .catch((cause: unknown) => {
        if (!active) return
        clearLocalSession()
        setUser(null)
        setError(cause instanceof Error ? cause : new Error('Session restoration failed'))
        setStatus('error')
      })

    return () => {
      active = false
    }
  }, [bootstrapAttempt])

  const retryBootstrap = useCallback(() => {
    setBootstrapAttempt((attempt) => attempt + 1)
  }, [])

  const login = useCallback(async (request: LoginRequest) => {
    setStatus('loading')
    setError(null)
    try {
      const authenticatedUser = await loginSession(request)
      setUser(authenticatedUser)
      setStatus('authenticated')
    } catch (cause) {
      setUser(null)
      setStatus('unauthenticated')
      throw cause
    }
  }, [])

  const register = useCallback(async (request: RegisterRequest) => {
    await registerUser(request)
  }, [])

  const value = useMemo(
    () => ({ user, status, error, retryBootstrap, login, register }),
    [user, status, error, retryBootstrap, login, register],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
