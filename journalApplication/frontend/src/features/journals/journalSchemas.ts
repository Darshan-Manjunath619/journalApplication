import { z } from 'zod'

export const journalSchema = z.object({
  title: z.string().trim().min(1, 'Title is required').max(160, 'Title cannot exceed 160 characters'),
  content: z.string().max(20_000, 'Content cannot exceed 20,000 characters'),
})

export type JournalFormValues = z.infer<typeof journalSchema>
