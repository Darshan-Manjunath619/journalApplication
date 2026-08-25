import { describe, expect, it } from 'vitest'
import { parseDashboardQuery, updateDashboardQuery } from './dashboardQueryState'

describe('dashboardQueryState', () => {
  it('parses a valid dashboard URL into a journal request', () => {
    const result = parseDashboardQuery(new URLSearchParams('page=2&q=spring&sort=title,asc&tag=3&favorite=true&from=2026-08-01&to=2026-08-25'))

    expect(result).toEqual({ page: 2, size: 10, query: 'spring', sortField: 'title', sortDirection: 'asc', tagId: 3, favorite: true, fromDate: '2026-08-01', toDate: '2026-08-25' })
  })

  it('uses safe defaults for invalid URL values', () => {
    const result = parseDashboardQuery(new URLSearchParams('page=-1&sort=password,sideways&tag=0&favorite=maybe&from=2026-02-30&to=bad'))

    expect(result).toEqual({ page: 0, size: 10, query: '', sortField: 'createdAt', sortDirection: 'desc', tagId: null, favorite: null, fromDate: '', toDate: '' })
  })

  it('updates selected values while preserving unrelated query state', () => {
    const result = updateDashboardQuery(new URLSearchParams('q=spring&sort=title,asc&page=2'), { tag: 3, favorite: true, page: null })

    expect(result.toString()).toBe('q=spring&sort=title%2Casc&tag=3&favorite=true')
  })
})
