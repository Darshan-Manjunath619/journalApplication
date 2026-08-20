import { forwardRef } from 'react'
import type { InputHTMLAttributes } from 'react'

type FormFieldProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'id'> & {
  id: string
  label: string
  error?: string
}

export const FormField = forwardRef<HTMLInputElement, FormFieldProps>(
  function FormField({ id, label, error, ...inputProps }, ref) {
    const errorId = `${id}-error`

    return (
      <div className="form-field">
        <label htmlFor={id}>{label}</label>
        <input
          {...inputProps}
          id={id}
          ref={ref}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? errorId : inputProps['aria-describedby']}
        />
        {error && <p id={errorId} className="field-error" role="alert">{error}</p>}
      </div>
    )
  },
)
