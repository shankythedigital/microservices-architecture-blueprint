/**
 * Serializes values for Java {@code LocalDate} / JSR-310 in JSON: prefer {@code yyyy-MM-dd}.
 */
export function toApiLocalDate(value: string | undefined | null): string | undefined {
  if (value == null || value === '') return undefined
  const s = value.trim()
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s
  const d = new Date(s)
  if (Number.isNaN(d.getTime())) return undefined
  return d.toISOString().slice(0, 10)
}

/** Readable label for ISO date or date-time strings from the API (Java LocalDate / LocalDateTime JSON). */
export function formatApiDateForDisplay(iso: string | undefined | null): string {
  if (iso == null || iso === '') return '—'
  if (/^\d{4}-\d{2}-\d{2}$/.test(iso.trim())) return iso.trim()
  if (/^\d{4}-\d{2}-\d{2}T/.test(iso.trim())) {
    const s = iso.trim()
    return s.length >= 16 ? s.slice(0, 16).replace('T', ' ') : s
  }
  const d = new Date(iso)
  if (!Number.isNaN(d.getTime())) return d.toISOString().slice(0, 16).replace('T', ' ')
  return iso
}
