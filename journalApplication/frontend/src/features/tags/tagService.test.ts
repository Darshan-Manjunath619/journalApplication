import { afterEach, describe, expect, it, vi } from 'vitest'
import { getTags } from './tagService'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('tagService', () => {
  it('calls the extracted Journal Service', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify([]), {
      status: 200,
      headers: { 'Content-Type': 'application/json' },
    }))
    vi.stubGlobal('fetch', fetchMock)

    await getTags()

    expect(String(fetchMock.mock.calls[0][0]))
      .toBe('http://localhost:8081/journal/api/v1/tags')
  })
})
