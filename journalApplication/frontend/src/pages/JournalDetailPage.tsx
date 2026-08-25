import { Link, useLocation, useParams } from 'react-router-dom'
import type { ReactNode } from 'react'
import { ApiError } from '../lib/apiClient'
import { useJournal } from '../features/journals/useJournals'

export function JournalDetailPage() {
  const { id } = useParams()
  const location = useLocation()
  const parsedId = Number(id)
  const journalId = Number.isInteger(parsedId) && parsedId > 0 ? parsedId : null
  const journal = useJournal(journalId)
  const created = Boolean((location.state as { created?: boolean } | null)?.created)

  if (journalId === null || (journal.error instanceof ApiError && journal.error.status === 404)) {
    return <JournalMessage title={'Journal not found'}>The journal does not exist or is not available to your account.</JournalMessage>
  }
  if (journal.isPending) return <JournalMessage role={'status'} title={'Loading journal'}>Please wait...</JournalMessage>
  if (journal.isError) return <JournalMessage role={'alert'} title={'Unable to load journal'}><button className={'mt-4 rounded-lg bg-indigo-600 px-4 py-2 font-semibold text-white'} type={'button'} onClick={() => journal.refetch()}>Try again</button></JournalMessage>

  return (
    <article className={'rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-10'}>
      {created && <p className={'mb-5 rounded-lg bg-emerald-50 p-3 text-sm font-medium text-emerald-800'} role={'status'}>Journal created successfully.</p>}
      <div className={'flex flex-wrap items-start justify-between gap-4'}>
        <div>
          <p className={'text-sm font-semibold text-slate-500'}>{formatDate(journal.data.createdAt)}</p>
          <h1 className={'mt-2 text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl'}>{journal.data.title}</h1>
        </div>
        {journal.data.favorite && <span className={'rounded-full bg-amber-100 px-3 py-1 text-sm font-bold text-amber-800'}>Favorite</span>}
      </div>
      <p className={'mt-8 whitespace-pre-wrap leading-7 text-slate-700'}>{journal.data.content || 'No content'}</p>
      {journal.data.tags.length > 0 && <ul className={'mt-8 flex flex-wrap gap-2'} aria-label={'Tags'}>{journal.data.tags.map((tag) => <li className={'rounded-full bg-indigo-50 px-3 py-1 text-sm font-semibold text-indigo-700'} key={tag.id}>{tag.name}</li>)}</ul>}
      <Link className={'mt-8 inline-flex font-semibold text-indigo-700'} to={'/dashboard'}>Back to dashboard</Link>
    </article>
  )
}

function JournalMessage({ title, children, role }: { title: string, children: ReactNode, role?: 'alert' | 'status' }) {
  return <section className={'rounded-2xl border border-slate-200 bg-white p-8 text-center text-slate-600 shadow-sm'} role={role}><h1 className={'text-2xl font-bold text-slate-900'}>{title}</h1><div className={'mt-2'}>{children}</div><Link className={'mt-5 inline-flex font-semibold text-indigo-700'} to={'/dashboard'}>Back to dashboard</Link></section>
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'long', timeStyle: 'short' }).format(new Date(value))
}
