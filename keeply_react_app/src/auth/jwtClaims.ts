/** Decode JWT payload (no signature verification — identity is enforced by the API). */
export function parseJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const pad = base64.length % 4 === 0 ? '' : '='.repeat(4 - (base64.length % 4))
    const json = atob(base64 + pad)
    return JSON.parse(json) as Record<string, unknown>
  } catch {
    return null
  }
}

/** Numeric user id from access token (`uid` claim preferred, else `sub`). */
export function userIdFromAccessToken(token: string | null | undefined): number | null {
  if (!token) return null
  const p = parseJwtPayload(token)
  if (!p) return null
  const uid = p.uid ?? p.sub
  if (uid == null) return null
  const n = Number(uid)
  return Number.isFinite(n) ? n : null
}
