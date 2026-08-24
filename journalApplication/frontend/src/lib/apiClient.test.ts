import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiClient, setAccessToken, setSessionExpiredHandler } from './apiClient'

function jsonResponse(body: unknown, status = 200, contentType = 'application/json') {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': contentType } })
}

afterEach(() => {
  setAccessToken(null)
  setSessionExpiredHandler(null)
  vi.unstubAllGlobals()
})

describe('apiClient access-token refresh', () => {
  it('shares one refresh request and retries concurrent unauthorized requests', async () => {
    setAccessToken('expired-token')
    const fetchMock = vi.fn(async (input: string | URL | Request, init?: RequestInit) => {
      const url = String(input)
      if (url.endsWith('/auth/refresh')) {
        return jsonResponse({ accessToken: 'new-token' })
      }
      const headers = init?.headers as Headers
      if (headers.get('Authorization') === 'Bearer expired-token') {
        return jsonResponse({ status: 401 }, 401, 'application/problem+json')
      }
      return jsonResponse({ ok: true })
    })
    vi.stubGlobal('fetch', fetchMock)

    await Promise.all([apiClient.get('/journals'), apiClient.get('/users/me')])

    const refreshCalls = fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/auth/refresh'))
    expect(refreshCalls).toHaveLength(1)
    const retriedCalls = fetchMock.mock.calls.filter(([, init]) =>
      new Headers(init?.headers).get('Authorization') === 'Bearer new-token')
    expect(retriedCalls).toHaveLength(2)
  })

  it('clears the session when refresh is rejected', async () => {
    setAccessToken('expired-token')
    const onSessionExpired = vi.fn()
    setSessionExpiredHandler(onSessionExpired)
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse({ status: 401 }, 401, 'application/problem+json'))
      .mockResolvedValueOnce(jsonResponse({ status: 401 }, 401, 'application/problem+json'))
    vi.stubGlobal('fetch', fetchMock)

    await expect(apiClient.get('/journals')).rejects.toMatchObject({ status: 401 })

    expect(onSessionExpired).toHaveBeenCalledOnce()
    expect(fetchMock).toHaveBeenCalledTimes(2)
  })
})
