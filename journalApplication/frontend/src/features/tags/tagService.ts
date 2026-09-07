import { journalApiClient } from '../../lib/apiClient'
import type { JournalTag } from '../journals/journalTypes'

export function getTags() {
  return journalApiClient.get<JournalTag[]>('/tags')
}
