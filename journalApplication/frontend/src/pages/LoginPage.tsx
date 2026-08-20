import { Link } from 'react-router-dom'

export function LoginPage() {
  return (
    <main className={'grid min-h-screen place-items-center bg-slate-50 px-5 py-10'}>
      <section className={'w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-7 shadow-xl shadow-slate-200/60 sm:p-10'}>
        <p className={'mb-2 text-xs font-bold uppercase tracking-[0.16em] text-indigo-600'}>Welcome back</p>
        <h1 className={'text-3xl font-bold tracking-tight text-slate-900'}>Sign in</h1>
        <p className={'mt-4 leading-7 text-slate-600'}>The login form and backend connection arrive in Phase 1.9.</p>
        <Link className={'mt-6 inline-flex rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white no-underline transition hover:bg-indigo-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600'} to={'/dashboard'}>View dashboard shell</Link>
        <p className={'mt-6 text-sm text-slate-600'}>New here? <Link className={'font-semibold text-indigo-700'} to={'/register'}>Create an account</Link></p>
      </section>
    </main>
  )
}
