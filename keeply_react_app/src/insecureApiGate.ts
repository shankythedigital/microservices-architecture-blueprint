import { getServiceBase, type ServiceName } from './config'

const SERVICES: ServiceName[] = ['auth', 'asset', 'notification', 'helpdesk']

const SESSION_KEY = 'keeply_insecure_http_warning_ok'

export function usesHttpApiBackend(): boolean {
  return SERVICES.some((s) => {
    const b = getServiceBase(s)
    return b.length > 0 && b.startsWith('http://')
  })
}

/**
 * If any service base is `http://`, prompt once per browser/WebView session before the app runs.
 * @returns false if the user declined.
 */
export function acknowledgeInsecureHttpIfNeeded(): boolean {
  if (typeof window === 'undefined') return true
  if (!usesHttpApiBackend()) return true
  if (sessionStorage.getItem(SESSION_KEY) === '1') return true

  const ok = window.confirm(
    [
      'Keeply is using HTTP for API calls (not HTTPS).',
      'Traffic can be observed or changed on the network. Use only for temporary development.',
      '',
      'Continue?',
    ].join('\n'),
  )
  if (ok) sessionStorage.setItem(SESSION_KEY, '1')
  return ok
}
