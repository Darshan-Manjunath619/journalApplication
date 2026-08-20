import { ApiError, apiClient, setAccessToken } from '../../lib/apiClient'
import type { AuthResponse, CurrentUser, LoginRequest, RegisterRequest } from './authTypes'

async function getCurrentUser() {
  return apiClient.get<CurrentUser>('/users/me')
}

export async function loginSession(request: LoginRequest): Promise<CurrentUser> {
  const auth = await apiClient.post<AuthResponse>('/auth/login', request)
  setAccessToken(auth.accessToken)
  try {
    return await getCurrentUser()
  } catch (error) {
    setAccessToken(null)
    throw error
  }
}

export function registerUser(request: RegisterRequest) {
  return apiClient.post<CurrentUser>('/auth/register', request)
}

export async function restoreSession(): Promise<CurrentUser | null> {
  try {
    const auth = await apiClient.post<AuthResponse>('/auth/refresh')
    setAccessToken(auth.accessToken)
    return await getCurrentUser()
  } catch (error) {
    setAccessToken(null)
    if (error instanceof ApiError && error.status === 401) {
      return null
    }
    throw error
  }
}

export function clearLocalSession() {
  setAccessToken(null)
}
