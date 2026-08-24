import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuthContext } from '../auth/AuthContext'
import type { AuthContextValue } from '../auth/AuthContext'
import { ProfilePage } from '../../pages/ProfilePage'
import { LoginPage } from '../../pages/LoginPage'

function renderProfile(overrides: Partial<AuthContextValue> = {}) {
  const auth: AuthContextValue = {
    user: { id: 7, userName: 'darshan_01', email: 'old@example.com', sentimentAnalysis: false, roles: ['USER'] },
    status: 'authenticated',
    error: null,
    retryBootstrap: vi.fn(),
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    updateProfile: vi.fn().mockResolvedValue(undefined),
    changePassword: vi.fn().mockResolvedValue(undefined),
    ...overrides,
  }
  render(
    <AuthContext.Provider value={auth}>
      <MemoryRouter initialEntries={['/profile']}>
        <Routes>
          <Route path={'/profile'} element={<ProfilePage />} />
          <Route path={'/login'} element={<LoginPage />} />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>,
  )
  return auth
}

describe('ProfilePage', () => {
  it('shows identity fields and submits editable profile values', async () => {
    const browser = userEvent.setup()
    const auth = renderProfile()

    expect(screen.getByText('darshan_01')).toBeInTheDocument()
    expect(screen.getByText('USER')).toBeInTheDocument()
    const email = screen.getByLabelText('Email')
    await browser.clear(email)
    await browser.type(email, 'new@example.com')
    await browser.click(screen.getByText('Enable sentiment-analysis emails when this optional feature becomes available.'))
    await browser.click(screen.getByRole('button', { name: 'Save profile' }))

    await waitFor(() => expect(auth.updateProfile).toHaveBeenCalledWith({
      email: 'new@example.com',
      sentimentAnalysis: true,
    }))
    expect(await screen.findByRole('status')).toHaveTextContent('Profile updated successfully')
  })

  it('validates password confirmation before calling the backend', async () => {
    const browser = userEvent.setup()
    const auth = renderProfile()

    await browser.type(screen.getByLabelText('Current password'), 'CurrentPass123!')
    await browser.type(screen.getByLabelText('New password'), 'NewStrongPass456!')
    await browser.type(screen.getByLabelText('Confirm new password'), 'DifferentPass789!')
    await browser.click(screen.getByRole('button', { name: 'Change password' }))

    expect(await screen.findByText('Passwords do not match')).toBeInTheDocument()
    expect(auth.changePassword).not.toHaveBeenCalled()
  })

  it('changes the password and redirects to login', async () => {
    const browser = userEvent.setup()
    const auth = renderProfile()

    await browser.type(screen.getByLabelText('Current password'), 'CurrentPass123!')
    await browser.type(screen.getByLabelText('New password'), 'NewStrongPass456!')
    await browser.type(screen.getByLabelText('Confirm new password'), 'NewStrongPass456!')
    await browser.click(screen.getByRole('button', { name: 'Change password' }))

    await waitFor(() => expect(auth.changePassword).toHaveBeenCalledWith({
      currentPassword: 'CurrentPass123!',
      newPassword: 'NewStrongPass456!',
    }))
    expect(await screen.findByText('Password changed. Sign in again with your new password.')).toBeInTheDocument()
  })
})
