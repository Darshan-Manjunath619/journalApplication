import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { CreateJournalPage } from '../../pages/CreateJournalPage'
import { JournalDetailPage } from '../../pages/JournalDetailPage'

function journalResponse(id = 9) {
  return {
    id,
    title: 'Learning React Query',
    content: 'Server state belongs in a query cache.',
    date: '2026-08-25T10:00:00',
    createdAt: '2026-08-25T10:00:00Z',
    updatedAt: '2026-08-25T10:00:00Z',
    favorite: false,
    tags: [{ id: 2, name: 'Learning', createdAt: '2026-08-25T09:00:00Z' }],
  }
}

function renderJournalRoute(path: string) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, staleTime: 30_000 }, mutations: { retry: false } } })
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path={'/journals/new'} element={<CreateJournalPage />} />
          <Route path={'/journals/:id'} element={<JournalDetailPage />} />
          <Route path={'/dashboard'} element={<h1>Dashboard destination</h1>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

function jsonResponse(body: unknown, status = 200, contentType = 'application/json') {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': contentType } })
}

afterEach(() => vi.unstubAllGlobals())

describe('journal create and detail workflows', () => {
  it('validates, creates with selected tags, and opens the new journal', async () => {
    const browser = userEvent.setup()
    const created = journalResponse()
    const fetchMock = vi.fn(async (input: string | URL | Request, init?: RequestInit) => {
      const url = String(input)
      if (url.endsWith('/tags')) return jsonResponse(created.tags)
      if (url.endsWith('/journals') && init?.method === 'POST') return jsonResponse(created, 201)
      return jsonResponse(created)
    })
    vi.stubGlobal('fetch', fetchMock)
    renderJournalRoute('/journals/new')

    await browser.type(screen.getByLabelText('Title'), created.title)
    await browser.type(screen.getByLabelText('Content'), created.content)
    await browser.click(await screen.findByText('Learning'))
    await browser.click(screen.getByRole('button', { name: 'Create journal' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Journal created successfully')
    expect(screen.getByRole('heading', { name: created.title })).toBeInTheDocument()
    const createCall = fetchMock.mock.calls.find(([url, init]) => String(url).endsWith('/journals') && init?.method === 'POST')
    expect(JSON.parse(String(createCall?.[1]?.body))).toEqual({ title: created.title, content: created.content, tagIds: [2] })
  })

  it('blocks creation when the title is blank', async () => {
    const browser = userEvent.setup()
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse([]))
    vi.stubGlobal('fetch', fetchMock)
    renderJournalRoute('/journals/new')

    await browser.click(screen.getByRole('button', { name: 'Create journal' }))

    expect(await screen.findByText('Title is required')).toBeInTheDocument()
    await waitFor(() => expect(fetchMock.mock.calls.filter(([, init]) => init?.method === 'POST')).toHaveLength(0))
  })

  it('loads a journal detail by URL id', async () => {
    const entry = journalResponse(42)
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(entry)))
    renderJournalRoute('/journals/42')

    expect(await screen.findByRole('heading', { name: entry.title })).toBeInTheDocument()
    expect(screen.getByText(entry.content)).toBeInTheDocument()
    expect(screen.getByText('Learning')).toBeInTheDocument()
  })

  it('shows a safe not-found state for an unowned journal', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(
      { title: 'Not Found', status: 404 }, 404, 'application/problem+json',
    )))
    renderJournalRoute('/journals/404')

    expect(await screen.findByRole('heading', { name: 'Journal not found' })).toBeInTheDocument()
  })
})
