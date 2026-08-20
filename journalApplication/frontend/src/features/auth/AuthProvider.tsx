import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { AuthContext } from './AuthContext'
import type { AuthStatus, CurrentUser } from './authTypes'
import { clearLocalSession, restoreSession } from './authService'

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

  const value = useMemo(
    () => ({ user, status, error, retryBootstrap }),
    [user, status, error, retryBootstrap],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
