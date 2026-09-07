export type JournalTag = {
  id: number
  name: string
  createdAt: string
}

export type Journal = {
  id: number
  title: string
  content: string
  date: string
  createdAt: string
  updatedAt: string
  favorite: boolean
  tags: JournalTag[]
}

export type JournalPageRequest = {
  page: number
  size: number
  query: string
  sortField: 'createdAt' | 'updatedAt' | 'title'
  sortDirection: 'asc' | 'desc'
  tagId: number | null
  favorite: boolean | null
  fromDate: string
  toDate: string
}

export type CreateJournalRequest = {
  title: string
  content: string
  tagIds: number[]
}

export type UpdateJournalRequest = CreateJournalRequest & {
  favorite: boolean
}
