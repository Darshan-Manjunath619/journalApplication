import { apiClient } from '../../lib/apiClient'
import type { CurrentUser } from '../auth/authTypes'
import type { ChangePasswordRequest, UpdateProfileRequest } from './profileTypes'

export function updateProfileRequest(request: UpdateProfileRequest) {
  return apiClient.patch<CurrentUser>('/users/me', request)
}

export function changePasswordRequest(request: ChangePasswordRequest) {
  return apiClient.patch<void>('/users/me/password', request)
}
