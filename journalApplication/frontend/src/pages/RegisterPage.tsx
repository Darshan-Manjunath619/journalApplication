import { Link } from 'react-router-dom'

export function RegisterPage() {
  return <main className="auth-page"><section className="auth-card"><p className="eyebrow">Start writing</p><h1>Create an account</h1><p>The registration form and backend connection arrive in Phase 1.9.</p><p>Already registered? <Link to="/login">Sign in</Link></p></section></main>
}
