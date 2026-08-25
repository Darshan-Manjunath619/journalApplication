import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
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

function renderDashboard() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, gcTime: 0 } } })
  render(<QueryClientProvider client={queryClient}><MemoryRouter><DashboardPage /></MemoryRouter></QueryClientProvider>)
  return queryClient
}

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

afterEach(() => vi.unstubAllGlobals())

describe('DashboardPage', () => {
  it('shows a loading state while journals are requested', () => {
    vi.stubGlobal('fetch', vi.fn(() => new Promise<Response>(() => undefined)))
    renderDashboard()

    expect(screen.getByRole('status')).toHaveTextContent('Loading journals')
  })

  it('shows an empty state for a successful empty page', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(pageResponse([]))))
    renderDashboard()

    expect(await screen.findByRole('heading', { name: 'No journal entries yet' })).toBeInTheDocument()
  })

  it('renders journal details and loads the next page', async () => {
    const browser = userEvent.setup()
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse(pageResponse([journal(1, 'First entry')], { totalElements: 2, totalPages: 2, last: false })))
      .mockResolvedValueOnce(jsonResponse(pageResponse([journal(2, 'Second entry')], { page: 1, totalElements: 2, totalPages: 2, first: false })))
    vi.stubGlobal('fetch', fetchMock)
    renderDashboard()

    expect(await screen.findByRole('heading', { name: 'First entry' })).toBeInTheDocument()
    expect(screen.getByText('Favorite')).toBeInTheDocument()
    expect(screen.getByText('Personal')).toBeInTheDocument()
    await browser.click(screen.getByRole('button', { name: 'Next' }))

    expect(await screen.findByRole('heading', { name: 'Second entry' })).toBeInTheDocument()
    expect(String(fetchMock.mock.calls[1][0])).toContain('page=1')
  })

  it('shows an error and retries the request', async () => {
    const browser = userEvent.setup()
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse({ message: 'failure' }, 500))
      .mockResolvedValueOnce(jsonResponse(pageResponse([])))
    vi.stubGlobal('fetch', fetchMock)
    renderDashboard()

    expect(await screen.findByRole('alert')).toHaveTextContent('Unable to load your journals')
    await browser.click(screen.getByRole('button', { name: 'Try again' }))

    expect(await screen.findByRole('heading', { name: 'No journal entries yet' })).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledTimes(2)
  })
})
