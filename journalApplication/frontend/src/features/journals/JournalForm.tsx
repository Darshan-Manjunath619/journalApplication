import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import type { JournalTag } from './journalTypes'
import { journalSchema } from './journalSchemas'
import type { JournalFormValues } from './journalSchemas'
import { FormField } from '../../components/forms/FormField'

type JournalFormProps = {
  tags: JournalTag[]
  isSubmitting: boolean
  serverError: string | null
  initialValues?: JournalFormValues
  initialTagIds?: number[]
  initialFavorite?: boolean
  showFavorite?: boolean
  submitLabel?: string
  cancelTo?: string
  onSubmit: (values: JournalFormValues, tagIds: number[], favorite: boolean) => Promise<void>
}

export function JournalForm({ tags, isSubmitting, serverError, initialValues, initialTagIds = [], initialFavorite = false, showFavorite = false, submitLabel = 'Create journal', cancelTo = '/dashboard', onSubmit }: JournalFormProps) {
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>(initialTagIds)
  const [favorite, setFavorite] = useState(initialFavorite)
  const form = useForm<JournalFormValues>({
    resolver: zodResolver(journalSchema),
    defaultValues: initialValues ?? { title: '', content: '' },
  })

  function toggleTag(id: number) {
    setSelectedTagIds((current) => current.includes(id)
      ? current.filter((tagId) => tagId !== id)
      : [...current, id])
  }

  return (
    <form className={'space-y-5'} noValidate onSubmit={form.handleSubmit((values) => onSubmit(values, selectedTagIds, favorite))}>
      {serverError && <p className={'rounded-lg bg-rose-50 p-3 text-sm font-medium text-rose-700'} role={'alert'}>{serverError}</p>}
      <FormField id={'journal-title'} label={'Title'} maxLength={160} error={form.formState.errors.title?.message} {...form.register('title')} />
      <div className={'space-y-2'}>
        <label className={'block text-sm font-semibold text-slate-700'} htmlFor={'journal-content'}>Content</label>
        <textarea className={'min-h-64 w-full resize-y rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-slate-900 outline-none transition focus:border-indigo-500 focus:ring-3 focus:ring-indigo-100'} id={'journal-content'} maxLength={20_000} aria-invalid={Boolean(form.formState.errors.content)} {...form.register('content')} />
        {form.formState.errors.content && <p className={'text-sm font-medium text-rose-600'} role={'alert'}>{form.formState.errors.content.message}</p>}
      </div>
      {tags.length > 0 && (
        <fieldset>
          <legend className={'text-sm font-semibold text-slate-700'}>Tags</legend>
          <div className={'mt-2 flex flex-wrap gap-2'}>
            {tags.map((tag) => (
              <label className={'cursor-pointer rounded-full border border-slate-300 px-3 py-1.5 text-sm has-checked:border-indigo-600 has-checked:bg-indigo-50 has-checked:text-indigo-700'} key={tag.id}>
                <input className={'sr-only'} type={'checkbox'} checked={selectedTagIds.includes(tag.id)} onChange={() => toggleTag(tag.id)} />
                {tag.name}
              </label>
            ))}
          </div>
        </fieldset>
      )}
      {showFavorite && <label className={'flex items-center gap-3 text-sm font-semibold text-slate-700'}><input className={'size-4 rounded border-slate-300 text-indigo-600'} type={'checkbox'} checked={favorite} onChange={(event) => setFavorite(event.target.checked)} />Favorite journal</label>}
      <div className={'flex flex-wrap gap-3'}>
        <button className={'rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white disabled:cursor-not-allowed disabled:opacity-60'} type={'submit'} disabled={isSubmitting}>
          {isSubmitting ? 'Saving...' : submitLabel}
        </button>
        <Link className={'rounded-lg border border-slate-300 px-4 py-2.5 font-semibold text-slate-700 no-underline'} to={cancelTo}>Cancel</Link>
      </div>
    </form>
  )
}
