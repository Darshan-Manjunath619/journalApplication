import { apiClient } from '../../lib/apiClient'
import type { JournalTag } from '../journals/journalTypes'

export function getTags() {
  return apiClient.get<JournalTag[]>('/tags')
}
