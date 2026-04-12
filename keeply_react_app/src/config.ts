const base = (envKey: string) => (import.meta.env[envKey] as string | undefined) || ''

/** Origin or full base URL per service. Empty string = same-origin (Vite proxy in dev). */
const envBases = {
  auth: base('VITE_AUTH_BASE'),
  asset: base('VITE_ASSET_BASE'),
  notification: base('VITE_NOTIFICATION_BASE'),
  helpdesk: base('VITE_HELPDESK_BASE'),
} as const

export type ServiceName = keyof typeof envBases

type RuntimeBases = Partial<Record<ServiceName, string>>

const RUNTIME_BASES_KEY = 'keeply_service_bases'

function isBrowser(): boolean {
  return typeof window !== 'undefined' && typeof localStorage !== 'undefined'
}

function readRuntimeBases(): RuntimeBases {
  if (!isBrowser()) return {}
  try {
    const raw = localStorage.getItem(RUNTIME_BASES_KEY)
    if (!raw) return {}
    const parsed = JSON.parse(raw) as unknown
    if (!parsed || typeof parsed !== 'object') return {}
    return parsed as RuntimeBases
  } catch {
    return {}
  }
}

/** Keep `http://` API bases when the page is HTTPS (avoids wrong scheme on remote HTTP-only servers). */
function allowInsecureHttpApiBases(): boolean {
  return (
    import.meta.env.MODE === 'capacitor' ||
    import.meta.env.VITE_ALLOW_INSECURE_HTTP_API === 'true'
  )
}

function httpsSafeBase(prefix: string): string {
  if (!prefix) return ''
  if (!isBrowser()) return prefix
  if (allowInsecureHttpApiBases()) return prefix
  if (window.location.protocol === 'https:' && prefix.startsWith('http://')) {
    return `https://${prefix.slice('http://'.length)}`
  }
  return prefix
}

export function getServiceBase(service: ServiceName): string {
  const runtime = readRuntimeBases()
  const prefix = runtime[service] || envBases[service] || ''
  return httpsSafeBase(prefix).replace(/\/$/, '')
}

/**
 * Build a service URL.
 *
 * - In dev: leave bases empty to use Vite proxy on same-origin `/api/*`
 * - In prod: set `VITE_*_BASE` OR store runtime bases in localStorage under `keeply_service_bases`
 * - When app is on HTTPS, bases are auto-upgraded to HTTPS to avoid mixed-content blocks
 *   unless `VITE_ALLOW_INSECURE_HTTP_API=true` or mode is `capacitor` (temporary HTTP backends).
 */
export function url(service: ServiceName, path: string): string {
  const prefix = getServiceBase(service)
  const p = path.startsWith('/') ? path : `/${path}`
  return prefix ? `${prefix.replace(/\/$/, '')}${p}` : p
}

// Back-compat export name used around the codebase.
export const bases = envBases
