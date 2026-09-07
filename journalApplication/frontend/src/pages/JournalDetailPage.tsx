import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { useEffect, useRef, useState } from 'react'
import type { KeyboardEvent as ReactKeyboardEvent } from 'react'
import { ApiError } from '../lib/apiClient'
import { useDeleteJournal, useJournal } from '../features/journals/useJournals'
import { journalErrorMessage } from '../features/journals/journalErrors'
import { FeedbackPanel } from '../components/FeedbackPanel'

export function JournalDetailPage() {
  const { id } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const parsedId = Number(id)
  const journalId = Number.isInteger(parsedId) && parsedId > 0 ? parsedId : null
  const journal = useJournal(journalId)
  const deleteJournal = useDeleteJournal(journalId ?? 0)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const deleteButtonRef = useRef<HTMLButtonElement>(null)
  const confirmButtonRef = useRef<HTMLButtonElement>(null)
  const cancelButtonRef = useRef<HTMLButtonElement>(null)
  const created = Boolean((location.state as { created?: boolean } | null)?.created)
  const updated = Boolean((location.state as { updated?: boolean } | null)?.updated)

  useEffect(() => {
    if (confirmDelete) confirmButtonRef.current?.focus()
  }, [confirmDelete])

  function closeDeleteConfirmation() {
    setConfirmDelete(false)
    queueMicrotask(() => deleteButtonRef.current?.focus())
  }

  function handleDialogKeyDown(event: ReactKeyboardEvent<HTMLDivElement>) {
    if (event.key === 'Escape' && !deleteJournal.isPending) {
      event.preventDefault()
      closeDeleteConfirmation()
      return
    }
    if (event.key !== 'Tab') return
    if (event.shiftKey && document.activeElement === confirmButtonRef.current) {
      event.preventDefault()
      cancelButtonRef.current?.focus()
    } else if (!event.shiftKey && document.activeElement === cancelButtonRef.current) {
      event.preventDefault()
      confirmButtonRef.current?.focus()
    }
  }

  async function removeJournal() {
    setDeleteError(null)
    try {
      await deleteJournal.mutateAsync()
      navigate('/dashboard', { replace: true, state: { deleted: true } })
    } catch (error) {
      setDeleteError(journalErrorMessage(error, 'Unable to delete your journal. Please try again.'))
    }
  }

  if (journalId === null || (journal.error instanceof ApiError && journal.error.status === 404)) {
    return <FeedbackPanel title={'Journal not found'} footer={<Link className={'font-semibold text-indigo-700'} to={'/dashboard'}>Back to dashboard</Link>}>The journal does not exist or is not available to your account.</FeedbackPanel>
  }
  if (journal.isPending) return <FeedbackPanel role={'status'} title={'Loading journal'}>Please wait...</FeedbackPanel>
  if (journal.isError) return <FeedbackPanel role={'alert'} title={'Unable to load journal'} actionLabel={'Try again'} actionDisabled={journal.isFetching} onAction={() => journal.refetch()} footer={<Link className={'font-semibold text-indigo-700'} to={'/dashboard'}>Back to dashboard</Link>}>Your journal could not be loaded.</FeedbackPanel>

  return (
    <article className={'rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-10'}>
      {created && <p className={'mb-5 rounded-lg bg-emerald-50 p-3 text-sm font-medium text-emerald-800'} role={'status'}>Journal created successfully.</p>}
      {updated && <p className={'mb-5 rounded-lg bg-emerald-50 p-3 text-sm font-medium text-emerald-800'} role={'status'}>Journal updated successfully.</p>}
      <div className={'flex flex-wrap items-start justify-between gap-4'}>
        <div>
          <p className={'text-sm font-semibold text-slate-500'}>{formatDate(journal.data.createdAt)}</p>
          <h1 className={'mt-2 text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl'}>{journal.data.title}</h1>
        </div>
        {journal.data.favorite && <span className={'rounded-full bg-amber-100 px-3 py-1 text-sm font-bold text-amber-800'}>Favorite</span>}
      </div>
      <p className={'mt-8 whitespace-pre-wrap leading-7 text-slate-700'}>{journal.data.content || 'No content'}</p>
      {journal.data.tags.length > 0 && <ul className={'mt-8 flex flex-wrap gap-2'} aria-label={'Tags'}>{journal.data.tags.map((tag) => <li className={'rounded-full bg-indigo-50 px-3 py-1 text-sm font-semibold text-indigo-700'} key={tag.id}>{tag.name}</li>)}</ul>}
      {deleteError && <p className={'mt-6 rounded-lg bg-rose-50 p-3 text-sm font-medium text-rose-700'} role={'alert'}>{deleteError}</p>}
      {confirmDelete && <div id={'delete-confirmation'} className={'mt-6 rounded-xl border border-rose-200 bg-rose-50 p-4'} role={'dialog'} aria-modal={'true'} aria-labelledby={'delete-dialog-title'} aria-describedby={'delete-dialog-description'} onKeyDown={handleDialogKeyDown}><p className={'font-semibold text-rose-900'} id={'delete-dialog-title'}>Delete this journal permanently?</p><p className={'mt-1 text-sm text-rose-800'} id={'delete-dialog-description'}>This action cannot be undone.</p><div className={'mt-3 flex flex-col gap-3 sm:flex-row'}><button ref={confirmButtonRef} className={'rounded-lg bg-rose-600 px-4 py-2 font-semibold text-white disabled:opacity-60'} type={'button'} disabled={deleteJournal.isPending} onClick={removeJournal}>{deleteJournal.isPending ? 'Deleting...' : 'Confirm delete'}</button><button ref={cancelButtonRef} className={'rounded-lg border border-slate-300 px-4 py-2 font-semibold'} type={'button'} disabled={deleteJournal.isPending} onClick={closeDeleteConfirmation}>Cancel</button></div></div>}
      <div className={'mt-8 flex flex-col items-start gap-4 sm:flex-row sm:flex-wrap sm:items-center'}>
        <Link className={'font-semibold text-indigo-700'} to={`/journals/${journalId}/edit`}>Edit journal</Link>
        <button ref={deleteButtonRef} className={'font-semibold text-rose-700'} type={'button'} aria-expanded={confirmDelete} aria-controls={'delete-confirmation'} onClick={() => setConfirmDelete(true)}>Delete journal</button>
        <Link className={'font-semibold text-slate-600'} to={'/dashboard'}>Back to dashboard</Link>
      </div>
    </article>
  )
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'long', timeStyle: 'short' }).format(new Date(value))
}
