import { afterEach, describe, expect, it, vi } from 'vitest'
import { setAccessToken } from '../../lib/apiClient'
import { loginSession, registerUser } from './authService'

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

afterEach(() => {
  setAccessToken(null)
  vi.unstubAllGlobals()
})

describe('authService', () => {
  it('sends login credentials and loads the profile with the returned token', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse({ accessToken: 'access-token', tokenType: 'Bearer', expiresIn: 900 }))
      .mockResolvedValueOnce(jsonResponse({
        id: 7,
        userName: 'darshan_01',
        email: 'darshan@example.com',
        sentimentAnalysis: false,
        roles: ['USER'],
      }))
    vi.stubGlobal('fetch', fetchMock)

    await loginSession({ userName: 'darshan_01', password: 'StrongPass123!' })

    expect(fetchMock.mock.calls[0][0]).toContain('/auth/login')
    expect(fetchMock.mock.calls[0][1]).toMatchObject({
      method: 'POST',
      body: JSON.stringify({ userName: 'darshan_01', password: 'StrongPass123!' }),
    })
    const profileHeaders = fetchMock.mock.calls[1][1]?.headers as Headers
    expect(profileHeaders.get('Authorization')).toBe('Bearer access-token')
  })

  it('sends the complete registration contract', async () => {
    const request = {
      userName: 'darshan_01',
      email: 'darshan@example.com',
      password: 'StrongPass123!',
      sentimentAnalysis: true,
    }
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({
      id: 7,
      userName: request.userName,
      email: request.email,
      sentimentAnalysis: true,
      roles: ['USER'],
    }, 201))
    vi.stubGlobal('fetch', fetchMock)

    await registerUser(request)

    expect(fetchMock.mock.calls[0][0]).toContain('/auth/register')
    expect(fetchMock.mock.calls[0][1]).toMatchObject({
      method: 'POST',
      body: JSON.stringify(request),
    })
  })
})
