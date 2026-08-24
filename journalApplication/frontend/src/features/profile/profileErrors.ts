import { ApiError } from '../../lib/apiClient'

export function profileErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError && error.status < 500) {
    return error.problem?.detail ?? error.problem?.title ?? fallback
  }
  return fallback
}
