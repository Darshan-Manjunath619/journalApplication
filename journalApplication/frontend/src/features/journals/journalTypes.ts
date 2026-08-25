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
}

export type CreateJournalRequest = {
  title: string
  content: string
  tagIds: number[]
}

export type UpdateJournalRequest = CreateJournalRequest & {
  favorite: boolean
}
