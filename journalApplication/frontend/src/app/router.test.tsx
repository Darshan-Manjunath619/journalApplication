import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { vi } from 'vitest'
import { appRoutes } from './router'
import { AuthContext } from '../features/auth/AuthContext'
import type { AuthContextValue } from '../features/auth/AuthContext'

function renderRoute(path: string, authOverrides: Partial<AuthContextValue> = {}) {
  const router = createMemoryRouter(appRoutes, { initialEntries: [path] })
  const auth: AuthContextValue = {
    user: { id: 7, userName: 'darshan_01', email: 'darshan@example.com', sentimentAnalysis: false, roles: ['USER'] },
    status: 'authenticated',
    error: null,
    retryBootstrap: vi.fn(),
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn().mockResolvedValue(undefined),
    updateProfile: vi.fn().mockResolvedValue(undefined),
    changePassword: vi.fn().mockResolvedValue(undefined),
    ...authOverrides,
  }
  render(<AuthContext.Provider value={auth}><RouterProvider router={router} /></AuthContext.Provider>)
  return auth
}

describe('application routing', () => {
  it('renders the profile inside the shared application shell', () => {
    renderRoute('/profile')

    expect(screen.getByRole('navigation', { name: 'Primary navigation' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Profile' })).toBeInTheDocument()
  })

  it('navigates between shell pages without a document reload', async () => {
    const user = userEvent.setup()
    renderRoute('/dashboard')

    await user.click(screen.getByRole('link', { name: 'Profile' }))

    expect(screen.getByRole('heading', { name: 'Profile' })).toBeInTheDocument()
  })

  it('renders the not-found page for an unknown route', () => {
    renderRoute('/missing-page')

    expect(screen.getByRole('heading', { name: 'Page not found' })).toBeInTheDocument()
  })

  it('redirects unauthenticated users from a protected page to login', () => {
    renderRoute('/profile', { user: null, status: 'unauthenticated' })

    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Profile' })).not.toBeInTheDocument()
  })

  it('waits for session bootstrap before rendering a protected page', () => {
    renderRoute('/dashboard', { user: null, status: 'loading' })

    expect(screen.getByRole('status')).toHaveTextContent('Checking your session')
    expect(screen.queryByRole('heading', { name: 'Your journals' })).not.toBeInTheDocument()
  })

  it('logs out and returns to login', async () => {
    const user = userEvent.setup()
    const auth = renderRoute('/dashboard')

    await user.click(screen.getByRole('button', { name: 'Logout' }))

    expect(auth.logout).toHaveBeenCalledOnce()
    expect(await screen.findByRole('heading', { name: 'Sign in' })).toBeInTheDocument()
  })
})
