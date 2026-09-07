import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'

export function AppShell() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [isLoggingOut, setIsLoggingOut] = useState(false)

  async function handleLogout() {
    setIsLoggingOut(true)
    try {
      await logout()
    } finally {
      navigate('/login', { replace: true })
    }
  }

  return (
    <div className={'min-h-screen bg-slate-50'}>
      <header className={'flex flex-col gap-3 border-b border-slate-200 bg-white px-5 py-4 sm:flex-row sm:items-center sm:justify-between lg:px-[max(2rem,calc((100vw-68rem)/2))]'}>
        <NavLink className={'text-xl font-bold tracking-tight text-slate-900 no-underline'} to={'/dashboard'}>Journal</NavLink>
        <nav className={'flex w-full flex-wrap gap-2 sm:w-auto'} aria-label={'Primary navigation'}>
          <NavLink className={navigationClassName} to={'/dashboard'}>Dashboard</NavLink>
          <NavLink className={navigationClassName} to={'/profile'}>Profile</NavLink>
          <button className={'flex-1 rounded-lg px-3 py-2 text-sm font-semibold text-slate-600 transition-colors hover:bg-slate-100 hover:text-slate-900 disabled:opacity-60 sm:flex-none'} type={'button'} disabled={isLoggingOut} onClick={handleLogout}>
            {isLoggingOut ? 'Signing out...' : 'Logout'}
          </button>
        </nav>
      </header>
      <main className={'mx-auto w-full max-w-6xl px-4 py-6 sm:px-5 sm:py-10'}><Outlet /></main>
    </div>
  )
}

function navigationClassName({ isActive }: { isActive: boolean }) {
  const base = 'flex-1 rounded-lg px-3 py-2 text-center text-sm font-semibold no-underline transition-colors sm:flex-none'
  return isActive
    ? `${base} bg-indigo-600 text-white`
    : `${base} text-slate-600 hover:bg-slate-100 hover:text-slate-900`
}
