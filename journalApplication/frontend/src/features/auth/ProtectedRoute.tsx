import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { FeedbackPanel } from '../../components/FeedbackPanel'
import { useAuth } from './useAuth'

export function ProtectedRoute() {
  const { status, retryBootstrap } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return <main className={'grid min-h-screen place-items-center bg-slate-50 px-5'}><FeedbackPanel title={'Checking your session'} role={'status'}>Please wait...</FeedbackPanel></main>
  }

  if (status === 'error') {
    return (
      <main className={'grid min-h-screen place-items-center bg-slate-50 px-5'}>
        <FeedbackPanel title={'Unable to check your session'} role={'alert'} actionLabel={'Try again'} onAction={retryBootstrap}>Confirm the backend is running, then try again.</FeedbackPanel>
      </main>
    )
  }

  if (status !== 'authenticated') {
    return <Navigate to={'/login'} replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}
