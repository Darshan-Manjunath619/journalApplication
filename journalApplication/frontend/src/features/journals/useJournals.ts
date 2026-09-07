import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { createJournal, deleteJournal, getJournal, getJournals, updateJournal } from './journalService'
import type { CreateJournalRequest, JournalPageRequest, UpdateJournalRequest } from './journalTypes'

export const journalKeys = {
  all: ['journals'] as const,
  lists: () => [...journalKeys.all, 'page'] as const,
  page: (request: JournalPageRequest) => [...journalKeys.lists(), request] as const,
  detail: (id: number) => [...journalKeys.all, 'detail', id] as const,
}

export function useJournal(id: number | null) {
  return useQuery({
    queryKey: journalKeys.detail(id ?? 0),
    queryFn: () => getJournal(id!),
    enabled: id !== null,
  })
}

export function useCreateJournal() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: CreateJournalRequest) => createJournal(request),
    onSuccess: (created) => {
      queryClient.setQueryData(journalKeys.detail(created.id), created)
      return queryClient.invalidateQueries({ queryKey: journalKeys.lists() })
    },
  })
}

export function useUpdateJournal(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: UpdateJournalRequest) => updateJournal(id, request),
    onSuccess: (updated) => {
      queryClient.setQueryData(journalKeys.detail(id), updated)
      return queryClient.invalidateQueries({ queryKey: journalKeys.lists() })
    },
  })
}

export function useDeleteJournal(id: number) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => deleteJournal(id),
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: journalKeys.detail(id) })
      return queryClient.invalidateQueries({ queryKey: journalKeys.lists() })
    },
  })
}

export function useJournals(request: JournalPageRequest) {
  return useQuery({
    queryKey: journalKeys.page(request),
    queryFn: () => getJournals(request),
    placeholderData: keepPreviousData,
  })
}
