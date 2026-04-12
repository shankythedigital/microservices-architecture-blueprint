/** Dispatched when an authenticated API call returns 401 — AuthProvider listens and clears session + /login. */
export const SESSION_EXPIRED_EVENT = 'keeply:session-expired'

let sessionExpiredLatch = false

/** Clears one-shot latch (call after login or local logout). */
export function resetSessionExpiredLatch(): void {
  sessionExpiredLatch = false
}

/**
 * If the app sent an access token and the server rejected it (401), notify once so we can redirect to login.
 */
export function considerUnauthorizedResponse(response: Response, accessTokenWasSent: boolean): void {
  if (!accessTokenWasSent || response.status !== 401) return
  if (sessionExpiredLatch) return
  sessionExpiredLatch = true
  window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT))
}

export class ApiError extends Error {
  status: number
  body?: string

  constructor(message: string, status: number, body?: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export async function parseJson<T>(r: Response): Promise<T | undefined> {
  const ct = r.headers.get('content-type') || ''
  if (!ct.includes('application/json')) return undefined
  return (await r.json()) as T
}

export async function apiFetch(
  input: string,
  init: RequestInit & { token?: string | null } = {},
): Promise<Response> {
  const { token, headers: h, ...rest } = init
  const headers = new Headers(h)
  if (!headers.has('Accept')) headers.set('Accept', 'application/json')
  if (rest.body && typeof rest.body === 'string' && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) headers.set('Authorization', `Bearer ${token}`)

  if (import.meta.env.VITE_DEBUG_API === 'true') {
    // Visible in Chrome → Inspect WebView (see .env.example). No secrets logged.
    console.info('[Keeply API]', rest.method ?? 'GET', input)
  }

  const response = await fetch(input, { ...rest, headers })
  considerUnauthorizedResponse(response, Boolean(token))
  return response
}

export async function apiJson<T>(
  input: string,
  init: RequestInit & { token?: string | null } = {},
): Promise<T> {
  const r = await apiFetch(input, init)
  const data = await parseJson<T>(r)
  if (!r.ok) {
    const msg =
      (data as { error?: string } | undefined)?.error ||
      (data as { message?: string } | undefined)?.message ||
      r.statusText
    throw new ApiError(msg || `HTTP ${r.status}`, r.status)
  }
  return data as T
}
