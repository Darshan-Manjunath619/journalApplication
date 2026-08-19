import { NavLink, Outlet } from 'react-router-dom'

export function AppShell() {
  return (
    <div className="app-shell">
      <header className="site-header">
        <NavLink className="brand" to="/dashboard">Journal</NavLink>
        <nav aria-label="Primary navigation">
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/profile">Profile</NavLink>
        </nav>
      </header>
      <main className="page-container"><Outlet /></main>
    </div>
  )
}
