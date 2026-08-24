import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './useAuth'

export function ProtectedRoute() {
  const { status, retryBootstrap } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return <main className={'grid min-h-screen place-items-center bg-slate-50'}><p role={'status'}>Checking your session...</p></main>
  }

  if (status === 'error') {
    return (
      <main className={'grid min-h-screen place-items-center bg-slate-50 px-5'}>
        <section className={'rounded-2xl border border-slate-200 bg-white p-7 text-center shadow-lg'}>
          <h1 className={'text-xl font-bold text-slate-900'}>Unable to check your session</h1>
          <p className={'mt-2 text-slate-600'}>Confirm the backend is running, then try again.</p>
          <button className={'mt-5 rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white'} type={'button'} onClick={retryBootstrap}>Try again</button>
        </section>
      </main>
    )
  }

  if (status !== 'authenticated') {
    return <Navigate to={'/login'} replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}
