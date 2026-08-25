import { apiClient } from '../../lib/apiClient'
import type { PageResponse } from '../../types/api'
import type { CreateJournalRequest, Journal, JournalPageRequest, UpdateJournalRequest } from './journalTypes'

export function getJournals({ page, size }: JournalPageRequest) {
  const parameters = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort: 'createdAt,desc',
  })
  return apiClient.get<PageResponse<Journal>>(`/journals?${parameters.toString()}`)
}

export function getJournal(id: number) {
  return apiClient.get<Journal>(`/journals/${id}`)
}

export function createJournal(request: CreateJournalRequest) {
  return apiClient.post<Journal>('/journals', request)
}

export function updateJournal(id: number, request: UpdateJournalRequest) {
  return apiClient.patch<Journal>(`/journals/${id}`, request)
}

export function deleteJournal(id: number) {
  return apiClient.delete(`/journals/${id}`)
}
