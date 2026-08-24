import { afterEach, describe, expect, it, vi } from 'vitest'
import { setAccessToken } from '../../lib/apiClient'
import { changePasswordRequest, updateProfileRequest } from './profileService'

afterEach(() => {
  setAccessToken(null)
  vi.unstubAllGlobals()
})

describe('profileService', () => {
  it('sends only editable profile fields', async () => {
    setAccessToken('access-token')
    const request = { email: 'new@example.com', sentimentAnalysis: true }
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      id: 7, userName: 'darshan_01', roles: ['USER'], ...request,
    }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
    vi.stubGlobal('fetch', fetchMock)

    await updateProfileRequest(request)

    expect(fetchMock.mock.calls[0][0]).toContain('/users/me')
    expect(fetchMock.mock.calls[0][1]).toMatchObject({ method: 'PATCH', body: JSON.stringify(request) })
  })

  it('sends the password change contract', async () => {
    setAccessToken('access-token')
    const request = { currentPassword: 'CurrentPass123!', newPassword: 'NewStrongPass456!' }
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 }))
    vi.stubGlobal('fetch', fetchMock)

    await changePasswordRequest(request)

    expect(fetchMock.mock.calls[0][0]).toContain('/users/me/password')
    expect(fetchMock.mock.calls[0][1]).toMatchObject({ method: 'PATCH', body: JSON.stringify(request) })
  })
})
