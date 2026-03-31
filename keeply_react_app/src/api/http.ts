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

  return fetch(input, { ...rest, headers })
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
