import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { EditJournalPage } from '../../pages/EditJournalPage'
import { JournalDetailPage } from '../../pages/JournalDetailPage'

function entry() {
  return {
    id: 12,
    title: 'Original title',
    content: 'Original content',
    date: '2026-08-25T10:00:00',
    createdAt: '2026-08-25T10:00:00Z',
    updatedAt: '2026-08-25T10:00:00Z',
    favorite: false,
    tags: [{ id: 2, name: 'Learning', createdAt: '2026-08-25T09:00:00Z' }],
  }
}

function renderRoute(path: string) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false, staleTime: 30_000 }, mutations: { retry: false } } })
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path={'/journals/:id'} element={<JournalDetailPage />} />
          <Route path={'/journals/:id/edit'} element={<EditJournalPage />} />
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

describe('journal edit and delete workflows', () => {
  it('loads existing values, updates the journal, and returns to detail', async () => {
    const browser = userEvent.setup()
    const original = entry()
    const updated = { ...original, title: 'Updated title', favorite: true }
    const fetchMock = vi.fn(async (input: string | URL | Request, init?: RequestInit) => {
      const url = String(input)
      if (url.endsWith('/tags')) return jsonResponse(original.tags)
      if (url.endsWith('/journals/12') && init?.method === 'PATCH') return jsonResponse(updated)
      return jsonResponse(original)
    })
    vi.stubGlobal('fetch', fetchMock)
    renderRoute('/journals/12/edit')

    const title = await screen.findByLabelText('Title')
    expect(title).toHaveValue(original.title)
    await browser.clear(title)
    await browser.type(title, updated.title)
    await browser.click(screen.getByText('Favorite journal'))
    await browser.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByRole('status')).toHaveTextContent('Journal updated successfully')
    expect(screen.getByRole('heading', { name: updated.title })).toBeInTheDocument()
    const updateCall = fetchMock.mock.calls.find(([url, init]) => String(url).endsWith('/journals/12') && init?.method === 'PATCH')
    expect(JSON.parse(String(updateCall?.[1]?.body))).toEqual({
      title: updated.title,
      content: original.content,
      tagIds: [2],
      favorite: true,
    })
  })

  it('requires confirmation before deleting and then returns to dashboard', async () => {
    const browser = userEvent.setup()
    const original = entry()
    const fetchMock = vi.fn(async (_input: string | URL | Request, init?: RequestInit) =>
      init?.method === 'DELETE' ? new Response(null, { status: 204 }) : jsonResponse(original))
    vi.stubGlobal('fetch', fetchMock)
    renderRoute('/journals/12')

    await browser.click(await screen.findByRole('button', { name: 'Delete journal' }))
    expect(screen.getByRole('dialog', { name: 'Delete this journal permanently?' })).toBeInTheDocument()
    expect(fetchMock.mock.calls.filter(([, init]) => init?.method === 'DELETE')).toHaveLength(0)
    await browser.click(screen.getByRole('button', { name: 'Confirm delete' }))

    expect(await screen.findByRole('heading', { name: 'Dashboard destination' })).toBeInTheDocument()
    await waitFor(() => expect(fetchMock.mock.calls.filter(([, init]) => init?.method === 'DELETE')).toHaveLength(1))
  })

  it('moves focus into delete confirmation and restores it after Escape', async () => {
    const browser = userEvent.setup()
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(entry())))
    renderRoute('/journals/12')

    const deleteButton = await screen.findByRole('button', { name: 'Delete journal' })
    await browser.click(deleteButton)

    expect(screen.getByRole('button', { name: 'Confirm delete' })).toHaveFocus()
    expect(screen.getByRole('dialog', { name: 'Delete this journal permanently?' })).toHaveAttribute('aria-modal', 'true')
    await browser.keyboard('{Escape}')

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(deleteButton).toHaveFocus()
  })

  it('shows the same safe not-found state for an unowned edit URL', async () => {
    vi.stubGlobal('fetch', vi.fn(async (input: string | URL | Request) =>
      String(input).endsWith('/tags')
        ? jsonResponse([])
        : jsonResponse({ title: 'Not Found', status: 404 }, 404, 'application/problem+json')))
    renderRoute('/journals/999/edit')

    expect(await screen.findByRole('heading', { name: 'Journal not found' })).toBeInTheDocument()
  })
})
