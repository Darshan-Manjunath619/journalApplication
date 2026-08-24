import { afterEach, describe, expect, it, vi } from 'vitest'
import { setAccessToken } from '../../lib/apiClient'
import { getJournals } from './journalService'

afterEach(() => {
  setAccessToken(null)
  vi.unstubAllGlobals()
})

describe('journalService', () => {
  it('requests a bounded page using the stable default sort', async () => {
    setAccessToken('access-token')
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify({
      content: [], page: 1, size: 10, totalElements: 0, totalPages: 0, first: false, last: true,
    }), { status: 200, headers: { 'Content-Type': 'application/json' } }))
    vi.stubGlobal('fetch', fetchMock)

    await getJournals({ page: 1, size: 10 })

    const url = String(fetchMock.mock.calls[0][0])
    expect(url).toContain('/journals?')
    expect(url).toContain('page=1')
    expect(url).toContain('size=10')
    expect(url).toContain('sort=createdAt%2Cdesc')
  })
})
