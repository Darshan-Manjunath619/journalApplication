import { ApiError, apiClient, setAccessToken } from '../../lib/apiClient'
import type { AuthResponse, CurrentUser } from './authTypes'

async function getCurrentUser() {
  return apiClient.get<CurrentUser>('/users/me')
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
