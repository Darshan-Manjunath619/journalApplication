import { NavLink, Outlet } from 'react-router-dom'

export function AppShell() {
  return (
    <div className={'min-h-screen bg-slate-50'}>
      <header className={'flex flex-col gap-3 border-b border-slate-200 bg-white px-5 py-4 sm:flex-row sm:items-center sm:justify-between lg:px-[max(2rem,calc((100vw-68rem)/2))]'}>
        <NavLink className={'text-xl font-bold tracking-tight text-slate-900 no-underline'} to={'/dashboard'}>Journal</NavLink>
        <nav className={'flex gap-2'} aria-label={'Primary navigation'}>
          <NavLink className={navigationClassName} to={'/dashboard'}>Dashboard</NavLink>
          <NavLink className={navigationClassName} to={'/profile'}>Profile</NavLink>
        </nav>
      </header>
      <main className={'mx-auto w-full max-w-6xl px-5 py-8 sm:py-10'}><Outlet /></main>
    </div>
  )
}

function navigationClassName({ isActive }: { isActive: boolean }) {
  const base = 'rounded-lg px-3 py-2 text-sm font-semibold no-underline transition-colors'
  return isActive
    ? `${base} bg-indigo-600 text-white`
    : `${base} text-slate-600 hover:bg-slate-100 hover:text-slate-900`
}
