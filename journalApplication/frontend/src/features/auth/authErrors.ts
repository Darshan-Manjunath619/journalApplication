import { ApiError } from '../../lib/apiClient'

export function loginErrorMessage(error: unknown) {
  if (error instanceof ApiError && error.status === 401) {
    return 'Invalid username or password'
  }
  return safeApiMessage(error, 'Unable to sign in. Please try again.')
}

export function registrationErrorMessage(error: unknown) {
  return safeApiMessage(error, 'Unable to create your account. Please try again.')
}

function safeApiMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError && error.status < 500) {
    return error.problem?.detail ?? error.problem?.title ?? fallback
  }
  return fallback
}
