import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation, useSearchParams } from 'react-router-dom'
import { JournalCard } from '../features/journals/JournalCard'
import { useJournals } from '../features/journals/useJournals'
import { parseDashboardQuery, updateDashboardQuery } from '../features/journals/dashboardQueryState'
import { useTags } from '../features/tags/useTags'
import { FeedbackPanel } from '../components/FeedbackPanel'

export function DashboardPage() {
  const [searchParameters, setSearchParameters] = useSearchParams()
  const request = parseDashboardQuery(searchParameters)
  const { page, query, sortField, sortDirection, tagId, favorite, fromDate, toDate } = request
  const [draftQuery, setDraftQuery] = useState(query)
  const location = useLocation()
  const journals = useJournals(request)
  const tags = useTags()
  const deleted = Boolean((location.state as { deleted?: boolean } | null)?.deleted)
  const hasFilters = tagId !== null || favorite !== null || Boolean(fromDate) || Boolean(toDate)
  const hasCriteria = Boolean(query) || hasFilters

  useEffect(() => setDraftQuery(query), [query])

  function updateQuery(changes: Record<string, string | number | boolean | null>) {
    setSearchParameters(updateDashboardQuery(searchParameters, changes))
  }

  function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    updateQuery({ q: draftQuery.trim(), page: null })
  }

  function clearSearch() {
    updateQuery({ q: null, page: null })
  }

  function clearFilters() {
    updateQuery({ tag: null, favorite: null, from: null, to: null, page: null })
  }

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
      {deleted && <p className={'rounded-lg bg-emerald-50 p-3 text-sm font-medium text-emerald-800'} role={'status'}>Journal deleted successfully.</p>}
      {tags.isError && <div className={'flex flex-wrap items-center justify-between gap-3 rounded-lg bg-amber-50 p-3 text-sm font-medium text-amber-800'} role={'status'}><span>Tags could not be loaded. Other filters still work.</span><button className={'font-bold underline'} type={'button'} disabled={tags.isFetching} onClick={() => tags.refetch()}>{tags.isFetching ? 'Retrying...' : 'Retry tags'}</button></div>}

      <section className={'rounded-xl border border-slate-200 bg-white p-4 shadow-sm'} aria-label={'Journal search and sorting'}>
        <form className={'grid gap-4 lg:grid-cols-[1fr_auto_auto] lg:items-end'} onSubmit={search}>
          <div>
            <label className={'block text-sm font-semibold text-slate-700'} htmlFor={'journal-search'}>Search journals</label>
            <input className={'mt-2 w-full rounded-lg border border-slate-300 px-3 py-2.5 outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-100'} id={'journal-search'} type={'search'} value={draftQuery} onChange={(event) => setDraftQuery(event.target.value)} placeholder={'Search title or content'} />
          </div>
          <button className={'rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white'} type={'submit'}>Search</button>
          {query && <button className={'rounded-lg border border-slate-300 px-4 py-2.5 font-semibold text-slate-700'} type={'button'} onClick={clearSearch}>Clear</button>}
        </form>
        <div className={'mt-4 grid gap-4 sm:grid-cols-2'}>
          <label className={'text-sm font-semibold text-slate-700'}>Sort by<select className={'mt-2 block w-full rounded-lg border border-slate-300 px-3 py-2.5'} value={sortField} onChange={(event) => updateQuery({ sort: `${event.target.value},${sortDirection}`, page: null })}><option value={'createdAt'}>Created date</option><option value={'updatedAt'}>Updated date</option><option value={'title'}>Title</option></select></label>
          <label className={'text-sm font-semibold text-slate-700'}>Direction<select className={'mt-2 block w-full rounded-lg border border-slate-300 px-3 py-2.5'} value={sortDirection} onChange={(event) => updateQuery({ sort: `${sortField},${event.target.value}`, page: null })}><option value={'desc'}>Descending</option><option value={'asc'}>Ascending</option></select></label>
        </div>
        <div className={'mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4'}>
          <label className={'text-sm font-semibold text-slate-700'}>Tag<select className={'mt-2 block w-full rounded-lg border border-slate-300 px-3 py-2.5'} value={tagId ?? ''} onChange={(event) => updateQuery({ tag: event.target.value, page: null })}><option value={''}>All tags</option>{Array.isArray(tags.data) && tags.data.map((tag) => <option value={tag.id} key={tag.id}>{tag.name}</option>)}</select></label>
          <label className={'text-sm font-semibold text-slate-700'}>Favorite<select className={'mt-2 block w-full rounded-lg border border-slate-300 px-3 py-2.5'} value={favorite === null ? '' : String(favorite)} onChange={(event) => updateQuery({ favorite: event.target.value, page: null })}><option value={''}>All journals</option><option value={'true'}>Favorites only</option><option value={'false'}>Not favorites</option></select></label>
          <label className={'text-sm font-semibold text-slate-700'}>From date<input className={'mt-2 block w-full rounded-lg border border-slate-300 px-3 py-2.5'} type={'date'} value={fromDate} max={toDate || undefined} onChange={(event) => updateQuery({ from: event.target.value, page: null })} /></label>
          <label className={'text-sm font-semibold text-slate-700'}>To date<input className={'mt-2 block w-full rounded-lg border border-slate-300 px-3 py-2.5'} type={'date'} value={toDate} min={fromDate || undefined} onChange={(event) => updateQuery({ to: event.target.value, page: null })} /></label>
        </div>
        {hasFilters && <button className={'mt-4 text-sm font-semibold text-indigo-700'} type={'button'} onClick={clearFilters}>Clear filters</button>}
      </section>

      {journals.isPending && <FeedbackPanel title={'Loading journals'} role={'status'}>Please wait...</FeedbackPanel>}

      {journals.isError && (
        <FeedbackPanel title={'Unable to load your journals'} role={'alert'} actionLabel={'Try again'} actionDisabled={journals.isFetching} onAction={() => journals.refetch()}>Your journals are still safe. Check the connection and retry.</FeedbackPanel>
      )}

      {journals.data?.content.length === 0 && (
        <FeedbackPanel title={hasCriteria ? 'No matching journals' : 'No journal entries yet'}>{hasCriteria ? 'No journals matched the selected search and filters.' : 'Your entries will appear here after you create your first journal.'}</FeedbackPanel>
      )}

      {journals.data && journals.data.content.length > 0 && (
        <>
          <p className={'text-sm text-slate-500'}>{journals.data.totalElements} {journals.data.totalElements === 1 ? 'entry' : 'entries'}</p>
          <div className={'grid gap-4 md:grid-cols-2'} aria-busy={journals.isFetching}>
            {journals.data.content.map((journal) => <JournalCard journal={journal} key={journal.id} />)}
          </div>
          <nav className={'grid grid-cols-2 items-center gap-3 rounded-xl border border-slate-200 bg-white p-4 sm:grid-cols-[auto_1fr_auto]'} aria-label={'Journal pagination'}>
            <button className={'row-start-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-50 sm:col-start-1 sm:row-start-1'} type={'button'} disabled={journals.data.first || journals.isFetching} onClick={() => updateQuery({ page: page - 1 || null })}>Previous</button>
            <span className={'col-span-2 row-start-1 text-center text-sm text-slate-600 sm:col-span-1 sm:col-start-2'}>Page {journals.data.page + 1} of {journals.data.totalPages}</span>
            <button className={'row-start-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-50 sm:col-start-3 sm:row-start-1'} type={'button'} disabled={journals.data.last || journals.isFetching} onClick={() => updateQuery({ page: page + 1 })}>Next</button>
          </nav>
        </>
      )}
    </div>
  )
}
