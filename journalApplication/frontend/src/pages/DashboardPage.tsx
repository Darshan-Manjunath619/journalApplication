import { useState } from 'react'
import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { JournalCard } from '../features/journals/JournalCard'
import { useJournals } from '../features/journals/useJournals'

const PAGE_SIZE = 10

export function DashboardPage() {
  const [page, setPage] = useState(0)
  const journals = useJournals({ page, size: PAGE_SIZE })

  return (
    <div className={'space-y-6'}>
      <section className={'rounded-2xl border border-slate-200 bg-white p-6 shadow-sm sm:p-10'}>
        <p className={'mb-2 text-xs font-bold uppercase tracking-[0.16em] text-indigo-600'}>Your journal</p>
        <h1 className={'text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl'}>Dashboard</h1>
        <div className={'mt-3 flex flex-wrap items-center justify-between gap-4'}>
          <p className={'text-slate-600'}>Review your latest thoughts and reflections.</p>
          <Link className={'rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white no-underline'} to={'/journals/new'}>New journal</Link>
        </div>
      </section>

      {journals.isPending && <DashboardMessage role={'status'}>Loading journals...</DashboardMessage>}

      {journals.isError && (
        <DashboardMessage role={'alert'}>
          <p>Unable to load your journals.</p>
          <button className={'mt-4 rounded-lg bg-indigo-600 px-4 py-2 font-semibold text-white'} type={'button'} onClick={() => journals.refetch()}>Try again</button>
        </DashboardMessage>
      )}

      {journals.data?.content.length === 0 && (
        <DashboardMessage>
          <h2 className={'text-xl font-bold text-slate-900'}>No journal entries yet</h2>
          <p className={'mt-2'}>Your entries will appear here after you create your first journal.</p>
        </DashboardMessage>
      )}

      {journals.data && journals.data.content.length > 0 && (
        <>
          <p className={'text-sm text-slate-500'}>{journals.data.totalElements} {journals.data.totalElements === 1 ? 'entry' : 'entries'}</p>
          <div className={'grid gap-4 md:grid-cols-2'} aria-busy={journals.isFetching}>
            {journals.data.content.map((journal) => <JournalCard journal={journal} key={journal.id} />)}
          </div>
          <nav className={'flex items-center justify-between rounded-xl border border-slate-200 bg-white p-4'} aria-label={'Journal pagination'}>
            <button className={'rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-50'} type={'button'} disabled={journals.data.first || journals.isFetching} onClick={() => setPage((current) => current - 1)}>Previous</button>
            <span className={'text-sm text-slate-600'}>Page {journals.data.page + 1} of {journals.data.totalPages}</span>
            <button className={'rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-50'} type={'button'} disabled={journals.data.last || journals.isFetching} onClick={() => setPage((current) => current + 1)}>Next</button>
          </nav>
        </>
      )}
    </div>
  )
}

function DashboardMessage({ children, role }: { children: ReactNode, role?: 'alert' | 'status' }) {
  return <section className={'rounded-2xl border border-slate-200 bg-white p-8 text-center text-slate-600 shadow-sm'} role={role}>{children}</section>
}
