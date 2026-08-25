import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { DashboardPage } from '../../pages/DashboardPage'
import { MemoryRouter } from 'react-router-dom'

function pageResponse(content: unknown[], overrides: Record<string, unknown> = {}) {
  return {
    content,
    page: 0,
    size: 10,
    totalElements: content.length,
    totalPages: content.length ? 1 : 0,
    first: true,
    last: true,
    ...overrides,
  }
}

function journal(id: number, title: string) {
  return {
    id,
    title,
    content: 'A journal reflection',
    date: '2026-08-24T10:00:00',
    createdAt: '2026-08-24T10:00:00Z',
    updatedAt: '2026-08-24T10:00:00Z',
    favorite: true,
    tags: [{ id: 3, name: 'Personal', createdAt: '2026-08-24T09:00:00Z' }],
  }
}

function renderDashboard(initialEntry = '/dashboard') {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, gcTime: 0 } } })
  render(<QueryClientProvider client={queryClient}><MemoryRouter initialEntries={[initialEntry]}><DashboardPage /></MemoryRouter></QueryClientProvider>)
  return queryClient
}

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

function routeFetch(journalResponses: Response[], tags: unknown[] = []) {
  let journalIndex = 0
  return vi.fn((input: string | URL | Request) => {
    if (String(input).endsWith('/tags')) return Promise.resolve(jsonResponse(tags))
    return Promise.resolve(journalResponses[Math.min(journalIndex++, journalResponses.length - 1)].clone())
  })
}

afterEach(() => vi.unstubAllGlobals())

describe('DashboardPage', () => {
  it('shows a loading state while journals are requested', () => {
    vi.stubGlobal('fetch', vi.fn(() => new Promise<Response>(() => undefined)))
    renderDashboard()

    expect(screen.getByRole('status')).toHaveTextContent('Loading journals')
  })

  it('shows an empty state for a successful empty page', async () => {
    vi.stubGlobal('fetch', routeFetch([jsonResponse(pageResponse([]))]))
    renderDashboard()

    expect(await screen.findByRole('heading', { name: 'No journal entries yet' })).toBeInTheDocument()
  })

  it('renders journal details and loads the next page', async () => {
    const browser = userEvent.setup()
    const fetchMock = routeFetch([
      jsonResponse(pageResponse([journal(1, 'First entry')], { totalElements: 2, totalPages: 2, last: false })),
      jsonResponse(pageResponse([journal(2, 'Second entry')], { page: 1, totalElements: 2, totalPages: 2, first: false })),
    ], [{ id: 3, name: 'Personal', createdAt: '2026-08-24T09:00:00Z' }])
    vi.stubGlobal('fetch', fetchMock)
    renderDashboard()

    expect(await screen.findByRole('heading', { name: 'First entry' })).toBeInTheDocument()
    expect(screen.getAllByText('Favorite')).toHaveLength(2)
    expect(screen.getAllByText('Personal')).toHaveLength(2)
    await browser.click(screen.getByRole('button', { name: 'Next' }))

    expect(await screen.findByRole('heading', { name: 'Second entry' })).toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([url]) => String(url).includes('page=1'))).toBe(true)
  })

  it('shows an error and retries the request', async () => {
    const browser = userEvent.setup()
    const fetchMock = routeFetch([jsonResponse({ message: 'failure' }, 500), jsonResponse(pageResponse([]))])
    vi.stubGlobal('fetch', fetchMock)
    renderDashboard()

    expect(await screen.findByRole('alert')).toHaveTextContent('Unable to load your journals')
    await browser.click(screen.getByRole('button', { name: 'Try again' }))

    expect(await screen.findByRole('heading', { name: 'No journal entries yet' })).toBeInTheDocument()
    expect(fetchMock.mock.calls.filter(([url]) => !String(url).endsWith('/tags'))).toHaveLength(2)
  })

  it('sends submitted search and allowed sorting and resets to page zero', async () => {
    const browser = userEvent.setup()
    const fetchMock = routeFetch([jsonResponse(pageResponse([]))])
    vi.stubGlobal('fetch', fetchMock)
    renderDashboard()
    await screen.findByRole('heading', { name: 'No journal entries yet' })

    await browser.type(screen.getByLabelText('Search journals'), ' spring security ')
    await browser.click(screen.getByRole('button', { name: 'Search' }))

    expect(await screen.findByRole('heading', { name: 'No matching journals' })).toBeInTheDocument()
    await waitFor(() => expect(String(fetchMock.mock.calls.at(-1)?.[0])).toContain('q=spring+security'))

    await browser.selectOptions(screen.getByLabelText('Sort by'), 'title')
    await browser.selectOptions(screen.getByLabelText('Direction'), 'asc')

    await waitFor(() => {
      const url = String(fetchMock.mock.calls.at(-1)?.[0])
      expect(url).toContain('page=0')
      expect(url).toContain('sort=title%2Casc')
      expect(url).toContain('q=spring+security')
    })
  })

  it('restores filters from the URL and sends them to the backend', async () => {
    const browser = userEvent.setup()
    const fetchMock = routeFetch([jsonResponse(pageResponse([]))], [{ id: 3, name: 'Personal', createdAt: '2026-08-24T09:00:00Z' }])
    vi.stubGlobal('fetch', fetchMock)
    renderDashboard('/dashboard?tag=3&favorite=true&from=2026-08-01&to=2026-08-25&page=2')

    await screen.findByRole('option', { name: 'Personal' })
    expect(screen.getByLabelText('Tag')).toHaveValue('3')
    expect(screen.getByLabelText('Favorite')).toHaveValue('true')
    expect(screen.getByLabelText('From date')).toHaveValue('2026-08-01')
    expect(screen.getByLabelText('To date')).toHaveValue('2026-08-25')
    await waitFor(() => {
      const url = fetchMock.mock.calls.map(([value]) => String(value)).find((value) => value.includes('/journals?')) ?? ''
      expect(url).toContain('tag=3')
      expect(url).toContain('favorite=true')
      expect(url).toContain('from=2026-08-01T00%3A00%3A00.000Z')
      expect(url).toContain('to=2026-08-25T23%3A59%3A59.999Z')
    })

    await browser.click(screen.getByRole('button', { name: 'Clear filters' }))
    await waitFor(() => {
      const urls = fetchMock.mock.calls.map(([value]) => String(value)).filter((value) => value.includes('/journals?'))
      const latest = urls.at(-1) ?? ''
      expect(latest).not.toContain('tag=')
      expect(latest).not.toContain('favorite=')
      expect(latest).not.toContain('from=')
      expect(latest).not.toContain('to=')
      expect(latest).toContain('page=0')
    })
  })
})
