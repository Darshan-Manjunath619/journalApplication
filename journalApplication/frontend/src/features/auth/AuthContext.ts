import { createContext } from 'react'
import type { AuthStatus, CurrentUser, LoginRequest, RegisterRequest } from './authTypes'

export type AuthContextValue = {
  user: CurrentUser | null
  status: AuthStatus
  error: Error | null
  retryBootstrap: () => void
  login: (request: LoginRequest) => Promise<void>
  register: (request: RegisterRequest) => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
