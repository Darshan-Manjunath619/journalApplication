import { apiClient } from '../../lib/apiClient'
import type { PageResponse } from '../../types/api'
import type { CreateJournalRequest, Journal, JournalPageRequest, UpdateJournalRequest } from './journalTypes'

export function getJournals({ page, size, query, sortField, sortDirection, tagId, favorite, fromDate, toDate }: JournalPageRequest) {
  const parameters = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort: `${sortField},${sortDirection}`,
  })
  if (query.trim()) parameters.set('q', query.trim())
  if (tagId !== null) parameters.set('tag', String(tagId))
  if (favorite !== null) parameters.set('favorite', String(favorite))
  if (fromDate) parameters.set('from', `${fromDate}T00:00:00.000Z`)
  if (toDate) parameters.set('to', `${toDate}T23:59:59.999Z`)
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
