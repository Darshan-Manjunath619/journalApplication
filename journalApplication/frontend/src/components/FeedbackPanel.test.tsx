import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { FeedbackPanel } from './FeedbackPanel'

describe('FeedbackPanel', () => {
  it('announces an error and runs its retry action', async () => {
    const retry = vi.fn()
    render(<FeedbackPanel title={'Unable to load'} role={'alert'} actionLabel={'Try again'} onAction={retry}>The server is unavailable.</FeedbackPanel>)

    expect(screen.getByRole('alert')).toHaveTextContent('Unable to load')
    await userEvent.setup().click(screen.getByRole('button', { name: 'Try again' }))
    expect(retry).toHaveBeenCalledOnce()
  })

  it('marks a loading panel as busy', () => {
    render(<FeedbackPanel title={'Loading'} role={'status'}>Please wait...</FeedbackPanel>)

    expect(screen.getByRole('status')).toHaveAttribute('aria-busy', 'true')
  })
})
