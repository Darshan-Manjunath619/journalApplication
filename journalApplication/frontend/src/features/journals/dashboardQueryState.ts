import type { JournalPageRequest } from './journalTypes'

const SORT_FIELDS = new Set(['createdAt', 'updatedAt', 'title'])

export function parseDashboardQuery(parameters: URLSearchParams): JournalPageRequest {
  const pageValue = Number(parameters.get('page'))
  const tagValue = Number(parameters.get('tag'))
  const [requestedField, requestedDirection] = (parameters.get('sort') ?? '').split(',')
  const favoriteValue = parameters.get('favorite')

  return {
    page: Number.isInteger(pageValue) && pageValue >= 0 ? pageValue : 0,
    size: 10,
    query: parameters.get('q')?.trim() ?? '',
    sortField: SORT_FIELDS.has(requestedField) ? requestedField as JournalPageRequest['sortField'] : 'createdAt',
    sortDirection: requestedDirection === 'asc' || requestedDirection === 'desc' ? requestedDirection : 'desc',
    tagId: Number.isInteger(tagValue) && tagValue > 0 ? tagValue : null,
    favorite: favoriteValue === 'true' ? true : favoriteValue === 'false' ? false : null,
    fromDate: validDate(parameters.get('from')),
    toDate: validDate(parameters.get('to')),
  }
}

export function updateDashboardQuery(current: URLSearchParams, changes: Record<string, string | number | boolean | null>) {
  const next = new URLSearchParams(current)
  for (const [name, value] of Object.entries(changes)) {
    if (value === null || value === '') next.delete(name)
    else next.set(name, String(value))
  }
  return next
}

function validDate(value: string | null) {
  if (!value || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return ''
  const date = new Date(`${value}T00:00:00.000Z`)
  return Number.isNaN(date.getTime()) || date.toISOString().slice(0, 10) !== value ? '' : value
}
