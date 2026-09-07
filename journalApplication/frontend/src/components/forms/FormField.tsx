import { forwardRef } from 'react'
import type { InputHTMLAttributes } from 'react'

type FormFieldProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'id'> & {
  id: string
  label: string
  error?: string
}

export const FormField = forwardRef<HTMLInputElement, FormFieldProps>(
  function FormField({ id, label, error, className, ...inputProps }, ref) {
    const errorId = `${id}-error`

    return (
      <div className={'space-y-2'}>
        <label className={'block text-sm font-semibold text-slate-700'} htmlFor={id}>{label}</label>
        <input
          {...inputProps}
          id={id}
          ref={ref}
          className={`w-full rounded-lg border bg-white px-3 py-2.5 text-slate-900 outline-none transition placeholder:text-slate-400 disabled:cursor-not-allowed disabled:bg-slate-100 ${
            error
              ? 'border-rose-500 focus:border-rose-500 focus:ring-3 focus:ring-rose-100'
              : 'border-slate-300 focus:border-indigo-500 focus:ring-3 focus:ring-indigo-100'
          } ${className ?? ''}`}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? errorId : inputProps['aria-describedby']}
        />
        {error && <p id={errorId} className={'text-sm font-medium text-rose-600'} role={'alert'}>{error}</p>}
      </div>
    )
  },
)
