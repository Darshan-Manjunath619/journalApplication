import { apiClient } from '../../lib/apiClient'
import type { PageResponse } from '../../types/api'
import type { Journal, JournalPageRequest } from './journalTypes'

export function getJournals({ page, size }: JournalPageRequest) {
  const parameters = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort: 'createdAt,desc',
  })
  return apiClient.get<PageResponse<Journal>>(`/journals?${parameters.toString()}`)
}
