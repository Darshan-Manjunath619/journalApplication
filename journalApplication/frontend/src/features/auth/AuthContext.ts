import { createContext } from 'react'
import type { AuthStatus, CurrentUser, LoginRequest, RegisterRequest } from './authTypes'
import type { ChangePasswordRequest, UpdateProfileRequest } from '../profile/profileTypes'

export type AuthContextValue = {
  user: CurrentUser | null
  status: AuthStatus
  error: Error | null
  retryBootstrap: () => void
  login: (request: LoginRequest) => Promise<void>
  register: (request: RegisterRequest) => Promise<void>
  logout: () => Promise<void>
  updateProfile: (request: UpdateProfileRequest) => Promise<void>
  changePassword: (request: ChangePasswordRequest) => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
