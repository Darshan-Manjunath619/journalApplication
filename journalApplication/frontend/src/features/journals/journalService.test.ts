import { afterEach, describe, expect, it, vi } from 'vitest'
import { setAccessToken } from '../../lib/apiClient'
import { createJournal, getJournal, getJournals } from './journalService'

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

  it('uses the create and owned-detail endpoints', async () => {
    setAccessToken('access-token')
    const request = { title: 'Learning', content: 'Today I learned...', tagIds: [2] }
    const response = { id: 9, ...request, date: '2026-08-25T10:00:00', createdAt: '2026-08-25T10:00:00Z', updatedAt: '2026-08-25T10:00:00Z', favorite: false, tags: [] }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify(response), { status: 201, headers: { 'Content-Type': 'application/json' } }))
      .mockResolvedValueOnce(new Response(JSON.stringify(response), { status: 200, headers: { 'Content-Type': 'application/json' } }))
    vi.stubGlobal('fetch', fetchMock)

    await createJournal(request)
    await getJournal(9)

    expect(fetchMock.mock.calls[0][0]).toContain('/journals')
    expect(fetchMock.mock.calls[0][1]).toMatchObject({ method: 'POST', body: JSON.stringify(request) })
    expect(fetchMock.mock.calls[1][0]).toContain('/journals/9')
  })
})
