import { useQuery } from '@tanstack/react-query'
import { getTags } from './tagService'

export const tagKeys = { all: ['tags'] as const }

export function useTags() {
  return useQuery({ queryKey: tagKeys.all, queryFn: getTags })
}
