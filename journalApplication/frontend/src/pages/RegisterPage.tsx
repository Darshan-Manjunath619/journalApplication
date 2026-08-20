import { Link } from 'react-router-dom'

export function RegisterPage() {
  return (
    <main className={'grid min-h-screen place-items-center bg-slate-50 px-5 py-10'}>
      <section className={'w-full max-w-lg rounded-2xl border border-slate-200 bg-white p-7 shadow-xl shadow-slate-200/60 sm:p-10'}>
        <p className={'mb-2 text-xs font-bold uppercase tracking-[0.16em] text-indigo-600'}>Start writing</p>
        <h1 className={'text-3xl font-bold tracking-tight text-slate-900'}>Create an account</h1>
        <p className={'mt-4 leading-7 text-slate-600'}>The registration form and backend connection arrive in Phase 1.9.</p>
        <p className={'mt-6 text-sm text-slate-600'}>Already registered? <Link className={'font-semibold text-indigo-700'} to={'/login'}>Sign in</Link></p>
      </section>
    </main>
  )
}
