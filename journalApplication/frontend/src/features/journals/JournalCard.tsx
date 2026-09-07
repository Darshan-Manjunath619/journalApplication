import type { Journal } from './journalTypes'
import { Link } from 'react-router-dom'

export function JournalCard({ journal }: { journal: Journal }) {
  return (
    <article className={'rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md'}>
      <div className={'flex items-start justify-between gap-4'}>
        <div>
          <p className={'text-xs font-semibold uppercase tracking-wide text-slate-500'}>{formatDate(journal.createdAt)}</p>
          <h2 className={'mt-1 text-xl font-bold text-slate-900'}><Link className={'text-inherit no-underline hover:text-indigo-700'} to={`/journals/${journal.id}`}>{journal.title}</Link></h2>
        </div>
        {journal.favorite && <span className={'rounded-full bg-amber-100 px-2.5 py-1 text-xs font-bold text-amber-800'}>Favorite</span>}
      </div>
      <p className={'mt-3 line-clamp-3 whitespace-pre-wrap leading-6 text-slate-600'}>{journal.content || 'No content'}</p>
      {journal.tags.length > 0 && (
        <ul className={'mt-4 flex flex-wrap gap-2'} aria-label={'Tags'}>
          {journal.tags.map((tag) => <li className={'rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700'} key={tag.id}>{tag.name}</li>)}
        </ul>
      )}
      <Link className={'mt-4 inline-flex text-sm font-semibold text-indigo-700'} to={`/journals/${journal.id}`}>Read journal</Link>
    </article>
  )
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
