import { createContext } from 'react'
import type { AuthStatus, CurrentUser } from './authTypes'

export type AuthContextValue = {
  user: CurrentUser | null
  status: AuthStatus
  error: Error | null
  retryBootstrap: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
