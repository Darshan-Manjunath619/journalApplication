import { Link } from 'react-router-dom'

export function LoginPage() {
  return <main className="auth-page"><section className="auth-card"><p className="eyebrow">Welcome back</p><h1>Sign in</h1><p>The login form and backend connection arrive in Phase 1.9.</p><Link className="primary-link" to="/dashboard">View dashboard shell</Link><p>New here? <Link to="/register">Create an account</Link></p></section></main>
}
