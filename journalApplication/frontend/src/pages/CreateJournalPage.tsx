import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { JournalForm } from '../features/journals/JournalForm'
import { journalErrorMessage } from '../features/journals/journalErrors'
import type { JournalFormValues } from '../features/journals/journalSchemas'
import { useCreateJournal } from '../features/journals/useJournals'
import { useTags } from '../features/tags/useTags'

export function CreateJournalPage() {
  const navigate = useNavigate()
  const createJournal = useCreateJournal()
  const tags = useTags()
  const [serverError, setServerError] = useState<string | null>(null)

  async function submit(values: JournalFormValues, tagIds: number[]) {
    setServerError(null)
    try {
      const created = await createJournal.mutateAsync({ ...values, tagIds })
      navigate(`/journals/${created.id}`, { replace: true, state: { created: true } })
    } catch (error) {
      setServerError(journalErrorMessage(error, 'Unable to create your journal. Please try again.'))
    }
  }

  return (
    <section className={'rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-10'}>
      <p className={'mb-2 text-xs font-bold uppercase tracking-[0.16em] text-indigo-600'}>New reflection</p>
      <h1 className={'text-3xl font-bold tracking-tight text-slate-900'}>Create journal</h1>
      {tags.isPending && <p className={'mt-4 text-sm text-slate-500'} role={'status'}>Loading tags...</p>}
      {tags.isError && <p className={'mt-4 text-sm text-amber-700'} role={'status'}>Tags could not be loaded. You can still create an untagged journal.</p>}
      <div className={'mt-6'}>
        <JournalForm tags={tags.data ?? []} isSubmitting={createJournal.isPending} serverError={serverError} onSubmit={submit} />
      </div>
    </section>
  )
}
