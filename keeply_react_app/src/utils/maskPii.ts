export type PiiVariant = 'email' | 'mobile' | 'text'

export function maskEmail(email: string): string {
  const trimmed = email.trim()
  const at = trimmed.indexOf('@')
  if (at < 0) return maskText(trimmed)
  const local = trimmed.slice(0, at)
  const domain = trimmed.slice(at + 1)
  if (!domain) return maskText(trimmed)
  if (local.length <= 1) return `•@${domain}`
  return `${local[0]}${'•'.repeat(Math.min(local.length - 1, 8))}@${domain}`
}

export function maskMobile(mobile: string): string {
  const digits = mobile.replace(/\D/g, '')
  if (digits.length <= 2) return '••'
  const last2 = digits.slice(-2)
  const hiddenLen = Math.min(10, digits.length - 2)
  return `${'•'.repeat(hiddenLen)}${last2}`
}

export function maskText(value: string): string {
  const t = value.trim()
  if (t.length === 0) return ''
  if (t.length <= 1) return '•'
  if (t.length <= 4) return '•'.repeat(t.length)
  const mid = Math.min(t.length - 2, 10)
  return `${t[0]}${'•'.repeat(mid)}${t[t.length - 1]}`
}

export function maskPii(value: string, variant: PiiVariant): string {
  switch (variant) {
    case 'email':
      return maskEmail(value)
    case 'mobile':
      return maskMobile(value)
    default:
      return maskText(value)
  }
}

/** Pick masking strategy from a single string (e.g. account header label). */
export function inferPiiVariant(value: string): PiiVariant {
  const t = value.trim()
  if (t.includes('@')) return 'email'
  if (/^[\d\s+()\-]+$/.test(t) && /\d{7,}/.test(t.replace(/\D/g, ''))) return 'mobile'
  return 'text'
}
