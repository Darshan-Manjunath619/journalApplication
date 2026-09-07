import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { AuthContext } from './AuthContext'
import type { AuthStatus, CurrentUser, LoginRequest, RegisterRequest } from './authTypes'
import { clearLocalSession, loginSession, logoutSession, registerUser, restoreSession } from './authService'
import { setSessionExpiredHandler } from '../../lib/apiClient'
import type { ChangePasswordRequest, UpdateProfileRequest } from '../profile/profileTypes'
import { changePasswordRequest, updateProfileRequest } from '../profile/profileService'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [status, setStatus] = useState<AuthStatus>('loading')
  const [error, setError] = useState<Error | null>(null)
  const [bootstrapAttempt, setBootstrapAttempt] = useState(0)

  useEffect(() => {
    setSessionExpiredHandler(() => {
      clearLocalSession()
      setUser(null)
      setError(null)
      setStatus('unauthenticated')
    })
    return () => setSessionExpiredHandler(null)
  }, [])

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

  const logout = useCallback(async () => {
    try {
      await logoutSession()
    } finally {
      setUser(null)
      setError(null)
      setStatus('unauthenticated')
    }
  }, [])

  const updateProfile = useCallback(async (request: UpdateProfileRequest) => {
    const updatedUser = await updateProfileRequest(request)
    setUser(updatedUser)
  }, [])

  const changePassword = useCallback(async (request: ChangePasswordRequest) => {
    await changePasswordRequest(request)
    await logoutSession().catch(() => undefined)
    setUser(null)
    setError(null)
    setStatus('unauthenticated')
  }, [])

  const value = useMemo(
    () => ({ user, status, error, retryBootstrap, login, register, logout, updateProfile, changePassword }),
    [user, status, error, retryBootstrap, login, register, logout, updateProfile, changePassword],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
