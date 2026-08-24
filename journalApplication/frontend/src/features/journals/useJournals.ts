import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { getJournals } from './journalService'
import type { JournalPageRequest } from './journalTypes'

export const journalKeys = {
  all: ['journals'] as const,
  page: (request: JournalPageRequest) => [...journalKeys.all, 'page', request] as const,
}

export function useJournals(request: JournalPageRequest) {
  return useQuery({
    queryKey: journalKeys.page(request),
    queryFn: () => getJournals(request),
    placeholderData: keepPreviousData,
  })
}
