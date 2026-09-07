import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <main className={'grid min-h-screen place-items-center bg-slate-50 px-5 py-10'}>
      <section className={'w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-7 text-center shadow-xl shadow-slate-200/60 sm:p-10'}>
        <p className={'text-sm font-bold tracking-[0.2em] text-indigo-600'}>404</p>
        <h1 className={'mt-2 text-3xl font-bold tracking-tight text-slate-900'}>Page not found</h1>
        <p className={'mt-4 leading-7 text-slate-600'}>The address does not match a page in the Journal Application.</p>
        <Link className={'mt-6 inline-flex rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white no-underline transition hover:bg-indigo-700'} to={'/dashboard'}>Return to dashboard</Link>
      </section>
    </main>
  )
}
