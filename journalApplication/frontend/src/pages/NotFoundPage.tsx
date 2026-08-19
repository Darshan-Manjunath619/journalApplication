import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return <main className="auth-page"><section className="auth-card"><p className="eyebrow">404</p><h1>Page not found</h1><p>The address does not match a page in the Journal Application.</p><Link className="primary-link" to="/dashboard">Return to dashboard</Link></section></main>
}
