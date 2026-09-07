import type { CurrentUser } from '../auth/authTypes'

export type UpdateProfileRequest = Pick<CurrentUser, 'email' | 'sentimentAnalysis'>

export type ChangePasswordRequest = {
  currentPassword: string
  newPassword: string
}
