import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { setAccessToken } from '../../lib/apiClient'
import { AuthProvider } from './AuthProvider'
import { useAuth } from './useAuth'

function AuthStateProbe() {
  const { user, status, error } = useAuth()
  return <div><p>status:{status}</p><p>user:{user?.userName ?? 'none'}</p><p>error:{error?.message ?? 'none'}</p></div>
}

function jsonResponse(body: unknown, status = 200, contentType = 'application/json') {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': contentType },
  })
}

afterEach(() => {
  setAccessToken(null)
  vi.unstubAllGlobals()
})

describe('AuthProvider session bootstrap', () => {
  it('restores the user after refreshing the access token', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse({ accessToken: 'new-access-token', tokenType: 'Bearer', expiresIn: 900 }))
      .mockResolvedValueOnce(jsonResponse({
        id: 7,
        userName: 'darshan_01',
        email: 'darshan@example.com',
        sentimentAnalysis: false,
        roles: ['USER'],
      }))
    vi.stubGlobal('fetch', fetchMock)

    render(<AuthProvider><AuthStateProbe /></AuthProvider>)

    expect(await screen.findByText('status:authenticated')).toBeInTheDocument()
    expect(screen.getByText('user:darshan_01')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledTimes(2)
    expect(fetchMock.mock.calls[0][0]).toContain('/auth/refresh')
    expect(fetchMock.mock.calls[1][0]).toContain('/users/me')
    const profileHeaders = fetchMock.mock.calls[1][1]?.headers as Headers
    expect(profileHeaders.get('Authorization')).toBe('Bearer new-access-token')
  })

  it('becomes unauthenticated when no valid refresh session exists', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(
      { title: 'Unauthorized', status: 401 },
      401,
      'application/problem+json',
    ))
    vi.stubGlobal('fetch', fetchMock)

    render(<AuthProvider><AuthStateProbe /></AuthProvider>)

    expect(await screen.findByText('status:unauthenticated')).toBeInTheDocument()
    expect(screen.getByText('user:none')).toBeInTheDocument()
    expect(screen.getByText('error:none')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('exposes a retryable error when the backend is unavailable', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    render(<AuthProvider><AuthStateProbe /></AuthProvider>)

    expect(await screen.findByText('status:error')).toBeInTheDocument()
    expect(screen.getByText('error:Failed to fetch')).toBeInTheDocument()
  })
})
