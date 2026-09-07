import { zodResolver } from '@hookform/resolvers/zod'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useForm } from 'react-hook-form'
import { describe, expect, it, vi } from 'vitest'
import { z } from 'zod'
import { emailSchema } from '../../validation/commonSchemas'
import { FormField } from './FormField'

const testSchema = z.object({ email: emailSchema })
type TestValues = z.infer<typeof testSchema>

function TestForm({ onSubmit }: { onSubmit: (values: TestValues) => void }) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<TestValues>({
    resolver: zodResolver(testSchema),
    defaultValues: { email: '' },
  })

  return (
    <form noValidate onSubmit={handleSubmit(onSubmit)}>
      <FormField
        id="email"
        label="Email"
        type="email"
        error={errors.email?.message}
        {...register('email')}
      />
      <button type="submit">Continue</button>
    </form>
  )
}

describe('FormField with schema validation', () => {
  it('connects its accessible label to the input', () => {
    render(<TestForm onSubmit={vi.fn()} />)

    expect(screen.getByLabelText('Email')).toHaveAttribute('id', 'email')
  })

  it('shows a validation error and prevents invalid submission', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn()
    render(<TestForm onSubmit={onSubmit} />)

    await user.type(screen.getByLabelText('Email'), 'invalid-email')
    await user.click(screen.getByRole('button', { name: 'Continue' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Enter a valid email address')
    expect(screen.getByLabelText('Email')).toHaveAttribute('aria-invalid', 'true')
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('submits normalized valid input', async () => {
    const user = userEvent.setup()
    const onSubmit = vi.fn()
    render(<TestForm onSubmit={onSubmit} />)

    await user.type(screen.getByLabelText('Email'), ' person@example.com ')
    await user.click(screen.getByRole('button', { name: 'Continue' }))

    expect(onSubmit.mock.calls[0][0]).toEqual({ email: 'person@example.com' })
  })
})
