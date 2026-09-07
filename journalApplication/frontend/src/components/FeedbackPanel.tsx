import type { ReactNode } from 'react'

type FeedbackPanelProps = {
  title: string
  children?: ReactNode
  role?: 'alert' | 'status'
  actionLabel?: string
  onAction?: () => void
  actionDisabled?: boolean
  footer?: ReactNode
}

export function FeedbackPanel({ title, children, role, actionLabel, onAction, actionDisabled = false, footer }: FeedbackPanelProps) {
  return (
    <section className={'rounded-2xl border border-slate-200 bg-white p-8 text-center text-slate-600 shadow-sm'} role={role} aria-busy={role === 'status' ? true : undefined}>
      <h1 className={'text-2xl font-bold text-slate-900'}>{title}</h1>
      {children && <div className={'mt-2'}>{children}</div>}
      {actionLabel && onAction && <button className={'mt-5 rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60'} type={'button'} disabled={actionDisabled} onClick={onAction}>{actionLabel}</button>}
      {footer && <div className={'mt-5'}>{footer}</div>}
    </section>
  )
}
