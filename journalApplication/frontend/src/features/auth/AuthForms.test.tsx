import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../lib/apiClient'
import { LoginPage } from '../../pages/LoginPage'
import { RegisterPage } from '../../pages/RegisterPage'
import { AuthContext } from './AuthContext'
import type { AuthContextValue } from './AuthContext'

function renderAuthPage(path: string, overrides: Partial<AuthContextValue> = {}) {
  const value: AuthContextValue = {
    user: null,
    status: 'unauthenticated',
    error: null,
    retryBootstrap: vi.fn(),
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    updateProfile: vi.fn(),
    changePassword: vi.fn(),
    ...overrides,
  }

  render(
    <AuthContext.Provider value={value}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/dashboard" element={<h1>Dashboard destination</h1>} />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
  return value
}

describe('authentication forms', () => {
  it('blocks login when frontend validation fails', async () => {
    const user = userEvent.setup()
    const login = vi.fn()
    renderAuthPage('/login', { login })

    await user.type(screen.getByLabelText('Username'), 'invalid name')
    await user.type(screen.getByLabelText('Password'), 'short')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findAllByRole('alert')).toHaveLength(2)
    expect(login).not.toHaveBeenCalled()
  })

  it('submits valid credentials and navigates to the dashboard', async () => {
    const user = userEvent.setup()
    const login = vi.fn().mockResolvedValue(undefined)
    renderAuthPage('/login', { login })

    await user.type(screen.getByLabelText('Username'), ' darshan_01 ')
    await user.type(screen.getByLabelText('Password'), 'StrongPass123!')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    await waitFor(() => expect(login).toHaveBeenCalledWith({
      userName: 'darshan_01',
      password: 'StrongPass123!',
    }))
    expect(await screen.findByRole('heading', { name: 'Dashboard destination' })).toBeInTheDocument()
  })

  it('shows a safe registration conflict returned by the backend', async () => {
    const user = userEvent.setup()
    const register = vi.fn().mockRejectedValue(new ApiError(409, { detail: 'Username already exists' }))
    renderAuthPage('/register', { register })

    await user.type(screen.getByLabelText('Username'), 'darshan_01')
    await user.type(screen.getByLabelText('Email'), 'darshan@example.com')
    await user.type(screen.getByLabelText('Password'), 'StrongPass123!')
    await user.click(screen.getByRole('button', { name: 'Create account' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Username already exists')
  })

  it('submits registration and redirects to login with confirmation', async () => {
    const user = userEvent.setup()
    const register = vi.fn().mockResolvedValue(undefined)
    renderAuthPage('/register', { register })

    await user.type(screen.getByLabelText('Username'), 'darshan_01')
    await user.type(screen.getByLabelText('Email'), 'darshan@example.com')
    await user.type(screen.getByLabelText('Password'), 'StrongPass123!')
    await user.click(screen.getByText('Opt in to future sentiment-analysis emails. This feature is currently disabled.'))
    await user.click(screen.getByRole('button', { name: 'Create account' }))

    await waitFor(() => expect(register).toHaveBeenCalledWith({
      userName: 'darshan_01',
      email: 'darshan@example.com',
      password: 'StrongPass123!',
      sentimentAnalysis: true,
    }))
    expect(await screen.findByRole('status')).toHaveTextContent('Account created')
  })
})
