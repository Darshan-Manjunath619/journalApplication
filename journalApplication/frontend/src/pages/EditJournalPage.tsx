import { useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { JournalForm } from '../features/journals/JournalForm'
import { journalErrorMessage } from '../features/journals/journalErrors'
import type { JournalFormValues } from '../features/journals/journalSchemas'
import { useJournal, useUpdateJournal } from '../features/journals/useJournals'
import { useTags } from '../features/tags/useTags'
import { ApiError } from '../lib/apiClient'

export function EditJournalPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const parsedId = Number(id)
  const journalId = Number.isInteger(parsedId) && parsedId > 0 ? parsedId : null
  const journal = useJournal(journalId)
  const tags = useTags()
  const update = useUpdateJournal(journalId ?? 0)
  const [serverError, setServerError] = useState<string | null>(null)

  if (journalId === null || (journal.error instanceof ApiError && journal.error.status === 404)) {
    return <EditMessage title={'Journal not found'}>The journal does not exist or is not available to your account.</EditMessage>
  }
  if (journal.isPending) return <EditMessage role={'status'} title={'Loading journal'}>Please wait...</EditMessage>
  if (journal.isError) return <EditMessage role={'alert'} title={'Unable to load journal'}><button type={'button'} onClick={() => journal.refetch()}>Try again</button></EditMessage>

  async function submit(values: JournalFormValues, tagIds: number[], favorite: boolean) {
    setServerError(null)
    try {
      await update.mutateAsync({ ...values, tagIds, favorite })
      navigate(`/journals/${journalId}`, { replace: true, state: { updated: true } })
    } catch (error) {
      setServerError(journalErrorMessage(error, 'Unable to update your journal. Please try again.'))
    }
  }

  const availableTags = tags.data ?? journal.data.tags
  return (
    <section className={'rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-10'}>
      <p className={'mb-2 text-xs font-bold uppercase tracking-[0.16em] text-indigo-600'}>Update reflection</p>
      <h1 className={'text-3xl font-bold tracking-tight text-slate-900'}>Edit journal</h1>
      {tags.isError && <p className={'mt-4 text-sm text-amber-700'} role={'status'}>All tags could not be loaded. Existing tags will be preserved unless changed.</p>}
      <div className={'mt-6'}>
        <JournalForm
          tags={availableTags}
          initialValues={{ title: journal.data.title, content: journal.data.content }}
          initialTagIds={journal.data.tags.map((tag) => tag.id)}
          initialFavorite={journal.data.favorite}
          showFavorite
          submitLabel={'Save changes'}
          cancelTo={`/journals/${journalId}`}
          isSubmitting={update.isPending}
          serverError={serverError}
          onSubmit={submit}
        />
      </div>
    </section>
  )
}

function EditMessage({ title, children, role }: { title: string, children: ReactNode, role?: 'alert' | 'status' }) {
  return <section className={'rounded-2xl border border-slate-200 bg-white p-8 text-center text-slate-600 shadow-sm'} role={role}><h1 className={'text-2xl font-bold text-slate-900'}>{title}</h1><div className={'mt-2'}>{children}</div></section>
}
